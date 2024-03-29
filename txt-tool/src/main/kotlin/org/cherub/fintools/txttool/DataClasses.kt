package org.cherub.fintools.txttool

import java.time.LocalDate
import java.time.LocalTime

data class Sms(
    val date: LocalDate, val time: LocalTime, val bank: String, val bank2: String, val content: String, val trans: Transaction
)

data class Push(
    val date: LocalDate, val time: LocalTime, val account: String, val operation: String, val message: String, val amount: String, val balance: String
)

data class Transaction(
    val account: String, val operation: String, val message: String, val amount: String, val balance: String?,
    val date: String? = null, val time: String? = null, val discount: String = ""
)

data class ProcessResult(
    val csvMap: Map<String, String>, val skipped: List<String>? = null
)

