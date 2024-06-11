package org.cherub.fintools.txttool.sms.mtsb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.config.ConfigData.Companion.MTS_BANK_ID
import org.cherub.fintools.txttool.*
import org.cherub.fintools.txttool.sms.IContentParser

val mtsbParsers = Pair(
    MTS_BANK_ID,
    listOf(
        MtsbParserMain(),
        MtsbParserAccountTransfer(),
        MtsbParserCardIncome(),
        MtsbParserCardTransfer(),
        MtsbParserCashback(),
        MtsbParserSBP()
    )
)

class MtsbParserMain : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            ("(?<operation>[^0-9]+) (?<amount>[0-9][0-9 ]*,[0-9]{2}) RUB( s uchetom skidki (?<discount>[0-9][0-9 ]*,[0-9]{2}) RUB)?" +
             " (?<message>.+) {2}Ostatok: (?<balance>[0-9][0-9 ]*,[0-9]{2}) RUB; (?<account>[*][0-9]{4}) ").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("account"),
            m.gv("operation"),
            m.gv("message"),
            getAmount(m.gv("amount"), m.gv("operation"), config),
            m.gv("balance").replace(" ", ""),
            null,
            null,
            m.gv("discount")
        )
    }
}

class MtsbParserAccountTransfer : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            "(?<operation>[^0-9]+) (?<account>[*][0-9]{4}); (?<amount>[0-9][0-9 ]*,[0-9]{2}) RUB; (Ostatok|Остаток): (?<balance>[0-9][0-9 ]*,[0-9]{2}) RUB".toRegex()
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

class MtsbParserCardIncome : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex =
            ("(?<operation>[^0-9]+) (?<amount>[0-9][0-9 ]*[.,][0-9]{2}) RUB" +
                    " (?<message>.+) (?<account>[*][0-9]{4})[.] (Ostatok|Остаток): (?<balance>[0-9][0-9 ]*[.,][0-9]{2}) RUB").toRegex()
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

class MtsbParserCardTransfer : IContentParser {
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

class MtsbParserCashback : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        val regex = ("^" +
                "На карту [*]{1}(?<account>[*][0-9]{4}) (?<message>.+): (?<amount>[0-9][0-9 ]*.[0-9]{2}) RUR." +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        val operation = "Cashback"
        return Transaction(
            m.gv("account"),
            operation,
            m.gv("message"),
            getAmount(m.gv("amount"), operation, config),
            ""
        )
    }
}

class MtsbParserSBP : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        val regex = ("^" +
                "Выполнен перевод (?<amount>[0-9][0-9 ]*(.[0-9]{2})?)р со счёта (?<account>[*][0-9]{4}). (?<message>.+) через СБП." +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        @Suppress("SpellCheckingInspection")
        val operation = "Perevod SBP"
        return Transaction(
            m.gv("account"),
            operation,
            "СБП ${m.gv("message")}",
            getAmount(m.gv("amount"), operation, config),
            ""
        )
    }
}

private fun getAmount(value: String, operation: String, config: ConfigData) =
    (config.findMtsOperationType(operation)?.sign ?: "") + value.fixAmountString()
