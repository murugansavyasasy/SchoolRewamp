package com.vs.schoolmessenger.Parent.Homework

import com.vs.schoolmessenger.Parent.Communication.TextAdapter
import com.vs.schoolmessenger.Parent.Communication.TextData

interface HomeWorkDateClickListener {
    fun onItemClick(data: TextData, holder: HomeWorkAdapter.DataViewHolder)
}