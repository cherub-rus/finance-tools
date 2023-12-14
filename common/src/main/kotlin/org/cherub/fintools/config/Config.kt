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
    @SerialName("operation-types")
    val operationTypes: List<OperationType>
) {
    companion object {
        const val MTS_BANK_ID = "MTS-Bank"
        const val SBER_BANK_ID = "900"
        const val US_BANK_ID = "URALSIB"
    }

    fun getSberOperationTypeNames() =
        this.operationTypes.filter { it.bankId == SBER_BANK_ID }.map { it.name }

    fun findSberOperationType(operation: String) =
        this.operationTypes.find { it.bankId == SBER_BANK_ID && it.name.equals(operation, ignoreCase = true) }

    fun findMtsOperationType(operation: String) =
        this.operationTypes.find { it.bankId == MTS_BANK_ID && it.name.equals(operation, ignoreCase = true) }

}

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
    val account: String,
    @SerialName("bank-id")
    val bankId: String,
    val type: String,
    val code: String
)

@Serializable
data class OperationType (
    @SerialName("bank-id")
    val bankId: String,
    val sign: String,
    val name: String
)


fun String.loadConfigFromFile(): ConfigData {
    val configSource = File(this).readText()
    return configFormatter.decodeFromString(configSource)
}

fun ConfigData.save(configName: String) {
    File(configName).writeText(configFormatter.encodeToString(this))
}


