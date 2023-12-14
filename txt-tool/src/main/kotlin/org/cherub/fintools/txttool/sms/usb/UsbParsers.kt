package org.cherub.fintools.txttool.sms.usb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.config.ConfigData.Companion.US_BANK_ID
import org.cherub.fintools.txttool.*
import org.cherub.fintools.txttool.sms.IContentParser

val usbParsers = Pair(
    US_BANK_ID,
    listOf(
        UsbParserPostuplenieNaSchet(),
        UsbParserPostuplenieKarta(),
        UsbParserPerevodSBP(),
        UsbParserPerevodSBP2(),
        UsbParserSpisanieSoScheta()
    )
)

class UsbParserPostuplenieNaSchet : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        val regex = ("^" +
                "BANK URALSIB[.] (?<operation>POSTUPLENIE) SREDSTV (?<message>NA SCHET): (?<amount>[0-9]{1,10}(.[0-9]{1,2})?) RUR (?<date>[0-9/]{10}) (?<time>[0-9:]{8})" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            "*****",
            m.gv("operation"),
            m.gv("message"),
            m.gv("amount").fixAmountString(),
            null,
            m.gv("date"),
            m.gv("time")
        )
    }
}

class UsbParserPostuplenieKarta : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        val regex = ("^" +
                "(?<operation>POSTUPLENIE) SREDSTV (?<message>KARTA) (?<cardId>[*][0-9]{4}): (?<amount>[0-9]{1,10}(.[0-9]{1,2})?) RUR[.] Karta [*][0-9]{4} (?<date>[0-9.]{8}) (?<time>[0-9:]{5})[.] Ostatok (?<balance>[0-9]{1,10}(.[0-9]{1,2})?) RUR[.]" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            "*****",
            m.gv("operation"),
            m.gv("message"),
            m.gv("amount").fixAmountString(),
            m.gv("balance").fixAmountString(),
            m.gv("date"),
            m.gv("time")
        )
    }
}

class UsbParserPerevodSBP : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        val regex = ("^" +
                "(?<operation>Perevod SBP) (?<message>.+[.]) Summa (?<amount>[0-9]{1,10}(.[0-9]{2})?) RUR so scheta (?<account>[*][0-9]{4})[.] Ispolnen (?<date>[0-9.]{10}) (?<time>[0-9:]{5})" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            "*****" /*m.gv("account")*/,
            m.gv("operation"),
            m.gv("message"),
            m.gv("amount").fixAmountString(),
            null,
            m.gv("date"),
            m.gv("time")
        )
    }
}

class UsbParserPerevodSBP2 : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        val regex = ("^" +
                "(?<operation>Перевод по СБП), счет (?<account>[*][0-9]{5}) (?<amount>[0-9]{1,10}(.[0-9]{2})?) RUB[.] НИКОМУ не говорите код [0-9]{5}" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            "*****" /*m.gv("account")*/,
            m.gv("operation"),
            "",
            m.gv("amount").fixAmountString(),
            null
        )
    }
}

class UsbParserSpisanieSoScheta : IContentParser {
    override fun parse(content: String, config: ConfigData): Transaction? {

        val regex = ("^" +
                "(?<operation>SPISANIE) SREDSTV (?<message>SO SCHETA): (?<amount>[0-9]{1,10}(.[0-9]{1,2})?) RUR[.] (?<date>[0-9.]{8}) (?<time>[0-9:]{5})[.] Ostatok (?<balance>[0-9]{1,10}(.[0-9]{1,2})?) RUR[.]" +
                "$").toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(
            "*****",
            m.gv("operation"),
            m.gv("message"),
            m.gv("amount").fixAmountString(),
            m.gv("balance").fixAmountString(),
            m.gv("date"),
            m.gv("time")
        )
    }
}
