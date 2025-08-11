package com.vs.schoolmessenger.School.LeaveRequests.Listener

import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestHistoryData
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData

interface SchoolLRClickListener {
    fun onSearchResultEmpty(isEmpty: Boolean)
    fun onApproveClicked(
        data: LeaveData,
        position: Int,
        isButtonClick: Boolean,
        resultCallback: (Boolean) -> Unit
    )

    fun onUpdateStatus(leaveData: LeaveData)

}