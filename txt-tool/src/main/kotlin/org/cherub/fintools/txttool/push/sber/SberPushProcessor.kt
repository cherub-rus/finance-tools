package org.cherub.fintools.txttool.push.sber

import org.cherub.fintools.txttool.formula_c11
import org.cherub.fintools.txttool.formula_c12
import org.cherub.fintools.txttool.makeAccountHeader
import org.cherub.fintools.txttool.sms.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val PUSH_REGEX_STRING = "\\[([0-9.]{10}) в ([0-9:]{5})\\]\t(.+)\t([0-9,+ ]+) ₽\t(.+) •• ([0-9]{4})\tБаланс: ([0-9, ]+) ₽"

class SberPushProcessor {

    fun process(fileText: String, sourceName: String): ProcessResult {
        val accountList = mutableMapOf<String, MutableList<Push>>()
        val notSmsList = mutableListOf<String>()

        val pushPerLineText = fileText.replace("\n", "\t").replace("\t\t", "\n")

        for (line in pushPerLineText.lines().reversed()) {
            parsePush(line)?.also { accountList.addPush(it) } ?: notSmsList.add(line)
        }

        val builder = StringBuilder()
        accountList.forEach { account ->
            builder.append(makeAccountHeader(account.key, sourceName))
            account.value.forEach {
                builder.appendLine(convertToCsv(it))
            }
            builder.appendLine()
        }
        return ProcessResult(builder.toString(), notSmsList)
    }

    private fun parsePush(source: String): Push? {
        val regex = PUSH_REGEX_STRING.toRegex()
        val m = regex.matchEntire(source) ?: return null

        val push = try {
            val operation = findOperation(m.gv(3))
            Push(
                LocalDate.parse(m.gv(1), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                LocalTime.parse(m.gv(2)),
                m.gv(5) + m.gv(6),
                operation,
                m.gv(3).substringAfter("$operation "),
                m.gv(4).replace(" ", "").convertSign().appendDecimals(),
                m.gv(7).replace(" ", "")
            )
        } catch (e: Exception) {
            System.err.println(source)
            e.printStackTrace() //todo log
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
        "${push.date}\t${push.message}\t${push.amount}\t\t\t\t${push.operation}\t${push.time}\t\t\t${push.balance ?: formula_c11}\t$formula_c12"

    private fun findOperation(message: String) = message.substringBefore(" ")

    private fun String.convertSign() = if (this.startsWith("+")) this.substringAfter("+") else "-$this"

    private fun String.appendDecimals() = if (this.contains(",")) this else "$this,00"

}