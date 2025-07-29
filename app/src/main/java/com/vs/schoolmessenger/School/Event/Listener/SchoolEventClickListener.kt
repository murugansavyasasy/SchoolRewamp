package com.vs.schoolmessenger.School.Event.Listener

import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.Category

interface SchoolEventClickListener {
    fun onSearchResultEmpty(isEmpty1: String, isEmpty: Boolean)

    fun onDeleteEvent(type: String?, id: String?, position: Int)

}
