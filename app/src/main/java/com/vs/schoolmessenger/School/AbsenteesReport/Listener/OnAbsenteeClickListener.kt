package com.vs.schoolmessenger.School.AbsenteesReport.Listener

interface OnAbsenteeClickListener {
    fun onAbsenteeClicked(absentOn: String, sectionId: String,classname: String, sectionname:String,absent:String,total:String)
}
