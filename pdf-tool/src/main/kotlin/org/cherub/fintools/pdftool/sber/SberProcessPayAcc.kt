package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val SB_ACCOUNT_REGEX = "<p>Номер счёта Дата открытия счёта Дата закрытия счёта Валюта<b>(?<account>40817 810 \\d \\d{4} \\d{7}) .+</b></p>".toRegex()
internal val SB_REPORT_DATES_REGEX = "<p><b>ОСТАТОК НА (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) ВСЕГО ПОПОЛНЕНИЙ ВСЕГО СПИСАНИЙ ОСТАТОК НА (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()
internal val SB_REPORT_BALANCES_REGEX = "<p><b>(?<startBalance>(\\d{1,3} )*\\d{1,3},\\d{2}) ((\\d{1,3} )*\\d{1,3},\\d{2}) ((\\d{1,3} )*\\d{1,3},\\d{2}) (?<endBalance>(\\d{1,3} )*\\d{1,3},\\d{2})</b></p>".toRegex()
internal val SB_ACCOUNT_CURRENT_DATE_REGEX = "<p>Дата формирования <b>(?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()

class SberProcessPayAcc(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)".toRegex(), "$1\n")
        .replace("(<p>Продолжение на следующей странице</p>)".toRegex(), "\n$1")
        .replace("(<p>Дата формирования <b>)".toRegex(), "\n$1")
        .replace("(<p>Дата формирования документа <b>)".toRegex(), "\n$1")
        .replace("(</p>)(<p><b>)".toRegex(), "$1\n$2")
        .replace("(</p><p>)".toRegex(), " ")


    override fun rowFilter(row: String) =
        row.contains("^<p><b>\\d\\d[.]\\d\\d".toRegex())

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5}) </b>[0-9]{6} <b>(.+) </b>([+-]?[0-9 ]+,[0-9]{2}) ([+-]?[0-9 ]+,[0-9]{2}) ([0-9.]{10}) (.+)[.] Операция по (карте|счету) [*]{4}[0-9]{4}</p>".toRegex(),
            prepareCsvOutputMask("$1", "$2:00", "-$4", "$7", "$5", "", "", "$3")
        )
        .replace("-+", "")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = SB_ACCOUNT_REGEX.matchEntire(text.lines()[4])
        val mDates = SB_REPORT_DATES_REGEX.matchEntire(text.lines()[6])
        val mBal = SB_REPORT_BALANCES_REGEX.matchEntire(text.lines()[7])
        val mCur = SB_ACCOUNT_CURRENT_DATE_REGEX.matchEntire(text.lines()[text.lines().size - 2])

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
            startBalance = mBal?.gv("startBalance"),
            endBalance = mBal?.gv("endBalance"),
            currentDate = mCur?.gv("currentDate"),
        )
    }
}