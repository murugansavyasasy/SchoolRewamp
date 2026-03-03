package com.vs.schoolmessenger.School.StaffLeaveRequest.Listner

import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestHistoryData
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData

interface StaffLeaveRequestClickListener {
    fun onItemImageClick(data: LeaveRequestHistoryData)
    fun onItemDeleteClick(data: LeaveData)
    fun onItemEditClick(data: LeaveData)

}