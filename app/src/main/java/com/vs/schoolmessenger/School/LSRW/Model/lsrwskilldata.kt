package com.vs.schoolmessenger.School.LSRW.Model

data class lsrwskilldata(
    val overview: List<Overview>,
    val active: List<LsrwTask>,
    val completed: List<LsrwTask>
)