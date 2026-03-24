package com.vs.schoolmessenger.School.Hostel.Listner

import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory.StaffLeaveData
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelRoomAttendanceStudentList.RoomStudentAttendanceData

interface RoomAttendanceListener {
    fun onAttendanceChanged(list: List<RoomStudentAttendanceData>)

    fun onApproveClicked(
        data: RoomStudentAttendanceData,
        position: Int,
        isButtonClick: Boolean,
        resultCallback: (Boolean) -> Unit
    )

}