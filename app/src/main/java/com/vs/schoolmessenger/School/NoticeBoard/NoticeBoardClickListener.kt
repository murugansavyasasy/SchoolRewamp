package com.vs.schoolmessenger.School.NoticeBoard

import android.view.View
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData

interface NoticeBoardClickListener {
//    fun onClickListener(data: CreateNoticeBoard)
    fun onClickListener(data: NoticeStaffData, anchorView: View, adapterPosition: Int)
    fun onSearchResultEmpty(isEmpty: Boolean)



//    fun onSearchResultEmpty(isEmpty: Boolean)
//
//    fun onDeleteNotice(type: String?, id: String?, position: Int)

}