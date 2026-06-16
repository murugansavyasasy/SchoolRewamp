package com.vs.schoolmessenger.Parent.RequestLeave

import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData

interface LeaveRequestClickListener {
    fun onItemImageClick(data: LeaveRequestHistoryData)
    fun onItemDeleteClick(data: LeaveData)
    fun onItemEditClick(data: LeaveData)

}