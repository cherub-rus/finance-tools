package org.cherub.fintools.pdftool.usb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import org.cherub.fintools.pdftool.*

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
            "$4\t$1\t$6\t\t\t\t$3\t$5\t\t\t$formula_c11\t$formula_c12\t$1"
        )
        .replace(
            "<p>(.+) (По номеру телефона через СБП)</p><p>([0-9]{2}[.][0-9]{2}[.][0-9]{4}) ([0-9]{2}:[0-9]{2}) <b>(-?[0-9]+[.][0-9]{2}) </b>RUB</p>".toRegex(),
            "$3\t$1\t$5\t\t\t\t$2\t$4\t\t\t$formula_c11\t$formula_c12\t$1"
        )
        .replace(
            "<p>(.+) (Комиссия) ([0-9]{2}[.][0-9]{2}[.][0-9]{4}) ([0-9]{2}:[0-9]{2}) <b>(-?[0-9]+[.][0-9]{2}) </b>RUB</p>".toRegex(),
            "$3\t$1\t$5\t\t\t\t$2\t$4\t\t\t$formula_c11\t$formula_c12\t$1"
        )
        .replace(
            "<p>(.+) (Между своими счетами) ([0-9]{2}[.][0-9]{2}[.][0-9]{4}) ([0-9]{2}:[0-9]{2}) <b>(-?[0-9]+[.][0-9]{2}) </b>RUB</p>".toRegex(),
            "$3\t$1\t$5\t\t\t\t$2\t$4\t\t\t$formula_c11\t$formula_c12\t$1"
        ).fixCsv()

    private fun String.fixCsv(): String {
        val fields = this.split("\t").toMutableList()

        try {
            fields[2] = fields[2].replace('.', ',') // Changed currency separator
        } catch (e: Exception) {
            log(e, this)
        }

        return fields.joinToString("\t")
    }
}
