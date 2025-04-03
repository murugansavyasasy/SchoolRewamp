package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.StaffListResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIdsResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.StandardResponse
import com.vs.schoolmessenger.CommonScreens.GroupList.GroupListResponse

class App(application: Application) : AndroidViewModel(application) {

    private var apiRepositories: AppServices = AppServices() // ✅ Initialize here


    var isDashBoardData: LiveData<DashboardResponse?>? = null
        private set

    var isGetAds: LiveData<AdsResponse?>? = null
        private set

    var isGetStaffList: LiveData<StaffListResponse?>? = null
        private set

    var isGetSubjectList: LiveData<NameAndIdsResponse?>? = null
        private set

    var isStandardSectionList: LiveData<StandardResponse?>? = null
        private set

    var isStudentList: LiveData<NameAndIdsResponse?>? = null

    var isGetGroupList: LiveData<GroupListResponse?>? = null
        private set

    fun init() {
        isDashBoardData = apiRepositories!!.isDashBoardLiveData
        isGetAds = apiRepositories!!.isGetAdsLiveData
        isGetStaffList = apiRepositories!!.isGetStaffListLiveData
        isGetSubjectList = apiRepositories!!.isSubjectListLiveData
        isStandardSectionList = apiRepositories!!.isGetStandardSectionLiveData
        isStudentList = apiRepositories!!.isStudentLiveData
        isGetGroupList = apiRepositories!!.isGetGroupLiveData
    }

    fun isDashBoardData(isToken: String, isMemberType: String, activity: Activity) {
        apiRepositories!!.isDashBoard(isToken, isMemberType, activity)
    }

    fun isGetAds(isToken: String, isMenuId: String, activity: Activity) {
        apiRepositories!!.isGetAds(isToken, isMenuId, activity)
    }

    fun isGetStaffList(isToken: String, activity: Activity) {
        apiRepositories!!.isGetStaffList(isToken, activity)
    }

    fun isGetSubjectList(isToken: String, activity: Activity) {
        apiRepositories.isGetSubjectList(isToken, activity)
    }


    fun isGetStandardSection(isToken: String, isSection: String, activity: Activity) {
        apiRepositories.isGetStandardSection(isToken,isSection, activity)
    }

    fun isGetStudentList(isToken: String, isSection: String, activity: Activity) {
        apiRepositories.isGetStudentList(isToken,isSection, activity)
    }
    fun isGetGroupList(isToken: String, activity: Activity) {
        apiRepositories!!.isGetGroupList(isToken, activity)
    }
}