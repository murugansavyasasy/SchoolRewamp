package com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList

import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds

interface StandardListClickListener {
    fun onIdCheck(standard: Standard)
    fun onIdUnchecked(standard: Standard)
}