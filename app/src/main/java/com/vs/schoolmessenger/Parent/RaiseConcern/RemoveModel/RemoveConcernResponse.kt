package com.vs.schoolmessenger.Parent.RaiseConcern.RemoveModel

data class RemoveConcernResponse (
    val status: Boolean,
    val message: String,
    val data: List<Any> = emptyList()
)