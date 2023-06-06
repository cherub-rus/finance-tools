package org.cherub.fintools.txttool.sms.mtsb

import org.cherub.fintools.txttool.sms.IContentParser
import org.cherub.fintools.txttool.sms.Transaction
import org.cherub.fintools.txttool.sms.gv

var useIncomes = false
val incomes = listOf("Perevod na kartu", "Prihod po schetu karty")
val expenses = listOf("Oplata", "Perevod s karty")

fun String.getSignForOperation() =
    if (useIncomes) {
        if (incomes.any { this.startsWith(it) }) "" else "-"
    } else {
        if (expenses.any { this.startsWith(it) }) "-" else ""
    }

val mtsbParsers = Pair("MTS-Bank", listOf(MtsbParser1(), MtsbParser2(), MtsbParser3()))

class MtsbParser1 : IContentParser {
    override fun parse(content: String): Transaction? {
        val regex =
            "([^0-9]+) ([0-9][0-9 ]*,[0-9]{2}) RUB (.+) {2}Ostatok: ([0-9][0-9 ]*,[0-9]{2}) RUB; ([*][0-9]{4}) ".toRegex()
        val m = regex.matchEntire(content) ?: return null

        val amount = m.gv(1).getSignForOperation() + m.gv(2).replace(" ", "")
        return Transaction(m.gv(5), m.gv(1), m.gv(3), amount, m.gv(4).replace(" ", ""))
    }
}

class MtsbParser2 : IContentParser {
     override fun parse(content: String): Transaction? {
        val regex =
            "([^0-9]+) ([*][0-9]{4}); ([0-9][0-9 ]*,[0-9]{2}) RUB; Ostatok: ([0-9][0-9 ]*,[0-9]{2}) RUB".toRegex()
        val m = regex.matchEntire(content) ?: return null

        val amount = m.gv(1).getSignForOperation() + m.gv(3).replace(" ", "")
        return Transaction(m.gv(2), m.gv(1), "", amount, m.gv(4).replace(" ", ""))
    }
}

class MtsbParser3 : IContentParser {
    override fun parse(content: String): Transaction? {
        val regex =
            "([^0-9]+) ([*][0-9]{4}) ([0-9.]{5}) ([0-9:]{5}) (.+?);? ([0-9][0-9 ]*,[0-9]{2}) RUB Ostatok: ([0-9][0-9 ]*,[0-9]{2}) RUB;? ".toRegex()
        val m = regex.matchEntire(content) ?: return null

        val amount = m.gv(1).getSignForOperation() + m.gv(6).replace(" ", "")
        return Transaction(m.gv(2), m.gv(1), m.gv(5), amount, m.gv(7).replace(" ", ""), m.gv(3), m.gv(4))
    }
}