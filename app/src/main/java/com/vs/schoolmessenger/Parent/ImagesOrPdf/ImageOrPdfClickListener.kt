package com.vs.schoolmessenger.Parent.ImagesOrPdf

import com.vs.schoolmessenger.School.Homework.HomeWorkReport

interface ImageOrPdfClickListener {
    fun onItemImageClick(data: ImageOrPdfData)
    fun onItemPDFClick(data: ImageOrPdfData)
}