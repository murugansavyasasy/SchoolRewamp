package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model

data class ActivityDataExamMark( val activity_id: String,
                                 val activity_name: String,
                                 val max_mark: String,
                                 val rubrics: List<RubricData>)
