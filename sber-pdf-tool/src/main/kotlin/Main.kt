import org.apache.commons.text.StringEscapeUtils
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.tools.PDFText2HTML
import java.io.File
import java.io.IOException
import java.io.StringWriter

const val WRITE_HTML = true

fun main(args: Array<String>) {

    val sourceName = args[0]
    val targetName = "$sourceName.csv"

    if (sourceName.isEmpty() || sourceName == ".") {
        println("File name is required argument!!!")
    }
    try {
        val fileText = StringEscapeUtils.unescapeHtml4(getHtmlContent(sourceName))
        if (WRITE_HTML) File("$sourceName.1.html").writeText(fileText)

        val result =
            if (fileText.contains("Выписка по платёжному счёту")) {
                ProcessPayAcc().process(fileText.removeNonBreakingSpace(), sourceName)
            } else if (fileText.contains("Выписка по счёту дебетовой карты")) {
                ProcessCard().process(fileText.removeNonBreakingSpace(), sourceName)
            } else if (fileText.contains("Выписка из лицевого счёта по вкладу")) {
                ProcessDeposit().process(fileText.removeNonBreakingSpace(), sourceName)
            } else "Невозможно определить тип выписки!"


        File(targetName).writeText(result)
    } catch (e: Exception) {
        println(e) //todo log
    }
}

private fun getHtmlContent(sourceFileName: String): String {
    var document: PDDocument? = null
    try {
        document = PDDocument.load(File(sourceFileName))

        val ap = document!!.currentAccessPermission
        if (!ap.canExtractContent()) {
            throw IOException("You do not have permission to extract text")
        }
        val output = StringWriter()
        PDFText2HTML().writeText(document, output)
        return output.toString().replace("charset=\"UTF-8", "charset=UTF-8")
    } finally {
        IOUtils.closeQuietly(document)
    }
}

private fun String.removeNonBreakingSpace() = this
    .replace('\u00A0', '\u0020')
