package org.cherub.fintools.txttool

import org.cherub.fintools.config.ReplaceRule
import org.cherub.fintools.log.log
import org.cherub.fintools.txttool.push.sber.SberPushProcessor
import org.cherub.fintools.txttool.sms.SmsProcessor
import org.cherub.fintools.config.loadConfigFromFile
import java.io.File

fun main(args: Array<String>) {

    val sourceName = args[0]
    val targetName = "$sourceName.csv"
    val configName = args[1]
    val config = configName.loadConfigFromFile()

    if (sourceName.isEmpty() || sourceName == ".") {
        println("File name is required argument!!!")
    }
    try {
        val fileText = getContent(sourceName).cleanUpText(config.replaceInText)

        val firstLine = fileText.lines()[0]
        @Suppress("RegExpRedundantEscape")
        val result =
            if (firstLine.matches("\\[([0-9.]{10}) в ([0-9:]){5}\\]".toRegex())) {
                SberPushProcessor().process(fileText, sourceName, config)
            } else if (firstLine.matches(SmsProcessor.checkFormatRegex)) {
                SmsProcessor().process(fileText, sourceName, config)
            } else ProcessResult("Невозможно определить тип выписки!")

        result.skipped?.also { File("$sourceName.unparsed").writeText(it.joinToString("\n"))}
        File(targetName).writeText(result.csv)
    } catch (e: Exception) {
        log(e)
    }
}

private fun getContent(sourceFileName: String): String =
    File(sourceFileName).readText().replaceNonBreakingSpace().normalizeNewLines()

private fun String.replaceNonBreakingSpace() = this
    .replace('\u00A0', '\u0020')

private fun String.normalizeNewLines() = this
    .replace("\r\n", "\n")
    .replace("\r", "\n")

private fun String.cleanUpText(rules: List<ReplaceRule>): String {
    var str = this
    rules.forEach { str = str.replace(it.s, it.r) }
    return str
}


