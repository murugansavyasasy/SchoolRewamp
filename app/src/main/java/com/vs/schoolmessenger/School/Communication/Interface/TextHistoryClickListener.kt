package com.vs.schoolmessenger.School.Communication.Interface

import com.vs.schoolmessenger.School.Communication.Adapter.TextHistoryAdapter
import com.vs.schoolmessenger.School.Communication.DataClass.TextDetail

interface TextHistoryClickListener {
    fun onItemClick(data: TextDetail, holder: TextHistoryAdapter.DataViewHolder)

}