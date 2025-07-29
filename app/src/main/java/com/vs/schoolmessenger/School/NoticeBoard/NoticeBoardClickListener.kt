package com.vs.schoolmessenger.School.NoticeBoard

interface NoticeBoardClickListener {
    fun onClickListener(data: CreateNoticeBoard)

    fun onSearchResultEmpty(isEmpty: Boolean)

    fun onDeleteNotice(type: String?, id: String?, position: Int)

}