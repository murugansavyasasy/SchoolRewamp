package com.vs.schoolmessenger.School.Hostel.Listner

import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelRoomAttendanceStudentList.RoomStudentAttendanceData

interface RoomAttendanceListener {
    fun onAttendanceChanged(list: List<RoomStudentAttendanceData>)

}