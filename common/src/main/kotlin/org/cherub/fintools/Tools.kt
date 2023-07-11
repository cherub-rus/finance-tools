package org.cherub.fintools

import org.cherub.fintools.config.ReplaceRule

fun String.cleanUpByRules(rules: List<ReplaceRule>): String {
    var str = this
    rules.forEach {
        str = if (it.sRx) {
            str.replace(it.s.toRegex(), it.r)
        } else {
            str.replace(it.s, it.r)
        }
    }
    return str
}
