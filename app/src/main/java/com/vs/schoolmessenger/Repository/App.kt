package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.GroupList.GroupListResponse

class App(application: Application) : AndroidViewModel(application) {

    private var apiRepositories: AppServices = AppServices() // ✅ Initialize here


    var isDashBoardData: LiveData<DashboardResponse?>? = null
        private set

    var isGetAds: LiveData<AdsResponse?>? = null
        private set


    var isGetGroupList: LiveData<GroupListResponse?>? = null
        private set

    fun init() {
        isDashBoardData = apiRepositories!!.isDashBoardLiveData
        isGetAds = apiRepositories!!.isGetAdsLiveData
        isGetGroupList = apiRepositories!!.isGetGroupLiveData
    }

    fun isDashBoardData(isToken: String, isMemberType: String, activity: Activity) {
        apiRepositories!!.isDashBoard(isToken, isMemberType, activity)
    }

    fun isGetMenuId(isToken: String, isMenuId: String, activity: Activity) {
        apiRepositories!!.isGetAds(isToken, isMenuId, activity)
    }

    fun isGetGroupList(isToken: String, activity: Activity) {
        apiRepositories!!.isGetGroupList(isToken, activity)
    }

}