package com.vs.schoolmessenger.School.PTM.InterFace

import android.view.View
import com.vs.schoolmessenger.School.PTM.DataClass.BookedSlotItem

interface BookedSlotCancelReOpenClickListener {
    fun onBookedSlotCancelReOpenClickListener(
        data: BookedSlotItem,
        view: View,
        adapterPosition: Int
    )

}