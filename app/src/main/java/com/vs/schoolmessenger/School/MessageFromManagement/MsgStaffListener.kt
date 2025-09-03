package com.vs.schoolmessenger.School.MessageFromManagement

import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData

interface MsgStaffListener {
    fun onStaffClick(data: GetMessagesStaffData)

}
