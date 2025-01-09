package com.vs.schoolmessenger.Parent.EventsHolidays

import com.vs.schoolmessenger.Parent.ImagesOrPdf.ImageOrPdfData

interface EventClickListener {
    fun onItemImageClick(data: EventData)
    fun onItemPDFClick(data: EventData)
}