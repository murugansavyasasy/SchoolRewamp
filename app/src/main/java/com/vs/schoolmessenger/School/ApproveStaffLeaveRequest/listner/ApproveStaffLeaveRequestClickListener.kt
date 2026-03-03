package com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.listner

import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData

interface ApproveStaffLeaveRequestClickListener {
    fun onSearchResultEmpty(isEmpty: Boolean)
    fun onApproveClicked(
        data: LeaveData,
        position: Int,
        isButtonClick: Boolean,
        resultCallback: (Boolean) -> Unit
    )

    fun onUpdateStatus(leaveData: LeaveData)

}