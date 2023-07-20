package org.cherub.fintools.config

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File

val configFormatter = Json { isLenient = true; prettyPrint = true }

@Serializable
data class ConfigData (
    val accounts: List<BankAccount>,
    @SerialName("replace-in-source")
    val replaceInSource: List<ReplaceRule>,
    @SerialName("replace-in-row")
    val replaceInRow: List<ReplaceRule>,
    @SerialName("replace-in-result")
    val replaceInResult: List<ReplaceRule>,
    @SerialName("sber-operation-type")
    val sberOperationType: List<String>,
    @SerialName("mtsb-use-incomes")
    val mtsbUseIncomes:  Boolean,
    @SerialName("mtsb-sms-incomes")
    val mtsbSmsIncomes:  List<String>,
    @SerialName("mtsb-sms-expenses")
    val mtsbSmsExpenses:  List<String>
)

@Serializable
data class ReplaceRule (
    val s: String,
    val r: String,
    val sRx: Boolean = false
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


