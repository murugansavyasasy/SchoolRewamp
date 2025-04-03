package com.vs.schoolmessenger.CommonScreens.GroupList

data class GroupListResponse (
    val status: Boolean,
    val message: String,
    val data: List<GroupListItem>
)