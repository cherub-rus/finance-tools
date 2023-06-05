package org.cherub.fintools.txttool.sms.mtsb

import org.cherub.fintools.txttool.sms.Transaction
import org.cherub.fintools.txttool.sms.gv

class MtsbContentParser {

    fun parse(content: String) : Transaction? {
        // TODO Set "-" by operation

        // TODO parsers collection?
        parse1(content)?.also { return it }
        parse2(content)?.also { return it }
        parse3(content)?.also { return it }

        return null
    }

    private fun parse1(content: String): Transaction? {
        val regex =
            "([^0-9]+) ([0-9][0-9 ]*,[0-9]{2}) RUB (.+) {2}Ostatok: ([0-9][0-9 ]*,[0-9]{2}) RUB; ([*][0-9]{4}) ".toRegex()
        val m = regex.matchEntire(content) ?: return null
        return Transaction(m.gv(5), m.gv(1), m.gv(3), "-${m.gv(2).replace(" ", "")}", m.gv(4).replace(" ", ""))
    }

    private fun parse2(content: String): Transaction? {
        val regex =
            "([^0-9]+) ([*][0-9]{4}); ([0-9][0-9 ]*,[0-9]{2}) RUB; Ostatok: ([0-9][0-9 ]*,[0-9]{2}) RUB".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(m.gv(2), m.gv(1), "", m.gv(3).replace(" ", ""), m.gv(4).replace(" ", ""))
    }

    private fun parse3(content: String): Transaction? {
        val regex =
            "([^0-9]+) ([*][0-9]{4}) ([0-9.]{5}) ([0-9:]{5}) (.+?);? ([0-9][0-9 ]*,[0-9]{2}) RUB Ostatok: ([0-9][0-9 ]*,[0-9]{2}) RUB;? ".toRegex()
        val m = regex.matchEntire(content) ?: return null

        return Transaction(m.gv(2), m.gv(1), m.gv(5), m.gv(6).replace(" ", ""), m.gv(7).replace(" ", ""), m.gv(3), m.gv(4))
    }

}