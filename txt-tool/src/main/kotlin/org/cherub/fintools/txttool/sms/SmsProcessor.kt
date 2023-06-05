package org.cherub.fintools.txttool.sms

import org.cherub.fintools.txttool.sms.mtsb.MtsbContentParser
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

const val formula_c11 = "=ОКРУГЛ(R[-1]C+RC[-8];2)"
const val formula_c12 = "=ОКРУГЛ(R[-1]C[-1]+RC[-9];2)"

private const val SMS_REGEX_STRING = "([0-9-]{10})\t([0-9:]{8})\tin\t(.+)\t(.+)\t(.+)"

class SmsProcessor {

    companion object {
        val checkFormatRegex = SMS_REGEX_STRING.toRegex()
    }

    fun process(fileText: String, sourceName: String): String {
        val accountList = mutableMapOf<String, MutableList<Sms>>()
        val notSmsList = mutableListOf<String>()

        for (line in fileText.lines()) {
            parseSms(line)?.also { accountList.addSms(it) } ?: notSmsList.add(line)
        }

        // TODO return list to main
        File("$sourceName.unparsed").writeText(notSmsList.joinToString("\n"))

        val builder = StringBuilder()
        accountList.forEach { account ->
            builder.append(makeAccountHeader(account.key, sourceName))
            account.value.forEach {
                builder.appendLine(convertToCsv(it))
            }
            builder.appendLine()
        }
        return builder.toString()
    }

    private fun parseSms(source: String): Sms? {
        val regex = SMS_REGEX_STRING.toRegex()
        val m = regex.matchEntire(source) ?: return null

        val trans = parseContent(m.gv(3), m.gv(5)) ?: return null
        return Sms(LocalDate.parse(m.gv(1)), LocalTime.parse(m.gv(2)), m.gv(3), m.gv(4), m.gv(5), trans)
    }

    private fun MutableMap<String, MutableList<Sms>>.addSms(sms: Sms) {
        val accKey = "${sms.bank}#${sms.trans.account}"
        if (!this.contains(accKey)) {
            this[accKey] = mutableListOf<Sms>()
        }
        this[accKey]!!.add(sms)
    }

    private fun parseContent(bank: String, content: String): Transaction? {
        // TODO parser registrator. CommonParser
        if (bank == "MTS-Bank") {
            return MtsbContentParser().parse(content)
        }
        return null
    }

    private fun convertToCsv(sms: Sms): String =
        "${sms.date}\t${sms.trans.message}\t${sms.trans.amount}\t\t\t\t${sms.trans.operation}\t${sms.time}\t\t\t${sms.trans.balance ?: formula_c11}\t$formula_c12"

    private fun makeAccountHeader(account: String, sourceName: String) =
        StringBuilder().also {
            it.appendLine("Account\t$account\tBank\t##TODO##\t\t\t\t\t\t\t\t") // TODO account config, remove tabs
            it.appendLine("")
            it.appendLine("#\t${File(sourceName).name}")
        }.toString()


}
