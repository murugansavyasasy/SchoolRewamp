package com.vs.schoolmessenger.CommonScreens.SpecificStudent

import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds

interface SpecificStudentSelectClickListener {
    fun onIdCheck(data: NameAndIds)
    fun onIdUnchecked(data: NameAndIds)
}