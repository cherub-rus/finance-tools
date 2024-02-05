package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val SB_CCARD_REGEX = "<p>Карта Ставка Кредитный лимит<b>Кредитная (?<card>[a-zA-Zа-яА-я ]+ •••• \\d{4}) [0-9.]{4}% [0-9 ]{1,7},\\d{2}</b></p>".toRegex()
private val SB_CCARD_DATES_REGEX = "<p><b>ОСТАТОК ПО СЧЁТУ НА (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) ОСТАТОК ПО СЧЁТУ НА (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()
private val SB_CCARD_BALANCES_REGEX = "<p><b>(?<startBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) (?<endBalance>(\\d{1,3} )?\\d{1,3},\\d{2})</b></p>".toRegex()
private val SB_CCARD_ACCOUNT_REGEX = "<p>Счёт получателя: <b>(?<account>40817 810 \\d \\d{4} \\d{7})</b></p>".toRegex()

class SberProcessCreditCard(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)".toRegex(), "$1\n")

    override fun cleanUpRow(row: String) = row
        .cleanUpByRules(config.replaceInRow)

    override fun rowFilter(row: String) =
        row.contains(".202") && row.contains("</p><p><b>") && !row.contains("<!DOCTYPE")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5})</b>[0-9.]{10} [0-9]{6} (Tomsk )?(.+)</p><p><b>(.+?)( • Сниженная ставка – [0-9.]{1,5}%)? ([+]?(\\d{1,3} )?\\d{1,3},\\d{2}) ((\\d{1,3} )+\\d{1,3},\\d{2})</b></p>".toRegex(),
            "$1\t$4\t-$7\t\t\t\t$5\t$2:00\t\t\t$9\t$formula_c12\t$4"
        )
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5})</b>[0-9.]{10} - (.+)</p><p><b>(.+?) ([+]?(\\d{1,3} )?\\d{1,3},\\d{2}) ((\\d{1,3} )+\\d{1,3},\\d{2})</b></p>".toRegex(),
            "$1\t$3\t-$5\t\t\t\t$4\t$2:00\t\t\t$7\t$formula_c12\t$3"
        )
        .replace("-+", "")

    // TODO Reorder by date and time

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mAcc = SB_CCARD_ACCOUNT_REGEX.matchEntire(text.lines()[text.lines().size - 9])
        val mCard = SB_CCARD_REGEX.matchEntire(text.lines()[3])
        val mDates = SB_CCARD_DATES_REGEX.matchEntire(text.lines()[8])
        val mBal = SB_CCARD_BALANCES_REGEX.matchEntire(text.lines()[9])

        val number = mAcc?.gv("account")
/*  code from footer
        val code = number?.let {
            val s = it.replace(" ", "").takeLast(4)
            config.accounts.findByAccountNumber("*$s")?.code
        }
*/
        val card = mCard?.gv("card")?.let { getAccountCard(it) }
        val code = card?.let { config.accounts.findByCard(it)?.code }

        return AccountInfo(
            accountCode = code,
            accountNumber = number,
            cardNumber = card,
            startDate = mDates?.gv("startDate"),
            endDate = mDates?.gv("endDate"),
            // TODO currentDate = mCard?.gv("currentDate"),
            startBalance = mBal?.gv("startBalance"),
            endBalance = mBal?.gv("endBalance"),
            // TODO currentBalance = mCard?.gv("currentBalance")
        )
    }
}