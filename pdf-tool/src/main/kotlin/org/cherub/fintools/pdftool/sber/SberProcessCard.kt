package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val SB_CARD_REGEX = "<p>Карта Номер счёта Дата открытия счёта<b>(?<card>[a-zA-Zа-яА-я ]+ •••• \\d{4}) (40817 810 \\d \\d{4} \\d{7}) ([0-9.]{10})</b></p>".toRegex()

class SberProcessCard(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(<p><b>)".toRegex(), "\n$1")
        .replace("(</b></p>)<".toRegex(), "$1\n<")
        .replace("(<p>Продолжение на)".toRegex(), "\n$1")
        .replace("</p><p>", " ")

    override fun rowFilter(row: String) =
        row.contains(".202") && !row.contains("</b></p>") && !row.contains("<!DOCTYPE") && !row.contains("по счёту не производилось.")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5}) </b>\\d{6} <b>(.+?) </b>([+]?(\\d{1,3} )*\\d{1,3},\\d{2}) ((\\d{1,3} )*\\d{1,3},\\d{2}) [0-9.]{10} (.+)\\. Операция по карте (.+)</p>".toRegex(),
            prepareCsvOutputMask("$1", "$2:00", "-$4", "$8", "$6", "", "", "$3")
        )
        .replace("-+", "")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mCard = SB_CARD_REGEX.matchEntire(text.lines()[3])
        val mDates = SB_REPORT_DATES_REGEX.matchEntire(text.lines()[6])
        val mBal = SB_REPORT_BALANCES_REGEX.matchEntire(text.lines()[7])
        val mCur = SB_ACCOUNT_CURRENT_DATE_REGEX.matchEntire(text.lines()[text.lines().size - 2])

        val card = mCard?.gv("card")?.let { getAccountCard(it) }
        val code = card?.let { config.accounts.findByCard(it)?.code }

        return AccountInfo(
            accountCode = code,
            cardNumber = card,
            startDate = mDates?.gv("startDate"),
            endDate = mDates?.gv("endDate"),
            startBalance = mBal?.gv("startBalance"),
            endBalance = mBal?.gv("endBalance"),
            currentDate = mCur?.gv("currentDate"),
        )
    }
}

internal fun getAccountCard(sbCardText: String) =
    sbCardText.let {
        val cParts = it.split(" •••• ")
        val cType = when {
            cParts[0] == "MIR" -> "MIR-"
            cParts[0] == "СберКарта" -> "MIR-"
            cParts[0].startsWith("МИР") -> "MIR-"
            cParts[0].startsWith("Visa") -> "VISA"
            cParts[0].startsWith("MasterCard") -> "ECMC"
            else -> "UNKNOWN"
        }
        "$cType${cParts[1]}"
    }
