package org.cherub.fintools

import org.cherub.fintools.config.ReplaceRule

fun String.cleanUpByRules(rules: List<ReplaceRule>): String {
    var str = this
    rules.forEach { str = str.replace(it.s, it.r) }
    return str
}
