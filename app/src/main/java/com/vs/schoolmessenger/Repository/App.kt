package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.vs.schoolmessenger.Dashboard.School.DashboardResponse

class App(application: Application) : AndroidViewModel(application) {

    private var apiRepositories: AppServices = AppServices() // ✅ Initialize here



    var isDashBoardData: LiveData<DashboardResponse?>? = null
        private set

    fun init() {
        isDashBoardData = apiRepositories!!.isDashBoardLiveData
    }

    fun isDashBoardData(isToken:String,isMemberType: String, activity: Activity) {
        apiRepositories!!.isDashBoard(isToken,isMemberType,activity)
    }

}