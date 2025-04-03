package com.vs.schoolmessenger.CommonScreens.SelectRecipient

import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails

interface SchoolClickListener {
    fun onItemClick(data: StaffDetails)
}