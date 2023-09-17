package org.cherub.fintools.txttool.sms.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.txttool.*
import org.cherub.fintools.txttool.sms.IContentParser

val sberParsers = Pair(
    "900",
    listOf(
        SberParserTransferFromPerson(),
        SberParserMain(),
        SberParserTransferToCard(),
        SberParserTransferToAccount(),
        SberParserDeposit(),
        SberParserSMS()
    )
)

class SberParserMain : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        if (content.contains("Недостаточно средств")) return null

        val regex = ("^" +
                "(?<cardId>[a-zA-Zа-яА-ЯёЁ-]{4}[0-9]{4})( (?<date>[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}))?( (?<time>[0-9]{2}:[0-9]{2}))? (?<operation>[^0-9]+) (?<amount>[0-9]{1,10}(.[0-9]{2})?)р" +
                "( (с комиссией (?<fee>[0-9]{1,4}(.[0-9]{2})?)р ))?( (?<message>.+))? Баланс: (?<balance>.+)р( Сообщение\\: \"(?<userMessage>.+)\")?" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("cardId"),
            m.gv("operation"),
            m.gv("message") + m.gv("userMessage").quote(),
            getAmount(m.gv("amount"), m.gv("operation"), config),
            m.gv("balance").fixAmountString()
        )
    }
}

class SberParserTransferFromPerson : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        val regex = ("^" +
                "(?<cardId>[a-zA-Zа-яА-ЯёЁ-]{4}[0-9]{4})( (?<time>[0-9:]{5}))? Перевод (?<amount>[0-9]{1,10}(.[0-9]{2})?)р" +
                " от ((?<payer>.+) )?Баланс: (?<balance>.+)р( \"(?<payerMessage>.+)\")?" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("cardId"),
            "Перевод от",
            "от ${m.gv("payer")}" + m.gv("payerMessage").quote(),
            getAmount(m.gv("amount"), "Перевод от", config),
            m.gv("balance").fixAmountString()
        )
    }
}

class SberParserTransferToCard : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        val regex = ("^" +
                "(?<depositName>.+) (?<depositId>\\*[0-9]{4}) (?<time>[0-9:]{5}) Перевод (?<amount>[0-9]{1,10}(.[0-9]{2})?)р на карту (?<cardId>[a-zA-Z-]{4}[0-9]{4})." +
                " (Баланс (вклада|счёта): (?<depositBalance>.+)р, )?[Бб]аланс карты: (?<balance>.+)р" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        val operation = "Перевод на карту"
        return Transaction(
            m.gv("cardId"),
            operation,
            "${m.gv("depositName")} ${m.gv("depositId")}",
            getAmount(m.gv("amount"), operation, config),
            m.gv("balance").fixAmountString()
        )
    }
}

class SberParserTransferToAccount : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {
        val regex = ("^" +
                "(?<cardId>[a-zA-Z-]{4}[0-9]{4}) Зачисление средств (?<amount>[0-9]{1,10}(.[0-9]{2})?)р на счёт (?<depositName>.+) (?<depositId>\\*[0-9]{4})." +
                " (Баланс карты: (?<balance>.+)р, )?[Бб]аланс (вклада|счёта): (?<depositBalance>.+)р" +
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
                "(?<depositName>.+) (?<depositId>\\*[0-9]{4})( (?<time>[0-9:]{5}))? (?<operation>[^0-9]+) (?<amount>[0-9]{1,10}(.[0-9]{2})?)р(.)?" +
                " Баланс: (?<balance>.+)р(. (?<info>).+)?" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("depositId"),
            m.gv("operation"),
            m.gv("info"),
            getAmount(m.gv("amount"), m.gv("operation"), config),
            m.gv("balance").fixAmountString()
        )
    }
}

class SberParserSMS : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        val regex = ("^" +
                "(?<cardId>[a-zA-Z-]{4}[0-9]{4}) (?<time>[0-9]{2}:[0-9]{2}) (?<operation>[^0-9]+) (?<amount>[0-9]{1,10}(.[0-9]{2})?)р" +
                " за уведомления. .+ Баланс (?<balance>.+)р" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            m.gv("cardId"),
            m.gv("operation"),
            "SMS уведомления",
            getAmount(m.gv("amount"), m.gv("operation"), config),
            m.gv("balance").fixAmountString()
        )
    }
}

private fun getAmount(value: String, operation: String, config: ConfigData) =
    if (config.sberUseIncomes) {
        if (operation.equalsAny(config.sberSmsIncomes)) "" else "-"
    } else {
        if (operation.equalsAny(config.sberSmsExpenses)) "-" else ""
    } + value.fixAmountString()
