package com.vs.schoolmessenger.Parent.RaiseConcern.ParentConcernlistModel

data class ParentConcernResponse (
    val status: Boolean,
    val message: String,
    val data: List<ParentConcern>
)