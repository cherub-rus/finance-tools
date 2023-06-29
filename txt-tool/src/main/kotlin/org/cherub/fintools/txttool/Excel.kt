package org.cherub.fintools.txttool

import java.io.File

const val formula_c11 = "=ОКРУГЛ(R[-1]C+RC[-8];2)"
const val formula_c12 = "=ОКРУГЛ(R[-1]C[-1]+RC[-9];2)"

fun makeAccountHeader(account: String, sourceName: String) =
    StringBuilder().also {
        it.appendLine("Account\t$account\tBank\t##TODO##\t\t\t\t\t\t\t\t") // TODO account config, remove tabs
        it.appendLine("")
        it.appendLine("#\t${File(sourceName).name}")
    }.toString()
