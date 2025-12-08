package com.vs.schoolmessenger.Parent.LSRW.Model

data class LsrwSkillResponse(
    val status: Boolean,
    val message: String,
    val data: List<SkillData>
)