package org.cherub.fintools.txttool.sms

interface IContentParser {
    fun parse(content: String): Transaction?
}