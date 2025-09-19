package com.vs.schoolmessenger.School.Homework

import android.view.View
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportData

interface HomeWorkReportClickListener {
    fun onClickListener(data: HomeWorkReportData, anchorView: View, adapterPosition: Int)

}