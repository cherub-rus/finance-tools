package org.cherub.fintools.pdftool.mtsb

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.*

private val MBSA_CURRENT_DATE_REGEX = "<p>ИНН \\d{10} ____(?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4})____ {4}№ ______б/н________</p>".toRegex()
private val MBSA_REPORT_DATES_REGEX = "<p>Выписка за период с (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) г. по (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4}) г.</p>".toRegex()
private val MBSA_ACCOUNT_AND_IN_BALANCE_REGEX = "<p>Счет № (?<account>40817810\\d{12}) {3}Дата Входящий остаток (?<curDate>\\d{2}\\.\\d{2}\\.\\d{4}) (?<startBalance>\\d+\\.\\d{2})</p>".toRegex()
private val MBSA_OUT_BALANCE_REGEX = "<p>Дата Исходящий остаток (?<curDate>\\d{2}\\.\\d{2}\\.\\d{4}) (?<endBalance>\\d+\\.\\d{2})</p>".toRegex()

class MtsbProcessSavingsAccount(config: ConfigData) : CommonProcessor(config, true) {

    override fun cleanUpHtmlSpecific(text: String) = text
        .replace("(</p>) (</div>)".toRegex(), "$1\n$2")
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

    override fun fixCsv(csvRow: String): String {
        val fields = csvRow.split("\t").toMutableList()

        when {
            fields[13].contains("Перевод с карты на счет") -> {
                fields[12] = "~Зачисление"
            }

            fields[13].contains("Перевод между счетами") -> {
                fields[12] = "~Списание"
                fields[3] = "-" + fields[3]
            }
        }
        return super.fixCsv(fields.joinToString("\t"))
    }

    override fun cleanUpRow(row: String) = row
        .cleanUpByRules(config.replaceInRow)

    override fun discoverAccountInfo(text: String): AccountInfo {
        val mAcc = MBSA_ACCOUNT_AND_IN_BALANCE_REGEX.matchEntire(text.lines()[23])
        val mCurrent = MBSA_CURRENT_DATE_REGEX.matchEntire(text.lines()[15])
        val mDates = MBSA_REPORT_DATES_REGEX.matchEntire(text.lines()[22])
        val mEnd = MBSA_OUT_BALANCE_REGEX.matchEntire(text.lines()[text.lines().size - 2])

        val number = mAcc?.gv("account")
        val code =  number?.let {
            val s = it.takeLast(4)
            config.accounts.findByAccountNumber("*$s")?.code
        }

        return AccountInfo(
            accountCode = code,
            accountNumber = number,
            startDate = mDates?.gv("startDate"),
            endDate = mDates?.gv("endDate"),
            currentDate = mCurrent?.gv("currentDate"),
            startBalance = mAcc?.gv("startBalance"),
            endBalance = mEnd?.gv("endBalance"),
        )
    }
}