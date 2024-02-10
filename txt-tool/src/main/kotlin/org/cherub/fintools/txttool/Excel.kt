package org.cherub.fintools.txttool

import org.cherub.fintools.config.BankAccount
import java.io.File

const val FORMULA_AMOUNT2 = "=RC[-1]-RC4"
const val FORMULA_BALANCE1 = "=ОКРУГЛ(R[-1]C+RC4;2)"
const val FORMULA_BALANCE2 = "=ОКРУГЛ(R[-1]C[-1]+RC4;2)"

const val KEY_SEPARATOR = "#"

fun makeAccountHeader(accountId: String, accounts: List<BankAccount>, sourceName: String) =
    StringBuilder().also {
        it.appendLine("".addTabs())
        it.appendLine("Account\t$accountId\t${accounts.findType(accountId)}\t${accounts.findCode(accountId)}".addTabs())
        it.appendLine("".addTabs())
        it.appendLine("#\t${File(sourceName).name}".addTabs())
    }.toString()

fun List<BankAccount>.findCode(accountId: String) =
    findAccount(accountId).let { it?.code ?: "#UNKNOWN#" }

fun List<BankAccount>.findType(accountId: String) =
    findAccount(accountId).let { it?.type ?: "#UNKNOWN#" }

private fun List<BankAccount>.findAccount(id: String): BankAccount? =
    if (!id.contains(KEY_SEPARATOR)) {
        this.firstOrNull { it.cardId == id }
    } else {
        this.firstOrNull { it.bankId == id.substringBefore(KEY_SEPARATOR) &&
                           it.cardId == id.substringAfter(KEY_SEPARATOR) }
    }

fun List<BankAccount>.findByAccountNumber(number: String): BankAccount? =
    this.firstOrNull { it.account == number }

private fun String.addTabs() = this + "\t".repeat(12 - this.count { it == '\t' })
