package com.vs.schoolmessenger.Parent.BusTracking

import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.BusListData


interface BusClickListner {

    fun OnBusClick(data: BusListData)
}