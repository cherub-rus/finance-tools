package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val SB_CARD_REGEX = "<p>Карта (?<card>[a-zA-Zа-яА-я ]+ •••• \\d{4})</p>".toRegex()

class SberProcessCard(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(<p><b>)".toRegex(), "\n$1")
        .replace("([.] Операция по[а-я ]{0,6})</p><p>([а-я ]{0,6}[*]{4}[0-9]{4}</p>)".toRegex(), "$1 $2")
        .replace("(</p>)\n".toRegex(), "$1")
        .replace("(</p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains("^<p><b>\\d\\d[.]\\d\\d".toRegex())

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5}) (.+?) </b>([+]?(\\d{1,3} )*\\d{1,3},\\d{2}) ((\\d{1,3} )*\\d{1,3},\\d{2}) [0-9.]{10} \\d{6} (.+)\\. Операция по карте (.+)</p>".toRegex(),
            prepareCsvOutputMask("$1", "$2:00", "-$4", "$8", "$6", "", "", "$3")
        )
        .replace("-+", "")

    override fun discoverAccountInfo(text: String): AccountInfo {

        val mCard = SB_CARD_REGEX.matchEntire(text.lines()[10])
        val mStart = SB_REPORT_START_REGEX.matchEntire(text.lines()[15])
        val mEnd = SB_REPORT_END_REGEX.matchEntire(text.lines()[18])
        val mCur = SB_REPORT_CURRENT_DATE_REGEX.matchEntire(text.lines()[text.lines().size - 8])

        val card = mCard?.gv("card")?.let { getAccountCard(it) }
        val code = card?.let { config.accounts.findByCard(it)?.code }

        return AccountInfo(
            accountCode = code,
            cardNumber = card,
            startDate = mStart?.gv("startDate"),
            startBalance = mStart?.gv("startBalance"),
            endDate = mEnd?.gv("endDate"),
            endBalance = mEnd?.gv("endBalance"),
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
