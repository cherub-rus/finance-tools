package org.cherub.fintools.txttool

import org.cherub.fintools.txttool.sms.ProcessResult
import org.cherub.fintools.txttool.sms.SmsProcessor
import java.io.File

fun main(args: Array<String>) {

    val sourceName = args[0]
    val targetName = "$sourceName.csv"

    if (sourceName.isEmpty() || sourceName == ".") {
        println("File name is required argument!!!")
    }
    try {
        val fileText = getContent(sourceName).removeNonBreakingSpace() // TODO  .normalizeNewLines()

        val firstLine = fileText.lines()[0]
        val result =
            if (firstLine.matches("\\[([0-9.]{10}) в ([0-9:]){5}\\]".toRegex())) {
                // TODO Sber Push
                ProcessResult("Sber Push")
            } else if (firstLine.matches(SmsProcessor.checkFormatRegex)) {
                SmsProcessor().process(fileText, sourceName)
            } else ProcessResult("Невозможно определить тип выписки!")

        result.skipped?.also { File("$sourceName.unparsed").writeText(it.joinToString("\n"))}
        File(targetName).writeText(result.csv)
    } catch (e: Exception) {
        e.printStackTrace() //todo log
    }
}

private fun getContent(sourceFileName: String): String {
    return File(sourceFileName).readText()
}

private fun String.removeNonBreakingSpace() = this
    .replace('\u00A0', '\u0020')

private fun String.normalizeNewLines() = this
    .replace("\r\n", "\n")
    .replace("\r", "\n")

