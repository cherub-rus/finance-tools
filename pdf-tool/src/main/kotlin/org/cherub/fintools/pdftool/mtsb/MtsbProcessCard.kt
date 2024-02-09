package org.cherub.fintools.pdftool.mtsb

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import org.cherub.fintools.pdftool.*

private const val BIN = 220028 // Bank Identification Number for PAYMENT SYSTEM: NSPK MIR; BANK: PJSC MTS BANK

private val MB_ACCOUNT_REGEX = "<p>Номер счета: (?<account>40817810\\d{12})</p>".toRegex()
private val MB_CURRENT_REGEX = "<p>Доступный остаток\\* на (?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4}): (?<currentBalance>\\d{1,6}\\.\\d{1,2}) RUB</p>".toRegex()
private val MB_REPORT_DATES_REGEX = "<p><b>Операции за период с (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) по (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()
private val MB_CARD_REGEX = "<p> {2}###(?<card>${BIN}\\*{6}\\d{4})Номер карты:([а-яА-Я ]+)</p>".toRegex()

class MtsbProcessCard(config: ConfigData) : CommonProcessor(config, true) {

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
            prepareCsvOutputMask("$1", "$2", "", "$8", formula_c11, formula_c12, "$11", "$3", "$6")
        )

    override fun fixCsv(csvRow: String): String {
        val fields = csvRow.split("\t").toMutableList()

        try {
            val sign = if (fields[12].replace("~", "").startsWith("Зачисление")) "" else "-"
            fields[3] = sign + fields[11].replace('.', ',') // Added minus sign to expense amount and change currency separator
            fields[11] = ""

            if (fields[10].isNotEmpty()) {
                if (fields[1].endsWith("00:00:00")) { // If transaction time exists, replace log time with it
                    fields[1] = fields[10]
                }
                fields[10] = ""
            }
        } catch (e: Exception) {
            log(e, csvRow)
        }

        return super.fixCsv(fields.joinToString("\t"))
    }

    override fun cleanUpRow(row: String) = row
        .cleanUpByRules(config.replaceInRow)

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