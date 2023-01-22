import kotlinx.serialization.*
import kotlinx.serialization.json.Json

val configFormatter = Json { isLenient = true; prettyPrint = true }

@Serializable
data class Config(
    val accountNamesForReplace: List<Pair<String,String>>
)

fun main() {
    val config = Config(listOf(Pair("a","b"),Pair("c","d")))
    println(configFormatter.encodeToString(config))

    val config2 = configFormatter.decodeFromString<Config>("""
{
    "accountNamesForReplace": [
        {
            "first": "a",
            "second": "b"
        },
        {
            "first": "c",
            "second": "d"
        }
    ]
}
    """)
    println(config2)
}
