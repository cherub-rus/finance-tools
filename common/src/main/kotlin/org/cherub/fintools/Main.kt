package org.cherub.fintools

import org.cherub.fintools.config.*
import org.cherub.fintools.log.log

fun main(args: Array<String>) {

    if (args.isEmpty() || args[0].isEmpty() || args[0] == ".") {
        println("File name is required argument!!!")
        return
    }
    val configName = args[0]

    try {
        @Suppress("ConstantConditionIf")
        if (false) generateExample(configName)

        val config = configName.loadConfigFromFile()
        println(config.toString())

    } catch (e: Exception) {
        log(e)
    }
}

@Suppress("SpellCheckingInspection")
private fun generateExample(configName: String) {
    val configGenerated = ConfigData(
        listOf(
            BankAccount("card1", "bank1", "Bank", "acc1"),
            BankAccount("card2", "bank1", "CCard", "acc2")
        ),
        listOf("oper1", "oper2"),
        listOf(
            ReplaceRule("str1", "str11"),
            ReplaceRule("str2", "str21"),
        ),
        listOf(
            ReplaceRule("xstr1", "xstr11"),
            ReplaceRule("xstr2", "xstr21"),
        ),
        listOf(
            ReplaceRule("zstr1", "zstr11"),
            ReplaceRule("zstr2", "zstr21"),
        ),
        listOf("istr1", "istr2")
    )
    configGenerated.save("$configName.generated")
}
