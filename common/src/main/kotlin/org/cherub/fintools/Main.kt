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
        val config = configName.loadConfigFromFile()
        println(config.toString())

    } catch (e: Exception) {
        log(e)
    }
}
