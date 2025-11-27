package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam

data class getStaffWisExam (
    val status: Boolean,
    val message: String,
    val data: List<getStaffWisExamData>
)