package org.cherub.fintools.pdftool

import java.io.File

const val formula_c11 = "=ОКРУГЛ(R[-1]C+RC[-8];2)"
const val formula_c12 = "=ОКРУГЛ(R[-1]C[-1]+RC[-9];2)"

abstract class CommonProcessor{

    fun process(
        fileText: String,
        sourceName: String
    ): String {
        val html = cleanUpHtml(removeNewLines(fileText))
        if (WRITE_HTML) File("$sourceName.2.html").writeText(html)

        val builder = StringBuilder()
        splitToTransactionRows(html).forEach {
            builder.appendLine(transformToCsv(cleanUpRow(it)))
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
        .replace("\u00a0", "") // removing No-Break Space in sums // TODO check to remove

    open fun cleanUpRow(row: String) = row

    private fun splitToTransactionRows(text: String): List<String> = text
        .lines().filter { rowFilter(it) }
        .reversed()

    protected abstract fun cleanUpTransactions(content: String): String
}
