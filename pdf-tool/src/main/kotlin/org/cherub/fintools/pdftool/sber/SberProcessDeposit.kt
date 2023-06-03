package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.pdftool.formula_c12

class SberProcessDeposit : SberProcessor() {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)(<p><b>)".toRegex(), "$1\n$2")
        .replace("(<p>ШИФР СУММА ОПЕРАЦИИ ОСТАТОК НА СЧЁТЕ3</p>)".toRegex(), "$1\n")
        .replace("(<p>Продолжение на следующей странице</p>)".toRegex(), "\n$1")
        .replace("(<p>Дата формирования:</p>)".toRegex(), "\n$1")

    override fun rowFilter(row: String) =
        row.contains("к/с ")

    override fun transformToCsv(row: String) = row
        .replace(
//            "<p><b>([0-9.]{10}) ([0-9:]{5})</b>.{12,17}</p><p><b>(.+)</b>(.+)</p><p><b>(-?[0-9,]+) (-?[0-9,]+)</b></p>".toRegex(),
//            "$1\t$3\t$5\t\t\t\t$4\t$2:00\t\t\t$6\t=ОКРУГЛ(R[-1]C[-1]+RC[-9];2)\t$3"
            "<p><b>([0-9.]{10}) (.+?)</b></p><p>(.+?)<b>[0-9] ([+-]?[0-9 ]+,[0-9]{2}) ([0-9 ]+,[0-9]{2})</b></p>".toRegex(),
            "$1\t$3\t-$4\t\t\t\t$2\t00:00\t\t\t$5\t$formula_c12\t$3"
        )
        .replace("-+", "")
        .replace("--", "-")
}