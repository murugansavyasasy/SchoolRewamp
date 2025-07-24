package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model

import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.Category

interface EventClickListener {
    fun onSearchResultEmpty(isEmpty: Boolean)
    fun onCategoryClicked(data: Category)

}
