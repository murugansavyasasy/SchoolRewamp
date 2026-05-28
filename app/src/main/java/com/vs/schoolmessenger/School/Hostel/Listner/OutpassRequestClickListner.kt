package com.vs.schoolmessenger.School.Hostel.Listner

import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.OutpassRequestWiseData

interface OutpassRequestClickListner {

    fun onApproveClicked(
        data: OutpassRequestWiseData,
        position: Int,
        isButtonClick: Boolean,
        resultCallback: (Boolean) -> Unit
    )
}