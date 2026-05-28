package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities

data class getSubjectWiseACtivities(
    val status: Boolean,
    val message: String,
    val data: List<getSubjectWiseACtivitiesData>
)