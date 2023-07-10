package org.cherub.fintools.txttool.sms.mtsb

import org.cherub.fintools.txttool.*
import org.cherub.fintools.txttool.sms.IContentParser

private const val MTS_BANK_SENDER = "MTS-Bank"

var useIncomes = false
@Suppress("SpellCheckingInspection")
val incomes = listOf("Perevod na kartu", "Prihod po schetu karty")
@Suppress("SpellCheckingInspection")
val expenses = listOf("Oplata", "Perevod s karty")

fun getAmount(value: String, operation: String) =
    if (useIncomes) {
        if (incomes.any { operation.lowercase().startsWith(it.lowercase()) }) "" else "-"
    } else {
        if (expenses.any { operation.lowercase().startsWith(it.lowercase()) }) "-" else ""
    } + value.replace(" ", "")

val mtsbParsers = Pair(MTS_BANK_SENDER, listOf(MtsbParser1(), MtsbParser2(), MtsbParser3()))

class MtsbParser1 : IContentParser {
    override fun parse(content: String): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "([^0-9]+) ([0-9][0-9 ]*,[0-9]{2}) RUB (.+) {2}Ostatok: ([0-9][0-9 ]*,[0-9]{2}) RUB; ([*][0-9]{4}) ".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            account = m.gv(5),
            operation = m.gv(1),
            message = m.gv(3),
            amount = getAmount(m.gv(2), m.gv(1)),
            balance = m.gv(4).replace(" ", "")
        )
    }
}

class MtsbParser2 : IContentParser {
     override fun parse(content: String): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "([^0-9]+) ([*][0-9]{4}); ([0-9][0-9 ]*,[0-9]{2}) RUB; Ostatok: ([0-9][0-9 ]*,[0-9]{2}) RUB".toRegex()
        val m = regex.matchEntire(content) ?: return null

         return Transaction(
             account = m.gv(2),
             operation = m.gv(1),
             message = "",
             amount = getAmount(m.gv(3), m.gv(1)),
             balance = m.gv(4).replace(" ", "")
         )
    }
}

class MtsbParser3 : IContentParser {
    override fun parse(content: String): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "([^0-9]+) ([*][0-9]{4}) ([0-9.]{5}) ([0-9:]{5}) (.+?);? ([0-9][0-9 ]*,[0-9]{2}) RUB Ostatok: ([0-9][0-9 ]*,[0-9]{2}) RUB;? ".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            account = m.gv(2),
            operation = m.gv(1),
            message = m.gv(5),
            amount = getAmount(m.gv(6), m.gv(1)),
            balance = m.gv(7).replace(" ", ""),
            date = m.gv(3),
            time = m.gv(4)
        )
    }
}