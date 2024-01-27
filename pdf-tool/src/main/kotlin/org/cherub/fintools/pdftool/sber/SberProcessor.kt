package org.cherub.fintools.pdftool.sber

import org.cherub.fintools.config.ConfigData
import org.cherub.fintools.pdftool.AccountInfo
import org.cherub.fintools.pdftool.CommonProcessor
import org.cherub.fintools.pdftool.findByAccountNumber
import org.cherub.fintools.pdftool.gv

abstract class SberProcessor(config: ConfigData) : CommonProcessor(config) {

    override fun cleanUpHtml(text: String) = super.cleanUpHtml(text)
        .replace("(В валюте счёта</p>)".toRegex(), "$1\n")
        .replace("(<p>Сумма в валюте операции²</p>)".toRegex(), "$1\n")
}
