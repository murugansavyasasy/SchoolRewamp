package com.vs.schoolmessenger.CommonScreens.GroupList

import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds

interface GroupListClickListener {
    fun onGroupClick(group: NameAndIds)
}
