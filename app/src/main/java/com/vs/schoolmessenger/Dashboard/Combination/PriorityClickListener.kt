package com.vs.schoolmessenger.Dashboard.Combination

import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails

interface PriorityClickListener {
    fun onItemClick(data: ChildDetails)
}