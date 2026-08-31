package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.log.log
import org.cherub.fintools.pdftool.C_DATE
import org.cherub.fintools.pdftool.C_TIME
import org.cherub.fintools.pdftool.CommonProcessor
import org.cherub.fintools.pdftool.TIME_OFFSET_LOCAL_TO_BASE_HOURS

internal val SB_REPORT_START_REGEX = "<p><b>Остаток на (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) </b>(?<startBalance>(\\d{1,3} )*\\d{1,3},\\d{2})</p>".toRegex()
internal val SB_REPORT_END_REGEX = "<p><b>Остаток на (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4}) </b>(?<endBalance>(\\d{1,3} )*\\d{1,3},\\d{2})</p>".toRegex()
internal val SB_REPORT_CURRENT_DATE_REGEX = "<p>Дата формирования документа <b>(?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()

abstract class SberProcessor(config: ConfigData, reorderCsvRows: Boolean = false) : CommonProcessor(config, reorderCsvRows) {

    override fun fixCsv(csvRow: String): String {
        val fields = csvRow.split("\t").toMutableList()

        try {
            val operationDate = parseDate("${fields[C_DATE]} ${fields[C_TIME]}")
            overrideOperationDate(fields, operationDate, TIME_OFFSET_LOCAL_TO_BASE_HOURS)
        } catch (e: Exception) {
            log(e, csvRow)
        }

        return super.fixCsv(fields.joinToString("\t"))
    }

}
