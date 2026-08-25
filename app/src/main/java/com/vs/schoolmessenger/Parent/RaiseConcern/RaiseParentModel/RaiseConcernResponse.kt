package com.vs.schoolmessenger.Parent.RaiseConcern.RaiseParentModel

data class RaiseConcernResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)