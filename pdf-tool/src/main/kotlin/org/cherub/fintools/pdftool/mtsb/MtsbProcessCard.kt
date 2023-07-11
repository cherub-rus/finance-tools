package org.cherub.fintools.pdftool.mtsb

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.cherub.fintools.pdftool.CommonProcessor
import org.cherub.fintools.pdftool.formula_c11
import org.cherub.fintools.pdftool.formula_c12

private const val BIN = 220028 // Bank Identification Number for PAYMENT SYSTEM: NSPK MIR; BANK: PJSC MTS BANK

class MtsbProcessCard(config: ConfigData) : CommonProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</p><p>)($BIN\\*\\*)".toRegex(), " ###$2")
        .replace("( )($BIN\\*\\*)".toRegex(), " ###$2")
        .replace("([0-9]{1,3} [0-9.]{10} [0-9:]{8})</p><p>".toRegex(), "$1 ")
        .replace("(</p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains("$BIN**") && !row.contains("Номер карты:")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p>[0-9]{1,3} ([0-9.]{10}) ([0-9:]{8}) ([0-9]+\\.[0-9]{1,2}) RUR (((.+?)(, ))?(.+?))( дата транзакции ([0-9/]{10}) ([0-9:]{8}))? ###$BIN.+</p>".toRegex(),
            "$1\t$8\t\t\t\t\t$6\t$2\t$11\t$3\t$formula_c11\t$formula_c12\t$8"
        )
        .fixCsv()

    private fun String.fixCsv(): String {
        val fields = this.split("\t").toMutableList()

        try {
            fields[9] = fields[9].replace('.', ',') // Changed currency separator
            val sign = if (fields[6].replace("~", "").startsWith("Зачисление")) "" else "-"
            fields[2] = sign + fields[9] // Added minus sign to expense amount

            if (fields[8].isNotEmpty()) { // If transaction time exists, replace log time with it
                fields[7] = fields[8]
                fields[8] = ""
            }
        } catch (e: Exception) {
            log(e, this)
        }

        return fields.joinToString("\t")
    }

    override fun cleanUpRow(row: String) = row
        .cleanUpByRules(config.replaceInRow)

    // reorder by date and time
    override fun reorderCsvRows(text: String) = text
        .split("\n")
        .filter { it.isNotEmpty() }
        .sortedBy {
            try {
                val fields = it.split("\t")
                LocalDateTime.parse("${fields[0]} ${fields[7]}", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
            } catch (e: Exception) {
                log(e, it)
                LocalDateTime.MIN
            }
        }
        .joinToString(separator = "\n", postfix = "\n")
}