package com.vs.schoolmessenger.Parent.Homework

interface HomeWorkItemClick {
    fun onItemTextClick(data: HomeWorkList)
    fun onItemImageClick(data: HomeWorkList)
    fun onItemPDFClick(data: HomeWorkList)
    fun onItemVoiceClick(data: HomeWorkList)
    fun onItemVideoClick(data: HomeWorkList)
}