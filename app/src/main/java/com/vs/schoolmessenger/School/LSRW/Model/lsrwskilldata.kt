package com.vs.schoolmessenger.School.LSRW.Model
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath

data class lsrwskilldata(
    val overview: List<Overview>,
    val active: List<LsrwTask>,
    val completed: List<LsrwTask>
)