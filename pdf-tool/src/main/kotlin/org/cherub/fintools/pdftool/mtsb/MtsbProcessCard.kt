package org.cherub.fintools.pdftool.mtsb

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import org.cherub.fintools.pdftool.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val BIN = 220028 // Bank Identification Number for PAYMENT SYSTEM: NSPK MIR; BANK: PJSC MTS BANK

private val MB_ACCOUNT_REGEX = "<p>Номер счета: (?<account>40817810\\d{12})</p>".toRegex()
private val MB_CURRENT_REGEX = "<p>Доступный остаток\\* на (?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4}): (?<currentBalance>\\d{1,6}\\.\\d{1,2}) RUB</p>".toRegex()
private val MB_REPORT_DATES_REGEX = "<p><b>Операции за период с (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) по (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()
private val MB_CARD_REGEX = "<p>  ###(?<card>${BIN}\\*{6}\\d{4})Номер карты:([а-яА-Я ]+)</p>".toRegex()

class MtsbProcessCard(config: ConfigData) : CommonProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</p><p>)($BIN\\*\\*)".toRegex(), " ###$2")
        .replace("( )($BIN\\*\\*)".toRegex(), " ###$2")
        .replace("([0-9]{1,3} [0-9.]{10} [0-9:]{8})</p><p>".toRegex(), "$1 ")
        .replace("(</p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains("$BIN**") && !row.contains("Номер карты:")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p>[0-9]{1,3} ([0-9.]{10}) ([0-9:]{8}) ([0-9]+\\.[0-9]{1,2}) RUR (((.+?)(, ))?(.+?))( дата транзакции ([0-9/]{10}) ([0-9:]{8}))? ###$BIN.+</p>".toRegex(),
            "$1\t$8\t\t\t\t\t$6\t$2\t$11\t$3\t$formula_c11\t$formula_c12\t$8"
        )

    override fun fixCsv(csvRow: String): String {
        val fields = csvRow.split("\t").toMutableList()

        try {
            fields[9] = fields[9].replace('.', ',') // Changed currency separator
            val sign = if (fields[6].replace("~", "").startsWith("Зачисление")) "" else "-"
            fields[2] = sign + fields[9] // Added minus sign to expense amount

            if (fields[8].isNotEmpty()) {
                if (fields[7].endsWith("00:00:00")) { // If transaction time exists, replace log time with it
                    fields[7] = fields[8]
                }
                fields[8] = ""
            }
        } catch (e: Exception) {
            log(e, csvRow)
        }

        return super.fixCsv(fields.joinToString("\t"))
    }

    override fun cleanUpRow(row: String) = row
        .cleanUpByRules(config.replaceInRow)

    // reorder by date and time
    override fun reorderCsvRows(text: String) = text
        .split("\n")
        .filter { it.isNotEmpty() }
        .sortedBy {
            try {
                val fields = it.split("\t")
                LocalDateTime.parse("${fields[0]} ${fields[7]}", DateTimeFormatter.ofPattern(TIMESTAMP_ISO_PATTERN))
            } catch (e: Exception) {
                log(e, it)
                LocalDateTime.MIN
            }
        }
        .joinToString(separator = "\n", postfix = "\n")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = MB_ACCOUNT_REGEX.matchEntire(text.lines()[4])
        val mCurrent = MB_CURRENT_REGEX.matchEntire(text.lines()[7])
        val mDates = MB_REPORT_DATES_REGEX.matchEntire(text.lines()[11])
        val mCard = MB_CARD_REGEX.matchEntire(text.lines()[9])

        val number = mAcc?.gv("account")
        val code =  number?.let {
            val s = it.takeLast(4)
            config.accounts.findByAccountNumber("*$s")?.code
        }

        return AccountInfo(
            accountCode = code,
            accountNumber = number,
            cardNumber = mCard?.gv("card"),
            startDate = mDates?.gv("startDate"),
            endDate = mDates?.gv("endDate"),
            currentDate = mCurrent?.gv("currentDate"),
            currentBalance = mCurrent?.gv("currentBalance")?.replace(".",",")
        )
    }
}