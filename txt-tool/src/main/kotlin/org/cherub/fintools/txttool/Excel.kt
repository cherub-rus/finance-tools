package org.cherub.fintools.txttool

import org.cherub.fintools.config.BankAccount
import java.io.File

const val formula_c10 = "=ЕСЛИ(RC[-1]>0;RC[-1]-RC[-7];\"\")"
const val formula_c11 = "=ОКРУГЛ(R[-1]C+RC[-8];2)"
const val formula_c12 = "=ОКРУГЛ(R[-1]C[-1]+RC[-9];2)"

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

// TODO remove tabs. Tabs added only for temporary CSV validation
private fun String.addTabs() = this + "\t".repeat(12 - this.count { it == '\t' })
