package com.vs.schoolmessenger.School.InteractionWithStudent.Response

import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData

interface InteractionWithStudentListener {

    fun onSearchResultEmpty(isEmpty: Boolean)

    fun onClickItem(data: StudentChatData)
}
