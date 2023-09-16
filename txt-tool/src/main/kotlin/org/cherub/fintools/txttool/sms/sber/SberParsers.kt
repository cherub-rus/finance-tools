package org.cherub.fintools.txttool.sms.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.txttool.*
import org.cherub.fintools.txttool.sms.IContentParser

private const val SBER_BANK_SENDER = "900"
val sberParsers = Pair(SBER_BANK_SENDER, listOf(SberParser1()))

class SberParser1 : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        @Suppress("SpellCheckingInspection")
        val regex = // TODO \d to [0-9]; {0,1} to ?
            "^(?<cardId>[a-zA-Zа-яА-ЯёЁ]{4}\\d{4})( (?<date>\\d{2}\\.\\d{2}\\.\\d{2})){0,1}( (?<time>\\d{2}:\\d{2})){0,1} (?<operation>[^0-9]+) (?<amount>\\d{1,10}(.\\d{2}){0,1})р (с комиссией (?<fee>\\d{1,4}(.\\d{2}){0,1})р ){0,1}(?<message>.+)?Баланс: (?<balance>.+)р( Сообщение\\: \"(?<userMessage>.+)\"){0,1}$".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("cardId"),
            m.gv("operation"),
            m.gv("message") + m.gv("userMessage"), // TODO
            getAmount(m.gv("amount"), m.gv("operation"), config)./*TODO*/replace(" ", "").replace(".", ",").appendDecimals(),
            m.gv("balance")./*TODO*/replace(" ", "").replace(".", ",").appendDecimals()
        )
    }
}

private fun getAmount(value: String, operation: String, config: ConfigData) =
    if (config.sberUseIncomes) {
        if (operation.startsWithAny(config.sberSmsIncomes)) "" else "-"
    } else {
        if (operation.startsWithAny(config.sberSmsExpenses)) "-" else ""
    } + value
