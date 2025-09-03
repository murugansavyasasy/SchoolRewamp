package com.vs.schoolmessenger.School.PTM.InterFace

import android.view.View
import com.vs.schoolmessenger.School.PTM.DataClass.Slot

interface StaffSlotCancelReOpenClickListener {
    fun onStaffSlotCancelReOpenClickListener(data: Slot, view: View, adapterPosition: Int)
}