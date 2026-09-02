package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model



data class ClassData (

    val count: String,
    val pass: String,
    val fail: String,
    val passPercentage: String,
    val average: String,
    val median: String,
    val sd: String,
    val totalMax: String,
    val highest: ClassStudent,
    val lowest: ClassStudent,
    val spread: String,
    val bands: List<ScoreBand>,
    val grades: List<ClassGrade>,
    val sections: List<String>
        )
