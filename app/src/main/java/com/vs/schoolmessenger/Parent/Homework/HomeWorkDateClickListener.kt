package com.vs.schoolmessenger.Parent.Homework

import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails

interface HomeWorkDateClickListener {
    fun onItemClick(data: GetHomeworkDetails,isHomeWorkDate: String)
}