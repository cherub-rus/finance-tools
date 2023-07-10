package org.cherub.fintools.txttool.push.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.config.ReplaceRule
import org.cherub.fintools.log.log
import org.cherub.fintools.txttool.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val PUSH_REGEX_STRING = "\\[([0-9.]{10}) в ([0-9:]{5})\\]\t(.+)\t([0-9,+ ]+) ₽\t(.+) •• ([0-9]{4})\tБаланс: ([0-9, ]+) ₽"

class SberPushProcessor {

    fun process(fileText: String, sourceName: String, config: ConfigData): ProcessResult {
        val accountList = mutableMapOf<String, MutableList<Push>>()
        val notSmsList = mutableListOf<String>()

        val pushPerLineText = fileText.cleanUp(config.replaceBeforeParse).replace("\n", "\t").replace("\t\t", "\n")

        for (line in pushPerLineText.lines().reversed()) {
            parsePush(line, config.sberOperationType)?.also { accountList.addPush(it) } ?: notSmsList.add(line)
        }

        val builder = StringBuilder()
        accountList.forEach { account ->
            builder.append(makeAccountHeader(account.key, config.accounts, sourceName))
            account.value.forEach {
                builder.appendLine(convertToCsv(it))
            }
        }
        return ProcessResult(builder.toString(), notSmsList)
    }

    private fun String.cleanUp(rules: List<ReplaceRule>): String {
        var str = this
        rules.forEach { str = str.replace(it.s, it.r) }
        return str
    }

    private fun parsePush(source: String, operationTypes: List<String>): Push? {
        val regex = PUSH_REGEX_STRING.toRegex()
        val m = regex.matchEntire(source) ?: return null

        val push = try {
            val operation = findOperation(m.gv(3), operationTypes)
            Push(
                LocalDate.parse(m.gv(1), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                LocalTime.parse(m.gv(2)),
                m.gv(5) + m.gv(6),
                operation,
                m.gv(3).substringAfter("$operation "),
                m.gv(4).replace(" ", "").convertSign().appendDecimals(),
                m.gv(7).replace(" ", "").appendDecimals()
            )
        } catch (e: Exception) {
            log(e, source)
            null
        }
        return push
    }

    private fun MutableMap<String, MutableList<Push>>.addPush(push: Push) {
        val key = push.account
        if (!this.contains(key)) {
            this[key] = mutableListOf()
        }
        this[key]!!.add(push)
    }

    private fun convertToCsv(push: Push): String =
        "${push.date}\t${push.message}\t${push.amount}\t\t\t\t${push.operation}\t${push.time}\t\t\t${push.balance}\t$formula_c12"

    private fun findOperation(message: String, operationTypes: List<String>) =
        operationTypes.sortedByDescending { it.length }
            .firstOrNull { message.lowercase().startsWith(it) }
            .let { message.substring(0, it?.length ?: message.indexOf(' ')) }

    private fun String.convertSign() = if (this.startsWith("+")) this.substringAfter("+") else "-$this"

    private fun String.appendDecimals() = if (this.contains(",")) this else "$this,00"

}