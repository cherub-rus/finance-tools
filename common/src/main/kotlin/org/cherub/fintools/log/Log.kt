package org.cherub.fintools.log

fun log(e: Exception, row: String? = null) {
    System.err.println(e)
    row?.also { System.err.println(it) }
    //e.printStackTrace()
}
