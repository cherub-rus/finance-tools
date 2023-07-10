package org.cherub.fintools.txttool

fun MatchResult.gv(index: Int) =
    this.groups[index]?.value ?: ""