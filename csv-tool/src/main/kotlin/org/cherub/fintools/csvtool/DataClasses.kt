package org.cherub.fintools.csvtool

data class Account(
    val cardId: String,
    val type: String,
    val code: String,
    val transactions: MutableList<Transaction>,
)

data class Transaction(
    val date: String,
    val amount: String,
    val payee: String,
    val category: String,
    val cleaned: Boolean,
    val comment: String,
)

