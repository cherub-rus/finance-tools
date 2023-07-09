package org.cherub.fintools.txttool.sms

fun MatchResult.gv(index: Int) =
    this.groups[index]?.value ?: ""