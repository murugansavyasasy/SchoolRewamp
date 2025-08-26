package com.vs.schoolmessenger.School.LSRW.AvgPerformanceModel

data class AvgSkillData (
    val today_submitted: List<AvgStudentSubmission>?,
    val listening: ListeningCategory?,
    val speaking: SpeakingCategory?,
    val reading: ReadingCategory?,
    val writing: WritingCategory?
)