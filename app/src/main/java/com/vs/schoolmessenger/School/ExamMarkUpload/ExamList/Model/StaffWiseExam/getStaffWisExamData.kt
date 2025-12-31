package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class getStaffWisExamData(
    val id: String,  //////exam_id
    val name: String,
    val date: String,
    val ref_flag: Int
) : Parcelable