package org.cherub.fintools.pdftool.gpb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val GPB_CARD_ACCOUNT_REGEX = "<p>.+ (?<account>40817810\\d{12}) Российский Рубль (?<card>\\*{12}\\d{4})</p>".toRegex()
private val GPB_CARD_DATES_REGEX = "<p><b>ВЫПИСКА ПО КАРТЕ</b>За период с (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) по (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4}) Выписка сформирована на (?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2})</p>".toRegex()

private const val DATAROW_START = "[0-9]{2}[.][0-9]{2}[.][0-9]{4} [0-9]{2}[.][0-9]{2}[.][0-9]{4}"

class GpbCardProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun reversRows() = false

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</p>)".toRegex(), "$1\n")
        .replace("</p>\n<p>+"," +")
        .replace("(С КАРТЫ НА)</p>\n<p>(КАРТУ)".toRegex(), "$1$2")
        .replace("(возникшего по счету)</p>\n<p>(банковской карты)".toRegex(), "$1$2")
        .replace((" ($DATAROW_START)").toRegex(), "</p>\n<p>$1")

    override fun rowFilter(row: String) =
        row.contains(("<p>$DATAROW_START").toRegex())  &&
        !row.contains("Погашение ТО")

    override fun cleanUpRow(row: String) = row
        .replace(" -0,00", "")
        .replace("+0,00 ", "")
        .replace(" +".toRegex(), " ")

    // TODO
    override fun transformToCsv(row: String) = row
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} Операция: (.+ [(](.+)[)]) [(]ФИЛИАЛ ГПБ[)][.] Устройство: (.+)[.] Город: .+[.] Сумма операции: .+[.] Валюта операции: Российские рубли[.] (.+)</p>".toRegex(),
            prepareCsvOutputMask("$1", "", "$5", "$4", FORMULA_BALANCE1, FORMULA_BALANCE2, "", "", "$3")
        )
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} Операция: (.+)[.] Карта .+[.] Устройство: (.*)[.] Город: .*[.] Сумма операции: .+[.] Валюта операции: Российские рубли[.] (.+)</p>".toRegex(),
            prepareCsvOutputMask("$1", "", "$4", "$3", FORMULA_BALANCE1, FORMULA_BALANCE2, "", "", "$2")
        )
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} (Перевод с банковской карты) [0-9]{6}x{8}[0-9]{4} (на счет [0-9]{20})[.]( .+[.])? (.+)</p>".toRegex(),
            prepareCsvOutputMask("$1", "", "$5", "$3", FORMULA_BALANCE1, FORMULA_BALANCE2, "", "", "$2")
        )
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} (Перевод в онлайн-сервисе) (согласно распоряжению) [0-9A-Z]{12} (.+)</p>".toRegex(),
            prepareCsvOutputMask("$1", "", "$4", "$3", FORMULA_BALANCE1, FORMULA_BALANCE2, "", "", "$2")
        )
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} (Перевод) (между своими счетами) (.+)</p>".toRegex(),
            prepareCsvOutputMask("$1", "", "$4", "$3", FORMULA_BALANCE1, FORMULA_BALANCE2, "", "", "$2")
        )

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = GPB_CARD_ACCOUNT_REGEX.matchEntire(text.lines()[5])
        val mDates = GPB_CARD_DATES_REGEX.matchEntire(text.lines()[6])

        val number = mAcc?.gv("account")
        val code =  number?.let {
            val s = it.takeLast(4)
            config.accounts.findByAccountNumber("*$s")?.code
        }

        return AccountInfo(
            accountCode = code,
            accountNumber = number,
            cardNumber = mAcc?.gv("card"),
            startDate = mDates?.gv("startDate"),
            endDate = mDates?.gv("endDate"),
            currentDate = mDates?.gv("currentDate"),
        )
    }
}
