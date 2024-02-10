package org.cherub.fintools.txttool.push.sber

import org.cherub.fintools.config.BankAccount
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import org.cherub.fintools.txttool.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Suppress("RegExpRedundantEscape")
private const val PUSH_REGEX_STRING = "\\[([0-9.]{10}) в ([0-9:]{5})\\]\t(.+)\t([0-9,+ ]+) ₽\t(.+) •• ([0-9]{4})\tБаланс: ([0-9, ]+) ₽"

class SberPushProcessor (private val config: ConfigData) {

    @Suppress("DuplicatedCode")
    fun process(fileText: String, sourceName: String): ProcessResult {
        val accountList = mutableMapOf<String, MutableList<Push>>()
        val notSmsList = mutableListOf<String>()

        val pushPerLineText = fileText.replace("\n", "\t").replace("\t\t", "\n")

        for (line in pushPerLineText.lines().reversed()) {
            parsePush(line, config.getSberOperationTypeNames())?.also { accountList.addPush(it, config.accounts) } ?: notSmsList.add(line)
        }

        val resultMap = mutableMapOf<String, String>()
        accountList.forEach { account ->
            val builder = StringBuilder()
            builder.append(makeAccountHeader(account.key, config.accounts, sourceName))
            account.value.forEach {
                builder.appendLine(convertToCsv(it))
            }
            resultMap[config.accounts.findCode(account.key)] = builder.toString()
        }
        return ProcessResult(resultMap, notSmsList)
    }

    private fun parsePush(source: String, operationTypes: List<String>): Push? {
        val regex = PUSH_REGEX_STRING.toRegex()
        val m = regex.matchEntire(source) ?: return null

        val push = try {
            val operation = findOperation(m.gv(3), operationTypes)
            Push(
                date = LocalDate.parse(m.gv(1), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                time = LocalTime.parse(m.gv(2)),
                account = m.gv(5) + m.gv(6),
                operation = operation,
                message = m.gv(3).substringAfter("$operation "),
                amount = m.gv(4).replace(" ", "").convertSign().appendDecimals(),
                balance = m.gv(7).replace(" ", "").appendDecimals()
            )
        } catch (e: Exception) {
            log(e, source)
            null
        }
        return push
    }

    private fun MutableMap<String, MutableList<Push>>.addPush(push: Push, accounts: List<BankAccount>) {
        val key = accounts.findByAccountNumber(push.account)?.cardId ?: push.account
        if (!this.contains(key)) {
            this[key] = mutableListOf()
        }
        this[key]!!.add(push)
    }

    private fun convertToCsv(push: Push): String =
        mutableListOf<String>().apply {
            add(push.date.toString())
            add(push.time.toString())
            add("")
            add(push.amount)
            add("")
            add("")
            add(push.message)
            add("")
            add(push.balance)
            add(FORMULA_BALANCE2)
            add("")
            add("")
            add(push.operation)
            add(push.message)
        }.joinToString("\t")

    private fun findOperation(message: String, operationTypes: List<String>) =
        operationTypes.sortedByDescending { it.length }
            .firstOrNull { message.lowercase().startsWith(it) }
            .let { message.substring(0, it?.length ?: message.indexOf(' ')) }

    private fun String.convertSign() = if (this.startsWith("+")) this else "-$this"

}