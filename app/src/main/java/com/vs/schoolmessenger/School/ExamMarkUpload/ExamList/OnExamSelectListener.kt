package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList

import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam.getStaffWisExamData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.getExamListData

interface OnExamSelectListener {
    fun onExamSelected(item: getStaffWisExamData?)
    fun onExamApiCall(item: getStaffWisExamData?)

}