package com.vs.schoolmessenger.School.LeaveRequests.Listener

import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestHistoryData
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData

interface SchoolLRClickListener {
    fun onSearchResultEmpty(isEmpty: Boolean)
    fun onApproveClicked(data: LeaveData, position: Int)
    fun onRejectClicked(data: LeaveData, position: Int)

}