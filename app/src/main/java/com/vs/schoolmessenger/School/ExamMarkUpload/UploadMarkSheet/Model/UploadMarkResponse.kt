package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model

data class UploadMarkResponse (
    val message: String,
    val data: List<ExtractionResult>
)