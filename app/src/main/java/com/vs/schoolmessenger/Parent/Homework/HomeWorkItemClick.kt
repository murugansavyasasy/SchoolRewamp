package com.vs.schoolmessenger.Parent.Homework

import com.vs.schoolmessenger.School.Homework.HomeWorkReport

interface HomeWorkItemClick {
    fun onItemTextClick(data: HomeWorkList)
    fun onItemImageClick(data: HomeWorkList)
    fun onItemPDFClick(data: HomeWorkList)
    fun onItemVoiceClick(data: HomeWorkList)
    fun onItemVideoClick(data: HomeWorkList)
}