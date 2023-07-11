package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.cleanUpByRules
import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.CommonProcessor

abstract class SberProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun cleanUpHtml(text: String) = super.cleanUpHtml(text)
        .replace("(В валюте счёта</p>)".toRegex(), "$1\n")
        .replace("(<p>Сумма в валюте операции²</p>)".toRegex(), "$1\n")

    @Suppress("SpellCheckingInspection")
    override fun cleanUpResult(content: String) = content
        .replace(" +".toRegex(), " ")
        .replace("\"", "")
        .cleanUpByRules(config.replaceInResult)

}
