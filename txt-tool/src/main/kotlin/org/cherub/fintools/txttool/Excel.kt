package org.cherub.fintools.txttool

import java.io.File

const val formula_c11 = "=ОКРУГЛ(R[-1]C+RC[-8];2)"
const val formula_c12 = "=ОКРУГЛ(R[-1]C[-1]+RC[-9];2)"

fun makeAccountHeader(account: String, accountCode: String, sourceName: String) =
    StringBuilder().also {
        it.appendLine("".addTabs())
        it.appendLine("Account\t$account\tBank\t$accountCode".addTabs()) // TODO account config
        it.appendLine("".addTabs())
        it.appendLine("#\t${File(sourceName).name}".addTabs())
    }.toString()

// TODO remove tabs. Tabs added only for temporary CSV validation
private fun String.addTabs() = this + "\t".repeat(11 - this.count { it == '\t' })
