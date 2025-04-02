package com.vs.schoolmessenger.CommonScreens.SchoolList

import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails

interface SchoolListClickListener {
    fun onItemClick(data: StaffDetails)
}