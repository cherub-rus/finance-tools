package org.cherub.fintools.pdftool.gpb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val GPB_DEPOSIT_ACCOUNT_REGEX = "<p>.+ (?<account>40817810\\d{12}) Российский Рубль Депозитный</p>".toRegex()
private val GPB_DEPOSIT_DATES_REGEX = "<p><b>ВЫПИСКА ПО СЧЕТУ</b>За период (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) - (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4}) Выписка сформирована (?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2})</p>".toRegex()
private val GPB_DEPOSIT_BALANCES_REGEX = "<p>(?<startBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) руб\\. ((\\d{1,3} )?\\d{1,3},\\d{2}) руб\\. ((\\d{1,3} )?\\d{1,3},\\d{2}) руб\\. (?<endBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) руб\\.</p>".toRegex()

class GpbDepositProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun reversRows() = false

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(<b>валюте счета\\*</b>)".toRegex(), "$1\n")
        .replace("(</p><p>\\*без учета сумм заблокированных)".toRegex(), "\n$1")
        .replace("([^-])( )([0-9]{2}[.][0-9]{2}[.][0-9]{4} [А-Я])".toRegex(), "\$1\n$3")
        .replace("(</p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        !(row.contains("<p>") || row.contains("</p>") || row.contains("</div>"))

    override fun transformToCsv(row: String) = row
        .replace(
            "([0-9]{2}[.][0-9]{2}[.][0-9]{4}) (.+) ([+-])(([1-9]?[0-9]{0,2} )?[0-9]{1,3},[0-9]{2}) (([1-9]?[0-9]{0,2} )?[0-9]{1,3},[0-9]{2})".toRegex(),
            prepareCsvOutputMask("$1", "", "$3$4", "$2", "$6", "", "", "$2")
        )

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = GPB_DEPOSIT_ACCOUNT_REGEX.matchEntire(text.lines()[6])
        val mBal = GPB_DEPOSIT_BALANCES_REGEX.matchEntire(text.lines()[7])
        val mDates = GPB_DEPOSIT_DATES_REGEX.matchEntire(text.lines()[8])

        val number = mAcc?.gv("account")
        val code =  number?.let {
            val s = it.takeLast(4)
            config.accounts.findByAccountNumber("*$s")?.code
        }

        return AccountInfo(
            accountCode = code,
            accountNumber = number,
            startDate = mDates?.gv("startDate"),
            endDate = mDates?.gv("endDate"),
            currentDate = mDates?.gv("currentDate"),
            startBalance = mBal?.gv("startBalance"),
            endBalance = mBal?.gv("endBalance"),
        )
    }
}
