package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.pdftool.CommonProcessor

abstract class SberProcessor : CommonProcessor(){

    override fun cleanUpHtml(text: String) = super.cleanUpHtml(text)
        .replace("(В валюте счёта</p>)".toRegex(), "$1\n")
        .replace("(<p>Сумма в валюте операции²</p>)".toRegex(), "$1\n")

    @Suppress("SpellCheckingInspection")
    override fun cleanUpTransactions(content: String) = content
        .replace("  *".toRegex(), " ")
        .replace("\"", "")
        .replace(" Tomsk RUS", "")
        .replace(" TOMSK RUS", "")
        .replace(" TOMCK RUS", "")
        .replace(" Gorod Moskva RUS", "")
        .replace("SUPERMARKET SPAR", "SPAR")
        .replace("SBERBANK ONL@IN ", "Сбербанк Онлайн ")

}
