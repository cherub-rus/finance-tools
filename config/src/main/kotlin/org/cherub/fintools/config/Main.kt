package org.cherub.fintools.config

fun main(args: Array<String>) {

    if (args.isEmpty() || args[0].isEmpty() || args[0] == ".") {
        println("File name is required argument!!!")
        return
    }
    val configName = args[0]

    try {
        if (false) generateExample(configName)

        val config = configName.loadConfigFromFile()
        println(config.toString())

    } catch (e: Exception) {
        e.printStackTrace() //todo log
    }
}

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
        listOf("istr1", "istr2")
    )
    configGenerated.save("$configName.generated")
}

