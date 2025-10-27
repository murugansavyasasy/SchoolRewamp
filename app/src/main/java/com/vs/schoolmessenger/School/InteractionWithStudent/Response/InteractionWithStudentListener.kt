package com.vs.schoolmessenger.School.InteractionWithStudent.Response

import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.Staff
import com.vs.schoolmessenger.School.AbsenteesReport.Model.Student
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.BlockedStudent
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData

interface InteractionWithStudentListener {

    fun onSearchResultEmpty(isEmpty: Boolean)

    fun onClickItem(data: StudentChatData)

    fun onReadStatusClick(data: StudentChatData, isPosition: Int)
    fun onBlockedSearchResultEmpty(isEmpty: Boolean)
    fun onUnblockClick(data: BlockedStudent, isPosition: Int)





}
