package com.vs.schoolmessenger.School.InteractionWithStudent.Response

import com.vs.schoolmessenger.School.InteractionWithStudent.Model.BlockedStudent

data class BlockedStudentsResponse (
    val status: Boolean,
    val message: String,
    val data: List<BlockedStudent>
)