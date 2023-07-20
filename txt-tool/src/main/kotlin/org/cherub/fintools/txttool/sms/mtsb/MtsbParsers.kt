package org.cherub.fintools.txttool.sms.mtsb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.txttool.*
import org.cherub.fintools.txttool.sms.IContentParser

private const val MTS_BANK_SENDER = "MTS-Bank"
val mtsbParsers = Pair(MTS_BANK_SENDER, listOf(MtsbParser1(), MtsbParser2(), MtsbParser3()))

class MtsbParser1 : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "(?<operation>[^0-9]+) (?<amount>[0-9][0-9 ]*,[0-9]{2}) RUB (?<message>.+) {2}Ostatok: (?<balance>[0-9][0-9 ]*,[0-9]{2}) RUB; (?<account>[*][0-9]{4}) ".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            account = m.gv("account"),
            operation = m.gv("operation"),
            message = m.gv("message"),
            amount = getAmount(m.gv("amount"), m.gv("operation"), config),
            balance = m.gv("balance").replace(" ", "")
        )
    }
}

class MtsbParser2 : IContentParser {
     override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "(?<operation>[^0-9]+) (?<account>[*][0-9]{4}); (?<amount>[0-9][0-9 ]*,[0-9]{2}) RUB; Ostatok: (?<balance>[0-9][0-9 ]*,[0-9]{2}) RUB".toRegex()
        val m = regex.matchEntire(content) ?: return null

         return Transaction(
             account = m.gv("account"),
             operation = m.gv("operation"),
             message = "",
             amount = getAmount(m.gv("amount"), m.gv("operation"), config),
             balance = m.gv("balance").replace(" ", "")
         )
    }
}

class MtsbParser3 : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "(?<operation>[^0-9]+) (?<account>[*][0-9]{4}) (?<date>[0-9.]{5}) (?<time>[0-9:]{5}) (?<message>.+?);? (?<amount>[0-9][0-9 ]*,[0-9]{2}) RUB Ostatok: (?<balance>[0-9][0-9 ]*,[0-9]{2}) RUB;? ".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            account = m.gv("account"),
            operation = m.gv("operation"),
            message = m.gv("message"),
            amount = getAmount(m.gv("amount"), m.gv("operation"), config),
            balance = m.gv("balance").replace(" ", ""),
            date = m.gv("date"),
            time = m.gv("time")
        )
    }
}

fun getAmount(value: String, operation: String, config: ConfigData) =
    if (config.mtsbUseIncomes) {
        if (operation.startsWithAny(config.mtsbSmsIncomes)) "" else "-"
    } else {
        if (operation.startsWithAny(config.mtsbSmsExpenses)) "-" else ""
    } + value.replace(" ", "")

internal fun String.startsWithAny(list: List<String>) =
    list.any { this.startsWith(it, ignoreCase = true) }
