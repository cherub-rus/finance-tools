package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private const val SB_ACCOUNT_REGEX = "<p>Счёт Валюта Доступно на (?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4})<b>(?<account>40817 810 \\d \\d{4} \\d{7}) РУБЛЬ РФ (?<currentBalance>(\\d{1,3} )?\\d{1,3},\\d{2})</b></p>"
private const val SB_REPORT_DATES_REGEX = "<p><b>ОСТАТОК НА (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) ОСТАТОК НА (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4}) ВСЕГО СПИСАНИЙ ВСЕГО ПОПОЛНЕНИЙ</b></p>"
private const val SB_REPORT_BALANCES_REGEX = "<p><b>(?<startBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) (?<endBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) ((\\d{1,3} )?\\d{1,3},\\d{2}) ((\\d{1,3} )?\\d{1,3},\\d{2})</b></p>"

class SberProcessPayAcc(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains(".202") && row.contains("</p><p><b>")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5})</b>.{12,17}</p><p><b>(.+)</b>(.+)</p><p><b>(-?[0-9, ]+)</b></p>".toRegex(),
            "$1\t$3\t$5\t\t\t\t$4\t$2:00\t\t\t$formula_c11\t$formula_c12\t$3"
        )

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = SB_ACCOUNT_REGEX.toRegex().matchEntire(text.lines()[3])
        val mDates = SB_REPORT_DATES_REGEX.toRegex().matchEntire(text.lines()[5])
        val mBal = SB_REPORT_BALANCES_REGEX.toRegex().matchEntire(text.lines()[6])

        val number = mAcc?.gv("account")
        val code =  number?.let {
            val s = it.replace(" ", "").takeLast(4)
            config.accounts.findByAccountNumber("СЧЁТ$s")?.code
        }

        return AccountInfo(
            accountCode = code,
            accountNumber = number,
            startDate = mDates?.gv("startDate"),
            endDate = mDates?.gv("endDate"),
            currentDate = mAcc?.gv("currentDate"),
            startBalance = mBal?.gv("startBalance"),
            endBalance = mBal?.gv("endBalance"),
            currentBalance = mAcc?.gv("currentBalance")
        )
    }
}