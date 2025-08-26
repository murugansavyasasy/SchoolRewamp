package com.vs.schoolmessenger.School.LSRW.AvgPerformanceModel

data class AvgSkillResponse(
    val status: Boolean,
    val message: String,
    val data: List<AvgSkillData>
)