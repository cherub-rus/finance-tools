package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val SB_ACCOUNT_REGEX = "<p>Номер счёта <b>(?<account>40817 810 \\d \\d{4} \\d{7})</b></p>".toRegex()

class SberProcessPayAcc(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(<p><b>)".toRegex(), "\n$1")
        .replace("([.] Операция по[а-я ]{0,6})</p><p>([а-я ]{0,6}[*]{4}[0-9]{4}</p>)".toRegex(), "$1 $2")
        .replace("(</p>)\n".toRegex(), "$1")
        .replace("(</p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains("^<p><b>\\d\\d[.]\\d\\d".toRegex())

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5}) (.+) </b>([+-]?[0-9 ]+,[0-9]{2}) ([+-]?[0-9 ]+,[0-9]{2}) ([0-9.]{10}) [0-9]{6} (.+)[.] Операция по (карте|счету) [*]{4}[0-9]{4}</p>".toRegex(),
            prepareCsvOutputMask("$1", "$2:00", "-$4", "$7", "$5", "", "", "$3")
        )
        .replace("-+", "")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = SB_ACCOUNT_REGEX.matchEntire(text.lines()[9])
        val mStart = SB_REPORT_START_REGEX.matchEntire(text.lines()[14])
        val mEnd = SB_REPORT_END_REGEX.matchEntire(text.lines()[17])
        val mCur = SB_REPORT_CURRENT_DATE_REGEX.matchEntire(text.lines()[text.lines().size - 8])

        val number = mAcc?.gv("account")
        val code =  number?.let {
            val s = it.replace(" ", "").takeLast(4)
            config.accounts.findByAccountNumber("СЧЁТ$s")?.code
        }

        return AccountInfo(
            accountCode = code,
            accountNumber = number,
            startDate = mStart?.gv("startDate"),
            startBalance = mStart?.gv("startBalance"),
            endDate = mEnd?.gv("endDate"),
            endBalance = mEnd?.gv("endBalance"),
            currentDate = mCur?.gv("currentDate"),
        )
    }
}