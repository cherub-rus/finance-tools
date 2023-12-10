package org.cherub.fintools.pdftool.gpb

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.CommonProcessor

class GpbCardProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun reversRows() = false

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</b></p>)".toRegex(), "$1\n")
        .replace("</p><p>","</p>\n<p>")
        .replace("</p>\n<p>+","</p><p>+")

    override fun rowFilter(row: String) =
        row.contains("</p><p>+")

    override fun transformToCsv(row: String) = row
}
