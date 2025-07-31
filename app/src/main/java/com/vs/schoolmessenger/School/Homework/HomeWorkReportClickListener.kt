package com.vs.schoolmessenger.School.Homework

import android.view.View
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport

interface HomeWorkReportClickListener {
    fun onClickListener(data: HomeWorkReport, anchorView: View, adapterPosition: Int)

}