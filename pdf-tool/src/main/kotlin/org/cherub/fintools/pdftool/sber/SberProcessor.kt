package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.AccountInfo
import org.cherub.fintools.pdftool.CommonProcessor
import org.cherub.fintools.pdftool.findByAccountNumber
import org.cherub.fintools.pdftool.gv

private const val SB_ACCOUNT_REGEX = "<p>Счёт Валюта Доступно на (?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4})<b>(?<account>40817 810 \\d \\d{4} \\d{7}) РУБЛЬ РФ (?<currentBalance>(\\d{1,3} )?\\d{1,3},\\d{2})</b></p>"
private const val SB_REPORT_DATES_REGEX = "<p><b>ОСТАТОК НА (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) ОСТАТОК НА (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4}) ВСЕГО СПИСАНИЙ ВСЕГО ПОПОЛНЕНИЙ</b></p>"
private const val SB_REPORT_BALANCES_REGEX = "<p><b>(?<startBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) (?<endBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) ((\\d{1,3} )?\\d{1,3},\\d{2}) ((\\d{1,3} )?\\d{1,3},\\d{2})</b></p>"

abstract class SberProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun cleanUpHtml(text: String) = super.cleanUpHtml(text)
        .replace("(В валюте счёта</p>)".toRegex(), "$1\n")
        .replace("(<p>Сумма в валюте операции²</p>)".toRegex(), "$1\n")

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
