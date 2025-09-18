package com.vs.schoolmessenger.School.PTM.InterFace

import android.view.View
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail

interface StaffSlotClickListener {
    fun onClickListener(data: SlotDetail)
    fun onSlotCancelReOpenClick(data: SlotDetail, anchor: View)
}
