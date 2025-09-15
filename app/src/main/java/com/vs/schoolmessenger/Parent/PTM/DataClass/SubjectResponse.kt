package com.vs.schoolmessenger.Parent.PTM.DataClass

data class SubjectResponse( val status: Boolean,
                            val message: String,
                            val data: List<SubjectData>)
