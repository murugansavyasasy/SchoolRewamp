package com.vs.schoolmessenger.Parent.InteractionWithStaff.Listener

import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.Staff

interface InteractionWithStaffListener {
    fun onSearchResultEmpty(isEmpty: Boolean)

    fun onClickItem(data: Staff)

    fun onReadStatusClick(data: Staff, isPosition: Int)

}