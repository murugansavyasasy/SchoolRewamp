package com.vs.schoolmessenger.School.Homework

import com.vs.schoolmessenger.School.Event.EventHistoryData
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport

interface HomeWorkReportClickListener {
    fun onItemTextClick(data: HomeWorkReport)
    fun onItemImageClick(data: HomeWorkReport)
    fun onItemPDFClick(data: HomeWorkReport)
    fun onItemVoiceClick(data: HomeWorkReport)
    fun onItemVideoClick(data: HomeWorkReport)
}