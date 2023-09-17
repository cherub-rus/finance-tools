package org.cherub.fintools.txttool

fun String.quote() = if (this.isNotEmpty()) " '${this}'" else ""

fun String.appendDecimals() = if (this.contains(",")) this else "$this,00"

fun String.fixAmountString() = this.replace(" ", "").replace(".", ",").appendDecimals()

fun String.startsWithAny(list: List<String>) =
    list.any { this.startsWith(it, ignoreCase = true) }

fun String.equalsAny(list: List<String>) =
    list.any { this.equals(it, ignoreCase = true) }
