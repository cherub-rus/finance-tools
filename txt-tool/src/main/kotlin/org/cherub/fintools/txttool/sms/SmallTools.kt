package org.cherub.fintools.txttool.sms

import java.time.LocalDate
import java.time.LocalTime

interface IContentParser {
    fun parse(content: String): Transaction?
}

data class Sms(
    val date: LocalDate, val time: LocalTime, val bank: String, val bank2: String, val content: String, val trans: Transaction
)

data class Transaction(
    val account: String, val operation: String, val message: String, val amount: String, val balance: String?,
    val date: String? = null, val time: String? = null
)

data class ProcessResult(
    val csv: String, val skipped: List<String>? = null
)

fun MatchResult.gv(index: Int) =
    this.groups[index]?.value ?: ""
