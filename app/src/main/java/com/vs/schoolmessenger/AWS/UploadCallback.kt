package com.vs.schoolmessenger.AWS

interface UploadCallback {
    fun onUploadSuccess(response: String?, isFileUploaded: String?)
    fun onUploadError(error: String?)
}
