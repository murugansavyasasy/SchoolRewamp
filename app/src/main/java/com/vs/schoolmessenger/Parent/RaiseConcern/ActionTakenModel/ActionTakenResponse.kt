package com.vs.schoolmessenger.Parent.RaiseConcern.ActionTakenModel

data class ActionTakenResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)