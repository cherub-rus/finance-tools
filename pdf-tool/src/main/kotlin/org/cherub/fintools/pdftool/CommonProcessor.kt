package org.cherub.fintools.pdftool

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.BankAccount
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val formula_c11 = "=ОКРУГЛ(R[-1]C+RC[-8];2)"
const val formula_c12 = "=ОКРУГЛ(R[-1]C[-1]+RC[-9];2)"

private const val DATE_ISO_PATTERN = "yyyy-MM-dd"
private const val DATE_RUSSIAN_PATTERN = "dd.MM.yyyy"
internal const val TIMESTAMP_ISO_PATTERN = "$DATE_ISO_PATTERN HH:mm:ss"

abstract class CommonProcessor(val config: ConfigData, val reorderCsvRows: Boolean =  false) {

    open fun reversRows() = true

    fun process(fileText: String, sourceName: String): ProcessResult {
        val html = cleanUpHtml(removeNewLines(fileText))
        if (WRITE_HTML) File("$sourceName.2.html").writeText(html)

        val info = discoverAccountInfo(html)
        val hfPair = prepareHeaderAndFooter(info)

        val result = StringBuilder()
        hfPair.first?.let {result.appendLine(hfPair.first)}
        val builder = StringBuilder()
        splitToTransactionRows(html).forEach {
            builder.appendLine(fixCsv(transformToCsv(cleanUpRow(it))))
        }
        result.append(cleanUpResult(builder.toString()))
        hfPair.second?.let {result.appendLine(hfPair.second)}

        return ProcessResult(result.toString(), info.accountCode)
    }

    protected abstract fun rowFilter(row: String) : Boolean
    protected abstract fun transformToCsv(row: String): String
    protected abstract fun cleanUpHtmlSpecific(text: String): String

    open fun cleanUpHtml(text: String) =
        cleanUpHtmlSpecific(text)

    open fun cleanUpRow(row: String) = row

    private fun splitToTransactionRows(text: String): List<String> = text
        .lines().filter { rowFilter(it) }
        .let { if (reversRows()) it.reversed() else it }

    private fun cleanUpResult(content: String) =
        if (reorderCsvRows) sortCsvRows(content) else content
        .replace(" +".toRegex(), " ")
        .replace("\"", "")
        .cleanUpByRules(config.replaceInResult)

    private fun sortCsvRows(text: String) = text
        .split("\n")
        .filter { it.isNotEmpty() }
        .sortedBy {
            try {
                val fields = it.split("\t")
                LocalDateTime.parse("${fields[0]} ${fields[7]}", DateTimeFormatter.ofPattern(TIMESTAMP_ISO_PATTERN))
            } catch (e: Exception) {
                log(e, it)
                LocalDateTime.MIN
            }
        }
        .joinToString(separator = "\n", postfix = "\n")

    open fun fixCsv(csvRow: String): String {
        val fields = csvRow.split("\t").toMutableList()

        try {
            fields[0] = fields[0].fixRussianDate()
        } catch (e: Exception) {
            log(e, csvRow)
        }

        return fields.joinToString("\t")
    }

    open fun String.fixRussianDate(): String =
        if (this.isNotEmpty() && this != "#")
            SimpleDateFormat(DATE_ISO_PATTERN).format(SimpleDateFormat(DATE_RUSSIAN_PATTERN).parse(this))
        else
            this

    open fun discoverAccountInfo(text: String): AccountInfo =
        AccountInfo(null)

    private fun prepareHeaderAndFooter(info: AccountInfo): Pair<String?, String?> {

        val header = ifAnyNotNull(info.currentDate, info.startBalance) { currentDate, startBalance ->
            StringBuilder().apply {
                append("#\t")
                append("Report date: " + (currentDate ?: ""))
                info.startDate?.let {
                    append(" [" + it + ":" + (info.endDate ?: "") + "] ")
                }
                append("\t".repeat(9))
                append(startBalance ?: "")
                append("\t".repeat(2))
            }.toString()
        }

        val footer =  (info.endBalance ?: info.currentBalance)?.let {
            "#" + "\t".repeat(10) + it + "\t".repeat(2)
        }
        return Pair(header, footer)
    }

    private fun removeNewLines(text: String) = text
        .replace("\n([^<])".toRegex(), " $1")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("\n<", "<")
        .replace("\n ", " ")
        .replace("\n", " ")

}

data class AccountInfo (
    val accountCode: String?,
    val accountNumber: String? = null, val cardNumber: String? = null,
    val startDate: String? = null, val endDate: String? = null, val currentDate: String? = null,
    val startBalance: String? = null, val endBalance: String? = null, val currentBalance: String? = null
)

data class ProcessResult(
    val csvText: String, val accountCode: String? = null
)

fun MatchResult.gv(name: String) =
    this.groups[name]?.value

fun List<BankAccount>.findByAccountNumber(number: String): BankAccount? =
    this.firstOrNull { it.account == number }

fun List<BankAccount>.findByCard(number: String): BankAccount? =
    this.firstOrNull { it.cardId == number }

inline fun <T1: Any, T2: Any, R: Any> ifAnyNotNull(p1: T1?, p2: T2?, block: (T1?, T2?)->R?): R? {
    return if (p1 != null || p2 != null) block(p1, p2) else null
}
