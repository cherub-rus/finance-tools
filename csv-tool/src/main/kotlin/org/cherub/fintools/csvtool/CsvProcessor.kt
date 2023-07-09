package org.cherub.fintools.csvtool


class CsvProcessor {

    private val ls = "\n"// System.lineSeparator() // TODO windows LS

    fun process(fileText: String, qifStatus: String): String {
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
                        cardId = tokens[1], type = tokens[2], code = tokens[3], transactions = mutableListOf()
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
                builder.appendLine(convertToQif(it, qifStatus))
            }
        }
        return builder.toString()
    }

    private fun parseCsvTransaction(tokens: List<String>) =
        Transaction(
            date = tokens[0],
            comment = tokens[1],
            amount = tokens[2],
            payee = tokens[3],
            category = tokens[4],
            cleaned = tokens.size > 5 && tokens[5] == "*"
        )

    private fun makeAccountHeader(account: Account): String =
        "!Account${ls}N${account.code}${ls}T${account.type}${ls}^${ls}!Type:${account.type}"

    //TODO format
    private fun convertToQif(tr: Transaction, qifStatus: String): String = mutableListOf<String>().apply {
        add("D${tr.date.replace('.', '/')}")
        if (qifStatus.isNotEmpty() || tr.cleaned)
            add("C${qifStatus}${if (tr.cleaned) "*" else ""}")
        if (tr.comment.isNotEmpty())
            add("M${tr.comment}")
        add("T${tr.amount}")
        if (tr.payee.isNotEmpty())
            add("P${tr.payee}")
        add("L${tr.category}")
        add("^")
    }.joinToString(ls)

}