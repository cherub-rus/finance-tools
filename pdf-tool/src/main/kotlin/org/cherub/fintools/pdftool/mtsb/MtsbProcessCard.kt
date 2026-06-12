package org.cherub.fintools.pdftool.mtsb

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import org.cherub.fintools.pdftool.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private const val BIN = 220028 // Bank Identification Number for PAYMENT SYSTEM: NSPK MIR; BANK: PJSC MTS BANK

private val MB_ACCOUNT_REGEX = "<p>Номер счета: (?<account>40817810\\d{12})</p>".toRegex()
private val MB_CURRENT_REGEX = "<p>Доступный остаток\\* на (?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4}): (?<currentBalance>\\d{1,6}\\.\\d{1,2}) RUB</p>".toRegex()
private val MB_REPORT_DATES_REGEX = "<p><b>Операции за период с (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) по (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()
private val MB_CARD_REGEX = "<p> {2}===(?<card>${BIN}\\*{6}\\d{4})Номер карты:([а-яА-Я ]+)</p>".toRegex()

class MtsbProcessCard(config: ConfigData) : CommonProcessor(config, true) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</p><p>)($BIN\\*\\*)".toRegex(), " ===$2")
        .replace("( )($BIN\\*\\*)".toRegex(), " ===$2")
        .replace("([0-9]{1,3} [0-9.]{10} [0-9:]{8})</p><p>".toRegex(), "$1 ")
        .replace("(</p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains("$BIN**") && !row.contains("Номер карты:")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p>[0-9]{1,3} ([0-9.]{10}) ([0-9:]{8}) ([0-9]+\\.[0-9]{1,2}) RUR (((.+?)(, ))?(.+?))( ?дата транзакции ([0-9/]{10})( ([0-9:]{8}))?)? ===$BIN.+</p>".toRegex(),
            prepareCsvOutputMask("$1", "$2", "", "$8".replace("(.+)[.]".toRegex(), "$1"), "", "$10 $12", "$3", "$6")
        )

    override fun fixCsv(csvRow: String): String {
        val fields = csvRow.split("\t").toMutableList()

        try {
            val sign = if (fields[C_OPERATION].replace("~", "").startsWith("Зачисление") || fields[C_OPERATION].startsWith("Перевод СБП от ")) "" else "-"
            fields[C_AMOUNT] = sign + fields[C_VAR2].replace('.', ',') // Added minus sign to expense amount and change currency separator
            fields[C_VAR2] = ""

            val operationDate = parseDate("${fields[C_DATE]} ${fields[C_TIME]}")
            if (fields[C_VAR1].isNotBlank()) {
                val messageDate = parseDate(fields[C_VAR1].replace('/', '.'))

                if (abs(Duration.between(operationDate, messageDate).seconds) <= DURATION_AS_EQUAL){
                    overrideOperationDate(fields, operationDate, TIME_OFFSET_LOCAL_TO_BASE_HOURS)
                } else if (abs(Duration.between(operationDate.plusHours(TIME_OFFSET_BASE_TO_UTC_HOURS), messageDate).seconds) <= DURATION_AS_EQUAL){
                    overrideOperationDate(fields, operationDate, TIME_OFFSET_LOCAL_TO_UTC_HOURS)
                } else if (operationDate.toLocalTime().equals(LocalTime.MIDNIGHT)) { // If operation time is "empty", with time from message
                    overrideOperationDate(fields, messageDate)
                }
                fields[C_VAR1] = ""
            } else {
                overrideOperationDate(fields, operationDate, TIME_OFFSET_LOCAL_TO_BASE_HOURS)
            }
        } catch (e: Exception) {
            log(e, csvRow)
        }

        return super.fixCsv(fields.joinToString("\t"))
    }

    private fun overrideOperationDate(fields: MutableList<String>, date: LocalDateTime, offset: Long = 0) {
        val parts =
            date.plusHours(offset).format(DateTimeFormatter.ofPattern(TIMESTAMP_RUSSIAN_PATTERN))
                .split(" ")
        fields[C_DATE] = parts[0]
        fields[C_TIME] = parts[1]
    }

    private fun parseDate(text: String): LocalDateTime =
        LocalDateTime.parse(text, DateTimeFormatter.ofPattern(TIMESTAMP_RUSSIAN_PATTERN))

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