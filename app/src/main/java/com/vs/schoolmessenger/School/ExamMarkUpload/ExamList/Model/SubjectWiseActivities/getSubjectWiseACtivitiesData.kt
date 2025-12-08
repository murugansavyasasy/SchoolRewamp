package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities

class getSubjectWiseACtivitiesData(
    val section_id: String,
    val section_name: String,
    val class_id: String,
    val class_name: String,
    val subject_id: String,
    val subject_name: String,
    val splitup_details: List<getSplitDetailData>
)