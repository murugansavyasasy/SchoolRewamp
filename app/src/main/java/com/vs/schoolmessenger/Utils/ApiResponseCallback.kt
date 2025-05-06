package com.vs.schoolmessenger.Utils

import com.vs.schoolmessenger.School.Communication.DataClass.TextSendResponse

interface ApiResponseCallback {
    fun onSuccess(response: TextSendResponse?)
    fun onFailure(error: String)
}
