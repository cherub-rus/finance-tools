package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.formula_c12

class SberProcessDeposit(config: ConfigData) : SberProcessor(config) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)(<p><b>)".toRegex(), "$1\n$2")
        .replace("(<p>ШИФР СУММА ОПЕРАЦИИ ОСТАТОК НА СЧЁТЕ3</p>)".toRegex(), "$1\n")
        .replace("(<p>Продолжение на следующей странице</p>)".toRegex(), "\n$1")
        .replace("(<p>Дата формирования:</p>)".toRegex(), "\n$1")

    override fun rowFilter(row: String) =
        row.contains("к/с ")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) (.+?)</b></p><p>(.+?)<b>[0-9] ([+-]?[0-9 ]+,[0-9]{2}) ([0-9 ]+,[0-9]{2})</b></p>".toRegex(),
            "$1\t$3\t-$4\t\t\t\t$2\t00:00\t\t\t$5\t$formula_c12\t$3"
        )
        .replace("-+", "")
        .replace("--", "-")
}