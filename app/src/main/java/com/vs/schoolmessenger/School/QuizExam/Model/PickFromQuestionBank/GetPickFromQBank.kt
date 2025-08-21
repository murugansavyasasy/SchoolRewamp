package com.vs.schoolmessenger.School.QuizExam.Model.PickFromQuestionBank

import com.vs.schoolmessenger.School.QuizExam.Model.QuizCheckLevel.GetCheckLevelData

class GetPickFromQBank(
    val status: Boolean,
    val message: String,
    val data: List<GetPickFromQBankData>
)