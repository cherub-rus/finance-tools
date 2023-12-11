package org.cherub.fintools.pdftool.gpb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import org.cherub.fintools.pdftool.*

class GpbCardProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun reversRows() = false

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)".toRegex(), "$1\n")
        .replace("</p><p>","</p>\n<p>")
        .replace("</p>\n<p>+","</p><p>+")
        .replace("</p> </div></div>", "</p>\n</div></div>")

    override fun rowFilter(row: String) =
        row.contains("</p><p>+") && !row.contains("Погашение ТО, возникшего по счету банковской карты")

    override fun cleanUpRow(row: String) = row
        .replace(" -0,00", "")
        .replace("+0,00 ", "")
        .replace(" +".toRegex(), " ")


    override fun transformToCsv(row: String) = row
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} Операция: (.+ [(].+[)]) [(]ФИЛИАЛ ГПБ[)][.] Устройство: (.+)[.] Город: .+[.] Сумма операции: .+[.] Валюта операции: Российские рубли[.]</p><p>(.+)</p>".toRegex(),
            "$1\t$3\t$4\t\t\t\t$2\t00:00\t\t\t$formula_c11\t$formula_c12\t$3"
        )
        .replace(
            "<p>([0-9.]{10}) [0-9.]{10} Операция: (.+)[.] Карта .+[.] Устройство: (.*)[.] Город: .*[.] Сумма операции: .+[.] Валюта операции: Российские рубли[.]</p><p>(.+)</p>".toRegex(),
            "$1\t$3\t$4\t\t\t\t$2\t00:00\t\t\t$formula_c11\t$formula_c12\t$3"
        )
}
