package com.vs.schoolmessenger.Utils

interface fingerPrintAunthenticateListener {
    fun onAuthenticate(message: String, status: Boolean)
}