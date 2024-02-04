package org.cherub.fintools.pdftool.gpb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val GPB_CARD_ACCOUNT_REGEX = "<p>.+ (?<account>40817810\\d{12}) Российский Рубль (?<card>\\*{12}\\d{4})</p>".toRegex()
private val GPB_CARD_DATES_REGEX = "<p><b>ВЫПИСКА ПО КАРТЕ</b>За период с (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) по (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4}) Выписка сформирована на (?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2})</p>".toRegex()

class GpbCardProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun reversRows() = false

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)".toRegex(), "$1\n")
        .replace("</p><p>","</p>\n<p>")
        .replace("</p>\n<p>+","</p><p>+")
        .replace("</p> </div></div>", "</p>\n</div></div>")
        .replace(" ([0-9]{2}[.][0-9]{2}[.][0-9]{4} [0-9]{2}[.][0-9]{2}[.][0-9]{4})".toRegex(), "</p>\n<p>$1")
        .replace("С КАРТЫ НА</p>\n<p>КАРТУ", "С КАРТЫ НА КАРТУ")
        .replace("возникшего по счету</p>\n<p>банковской карты", "возникшего по счету банковской карты")

    override fun rowFilter(row: String) =
        row.contains("<p>[0-9]{2}[.][0-9]{2}[.][0-9]{4} [0-9]{2}[.][0-9]{2}[.][0-9]{4}".toRegex())  &&
        !row.contains("Погашение ТО, возникшего по счету банковской карты")

    override fun cleanUpRow(row: String) = row
        .replace(" -0,00", "")
        .replace("+0,00 ", "")
        .replace(" +".toRegex(), " ")


    override fun transformToCsv(row: String) = row
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} Операция: (.+ [(](.+)[)]) [(]ФИЛИАЛ ГПБ[)][.] Устройство: (.+)[.] Город: .+[.] Сумма операции: .+[.] Валюта операции: Российские рубли[.]</p><p>(.+)</p>".toRegex(),
            "$1\t$4\t$5\t\t\t\t$3\t\t\t\t$formula_c11\t$formula_c12\t$4"
        )
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} Операция: (.+)[.] Карта .+[.] Устройство: (.*)[.] Город: .*[.] Сумма операции: .+[.] Валюта операции: Российские рубли[.]</p><p>(.+)</p>".toRegex(),
            "$1\t$3\t$4\t\t\t\t$2\t\t\t\t$formula_c11\t$formula_c12\t$3"
        )
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} (Перевод с банковской карты) [0-9]{6}x{8}[0-9]{4} (на счет [0-9]{20})[.]( .+[.])?</p><p>(.+)</p>".toRegex(),
            "$1\t$3\t$5\t\t\t\t$2\t\t\t\t$formula_c11\t$formula_c12\t$3"
        )
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} (Перевод в онлайн-сервисе) (согласно распоряжению) [0-9A-Z]{12}</p><p>(.+)</p>".toRegex(),
            "$1\t$3\t$4\t\t\t\t$2\t\t\t\t$formula_c11\t$formula_c12\t$3"
        )
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} (Перевод) (между своими счетами) (.+)</p>".toRegex(),
            "$1\t$3\t$4\t\t\t\t$2\t\t\t\t$formula_c11\t$formula_c12\t$3"
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
