package org.cherub.fintools.config

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File

val configFormatter = Json { isLenient = true; prettyPrint = true }

@Serializable
data class ConfigData (
    val accounts: List<BankAccount>,
    @SerialName("sber-operation-type")
    val sberOperationType: List<String>,
    @SerialName("replace-before-parse")
    val replaceBeforeParse: List<ReplaceRule>,
    @SerialName("replace-after-parse")
    val replaceAfterParse: List<ReplaceRule>,
    @SerialName("ignore-messages")
    val ignoreMessages: List<String>
)

@Serializable
data class ReplaceRule (
    val s: String,
    val r: String,
    val sRx: Boolean = false,
    val rRx: Boolean = false
)

@Serializable
data class BankAccount (
    @SerialName("card-id")
    val cardId: String,
    @SerialName("bank-id")
    val bankId: String,
    val type: String,
    val code: String
)

fun String.loadConfigFromFile(): ConfigData {
    val configSource = File(this).readText()
    return configFormatter.decodeFromString(configSource)
}

fun ConfigData.save(configName: String) {
    File(configName).writeText(configFormatter.encodeToString(this))
}


