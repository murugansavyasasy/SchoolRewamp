package com.vs.schoolmessenger.Parent.LSRW

import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesStudentFooterData
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesStudentFooterListAdapter


interface LSRWClickListener {

    fun onItemClick(data: LSRWData, holder: LSRWAdapter.DataViewHolder)
}
