package org.cherub.fintools.pdftool.gpb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

class GpbDepositProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun reversRows() = false

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b>|<p>| )([0-9]{2}[.][0-9]{2}[.][0-9]{4} [А-Я])".toRegex(), "\n$2")
        .replace("(</p> </div></div>)".toRegex(), "\n$1")
        .replace("(</p><p>[*])".toRegex(), "\n$1")

    override fun rowFilter(row: String) =
        !row.contains("</p>")

    override fun transformToCsv(row: String) = row
        .replace(
            "([0-9]{2}[.][0-9]{2}[.][0-9]{4}) (.+) ([+-])(([1-9]?[0-9]{0,2} )?[0-9]{1,3},[0-9]{2}) (([1-9]?[0-9]{0,2} )?[0-9]{1,3},[0-9]{2})".toRegex(),
            "$1\t$2\t$3\$4\t\t\t\t$2\t\t\t\t$6\t$formula_c12\t$2"
        )
}
