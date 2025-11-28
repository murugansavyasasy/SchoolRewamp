package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities

import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam.getStaffWisExamData

data class getSubjectWiseACtivities (
    val status: Boolean,
    val message: String,
    val data: List<getSubjectWiseACtivitiesData>
)