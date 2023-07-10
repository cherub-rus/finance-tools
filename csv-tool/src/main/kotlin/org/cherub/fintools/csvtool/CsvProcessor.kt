package org.cherub.fintools.csvtool


private const val qif_cleaned = "*"

class CsvProcessor {

    private val ls = "\n"// System.lineSeparator() // TODO windows LS

    fun process(fileText: String): String {
        val accountList = mutableListOf<Account>()

        for (line in fileText.lines()) {
            if (line.trim().isEmpty() ||
                line.startsWith("#") ||
                line.replace("\t","").trim().isEmpty()
            ) continue

            val tokens = line.split('\t')
            if (tokens[0] == "Account") {
                accountList.add(
                    Account(
                        cardId = tokens[1].trim(),
                        type = tokens[2].trim(),
                        code = tokens[3].trim(),
                        transactions = mutableListOf()
                ))
                continue
            }
            if (accountList.isEmpty()) {
                throw Exception("Missing account header")
            }
            accountList.last().transactions.add(parseCsvTransaction(tokens))
        }

        val builder = StringBuilder()
        accountList.forEach { account ->
            builder.appendLine(makeAccountHeader(account))
            account.transactions.forEach {
                builder.appendLine(convertToQif(it))
            }
        }
        return builder.toString()
    }

    private fun parseCsvTransaction(tokens: List<String>) =
        Transaction(
            date = tokens[0].trim(),
            comment = tokens[1].trim(),
            amount = tokens[2].trim(),
            payee = tokens[3].trim(),
            category = tokens[4].trim(),
            cleaned = tokens.size > 5 && tokens[5].trim() == qif_cleaned
        )

    private fun makeAccountHeader(account: Account): String =
        "!Account${ls}N${account.code}${ls}T${account.type}${ls}^${ls}!Type:${account.type}"

    //TODO format
    private fun convertToQif(tr: Transaction): String = mutableListOf<String>().apply {
        add("D${tr.date.replace('.', '/')}")
        if (tr.cleaned)
            add("C${qif_cleaned}")
        if (tr.comment.isNotEmpty())
            add("M${tr.comment}")
        add("T${tr.amount}")
        if (tr.payee.isNotEmpty())
            add("P${tr.payee}")
        add("L${tr.category}")
        add("^")
    }.joinToString(ls)

}