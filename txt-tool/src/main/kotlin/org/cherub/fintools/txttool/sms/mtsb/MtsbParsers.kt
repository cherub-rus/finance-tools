package org.cherub.fintools.txttool.sms.mtsb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.txttool.*
import org.cherub.fintools.txttool.sms.IContentParser

val mtsbParsers = Pair(
    "MTS-Bank",
    listOf(
        MtsbParserMain(),
        MtsbParserAccountTransfer(),
        MtsbParserCardTfansfer()
    )
)

class MtsbParserMain : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "(?<operation>[^0-9]+) (?<amount>[0-9][0-9 ]*,[0-9]{2}) RUB (?<message>.+) {2}Ostatok: (?<balance>[0-9][0-9 ]*,[0-9]{2}) RUB; (?<account>[*][0-9]{4}) ".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("account"),
            m.gv("operation"),
            m.gv("message"),
            getAmount(m.gv("amount"), m.gv("operation"), config),
            m.gv("balance").replace(" ", "")
        )
    }
}

class MtsbParserAccountTransfer : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "(?<operation>[^0-9]+) (?<account>[*][0-9]{4}); (?<amount>[0-9][0-9 ]*,[0-9]{2}) RUB; Ostatok: (?<balance>[0-9][0-9 ]*,[0-9]{2}) RUB".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("account"),
            m.gv("operation"),
            "",
            getAmount(m.gv("amount"), m.gv("operation"), config),
            m.gv("balance").replace(" ", "")
        )
    }
}

class MtsbParserCardTfansfer : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "(?<operation>[^0-9]+) (?<account>[*][0-9]{4}) (?<date>[0-9.]{5}) (?<time>[0-9:]{5}) (?<message>.+?);? (?<amount>[0-9][0-9 ]*,[0-9]{2}) RUB Ostatok: (?<balance>[0-9][0-9 ]*,[0-9]{2}) RUB;? ".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("account"),
            m.gv("operation"),
            m.gv("message"),
            getAmount(m.gv("amount"), m.gv("operation"), config),
            m.gv("balance").replace(" ", ""),
            m.gv("date"),
            m.gv("time")
        )
    }
}

private fun getAmount(value: String, operation: String, config: ConfigData) =
    if (config.mtsbUseIncomes) {
        if (operation.startsWithAny(config.mtsbSmsIncomes)) "" else "-"
    } else {
        if (operation.startsWithAny(config.mtsbSmsExpenses)) "-" else ""
    } + value.replace(" ", "")
