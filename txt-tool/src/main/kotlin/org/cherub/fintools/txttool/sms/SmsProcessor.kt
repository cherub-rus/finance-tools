package org.cherub.fintools.txttool.sms

import org.cherub.fintools.txttool.formula_c11
import org.cherub.fintools.txttool.formula_c12
import org.cherub.fintools.txttool.makeAccountHeader
import org.cherub.fintools.txttool.sms.mtsb.*
import java.time.LocalDate
import java.time.LocalTime

private const val SMS_REGEX_STRING = "([0-9-]{10})\t([0-9:]{8})\tin\t(.+)\t(.+)\t(.+)"

class SmsProcessor {

    companion object {
        val checkFormatRegex = SMS_REGEX_STRING.toRegex()
    }

    private val parsers = mapOf(mtsbParsers)

    fun process(fileText: String, sourceName: String): ProcessResult {
        val accountList = mutableMapOf<String, MutableList<Sms>>()
        val notSmsList = mutableListOf<String>()

        for (line in fileText.lines()) {
            parseSms(line)?.also { accountList.addSms(it) } ?: notSmsList.add(line)
        }

        val builder = StringBuilder()
        accountList.forEach { account ->
            builder.append(makeAccountHeader(account.key, "##TODO##", sourceName))
            account.value.forEach {
                builder.appendLine(convertToCsv(it))
            }
        }
        return ProcessResult(builder.toString(), notSmsList)
    }

    private fun parseSms(source: String): Sms? {
        val regex = SMS_REGEX_STRING.toRegex()
        val m = regex.matchEntire(source) ?: return null

        val trans = parseContent(m.gv(3), m.gv(5)) ?: return null
        return Sms(LocalDate.parse(m.gv(1)), LocalTime.parse(m.gv(2)), m.gv(3), m.gv(4), m.gv(5), trans)
    }

    private fun MutableMap<String, MutableList<Sms>>.addSms(sms: Sms) {
        val key = "${sms.bank}#${sms.trans.account}"
        if (!this.contains(key)) {
            this[key] = mutableListOf()
        }
        this[key]!!.add(sms)
    }

    private fun parseContent(bank: String, content: String): Transaction? {
        parsers[bank]?.forEach { p ->
            p.parse(content)?.also { return it }
        }
        return null
    }

    private fun convertToCsv(sms: Sms): String =
        "${sms.date}\t${sms.trans.message}\t${sms.trans.amount}\t\t\t\t${sms.trans.operation}\t${sms.time}\t\t\t${sms.trans.balance ?: formula_c11}\t$formula_c12"

}
