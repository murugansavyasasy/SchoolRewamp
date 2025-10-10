package com.vs.schoolmessenger.School.InteractionWithStudent.Response

import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.Staff
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData

interface InteractionWithStudentListener {

    fun onSearchResultEmpty(isEmpty: Boolean)

    fun onClickItem(data: StudentChatData)

    fun onReadStatusClick(data: StudentChatData, isPosition: Int)

}
