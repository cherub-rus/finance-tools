package org.cherub.fintools.pdftool.mtsb

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.cherub.fintools.pdftool.CommonProcessor

private const val BIN = 220028 // Bank Identification Number for PAYMENT SYSTEM: NSPK MIR; BANK: PJSC MTS BANK

private const val PFX_ЗАЧИСЛЕНИЕ = "~Зачисление"
private const val PFX_КОМИССИЯ = "~Комиссия"
private const val PFX_СПИСАНИЕ = "~Списание"


class MtsbProcessCard : CommonProcessor() {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</p><p>)($BIN\\*\\*)".toRegex(), " ###$2")
        .replace("( )($BIN\\*\\*)".toRegex(), " ###$2")
        .replace("(</p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains("$BIN**") && !row.contains("Номер карты:")

    override fun transformToCsv(row: String) = row.cleanUpRow()
        .replace(
            "<p>[0-9]{1,3} ([0-9.]{10}) ([0-9:]{8}) ([0-9]+\\.[0-9]{1,2}) RUR (((.+?)(, ))?(.+?))( дата транзакции ([0-9/]{10}) ([0-9:]{8}))? ###$BIN.+</p>".toRegex(),
            "$1\t$8\t\t\t\t\t$6\t$2\t$11\t$3\t=ОКРУГЛ(R[-1]C+RC[-8];2)\t=ОКРУГЛ(R[-1]C[-1]+RC[-9];2)\t$8"
        )
        .fixCsv()

    private fun String.fixCsv(): String {
        val fields = this.split("\t").toMutableList()

        fields[9] = fields[9].replace('.', ',') // Changed currency separator
        val sign = if (fields[6].replace("~", "").startsWith("Зачисление")) "" else "-"
        fields[2] = sign + fields[9] // Added minus sign to expense amount

        if (fields[8].isNotEmpty()) { // If transaction time exists, replace log time with it
            fields[7] = fields[8]
            fields[8] = ""
        }

        return fields.joinToString("\t")
    }

    private fun String.cleanUpRow() = this
        .replace(" по Договору [0-9\\-/ ]{12,13} согласно платежной ведомости [ ]?#[A-Z0-9]{12}#".toRegex(), "")
        .replace(" на счет [0-9]{20}, ПАО \\\"МТС-Банк\\\" на имя .+ за ".toRegex(), " за ")
        .replace(". НДС не облагается.", "")
        .replace("\\", "@")
        .replace("( )?@(.)+@(.)+@[0-9]{6} RUSRUS@643@,".toRegex(), "")
        .replace(">MOSCOW RU@643@,", "")
        .replace(" Транзакции по картам МПС, включая комиссии (ЗК)", "")
        .replace("Комиссия за услугу \"Уведомления от банка\", подключенную к Карте МПС", "$PFX_КОМИССИЯ, Уведомления от банка")
        .replace("Комиссия за выпуск карты", "$PFX_КОМИССИЯ, Выпуск карты")
        .replace("Для зачисления зарплаты за первую половину месяца", "$PFX_ЗАЧИСЛЕНИЕ ЗП, Аванс")
        .replace("Для зачисления заработной платы", "$PFX_ЗАЧИСЛЕНИЕ ЗП, Зарплата")
        .replace("Перечисление работникам заработной платы на пластиковые карты", "$PFX_ЗАЧИСЛЕНИЕ ЗП, Зарплата")
        .replace("Для зачисления отпуска", "$PFX_ЗАЧИСЛЕНИЕ ЗП, Отпускные")
        .replace("Перечисление работникам отпуска на пластиковые карты", "$PFX_ЗАЧИСЛЕНИЕ ЗП, Отпускные")
        .replace("Перевод между счетами", "$PFX_ЗАЧИСЛЕНИЕ, Перевод между счетами")
        .replace("Перевод с карты на счет", "$PFX_СПИСАНИЕ, Перевод с карты на счет")
        .replace("Зачисление переводов СБП", "$PFX_ЗАЧИСЛЕНИЕ, Перевод СБП")

    override fun cleanUpTransactions(content: String) =
        orderCsvByDateAndTime(content)
        .replace("  *".toRegex(), " ")
        .replace("\"", "")
        .replace("SUPERMARKET SPAR", "SPAR")

    private fun orderCsvByDateAndTime(text: String) = text
        .split("\n")
        .filter { it.isNotEmpty() }
        .sortedBy {
            val fields = it.split("\t")
            LocalDateTime.parse("${fields[0]} ${fields[7]}", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        }
        .joinToString(separator = "\n", postfix = "\n")
}