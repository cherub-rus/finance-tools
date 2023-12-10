package org.cherub.fintools.pdftool.gpb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.CommonProcessor

class GpbDepositProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun reversRows() = false

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b>|<p>| )([0-9]{2}[.][0-9]{2}[.][0-9]{4} [А-Я])".toRegex(), "\n$2")
        .replace("(</p> </div></div>)".toRegex(), "\n$1")
        .replace("(</p><p>[*])".toRegex(), "\n$1")

    override fun rowFilter(row: String) =
        !row.contains("</p>")

    override fun transformToCsv(row: String) = row
}
