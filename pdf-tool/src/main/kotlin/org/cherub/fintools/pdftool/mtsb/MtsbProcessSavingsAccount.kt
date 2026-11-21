package org.cherub.fintools.pdftool.mtsb

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

// TODO parse account number and report dates

class MtsbProcessSavingsAccount(config: ConfigData) : CommonProcessor(config, true) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</p>)(<p>)".toRegex(), "$1\n$2")
        .replace("\n(<p>)(4[0-9]{15} )([0-9]{4})".toRegex(), "$1$3")
        .replace("(</p>)\n(<p>[0-9]+[.][0-9]{2} RUR</p>)\n(<p>)".toRegex(), "$1$2$3")
        .replace("(</p>)\n(<p>[0-9]+[.][0-9]{2} RUR [0-9]+[.][0-9]{2} RUR</p>)\n(<p>)".toRegex(), "$1$2$3")
        .replace("(<p>[0-9]{1,3} [0-9]{4,6} [0-9]{2}[.][0-9]{2}[.]20) ([0-9]{2})".toRegex(), "$1$2")

    override fun rowFilter(row: String) =
        row.contains("<p>[0-9]{1,3} [0-9]{4,6}".toRegex())

    override fun transformToCsv(row: String) = row
        .replace(
            "<p>[0-9]{1,3} [0-9]{4,6} ([0-9.]{10})</p><p>([0-9]{4})</p><p>([0-9]+[.][0-9]{2}) RUR( ([0-9]+[.][0-9]{2}) RUR)?</p><p>(([0-9]+[.][0-9]{2}) )?(.+), (.+)</p>".toRegex(),
            prepareCsvOutputMask("$1", "00:00", "$3", "$2 $9", "$7", "", "", "$8")
        )

// TODO fix csv data (amount sign)

    override fun cleanUpRow(row: String) = row
        .cleanUpByRules(config.replaceInRow)

// TODO parse account number and report dates

}