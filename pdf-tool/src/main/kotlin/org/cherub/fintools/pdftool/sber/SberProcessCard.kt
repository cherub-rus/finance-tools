package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.pdftool.formula_c11
import org.cherub.fintools.pdftool.formula_c12

class SberProcessCard : SberProcessor() {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)".toRegex(), "$1\n")

    override fun rowFilter(row: String) =
        row.contains(".202") && row.contains("</p><p><b>") && !row.contains("<!DOCTYPE")

    override fun transformToCsv(row: String) = row
        .replace(
            "<p><b>([0-9.]{10}) ([0-9:]{5})</b>.{12,17}</p><p><b>(.+)</b>(.+)</p><p><b>([+-]?[0-9, ]+)</b></p>".toRegex(),
            "$1\t$4\t-$5\t\t\t\t$3\t$2:00\t\t\t$formula_c11\t$formula_c12\t$4"
        )
        .replace("-+", "")
}
