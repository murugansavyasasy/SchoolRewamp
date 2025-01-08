package com.vs.schoolmessenger.Parent.Communication


interface TextClickListener{
    fun onItemClick(data: TextData, holder: TextAdapter.DataViewHolder)
}