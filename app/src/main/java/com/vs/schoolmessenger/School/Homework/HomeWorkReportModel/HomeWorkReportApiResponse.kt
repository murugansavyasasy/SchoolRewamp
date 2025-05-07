package com.vs.schoolmessenger.School.Homework.HomeWorkReportModel

data class HomeWorkReportApiResponse (
    val status: Boolean,
    val message: String,
    val data: List<HomeWorkReport>
)