package org.cherub.fintools.pdftool

import org.apache.commons.text.StringEscapeUtils
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.tools.PDFText2HTML
import org.cherub.fintools.config.loadConfigFromFile
import org.cherub.fintools.log.log
import java.io.File
import java.io.IOException
import java.io.StringWriter

import org.cherub.fintools.pdftool.mtsb.*
import org.cherub.fintools.pdftool.sber.*
import org.cherub.fintools.pdftool.gpb.*
import org.cherub.fintools.pdftool.usb.*
import kotlin.io.path.Path

const val WRITE_HTML = true

fun main(args: Array<String>) {

    if (args.size < 2) {
        println("File and config name are required arguments!!!")
    }

    val sourceName = args[0]
    val configName = args[1]
    val config = configName.loadConfigFromFile()

    try {
        val fileText = if (sourceName.contains(".manual-html")) File(sourceName).readText()
                       else StringEscapeUtils.unescapeHtml4(getHtmlContent(sourceName)).replaceNonBreakingSpace()
        if (WRITE_HTML) File("$sourceName.1.html").writeText(fileText)

        val result =
            if (fileText.contains("Выписка по платёжному счёту")) {
                SberProcessPayAcc(config).process(fileText, sourceName)
            } else if (fileText.contains("Выписка по счёту дебетовой карты")) {
                SberProcessCard(config).process(fileText, sourceName)
            } else if (fileText.contains("Выписка по счёту кредитной карты")) {
                SberProcessCreditCard(config).process(fileText, sourceName)
            } else if (fileText.contains("Выписка по лицевому счёту") ||
                       fileText.contains("Выписка из лицевого счёта по вкладу «Накопительный счёт»") ||
                       fileText.contains("Выписка по счёту «Накопительный счёт»"))  {
                SberProcessPersonalAccount(config).process(fileText, sourceName)
            } else if (fileText.contains("Выписка из лицевого счёта по вкладу")) {
                SberProcessDeposit(config).process(fileText, sourceName)
            } else if (fileText.contains("www.mtsbank.ru")) {
                MtsbProcessCard(config).process(fileText, sourceName)
            } else if (fileText.contains("www.gazprombank.ru") && fileText.contains("ВЫПИСКА ПО КАРТЕ")) {
                GpbCardProcessor(config).process(fileText, sourceName)
            } else if (fileText.contains("www.gazprombank.ru") && fileText.contains("ВЫПИСКА ПО СЧЕТУ")) {
                GpbDepositProcessor(config).process(fileText, sourceName)
            } else if (fileText.contains("Сведения об операции Категории Дата и время MSK Сумма Валюта")) {
                UsbCardProcessor(config).process(fileText, sourceName)
            } else ProcessResult("Невозможно определить тип выписки!")

        val targetName =
            result.accountCode?.let {
                val path = Path(sourceName).toAbsolutePath().parent
                path.resolve("$it.pdf.csv").toString()
            } ?: "$sourceName.csv"
        File(targetName).writeText(result.csvText)
    } catch (e: Exception) {
        log(e)
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

private fun String.replaceNonBreakingSpace() = this
    .replace('\u00A0', '\u0020')
