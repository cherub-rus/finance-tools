package org.cherub.fintools.pdftool.usb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import org.cherub.fintools.pdftool.*

private val USB_HEAD_REGEX = "<p>(?<currentDate>\\d{1,2} [а-я]* \\d{4}) г\\.</p><p><b>[А-Я ]+</b>Cчет: (?<account>40817810\\d{12})</p><p><b>Поступления:</b></p><p><b>([0-9 ]{1,9},\\d{2}) ₽ Расходы:</b></p><p><b>([0-9 ]{1,9},\\d{2}) ₽</b></p><p><b>Выписка за период</b>(?<startDate>\\d{1,2} [а-я]* \\d{4}) г\\. - (?<endDate>\\d{1,2} [а-я]* \\d{4}) г\\.</p><p><b>Доступно средств:</b></p><p><b>(?<currentBalance>(\\d{1,3} )?\\d{1,3},\\d{2}) ₽</b></p><p><b>Детальная информация</b></p><p><b>Сведения об операции Категории Дата и время MSK Сумма Валюта</b></p>".toRegex()

class UsbCardProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("</b>RUB</p>", "</b>RUB</p>\n")
        .replace("Сумма Валюта</b></p><p>", "Сумма Валюта</b></p>\n<p>")
        .replace("page-break-after:always\"><div><p>", "page-break-after:always\"><div>\n<p>")

    override fun rowFilter(row: String) =
        row.contains("</b>RUB</p>")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p>(.+)( |</p><p>)(Перевод на счет) ([0-9]{2}[.][0-9]{2}[.][0-9]{4}) ([0-9]{2}:[0-9]{2}) <b>([0-9]+[.][0-9]{2}) </b>RUB</p>".toRegex(),
            prepareCsvOutputMask("$4", "$5", "$6", "$1", formula_c11, formula_c12, "", "", "$3")
        )
        .replace(
            "<p>(.+) (По номеру телефона через СБП)</p><p>([0-9]{2}[.][0-9]{2}[.][0-9]{4}) ([0-9]{2}:[0-9]{2}) <b>(-?[0-9]+[.][0-9]{2}) </b>RUB</p>".toRegex(),
            prepareCsvOutputMask("$3", "$4", "$5", "$1", formula_c11, formula_c12, "", "", "$2")
        )
        .replace(
            "<p>(.+) (Комиссия) ([0-9]{2}[.][0-9]{2}[.][0-9]{4}) ([0-9]{2}:[0-9]{2}) <b>(-?[0-9]+[.][0-9]{2}) </b>RUB</p>".toRegex(),
            prepareCsvOutputMask("$3", "$4", "$5", "$1", formula_c11, formula_c12, "", "", "$2")
        )
        .replace(
            "<p>(.+) (Между своими счетами) ([0-9]{2}[.][0-9]{2}[.][0-9]{4}) ([0-9]{2}:[0-9]{2}) <b>(-?[0-9]+[.][0-9]{2}) </b>RUB</p>".toRegex(),
            prepareCsvOutputMask("$3", "$4", "$5", "$1", formula_c11, formula_c12, "", "", "$2")
        )

    override fun fixCsv(csvRow: String): String {
        val fields = csvRow.split("\t").toMutableList()

        try {
            fields[3] = fields[3].replace('.', ',') // Changed currency separator
        } catch (e: Exception) {
            log(e, csvRow)
        }
        return super.fixCsv(fields.joinToString("\t"))
    }
    override fun discoverAccountInfo(text: String): AccountInfo {

        val mHead = USB_HEAD_REGEX.matchEntire(text.lines()[1])

        val number = mHead?.gv("account")
        val code =  number?.let {
            val s = it.takeLast(4)
            config.accounts.findByAccountNumber("*$s")?.code
        }

        return AccountInfo(
            accountCode = code,
            accountNumber = number,
            startDate = mHead?.gv("startDate"),
            endDate = mHead?.gv("endDate"),
            currentDate = mHead?.gv("currentDate"),
            currentBalance = mHead?.gv("currentBalance")
        )
    }
}
