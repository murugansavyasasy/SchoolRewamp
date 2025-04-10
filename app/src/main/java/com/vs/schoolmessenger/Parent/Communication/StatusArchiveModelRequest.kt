package com.vs.schoolmessenger.Parent.Communication

import com.google.gson.JsonObject

data class StatusArchiveModelRequest(
    val token: String,
    val data: JsonObject
)
