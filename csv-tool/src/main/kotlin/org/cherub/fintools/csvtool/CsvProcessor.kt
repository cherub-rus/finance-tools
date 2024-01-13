package org.cherub.fintools.csvtool

private const val qif_cleaned = "*"

enum class Fields(val idx: Int) {
    DATE(0),
    AMOUNT(2),
    PAYEE(3),
    CATEGORY(4),
    MARK(5),
    COMMENT(1),
}

class CsvProcessor {

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
            if (tokens[Fields.MARK.idx].startsWith("x")) {
                continue
            }
            if (accountList.isEmpty()) {
                throw Exception("Missing account header")
            }
            accountList.last().transactions.add(parseCsvTransaction(tokens))
        }

        return mutableListOf<String>().apply {
            accountList.forEach { account ->
                add(makeAccountHeader(account))
                account.transactions.forEach {
                    add(convertToQif(it))
                }
            }
            add("")
        }.joinToString(System.lineSeparator())
    }

    private fun parseCsvTransaction(tokens: List<String>) =
        Transaction(
            date = tokens[Fields.DATE.idx].trim(),
            amount = tokens[Fields.AMOUNT.idx].trim(),
            payee = tokens[Fields.PAYEE.idx].trim(),
            category = tokens[Fields.CATEGORY.idx].trim(),
            cleaned = tokens[Fields.MARK.idx].trim() == qif_cleaned,
            comment = tokens[Fields.COMMENT.idx].trim()
        )

    private fun makeAccountHeader(account: Account): String = mutableListOf<String>().apply {
        add("!Account")
        add("N${account.code}")
        add("T${account.type}")
        add("^")
        add("!Type:${account.type}")
    }.joinToString(System.lineSeparator())

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
    }.joinToString(System.lineSeparator())

}