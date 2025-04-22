package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYearResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIdsResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.StaffListResponse
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardResponse
import com.vs.schoolmessenger.Parent.Communication.StatusArchiveResponse
import com.vs.schoolmessenger.Parent.Communication.VoiceData
import com.vs.schoolmessenger.Parent.Communication.VoiceDataResponse
import com.vs.schoolmessenger.School.Communication.TextDetailsResponse
import com.vs.schoolmessenger.School.Communication.TextSendResponse
import com.vs.schoolmessenger.School.Communication.VoiceDetails
import com.vs.schoolmessenger.School.MarkYourAttendance.LocationHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.PunchHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.StaffAttendanceReportResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.StaffLocationResponse

class App(application: Application) : AndroidViewModel(application) {

    private var apiRepositories: AppServices = AppServices() // ✅ Initialize here


    var isDashBoardData: LiveData<DashboardResponse?>? = null
        private set

    var isGetAds: LiveData<AdsResponse?>? = null
        private set

    var isGetStaffList: LiveData<NameAndIdsResponse?>? = null
        private set

    var isGetSubjectList: LiveData<NameAndIdsResponse?>? = null
        private set

    var isStandardSectionList: LiveData<StandardResponse?>? = null
        private set

    var isStudentList: LiveData<NameAndIdsResponse?>? = null

    var isGetGroupList: LiveData<NameAndIdsResponse?>? = null
        private set

    var isGetCommmunicationlist: LiveData<VoiceDataResponse?>? = null
        private set

    var isGetCommmunicationlistload: LiveData<VoiceDataResponse?>? = null
        private set



    var isGetVoiceHistory: LiveData<VoiceDetails?>? = null
        private set

    var isGetTextHistory: LiveData<TextDetailsResponse?>? = null
        private set

    var isSendText: LiveData<TextSendResponse?>? = null
        private set

    var isVoiceSend: LiveData<TextSendResponse?>? = null
        private set


    var isUpdateStatusArchive: LiveData<StatusArchiveResponse?>? = null
        private set

    var isGetAcademicList: LiveData<AcademicYearResponse?>? = null
    var isUpdateStatusCommunication: LiveData<StatusArchiveResponse?>? = null
        private set

    var isPunchAttendance: LiveData<StatusMessageModel?>? = null
    var isAddLocation: LiveData<StatusMessageModel?>? = null
    var isRemoveLocation: LiveData<StatusMessageModel?>? = null
    var isUpdateLocation: LiveData<StatusMessageModel?>? = null
    var isLocationHistory: LiveData<LocationHistoryResponse?>? = null
    var isStaffLocations: LiveData<StaffLocationResponse?>? = null
    var isPunchHistory: LiveData<PunchHistoryResponse?>? = null
    var isStaffAttendanceReport: LiveData<StaffAttendanceReportResponse?>? = null
    var isStaffWiseAttendanceReport: LiveData<StaffAttendanceReportResponse?>? = null




    fun init() {
        isDashBoardData = apiRepositories!!.isDashBoardLiveData
        isGetAds = apiRepositories!!.isGetAdsLiveData
        isGetStaffList = apiRepositories!!.isGetStaffListLiveData
        isGetSubjectList = apiRepositories!!.isSubjectListLiveData
        isStandardSectionList = apiRepositories!!.isGetStandardSectionLiveData
        isStudentList = apiRepositories!!.isStudentLiveData
        isGetGroupList = apiRepositories!!.isGetGroupLiveData
        isGetCommmunicationlist = apiRepositories!!.isGetCommunicationLiveData
        isGetCommmunicationlistload = apiRepositories!!.isGetCommunicationloadLiveData
        isGetVoiceHistory = apiRepositories!!.isGetVoiceHistoryLiveData
        isGetTextHistory = apiRepositories!!.isGetTextHistoryLiveData
        isSendText = apiRepositories!!.isSendTextLiveData
        isVoiceSend = apiRepositories!!.isSendVoiceLiveData
        isUpdateStatusArchive = apiRepositories!!.isUpdateStatusArchiveLiveData
        isGetAcademicList = apiRepositories!!.isGetAcademicLiveData
        isUpdateStatusCommunication = apiRepositories!!.isUpdateStatusCommunicationLiveData

        isPunchAttendance = apiRepositories!!.isPunchAttendanceLiveData
        isAddLocation = apiRepositories!!.isAddLocationLiveData
        isRemoveLocation = apiRepositories!!.isRemoveLocationLiveData
        isUpdateLocation = apiRepositories!!.isUpdateLocationLiveData
        isLocationHistory = apiRepositories!!.isLocationHistoryLiveData
        isStaffLocations = apiRepositories!!.isStaffLocationsLiveData
        isPunchHistory = apiRepositories!!.isPunchHistoryLiveData
        isStaffAttendanceReport = apiRepositories!!.isGiometricStaffAttendanceReportLiveData
        isStaffWiseAttendanceReport = apiRepositories!!.isGiometricStaffWiseAttendanceReportLiveData

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

    fun isGetSubjectList(isToken: String, isAcademicYearId: Int, isSectionId: String, activity: Activity) {
        apiRepositories.isGetSubjectList(isToken,isAcademicYearId,isSectionId, activity)
    }


    fun isGetStandardSection(isToken: String, isAcademicYearId: Int, activity: Activity) {
        apiRepositories.isGetStandardSection(isToken,isAcademicYearId, activity)
    }

    fun isGetStudentList(isToken: String, isSection: String,isAcademicYearId: Int, activity: Activity) {
        apiRepositories.isGetStudentList(isToken,isSection,isAcademicYearId, activity)
    }
    fun isGetGroupList(isToken: String, isAcademicYearId: Int, activity: Activity) {
        apiRepositories!!.isGetGroupList(isToken,isAcademicYearId, activity)
    }

    fun isGetCommmunicationlistload(isToken: String, activity: Activity) {
        apiRepositories!!.isGetCommmunicationlistload(isToken, activity)
    }


    fun isGetCommmunicationlist(isToken: String, activity: Activity) {
        apiRepositories!!.isGetCommmunicationlist(isToken, activity)
    }

    fun isGetTextHistory(isToken: String, activity: Activity) {
        apiRepositories!!.isGetTextHistory(isToken, activity)
    }

    fun isGetVoiceHistory(isToken: String, isEmergency: String, activity: Activity) {
        apiRepositories!!.isGetVoiceHistory(isToken, isEmergency, activity)
    }

    fun isSendText(isToken: String, josnObject: JsonObject, activity: Activity) {
        apiRepositories!!.isSendText(isToken, josnObject, activity)
    }

    fun isVoiceSend(isToken: String, josnObject: JsonObject, activity: Activity) {
        apiRepositories!!.isSendVoice(isToken, josnObject, activity)
    }

    fun isUpdateStatusArchive(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.isUpdateStatusArchive(isToken, jsonObject, activity)
    }

    fun isGetAcademicYear(isToken: String, activity: Activity) {
        apiRepositories?.isGetAcademicYear(isToken, activity)
    }




    fun isUpdateStatusCommunication(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.isUpdateStatusCommunication(isToken, jsonObject, activity)
    }

    fun punchAttendance(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.punchAttendance(isToken, jsonObject, activity)
    }

    fun addLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.addLocation(isToken, jsonObject, activity)
    }

    fun removeLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.removeLocation(isToken, jsonObject, activity)
    }

    fun updateLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.updateLocation(isToken, jsonObject, activity)
    }

    fun getStaffLocations(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.getStaffLocations(isToken, activity)
    }

    fun getPunchHistory(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.getPunchHistory(isToken, activity)
    }

    fun getLocationHistory(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.getLocationHistory(isToken, activity)
    }

    fun getStaffAttendanceReport(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.getGiometricStaffAttendancereport(isToken, activity)
    }
    fun getStaffWiseAttendanceReport(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiRepositories?.getGiometricStaffWiseAttendancereport(isToken, activity)
    }


}

