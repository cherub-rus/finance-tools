package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val SB_CCARD_REGEX = "<p>Карта Ставка Кредитный лимит<b>Кредитная (?<card>[a-zA-Zа-яА-я ]+ •••• \\d{4}) [0-9.]{4}% [0-9 ]{1,7},\\d{2}</b></p>".toRegex()
private val SB_CCARD_DATES_REGEX = "<p><b>ОСТАТОК ПО СЧЁТУ НА (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) ОСТАТОК ПО СЧЁТУ НА (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()
private val SB_CCARD_BALANCES_REGEX = "<p><b>(?<startBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) (?<endBalance>(\\d{1,3} )?\\d{1,3},\\d{2})</b></p>".toRegex()
private val SB_CCARD_CURRENT_DATE_REGEX = "<p>Дата формирования <b>(?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()

class SberProcessCreditCard(config: ConfigData) : CommonProcessor(config, true) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(<p><b>)".toRegex(), "\n$1")
        .replace("(</b></p>)<".toRegex(), "$1\n<")
        .replace("(<p>Продолжение на)".toRegex(), "\n$1<")

    override fun cleanUpRow(row: String) = row
        .cleanUpByRules(config.replaceInRow)

    override fun rowFilter(row: String) =
        row.contains(".202") && !row.contains("</b></p>")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5}) (.+?)( • Сниженная ставка – [0-9.]{1,5}%)?</b>([+]?(\\d{1,3} )?\\d{1,3},\\d{2}) ((\\d{1,3} )+\\d{1,3},\\d{2}) [0-9.]{10} [0-9]{6} (Tomsk )?(.+)</p>".toRegex(),
            "$1\t$10\t-$5\t\t\t\t$3\t$2:00\t\t\t$7\t$formula_c12\t$10"
        )
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5}) (.+?)</b>([+]?(\\d{1,3} )?\\d{1,3},\\d{2}) ((\\d{1,3} )+\\d{1,3},\\d{2}) [0-9.]{10} - (.+)</p>".toRegex(),
            "$1\t$8\t-$4\t\t\t\t$3\t$2:00\t\t\t$6\t$formula_c12\t$8"
        )
        .replace("-+", "")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mCard = SB_CCARD_REGEX.matchEntire(text.lines()[4])
        val mDates = SB_CCARD_DATES_REGEX.matchEntire(text.lines()[9])
        val mBal = SB_CCARD_BALANCES_REGEX.matchEntire(text.lines()[10])
        val mCur = SB_CCARD_CURRENT_DATE_REGEX.matchEntire(text.lines()[text.lines().size - 2])

        val card = mCard?.gv("card")?.let { getAccountCard(it) }
        val code = card?.let { config.accounts.findByCard(it)?.code }

        return AccountInfo(
            accountCode = code,
            cardNumber = card,
            startDate = mDates?.gv("startDate"),
            endDate = mDates?.gv("endDate"),
            startBalance = mBal?.gv("startBalance"),
            endBalance = mBal?.gv("endBalance"),
            currentDate = mCur?.gv("currentDate")
        )
    }
}