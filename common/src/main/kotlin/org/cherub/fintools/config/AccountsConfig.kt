package org.cherub.fintools.config

import kotlinx.serialization.*
import java.io.File

@Serializable
data class AccountsConfig (
    val accounts: List<BankAccount>
)

fun String.loadAccountsFromFile(): AccountsConfig {
    val source = File(this).readText()
    return configFormatter.decodeFromString(source)
}

fun AccountsConfig.save(configName: String) {
    File(configName).writeText(configFormatter.encodeToString(this))
}


