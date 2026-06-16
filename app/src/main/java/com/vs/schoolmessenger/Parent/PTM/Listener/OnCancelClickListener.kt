package com.vs.schoolmessenger.Parent.PTM.Listener

import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingItem

interface OnCancelClickListener {
    fun onCancelClick(meeting: MeetingItem, position: Int, reason: String)
}