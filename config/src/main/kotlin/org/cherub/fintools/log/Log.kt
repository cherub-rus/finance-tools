package org.cherub.fintools.log

fun log(e: Exception, row: String? = null) {
    System.err.println(e)
    System.err.println(row!!)
    //e.printStackTrace()
}
