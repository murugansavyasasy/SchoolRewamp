package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity

import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.getExamListData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivityExamListData

interface OnActivityExamSelectListener {
    fun onActivityExamSelected(item: getActivityExamListData?)

}