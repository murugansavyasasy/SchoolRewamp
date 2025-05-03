package com.vs.schoolmessenger.Parent.Homework

import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter

interface HomeWorkDateClickListener {
    fun onItemClick(data: HomeWorkDateData, holder: HomeWorkAdapter.DataViewHolder)
}