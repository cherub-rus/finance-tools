package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.CommonProcessor

internal val SB_REPORT_START_REGEX = "<p><b>Остаток на (?<startDate>\\d{2}\\.\\d{2}\\.\\d{4}) </b>(?<startBalance>(\\d{1,3} )*\\d{1,3},\\d{2})</p>".toRegex()
internal val SB_REPORT_END_REGEX = "<p><b>Остаток на (?<endDate>\\d{2}\\.\\d{2}\\.\\d{4}) </b>(?<endBalance>(\\d{1,3} )*\\d{1,3},\\d{2})</p>".toRegex()
internal val SB_REPORT_CURRENT_DATE_REGEX = "<p>Дата формирования документа <b>(?<currentDate>\\d{2}\\.\\d{2}\\.\\d{4})</b></p>".toRegex()

abstract class SberProcessor(config: ConfigData, reorderCsvRows: Boolean = false) : CommonProcessor(config, reorderCsvRows) {

}
