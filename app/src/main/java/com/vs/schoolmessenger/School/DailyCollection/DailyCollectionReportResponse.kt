package com.vs.schoolmessenger.School.DailyCollection

import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport

data class DailyCollectionReportResponse (
    val status: Boolean,
    val message: String,
    val data: List<DailyCollectionItem>

)