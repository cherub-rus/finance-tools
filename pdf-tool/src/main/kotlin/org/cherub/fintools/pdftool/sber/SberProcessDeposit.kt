package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.AccountInfo
import org.cherub.fintools.pdftool.findByAccountNumber
import org.cherub.fintools.pdftool.formula_c12
import org.cherub.fintools.pdftool.gv

private val SB_ACCOUNT_REGEX_D = ".+<p>Номер счёта<b>(?<account>40817 810 \\d \\d{4} \\d{7})</b></p>.+".toRegex()
private val SB_REPORT_INFO_REGEX = "<p><b>Остаток средств</b>на (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4})</p><p><b>(?<startBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) Остаток средств</b>на (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4})</p><p><b>(?<endBalance>(\\d{1,3} )?\\d{1,3},\\d{2})</b></p>".toRegex()

class SberProcessDeposit(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)(<p><b>)".toRegex(), "$1\n$2")
        .replace("(<p>ШИФР СУММА ОПЕРАЦИИ ОСТАТОК НА СЧЁТЕ3</p>)".toRegex(), "$1\n")
        .replace("(<p>Продолжение на следующей странице</p>)".toRegex(), "\n$1")
        .replace("(<p>Дата формирования:</p>)".toRegex(), "\n$1")

    override fun rowFilter(row: String) =
        row.contains("к/с ")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) (.+?)</b></p><p>(.+?)<b>-?[0-9] ([+-]?[0-9 ]+,[0-9]{2}) ([0-9 ]+,[0-9]{2})</b></p>".toRegex(),
            "$1\t00:00\t\t-$4\t\t\t\t$3\t$5\t$formula_c12\t\t\t$2\t$3"
        )
        .replace("-+", "")
        .replace("--", "-")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = SB_ACCOUNT_REGEX_D.matchAt(text.lines()[2], 0)
        val mRInfo = SB_REPORT_INFO_REGEX.matchEntire(text.lines()[3])

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