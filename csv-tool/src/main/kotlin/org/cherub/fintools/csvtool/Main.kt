package org.cherub.fintools.csvtool

import org.cherub.fintools.log.log
import java.io.File

fun main(args: Array<String>) {

    val sourceName = args[0]
    val targetName = "${sourceName}.qif"

    if (sourceName.isEmpty() || sourceName == ".") {
        println("File name is required argument!!!")
    }
    try {
        val fileText = getContent(sourceName)
        val result = CsvProcessor().process(fileText)

        File(targetName).writeUTF8(result)
    } catch (e: Exception) {
        log(e)
    }
}

private fun getContent(sourceFileName: String): String =
    File(sourceFileName).readText().replace("\uFEFF", "")

private fun File.writeUTF8(text: String) =
    this.writeText("\uFEFF" + text, Charsets.UTF_8)


