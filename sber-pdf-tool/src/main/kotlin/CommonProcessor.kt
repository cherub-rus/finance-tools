import java.io.File

abstract class CommonProcessor{

    fun process(
        fileText: String,
        sourceName: String
    ): String {
        val html = cleanUpHtml(removeNewLines(fileText))
        if (WRITE_HTML) File("$sourceName.2.html").writeText(html)

        val builder = StringBuilder()
        splitToTransactionRows(html).forEach {
            builder.appendLine(transformToCsv(it))
        }
        return cleanUpTransactions(builder.toString())
    }

    protected abstract fun rowFilter(row: String) : Boolean
    protected abstract fun transformToCsv(row: String): String
    protected abstract fun cleanUpHtmlSpecific(text: String): String

    private fun removeNewLines(text: String) = text
        .replace("\n([^<])".toRegex(), " $1")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("\n<", "<")
        .replace("\n ", " ")
        .replace("\n", " ")

    open fun cleanUpHtml(text: String) =
        cleanUpHtmlSpecific(text)
        .replace("(В валюте счёта</p>)".toRegex(), "$1\n")
        .replace("(<p>Сумма в валюте операции²</p>)".toRegex(), "$1\n")
        .replace("\u00a0", "") // removing No-Break Space in sums

    private fun splitToTransactionRows(text: String): List<String> = text
        .lines().filter { rowFilter(it) }
        .reversed()

    private fun cleanUpTransactions(content: String): String {
        return content
            .replace("  *".toRegex(), " ")
            .replace("\"", "")
            .replace(" Tomsk RUS", "")
            .replace(" TOMSK RUS", "")
            .replace(" TOMCK RUS", "")
            .replace(" Gorod Moskva RUS", "")
            .replace("SUPERMARKET SPAR", "SPAR")
            .replace("SBERBANK ONL@IN ", "Сбербанк Онлайн ")
    }
}
