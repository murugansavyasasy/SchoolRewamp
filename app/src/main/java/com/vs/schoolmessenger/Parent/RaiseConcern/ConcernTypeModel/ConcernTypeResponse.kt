package com.vs.schoolmessenger.Parent.RaiseConcern.ConcernTypeModel

data class ConcernTypeResponse (

    val status: Boolean,
    val message: String,
    val data: List<ConcernType>
)