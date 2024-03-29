package org.cherub.fintools.txttool

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.log.log
import org.cherub.fintools.txttool.push.sber.SberPushProcessor
import org.cherub.fintools.txttool.sms.SmsProcessor
import org.cherub.fintools.config.loadConfigFromFile
import java.io.File

fun main(args: Array<String>) {

    if (args.size < 2) {
        println("File and config name are required arguments!!!")
    }
    val sourceName = args[0]
    val targetName = "$sourceName.csv"
    val configName = args[1]
    val config = configName.loadConfigFromFile()

    try {
        val fileText = getContent(sourceName).cleanUpByRules(config.replaceInSource)

        val firstLine = fileText.lines()[0]
        @Suppress("RegExpRedundantEscape")
        val result =
            if (firstLine.matches("\\[([0-9.]{10}) в ([0-9:]){5}\\]".toRegex())) {
                SberPushProcessor(config).process(fileText, sourceName)
            } else if (firstLine.matches(SmsProcessor.checkFormatRegex)) {
                SmsProcessor(config).process(fileText, sourceName)
            } else ProcessResult(mapOf(Pair(targetName, "Невозможно определить тип выписки!")))

        result.skipped?.also { File("$sourceName.unparsed").writeText(it.joinToString("\n"))}
        result.csvMap.forEach { File(it.key + ".sms.csv").writeText(it.value) }
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
