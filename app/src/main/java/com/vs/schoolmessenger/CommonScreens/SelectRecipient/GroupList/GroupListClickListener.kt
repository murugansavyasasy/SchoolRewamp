package com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList

import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds

interface GroupListClickListener {
    fun onIdCheck(group: NameAndIds)
    fun onIdUnchecked(group: NameAndIds)
}