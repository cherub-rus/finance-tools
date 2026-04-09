package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.AccountInfo
import org.cherub.fintools.pdftool.findByAccountNumber
import org.cherub.fintools.pdftool.*

private val SB_ACCOUNT_REGEX_PA = "<p>Номер счёта <b>(?<account>40817 810 \\d \\d{4} \\d{7})</b></p>".toRegex()

class SberProcessPersonalAccount(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("([0-9]{7})</p><p>(<b>)".toRegex(), "$1$2")
        .replace("(</p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains("к/с ")

    override fun transformToCsv(row: String) = row
        .replace(
            //     ($1        ) ($2 )    ($3 )                     ($4        )  ($5                   ) ($6              )        #
            "<p><b>([0-9.]{10}) (.+?)</b>(.+?)<b>-?[0-9]{2},( № [0-9-]+)? ([+-]?[0-9 ]+,[0-9]{2}) ([0-9 ]+,[0-9]{2})</b></p>".toRegex(),
            prepareCsvOutputMask("$1", "00:00", "-$5", "$3", "$6", "", "", "$2")
        )
        .replace("-+", "")
        .replace("--", "-")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = SB_ACCOUNT_REGEX_PA.matchEntire(text.lines()[10])
        val mStart = SB_REPORT_START_REGEX.matchEntire(text.lines()[19])
        val mEnd = SB_REPORT_END_REGEX.matchEntire(text.lines()[23])
        val mCur = SB_REPORT_CURRENT_DATE_REGEX.matchEntire(text.lines()[text.lines().size - 12])

        val number = mAcc?.gv("account")
        val code = number?.let {
            val s = it.replace(" ", "").takeLast(4)
            config.accounts.findByAccountNumber("*$s")?.code
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