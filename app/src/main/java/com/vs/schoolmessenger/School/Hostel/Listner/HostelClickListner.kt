package com.vs.schoolmessenger.School.Hostel.Listner

import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility.getRoomAvailability
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.getHostelListData


interface HostelClickListner {
    fun onSearchResultEmpty(isEmpty: Boolean)
    fun onHostelClick(data: getHostelListData)
    fun onRoomClick(data: getRoomAvailability)

}