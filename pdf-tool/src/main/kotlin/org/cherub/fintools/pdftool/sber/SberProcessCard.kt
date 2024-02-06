package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val SB_CARD_REGEX = "<p>Карта Валюта Доступно на (?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4})<b>(?<card>[a-zA-Zа-яА-я ]+ •••• \\d{4}) РУБЛЬ РФ (?<currentBalance>(\\d{1,3} )?\\d{1,3},\\d{2})</b></p>".toRegex()

class SberProcessCard(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains(".202") && row.contains("</p><p><b>") && !row.contains("<!DOCTYPE") && !row.contains("по счёту не производилось.")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5})</b>.{12,17}</p><p><b>(.+)</b>(.+)</p><p><b>([+-]?[0-9, ]+)</b></p>".toRegex(),
            "$1\t$4\t-$5\t\t\t\t$3\t$2:00\t\t\t$formula_c11\t$formula_c12\t$4"
        )
        .replace("-+", "")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mCard = SB_CARD_REGEX.matchEntire(text.lines()[3])
        val mDates = SB_REPORT_DATES_REGEX.matchEntire(text.lines()[5])
        val mBal = SB_REPORT_BALANCES_REGEX.matchEntire(text.lines()[6])

        val card = mCard?.gv("card")?.let { getAccountCard(it) }
        val code = card?.let { config.accounts.findByCard(it)?.code }

        return AccountInfo(
            accountCode = code,
            cardNumber = card,
            startDate = mDates?.gv("startDate"),
            endDate = mDates?.gv("endDate"),
            currentDate = mCard?.gv("currentDate"),
            startBalance = mBal?.gv("startBalance"),
            endBalance = mBal?.gv("endBalance"),
            currentBalance = mCard?.gv("currentBalance")
        )
    }
}

internal fun getAccountCard(sbCardText: String) =
    sbCardText.let {
        val cParts = it.split(" •••• ")
        val cType = when {
            cParts[0] == "MIR" -> "MIR-"
            cParts[0] == "СберКарта" -> "MIR-"
            cParts[0].startsWith("Visa") -> "VISA"
            cParts[0].startsWith("MasterCard") -> "ECMC"
            else -> "UNKNOWN"
        }
        "$cType${cParts[1]}"
    }
