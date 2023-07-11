package org.cherub.fintools.pdftool.mtsb

import org.cherub.fintools.log.log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.cherub.fintools.pdftool.CommonProcessor
import org.cherub.fintools.pdftool.formula_c11
import org.cherub.fintools.pdftool.formula_c12

private const val BIN = 220028 // Bank Identification Number for PAYMENT SYSTEM: NSPK MIR; BANK: PJSC MTS BANK

private const val PFX_INCOME = "~Зачисление"
private const val PFX_COMMISSION = "~Комиссия"
private const val PFX_EXPENSE = "~Списание"

class MtsbProcessCard : CommonProcessor() {

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
        .replace(" по Договору( N)? [0-9\\-/ ]{12,13}( от [0-9.]{10}г\\.)? согласно платежной ведомости  ?#[A-Z0-9]{12}#".toRegex(), "")
        .replace(" на счет [0-9]{20}, ПАО \"МТС-Банк\" на имя .+ за ".toRegex(), " за ")
        .replace(". НДС не облагается.", "")
        .replace("\\", "@")
        .replace("( )?@(.)+@(.)+@[0-9]{6} (RUS|[0-9]{2} |[0-9]{2}F)RUS@643@,".toRegex(), "")
        .replace(">(MOSCOW)? RU@643@,".toRegex(), "")
        .replace(" Транзакции по картам МПС, включая комиссии (ЗК)", "")
        .replace("Комиссия за услугу \"Уведомления от банка\", подключенную к Карте МПС", "$PFX_COMMISSION, Уведомления от банка")
        .replace("Комиссия за выпуск карты", "$PFX_COMMISSION, Выпуск карты")
        .replace("Для зачисления зарплаты за первую половину месяца", "$PFX_INCOME ЗП, Аванс")
        .replace("Перечисление заработной платы за первую половину месяца работникам на пластиковые карты", "$PFX_INCOME ЗП, Аванс")
        .replace("Для зачисления заработной платы", "$PFX_INCOME ЗП, Зарплата")
        .replace("Перечисление работникам заработной платы на пластиковые карты", "$PFX_INCOME ЗП, Зарплата")
        .replace("Для зачисления отпуска", "$PFX_INCOME ЗП, Отпускные")
        .replace("Перечисление работникам отпуска на пластиковые карты", "$PFX_INCOME ЗП, Отпускные")
        .replace("Перевод между счетами", "$PFX_INCOME, Перевод между счетами")
        .replace("Перевод с карты на счет", "$PFX_EXPENSE, Перевод с карты на счет")
        .replace("Зачисление переводов СБП", "$PFX_INCOME, Перевод СБП")

    override fun cleanUpResult(content: String) =
        orderCsvByDateAndTime(content)
        .replace(" +".toRegex(), " ")
        .replace("\"", "")
        .replace("SUPERMARKET SPAR", "SPAR")

    private fun orderCsvByDateAndTime(text: String) = text
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