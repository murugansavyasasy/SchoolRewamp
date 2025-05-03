package com.vs.schoolmessenger.School.Communication.Interface

import com.vs.schoolmessenger.School.Communication.DataClass.TextDetail
import com.vs.schoolmessenger.School.Communication.Adapter.TextHistoryAdapter

interface TextHistoryClickListener {
    fun onItemClick(data: TextDetail, holder: TextHistoryAdapter.DataViewHolder)

}