package org.cherub.fintools.txttool.sms.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.config.ConfigData.Companion.SBER_BANK_ID
import org.cherub.fintools.txttool.*
import org.cherub.fintools.txttool.sms.IContentParser

val sberParsers = Pair(
    SBER_BANK_ID,
    listOf(
        SberParserMain(),
        SberParserTransferFromPerson(),
        SberParserTransferFromOtherBank(),
        SberParserTransferToCard(),
        SberParserTransferToAccount(),
        SberParserDeposit(),
        SberParserTransferInfo()
    )
)

class SberParserMain : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        if ( (".+ Перевод (?<amount>[0-9 ]{1,10}(.[0-9]{2})?)р от .+").toRegex().matches(content) ||
             (".+ Зачисление средств (?<amount>[0-9 ]{1,10}(.[0-9]{2})?)р на счёт .+").toRegex().matches(content) ) {
            return null
        }
        val regex = ("^" +
                "(?<cardId>[a-zA-Zа-яА-ЯёЁ-]{4}[0-9]{4})( (?<date>[0-9.]{8}))?( (?<time>[0-9:]{5}))? (?<operation>[^0-9]+) (?<amount>[0-9 ]{1,10}(.[0-9]{2})?)р" +
                "( (?<message>.+))? Баланс: (?<balance>.+)р( Сообщение\\: \"(?<userMessage>.+)\")?" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("cardId"),
            m.gv("operation"),
            m.gv("message") + m.gv("userMessage").quote(),
            getAmount(m.gv("amount"), m.gv("operation"), config),
            m.gv("balance").fixAmountString(),
            null,
            m.gv("time")
        )
    }
}

class SberParserTransferFromPerson : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        val regex = ("^" +
                "(?<cardId>[a-zA-Zа-яА-ЯёЁ-]{4}[0-9]{4})( (?<time>[0-9:]{5}))? Перевод( по СБП)? (?<bank>из .+ )?[+]?(?<amount>[0-9 ]{1,10}(.[0-9]{2})?)р" +
                " от ((?<payer>.+) )?Баланс: (?<balance>[0-9 ]{1,10}(.[0-9]{2})?)р(( Сообщение\\:)? \\\"(?<payerMessage>.+)\\\")?" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        val message =
            "от ${m.gv("payer")} ${m.gv("bank")} ${m.gv("payerMessage").quote()}"
        return Transaction(
            m.gv("cardId"),
            "Перевод от",
            message,
            getAmount(m.gv("amount"), "Перевод от", config),
            m.gv("balance").fixAmountString(),
            null,
            m.gv("time")
        )
    }
}

class SberParserTransferFromOtherBank : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        val regex = ("^" +
                "Перевод из (?<bank>.+) [+](?<amount>[0-9 ]{1,10}(.[0-9]{2})?)р от (?<payer>.+) " +
                "(?<cardId>[a-zA-Zа-яА-ЯёЁ-]{4}[0-9]{4}) — Баланс: (?<balance>[0-9 ]{1,10}(.[0-9]{2})?)р" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("cardId"),
            "Перевод от",
            "${m.gv("payer")} из ${m.gv("bank")} ",
            getAmount(m.gv("amount"), "Перевод от", config),
            m.gv("balance").fixAmountString(),
            null,
            null
        )
    }
}

class SberParserTransferToCard : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        val regex = ("^" +
                "(?<depositName>.+) (?<depositId>\\*[0-9]{4}) (?<time>[0-9:]{5}) Перевод (?<amount>[0-9 ]{1,10}(.[0-9]{2})?)р на карту (?<cardId>[a-zA-Zа-яА-ЯёЁ-]{4}[0-9]{4})." +
                " (Баланс (вклада|счёта): (?<depositBalance>.+)р, )?[Бб]аланс карты: (?<balance>.+)р" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        val operation = "Перевод на карту"
        return Transaction(
            m.gv("cardId"),
            operation,
            "${m.gv("depositName")} ${m.gv("depositId")}",
            getAmount(m.gv("amount"), operation, config),
            m.gv("balance").fixAmountString(),
            null,
            m.gv("time")
        )
    }
}

class SberParserTransferToAccount : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        val regex = ("^" +
                "(?<cardId>[a-zA-Zа-яА-ЯёЁ-]{4}[0-9]{4}) Зачисление средств (?<amount>[0-9 ]{1,10}(.[0-9]{2})?)р на счёт (?<depositName>.+) (?<depositId>\\*[0-9]{4})." +
                " (Баланс карты: (?<balance>.+)р)?(, )?([Бб]аланс (вклада|счёта): (?<depositBalance>.+)р)?" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        val operation = "Перевод на счёт"
        return Transaction(
            m.gv("cardId"),
            operation,
            "${m.gv("depositName")} ${m.gv("depositId")}",
            getAmount(m.gv("amount"), operation, config),
            m.gv("balance").fixAmountString()
        )
    }
}

class SberParserDeposit : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        val regex = ("^" +
                "(?<depositName>.+) (?<depositId>\\*[0-9]{4})( (?<time>[0-9:]{5}))? (?<operation>[^0-9]+) (?<amount>[0-9 ]{1,10}(.[0-9]{2})?)р(.)?" +
                " Баланс: (?<balance>.+)р(. (?<info>).+)?" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("depositId"),
            m.gv("operation"),
            m.gv("info"),
            getAmount(m.gv("amount"), m.gv("operation"), config),
            m.gv("balance").fixAmountString(),
            null,
            m.gv("time")
        )
    }
}

class SberParserTransferInfo : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        val regex = ("^" +
                "(?<cardId>[a-zA-Zа-яА-ЯёЁ-]{4}[0-9]{4})( (?<date>[0-9.]{8}))?( (?<time>[0-9:]{5}))? (?<operation>[^0-9]+) (?<amount>[0-9 ]{1,10}(.[0-9]{2})?)р" +
                " (?<message>.+?)( Сообщение[:] (?<userMessage>.+))?" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("cardId"),
            "#####",
            m.gv("message") + m.gv("userMessage").quote(),
            m.gv("amount"),
            "",
            m.gv("date"),
            m.gv("time")
        )
    }
}

private fun getAmount(value: String, operation: String, config: ConfigData) =
    (config.findSberOperationType(operation)?.sign ?: "") + value.fixAmountString()
