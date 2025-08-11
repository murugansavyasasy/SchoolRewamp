package com.vs.schoolmessenger.School.Event.Listener

import android.view.View
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem

interface SchoolEventClickListener {
    fun onSearchResultEmpty(isEmpty1: String, isEmpty: Boolean)
    fun onDeleteEvent(type: String?, id: String?, position: Int)

    fun onEditAndDelete(data: SchoolEventItem, anchorView: View, adapterPosition: Int)

}
