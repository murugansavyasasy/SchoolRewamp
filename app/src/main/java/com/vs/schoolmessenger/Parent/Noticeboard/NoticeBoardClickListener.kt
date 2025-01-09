package com.vs.schoolmessenger.Parent.Noticeboard

import com.vs.schoolmessenger.Parent.EventsHolidays.EventData

interface NoticeBoardClickListener {
    fun onItemImageClick(data: NoticeBoardData)
    fun onItemPDFClick(data: NoticeBoardData)
}