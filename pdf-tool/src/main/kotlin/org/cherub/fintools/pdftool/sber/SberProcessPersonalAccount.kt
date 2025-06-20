package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.AccountInfo
import org.cherub.fintools.pdftool.findByAccountNumber
import org.cherub.fintools.pdftool.*

private val SB_ACCOUNT_REGEX_PA = ".+<p>Номер счёта</p><p><b>(?<account>40817 810 \\d \\d{4} \\d{7})</b></p>.+".toRegex()
private val SB_REPORT_INFO_REGEX_PA = (
        "<p><b>Остаток средств (?<startBalance>(\\d{1,3} )*\\d{1,3},\\d{2})</b>на (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4})</p>" +
        "<p><b>Пополнение .+</b>.+</p><p>.+</p>" +
        "<p><b>Остаток средств (?<endBalance>(\\d{1,3} )*\\d{1,3},\\d{2})</b>на (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4})</p><p><b>Списание .+</b></p>"
    ).toRegex()

class SberProcessPersonalAccount(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)(<p><b>)".toRegex(), "$1\n$2")
        .replace("(<p>Продолжение на следующей странице</p>)".toRegex(), "\n$1")
        .replace("(<p>Дата формирования <b>)".toRegex(), "\n$1")

    override fun rowFilter(row: String) =
        row.contains("к/с ")

    override fun transformToCsv(row: String) = row
        .replace(
            //     ($1        ) ($2 )    ($3 )                     ($4        )  ($5                   ) ($6              )        #
            "<p><b>([0-9.]{10}) (.+?)</b>(.+?)</p><p><b>-?[0-9]{2},( № [0-9-]+)? ([+-]?[0-9 ]+,[0-9]{2}) ([0-9 ]+,[0-9]{2})</b></p>".toRegex(),
            prepareCsvOutputMask("$1", "00:00", "-$5", "$3", "$6", "", "", "$2")
        )
        .replace("-+", "")
        .replace("--", "-")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = SB_ACCOUNT_REGEX_PA.matchAt(text.lines()[3], 0)
        val mRInfo = SB_REPORT_INFO_REGEX_PA.matchEntire(text.lines()[4])

        val number = mAcc?.gv("account")
        val code =  number?.let {
            val s = it.replace(" ", "").takeLast(4)
            config.accounts.findByAccountNumber("*$s")?.code
        }

        return AccountInfo(
            accountCode = code,
            accountNumber = number,
            startDate = mRInfo?.gv("startDate"),
            endDate = mRInfo?.gv("endDate"),
            startBalance = mRInfo?.gv("startBalance"),
            endBalance = mRInfo?.gv("endBalance"),
        )
    }
}