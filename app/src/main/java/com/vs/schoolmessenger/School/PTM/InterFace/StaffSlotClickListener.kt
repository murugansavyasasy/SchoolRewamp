package com.vs.schoolmessenger.School.PTM.InterFace

import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail

interface StaffSlotClickListener {
    fun onClickListener(data: SlotDetail)
}