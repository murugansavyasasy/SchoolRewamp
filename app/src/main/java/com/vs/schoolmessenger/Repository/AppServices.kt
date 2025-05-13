package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYearResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIdsResponse
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardResponse
import com.vs.schoolmessenger.Parent.Communication.StatusArchiveResponse
import com.vs.schoolmessenger.Parent.Communication.VoiceDataResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextDetailsResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendResponse
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceDetails
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkData
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionReportResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportApiResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkSendResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.LocationHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffLocationResponse
import com.vs.schoolmessenger.School.SchoolStrength.SchoolStrengthResponse
import com.vs.schoolmessenger.Utils.SharedPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppServices {

    var client_auth: RestClient
    var isDashBoard: MutableLiveData<DashboardResponse?>
    var isGetAds: MutableLiveData<AdsResponse?>
    var isGetStaffList: MutableLiveData<NameAndIdsResponse?>
    var isGetSubjectList: MutableLiveData<NameAndIdsResponse?>
    var isGetStandardSection: MutableLiveData<StandardResponse?>
    var isGetStudentList: MutableLiveData<NameAndIdsResponse?>
    var isGetGroupList: MutableLiveData<NameAndIdsResponse?>
    var isGetCommmunicationlist: MutableLiveData<VoiceDataResponse?>
    var isGetCommmunicationlistload: MutableLiveData<VoiceDataResponse?>
    var isGetVoiceHistory: MutableLiveData<VoiceDetails?>
    var isGetTextHistory: MutableLiveData<TextDetailsResponse?>
    var isGetHomeWorkReport: MutableLiveData<HomeWorkReportApiResponse?>
    var isGetDailyCollectionReport: MutableLiveData<DailyCollectionReportResponse?>
    var isSendText: MutableLiveData<TextSendResponse?>
    var isSendHomeWork: MutableLiveData<HomeWorkSendResponse?>
    var isSendVoice: MutableLiveData<TextSendResponse?>
    var isUpdateStatusArchive: MutableLiveData<StatusArchiveResponse?>
    var isAcademicYear: MutableLiveData<AcademicYearResponse?>
    var isUpdateStatusCommunication: MutableLiveData<StatusArchiveResponse?>
    var isHomeWorkDetailsData: MutableLiveData<GetHomeworkData?>

    var isGetSchoolStrengthReport: MutableLiveData<SchoolStrengthResponse?>


    var isPunchAttendance: MutableLiveData<StatusMessageModel?>
    var isAddLocation: MutableLiveData<StatusMessageModel?>
    var isRemoveLocation: MutableLiveData<StatusMessageModel?>
    var isUpdateLocation: MutableLiveData<StatusMessageModel?>
    var isLocationHistory: MutableLiveData<LocationHistoryResponse?>
    var isStaffLocations: MutableLiveData<StaffLocationResponse?>
    var isPunchHistory: MutableLiveData<PunchHistoryResponse?>
    var isStaffAttendanceReport: MutableLiveData<StaffAttendanceReportResponse?>
    var isStaffWiseAttendanceReport: MutableLiveData<StaffAttendanceReportResponse?>
    var isStaffWiseAttendanceReportList: MutableLiveData<StaffAttendanceReportResponse?>


    init {
        client_auth = RestClient()
        isDashBoard = MutableLiveData()
        isGetAds = MutableLiveData()
        isGetStaffList = MutableLiveData()
        isGetSubjectList = MutableLiveData()
        isGetStandardSection = MutableLiveData()
        isGetStudentList = MutableLiveData()
        isGetGroupList = MutableLiveData()
        isGetCommmunicationlist = MutableLiveData()
        isGetCommmunicationlistload = MutableLiveData()
        isGetVoiceHistory = MutableLiveData()
        isGetTextHistory = MutableLiveData()
        isGetHomeWorkReport = MutableLiveData()
        isGetDailyCollectionReport = MutableLiveData()
        isSendText = MutableLiveData()
        isSendHomeWork = MutableLiveData()
        isSendVoice = MutableLiveData()
        isUpdateStatusArchive = MutableLiveData()
        isAcademicYear = MutableLiveData()
        isUpdateStatusCommunication = MutableLiveData()
        isHomeWorkDetailsData = MutableLiveData()

        isGetSchoolStrengthReport = MutableLiveData()
        isPunchAttendance = MutableLiveData()
        isAddLocation = MutableLiveData()
        isRemoveLocation = MutableLiveData()
        isUpdateLocation = MutableLiveData()
        isLocationHistory = MutableLiveData()
        isStaffLocations = MutableLiveData()
        isPunchHistory = MutableLiveData()
        isStaffAttendanceReport = MutableLiveData()
        isStaffWiseAttendanceReport = MutableLiveData()
        isStaffWiseAttendanceReportList = MutableLiveData()

    }


    fun isDashBoard(isToken: String, isMemberType: String, activity: Activity) {
        RestClient.apiInterfaces.isDashBoard(isToken, isMemberType)
            ?.enqueue(object : Callback<DashboardResponse?> {
                override fun onResponse(
                    call: Call<DashboardResponse?>, response: Response<DashboardResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isDashBoard.postValue(response.body())
                            } else {
                                isDashBoard.postValue(response.body())
                            }
                        }
                    } else {


                    }
                }

                override fun onFailure(call: Call<DashboardResponse?>, t: Throwable) {
                    isDashBoard.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isDashBoardLiveData: LiveData<DashboardResponse?>
        get() = isDashBoard


    fun isGetAds(isToken: String, isMenuId: String, activity: Activity) {
        RestClient.apiInterfaces.isGetAds(isToken, isMenuId)
            ?.enqueue(object : Callback<AdsResponse?> {
                override fun onResponse(
                    call: Call<AdsResponse?>, response: Response<AdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetAds.postValue(response.body())
                            } else {
                                isGetAds.postValue(response.body())
                            }
                        }
                    } else {
                    }
                }

                override fun onFailure(call: Call<AdsResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetAdsLiveData: LiveData<AdsResponse?>
        get() = isGetAds

    fun isGetStaffList(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.getStaffList(isToken)
            ?.enqueue(object : Callback<NameAndIdsResponse?> {
                override fun onResponse(
                    call: Call<NameAndIdsResponse?>, response: Response<NameAndIdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetStaffList.postValue(response.body())
                            } else {
                                isGetStaffList.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<NameAndIdsResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetStaffListLiveData: LiveData<NameAndIdsResponse?>
        get() = isGetStaffList

    fun isGetSubjectList(
        isToken: String, isAcademicYearId: Int, isSection: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getSubjectList(isToken, isAcademicYearId, isSection)
            ?.enqueue(object : Callback<NameAndIdsResponse?> {
                override fun onResponse(
                    call: Call<NameAndIdsResponse?>, response: Response<NameAndIdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetSubjectList.postValue(response.body())
                            } else {
                                isGetSubjectList.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<NameAndIdsResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSubjectListLiveData: LiveData<NameAndIdsResponse?>
        get() = isGetSubjectList


    fun isGetStandardSection(isToken: String, isAcademicYearId: Int, activity: Activity) {
        RestClient.apiInterfaces.getStandard(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<StandardResponse?> {
                override fun onResponse(
                    call: Call<StandardResponse?>, response: Response<StandardResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetStandardSection.postValue(response.body())
                            } else {
                                isGetStandardSection.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<StandardResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetStandardSectionLiveData: LiveData<StandardResponse?>
        get() = isGetStandardSection


    fun isGetStudentList(
        isToken: String, isSection: String, isAcademicYearId: Int, activity: Activity
    ) {
        RestClient.apiInterfaces.getStudentList(isToken, isSection, isAcademicYearId)
            ?.enqueue(object : Callback<NameAndIdsResponse?> {
                override fun onResponse(
                    call: Call<NameAndIdsResponse?>, response: Response<NameAndIdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetStudentList.postValue(response.body())
                            } else {
                                isGetStudentList.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<NameAndIdsResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isStudentLiveData: LiveData<NameAndIdsResponse?>
        get() = isGetStudentList


    fun isGetCommmunicationlist(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetCommmunicationlist(isToken)
            ?.enqueue(object : Callback<VoiceDataResponse?> {
                override fun onResponse(
                    call: Call<VoiceDataResponse?>, response: Response<VoiceDataResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetCommmunicationlist.postValue(response.body())
                            } else {
                                isGetCommmunicationlist.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<VoiceDataResponse?>, t: Throwable) {
                    isGetCommmunicationlist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetCommunicationLiveData: LiveData<VoiceDataResponse?>
        get() = isGetCommmunicationlist

    fun isGetCommmunicationlistload(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetCommmunicationlistload(isToken)
            ?.enqueue(object : Callback<VoiceDataResponse?> {
                override fun onResponse(
                    call: Call<VoiceDataResponse?>, response: Response<VoiceDataResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetCommmunicationlistload.postValue(response.body())
                            } else {
                                isGetCommmunicationlistload.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<VoiceDataResponse?>, t: Throwable) {
                    isGetCommmunicationlistload.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetCommunicationloadLiveData: LiveData<VoiceDataResponse?>
        get() = isGetCommmunicationlistload


    fun isGetGroupList(isToken: String, isAcademicYearId: Int, activity: Activity) {
        RestClient.apiInterfaces.isGroupList(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<NameAndIdsResponse?> {
                override fun onResponse(
                    call: Call<NameAndIdsResponse?>, response: Response<NameAndIdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetGroupList.postValue(response.body())
                            } else {
                                isGetGroupList.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<NameAndIdsResponse?>, t: Throwable) {
                    isGetGroupList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetGroupLiveData: LiveData<NameAndIdsResponse?>
        get() = isGetGroupList


    fun isGetVoiceHistory(isToken: String, isEmergency: String, activity: Activity) {
        RestClient.apiInterfaces.isGetVoiceHistory(isToken, isEmergency)
            ?.enqueue(object : Callback<VoiceDetails?> {
                override fun onResponse(
                    call: Call<VoiceDetails?>, response: Response<VoiceDetails?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetVoiceHistory.postValue(response.body())
                            } else {
                                isGetVoiceHistory.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<VoiceDetails?>, t: Throwable) {
                    isGetVoiceHistory.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetVoiceHistoryLiveData: LiveData<VoiceDetails?>
        get() = isGetVoiceHistory

    fun isGetTextHistory(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetTextHistory(isToken)
            ?.enqueue(object : Callback<TextDetailsResponse?> {
                override fun onResponse(
                    call: Call<TextDetailsResponse?>, response: Response<TextDetailsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetTextHistory.postValue(response.body())
                            } else {
                                isGetTextHistory.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<TextDetailsResponse?>, t: Throwable) {
                    isGetTextHistory.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetTextHistoryLiveData: LiveData<TextDetailsResponse?>
        get() = isGetTextHistory


    fun isGetHomeWorkReport(
        isToken: String, isSection: Int, isAcademicYearId: Int, isdate: String, activity: Activity
    ) {
        RestClient.apiInterfaces.isGetHomeWorkReport(isToken, isSection, isAcademicYearId, isdate)
            ?.enqueue(object : Callback<HomeWorkReportApiResponse?> {
                override fun onResponse(
                    call: Call<HomeWorkReportApiResponse?>,
                    response: Response<HomeWorkReportApiResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetHomeWorkReport.postValue(response.body())
                            } else {
                                isGetHomeWorkReport.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<HomeWorkReportApiResponse?>, t: Throwable) {
                    isGetHomeWorkReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isGetHomeWorkReportLiveData: LiveData<HomeWorkReportApiResponse?>
        get() = isGetHomeWorkReport


    fun isGetDailyCollectionReport(
        isToken: String, istype: String, isfromdate: String, istodate: String, activity: Activity
    ) {
        RestClient.apiInterfaces.isGetDailyCollectionReport(isToken, istype, isfromdate, istodate)
            ?.enqueue(object : Callback<DailyCollectionReportResponse?> {
                override fun onResponse(
                    call: Call<DailyCollectionReportResponse?>,
                    response: Response<DailyCollectionReportResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetDailyCollectionReport.postValue(response.body())
                            } else {
                                isGetDailyCollectionReport.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<DailyCollectionReportResponse?>, t: Throwable) {
                    isGetDailyCollectionReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isGetDailyCollectionReportLiveData: LiveData<DailyCollectionReportResponse?>
        get() = isGetDailyCollectionReport


    fun isGetSchoolStrengthReport(isToken: String, isAcademicYearId: Int, activity: Activity) {
        RestClient.apiInterfaces.isGetSchoolStrengthReport(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<SchoolStrengthResponse?> {
                override fun onResponse(
                    call: Call<SchoolStrengthResponse?>, response: Response<SchoolStrengthResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetSchoolStrengthReport.postValue(response.body())
                            } else {
                                isGetSchoolStrengthReport.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<SchoolStrengthResponse?>, t: Throwable) {
                    isGetSchoolStrengthReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isGetSchoolStrengthReportLiveData: LiveData<SchoolStrengthResponse?>
        get() = isGetSchoolStrengthReport


    fun isSendText(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.isSendText(isToken, jsonObject)
            ?.enqueue(object : Callback<TextSendResponse?> {
                override fun onResponse(
                    call: Call<TextSendResponse?>, response: Response<TextSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isSendText.postValue(response.body())
                    } else {
                        isSendText.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<TextSendResponse?>, t: Throwable) {
                    isSendText.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isSendTextLiveData: LiveData<TextSendResponse?>
        get() = isSendText


    fun isSendHomeWork(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.isSendHomeWork(isToken, jsonObject)
            ?.enqueue(object : Callback<HomeWorkSendResponse?> {
                override fun onResponse(
                    call: Call<HomeWorkSendResponse?>, response: Response<HomeWorkSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isSendHomeWork.postValue(response.body())
                    } else {
                        isSendHomeWork.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<HomeWorkSendResponse?>, t: Throwable) {
                    isSendHomeWork.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isSendHomeWorkLiveData: LiveData<HomeWorkSendResponse?>
        get() = isSendHomeWork


    fun isUpdateStatusArchive(isToken: String, jsonObject: JsonObject, activity: Activity) {
//        val request = StatusArchiveModelRequest(isToken, jsonObject)

        RestClient.apiInterfaces.isUpdateStatusArchive(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusArchiveResponse> {
                override fun onResponse(
                    call: Call<StatusArchiveResponse>, response: Response<StatusArchiveResponse>
                ) {
                    Log.d("isUpdateStatusArchive", "${response.code()} - ${response.body()}")

                    if (response.code() == 200 && response.body() != null) {
                        isUpdateStatusArchive.postValue(response.body())
                    } else {
                        isUpdateStatusArchive.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<StatusArchiveResponse>, t: Throwable) {
                    isUpdateStatusArchive.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isUpdateStatusArchiveLiveData: LiveData<StatusArchiveResponse?>
        get() = isUpdateStatusArchive


    fun isUpdateStatusCommunication(isToken: String, jsonObject: JsonObject, activity: Activity) {
//        val request = StatusArchiveModelRequest(isToken, jsonObject)

        RestClient.apiInterfaces.isUpdateStatusCommunication(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusArchiveResponse> {
                override fun onResponse(
                    call: Call<StatusArchiveResponse>, response: Response<StatusArchiveResponse>
                ) {
                    Log.d("isUpdateStatusArchive", "${response.code()} - ${response.body()}")

                    if (response.code() == 200 && response.body() != null) {
                        isUpdateStatusCommunication.postValue(response.body())
                    } else {
                        isUpdateStatusCommunication.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<StatusArchiveResponse>, t: Throwable) {
                    isUpdateStatusCommunication.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isUpdateStatusCommunicationLiveData: LiveData<StatusArchiveResponse?>
        get() = isUpdateStatusCommunication


    fun isSendVoice(isToken: String, jsonObject: JsonObject, activity: Activity) {

        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.isSendVoice(isToken, jsonObject)
            ?.enqueue(object : Callback<TextSendResponse?> {
                override fun onResponse(
                    call: Call<TextSendResponse?>, response: Response<TextSendResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    isSendVoice.postValue(response.body())
//                    if (response.code() == 200) {
//                        if (response.body() != null) {
//                            val status = response.body()!!.status
//                            if (status) {
//
//                            } else {
//                                isSendVoice.postValue(response.body())
//                            }
//                        }
//                    } else {
//
//                    }
                }

                override fun onFailure(call: Call<TextSendResponse?>, t: Throwable) {
                    isSendVoice.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSendVoiceLiveData: LiveData<TextSendResponse?>
        get() = isSendVoice


    fun isGetAcademicYear(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetAcademicYear(isToken)
            ?.enqueue(object : Callback<AcademicYearResponse?> {
                override fun onResponse(
                    call: Call<AcademicYearResponse?>, response: Response<AcademicYearResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isAcademicYear.postValue(response.body())
                            } else {
                                isAcademicYear.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<AcademicYearResponse?>, t: Throwable) {
                    isAcademicYear.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetAcademicLiveData: LiveData<AcademicYearResponse?>
        get() = isAcademicYear


    //    //get HomeworkDetails
    fun isHomeWorkDetails(isToken: String, activity: Activity) {
        Log.d("GetHomeworkData", isToken.toString())

        RestClient.apiInterfaces.isHomeWorkDetails(isToken)
            ?.enqueue(object : Callback<GetHomeworkData?> {
                override fun onResponse(
                    call: Call<GetHomeworkData?>, response: Response<GetHomeworkData?>
                ) {
                    Log.d("GetHomeworkData", response.body().toString())

                    if (response.code() == 200) {
                        if (response.body() != null) {
                            isHomeWorkDetailsData.postValue(response.body())
                            Log.d("GetHomeworkDataRespone", response.body().toString())

                        }
                    }
                }

                override fun onFailure(call: Call<GetHomeworkData?>, t: Throwable) {
                    t.printStackTrace()
                    isHomeWorkDetailsData.postValue(null)
                    Log.d("GetHomeworkData", "Response,No Data Found")
                }
            })
    }

    val isHomeWorkDetailsLiveData: LiveData<GetHomeworkData?>
        get() = isHomeWorkDetailsData

    fun punchAttendance(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.punchGiometricAttendance(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            isPunchAttendance.postValue(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isPunchAttendance.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isPunchAttendanceLiveData: LiveData<StatusMessageModel?>
        get() = isPunchAttendance


    fun addLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.addGiometricLocation(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "addLocation_res", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isAddLocation.postValue(response.body())
                            } else {
                                isAddLocation.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isAddLocation.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isAddLocationLiveData: LiveData<StatusMessageModel?>
        get() = isAddLocation


    fun removeLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.removeLocation(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "remove_location_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isRemoveLocation.postValue(response.body())
                            } else {
                                isRemoveLocation.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isRemoveLocation.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isRemoveLocationLiveData: LiveData<StatusMessageModel?>
        get() = isRemoveLocation


    fun updateLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.updateLocation(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "update_location_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isUpdateLocation.postValue(response.body())
                            } else {
                                isUpdateLocation.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isUpdateLocation.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isUpdateLocationLiveData: LiveData<StatusMessageModel?>
        get() = isUpdateLocation


    fun getStaffLocations(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.getStaffLocations(isToken)
            ?.enqueue(object : Callback<StaffLocationResponse?> {
                override fun onResponse(
                    call: Call<StaffLocationResponse?>, response: Response<StaffLocationResponse?>
                ) {
                    Log.d(
                        "staff_locations_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isStaffLocations.postValue(response.body())
                            } else {
                                isStaffLocations.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StaffLocationResponse?>, t: Throwable
                ) {
                    isStaffLocations.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isStaffLocationsLiveData: LiveData<StaffLocationResponse?>
        get() = isStaffLocations

    fun getLocationHistory(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.getLocationHistory(isToken)
            ?.enqueue(object : Callback<LocationHistoryResponse?> {
                override fun onResponse(
                    call: Call<LocationHistoryResponse?>,
                    response: Response<LocationHistoryResponse?>
                ) {
                    Log.d(
                        "location_history_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isLocationHistory.postValue(response.body())
                            } else {
                                isLocationHistory.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<LocationHistoryResponse?>, t: Throwable
                ) {
                    isLocationHistory.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isLocationHistoryLiveData: LiveData<LocationHistoryResponse?>
        get() = isLocationHistory


//    fun getPunchHistory(isToken: String, activity: Activity) {
//        RestClient.apiInterfaces.getPunchHistory(isToken)

    fun getPunchHistory(isToken: String, isDate: String, activity: Activity) {
        RestClient.apiInterfaces.getPunchHistory(isToken, isDate, isDate)
            ?.enqueue(object : Callback<PunchHistoryResponse?> {
                override fun onResponse(
                    call: Call<PunchHistoryResponse?>, response: Response<PunchHistoryResponse?>
                ) {
                    Log.d(
                        "punch_history_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isPunchHistory.postValue(response.body())
                            } else {
                                isPunchHistory.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<PunchHistoryResponse?>, t: Throwable
                ) {
                    isPunchHistory.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isPunchHistoryLiveData: LiveData<PunchHistoryResponse?>
        get() = isPunchHistory


    fun getGiometricStaffAttendancereport(
        isToken: String, attendance_dt: String, activity: Activity
    ) {

        RestClient.apiInterfaces.getStaffAttendanceReport(isToken, attendance_dt)
            ?.enqueue(object : Callback<StaffAttendanceReportResponse?> {
                override fun onResponse(
                    call: Call<StaffAttendanceReportResponse?>,
                    response: Response<StaffAttendanceReportResponse?>
                ) {
                    Log.d(
                        "staff_attendance_report",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isStaffAttendanceReport.postValue(response.body())
                            } else {
                                isStaffAttendanceReport.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StaffAttendanceReportResponse?>, t: Throwable
                ) {
                    isStaffAttendanceReport.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isGiometricStaffAttendanceReportLiveData: LiveData<StaffAttendanceReportResponse?>
        get() = isStaffAttendanceReport


    fun getGiometricStaffWiseAttendancereport(
        isToken: String, isCurrentDate: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getStaffWiseAttendanceReport(isToken, isCurrentDate)
            ?.enqueue(object : Callback<StaffAttendanceReportResponse?> {
                override fun onResponse(
                    call: Call<StaffAttendanceReportResponse?>,
                    response: Response<StaffAttendanceReportResponse?>
                ) {
                    Log.d(
                        "staffwise_attendance_report",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isStaffWiseAttendanceReport.postValue(response.body())
                            } else {
                                isStaffWiseAttendanceReport.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StaffAttendanceReportResponse?>, t: Throwable
                ) {
                    isStaffWiseAttendanceReport.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGiometricStaffWiseAttendanceReportLiveData: LiveData<StaffAttendanceReportResponse?>
        get() = isStaffWiseAttendanceReport

    fun getGiometricStaffWiseAttendancereportStaffList(
        isToken: String, isSelectedDate: String, isStaffId: Int, activity: Activity
    ) {
        RestClient.apiInterfaces.getStaffWiseAttendanceReportStaffList(
            isToken, isSelectedDate, isStaffId
        )?.enqueue(object : Callback<StaffAttendanceReportResponse?> {
                override fun onResponse(
                    call: Call<StaffAttendanceReportResponse?>,
                    response: Response<StaffAttendanceReportResponse?>
                ) {
                    Log.d(
                        "staffwise_attendance_report",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isStaffWiseAttendanceReportList.postValue(response.body())
                            } else {
                                isStaffWiseAttendanceReportList.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StaffAttendanceReportResponse?>, t: Throwable
                ) {
                    isStaffWiseAttendanceReportList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGiometricStaffWiseAttendanceReportLiveDataList: LiveData<StaffAttendanceReportResponse?>
        get() = isStaffWiseAttendanceReportList
}