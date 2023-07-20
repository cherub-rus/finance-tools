package org.cherub.fintools.txttool

fun MatchResult.gv(index: Int) =
    this.groups[index]!!.value

fun MatchResult.gv(name: String) =
    this.groups[name]!!.value