package com.vs.schoolmessenger.School.LSRW.Listener

import android.view.View
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask


interface lsrwskillreportlistener {

    fun onEditAndDeleteCompleted(data: LsrwTask, anchorView: View, adapterPosition: Int,source: String)

}