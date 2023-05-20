package org.cherub.fintools.pdftool.mtsb

import org.cherub.fintools.pdftool.CommonProcessor

private const val BIN = 220028 // Bank Identification Number for PAYMENT SYSTEM: NSPK MIR; BANK: PJSC MTS BANK

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

    private fun String.cleanUpRow() = this
        .replace(" по Договору [0-9\\-/ ]{12,13} согласно платежной ведомости [ ]?#[A-Z0-9]{12}#".toRegex(), "")
        .replace(" на счет [0-9]{20}, ПАО \\\"МТС-Банк\\\" на имя .+ за ".toRegex(), " за ")
        .replace(". НДС не облагается.", "")
        .replace("\\", "@")
        .replace("( )?@(.)+@(.)+@[0-9]{6} RUSRUS@643@,".toRegex(), "")
        .replace(">MOSCOW RU@643@,", "")
        .replace(" Транзакции по картам МПС, включая комиссии (ЗК)", "")
        .replace("Комиссия за услугу \"Уведомления от банка\", подключенную к Карте МПС", "~Комиссия, Уведомления от банка")
        .replace("Комиссия за выпуск карты", "~Комиссия, Выпуск карты")
        .replace("Для зачисления зарплаты за первую половину месяца", "~Зачисление ЗП, Аванс")
        .replace("Для зачисления заработной платы", "~Зачисление ЗП, Зарплата")
        .replace("Перечисление работникам заработной платы на пластиковые карты", "~Зачисление ЗП, Зарплата")
        .replace("Для зачисления отпуска", "~Зачисление ЗП, Отпускные")
        .replace("Перечисление работникам отпуска на пластиковые карты", "~Зачисление ЗП, Отпускные")
        .replace("Перевод между счетами", "~Зачисление, Перевод между счетами")
        .replace("Перевод с карты на счет", "~Списание, Перевод с карты на счет")
        .replace("Зачисление переводов СБП", "~Зачисление, Перевод СБП")

    override fun cleanUpTransactions(content: String) = content
        .replace("  *".toRegex(), " ")
        .replace("\"", "")
        .replace("SUPERMARKET SPAR", "SPAR")

}