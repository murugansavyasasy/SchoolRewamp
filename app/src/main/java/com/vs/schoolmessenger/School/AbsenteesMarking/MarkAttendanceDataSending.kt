package com.vs.schoolmessenger.School.AbsenteesMarking

data class MarkAttendanceDataSending(

    var class_id:String,
    var section_id:String,
    var all_present:String,
    var attendance_type:String,
    var session_type:String,
    var attendance_date:String,

)
