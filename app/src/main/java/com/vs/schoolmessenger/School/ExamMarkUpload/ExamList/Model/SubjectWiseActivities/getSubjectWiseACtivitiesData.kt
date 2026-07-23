package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities

import android.os.Parcelable
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.ActivityDataExamMark
import kotlinx.parcelize.Parcelize

data class getSubjectWiseACtivitiesData(
    val subject_id: String,
    val institute_subject_id: String,
    val subject_name: String,
    val activities: List<ActivityDataExamMark>
)

//@Parcelize

//class getSubjectWiseACtivitiesData(
//    val section_id: String,
//    val section_name: String,
//    val class_id: String,
//    val class_name: String,
//    val subject_id: String,
//    val subject_name: String,
//    val splitup_details: List<getSplitDetailData>
//) : Parcelable