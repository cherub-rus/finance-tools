package org.cherub.fintools.txttool.sms

import org.cherub.fintools.txttool.Transaction

interface IContentParser {
    fun parse(content: String): Transaction?
}