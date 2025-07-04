package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.JsonToken
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.GlobalVariableResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYearResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIdsResponse
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardResponse
import com.vs.schoolmessenger.Parent.Communication.StatusArchiveResponse
import com.vs.schoolmessenger.Parent.Communication.VoiceDataResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.CouponMenuResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.MyCouponSummaryRequest
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummaryResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponRequestModel.CategorySummaryRequest
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventResponse
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.HolidayResponse
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkData
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoardResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.SendAbsenteeSMSResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.StudentAttendanceReportDataResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudentsResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteesResponse
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextDetailsResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendResponse
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceDetails
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DailyCollectionReportResponse
import com.vs.schoolmessenger.School.Event.Response.EventSendResponse
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportModel.FeePendingReportResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportApiResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkSendResponse
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveApproveRequest
import com.vs.schoolmessenger.School.LeaveRequests.Response.LeaveActionResponse
import com.vs.schoolmessenger.School.LeaveRequests.Response.LeaveRequestResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanDeleteModel.LPDeleteResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.LessonPlanEditResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel.AllClassResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanUpdateModel.LessonPlanUpdateResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.LocationHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffLocationResponse
import com.vs.schoolmessenger.School.NoticeBoard.Response.NoticeBoardSendResponse
import com.vs.schoolmessenger.School.SchoolStrength.Model.SchoolStrengthResponse
import com.vs.schoolmessenger.School.StudentReport.GetStudentReportData
import com.vs.schoolmessenger.Utils.SharedPreference
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SchoolServices {

    var client_auth: RestClient
    var isDashBoard: MutableLiveData<DashboardResponse?>
    var isGetAds: MutableLiveData<AdsResponse?>
    var isGetGlobalVariables: MutableLiveData<GlobalVariableResponse?>
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
    var isGetAssignmentReport: MutableLiveData<AssignmentResponse?>
    var isAssignmentDelete: MutableLiveData<LPDeleteResponse?>
    var isGetDailyCollectionReport: MutableLiveData<DailyCollectionReportResponse?>
    var isSendText: MutableLiveData<TextSendResponse?>
    var isSendHomeWork: MutableLiveData<HomeWorkSendResponse?>
    var isSendAssignment: MutableLiveData<HomeWorkSendResponse?>
    var isSendVoice: MutableLiveData<TextSendResponse?>
    var isUpdateStatusArchive: MutableLiveData<StatusArchiveResponse?>
    var isAcademicYear: MutableLiveData<AcademicYearResponse?>
    var isUpdateStatusCommunication: MutableLiveData<StatusArchiveResponse?>
    var isHomeWorkDetailsData: MutableLiveData<GetHomeworkData?>
    var isNoticeBoardReport: MutableLiveData<NoticeBoardResponse?>
    var isGetSchoolStrengthReport: MutableLiveData<SchoolStrengthResponse?>
    var isDetailedPendingReport: MutableLiveData<FeePendingReportResponse?>

    var isDetailedWisePendingReport: MutableLiveData<FeePendingReportResponse?>

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
    var isStudentReportList: MutableLiveData<GetStudentReportData?>

    var IsGetEventReport: MutableLiveData<EventResponse?>

    var IsGetHolidayReport: MutableLiveData<HolidayResponse?>
    var isSendAbsenteeSMS: MutableLiveData<SendAbsenteeSMSResponse?>
    var isStudentAttendanceReportForSchool: MutableLiveData<StudentAttendanceReportDataResponse?>

    var getabsenteescountbydate: MutableLiveData<AbsenteesResponse?>

    var getabsenteesstudentbydate: MutableLiveData<AbsenteeStudentsResponse?>


    var sendnotice: MutableLiveData<NoticeBoardSendResponse?>
    var sendevent: MutableLiveData<EventSendResponse?>
    var isSendAttachment: MutableLiveData<NoticeBoardSendResponse?>
    var getleaverequest: MutableLiveData<LeaveRequestResponse?>
    var isleaverequestapprove: MutableLiveData<LeaveActionResponse?>
    var getlpStaffReport: MutableLiveData<AllClassResponse?>
    var getlpViewReport: MutableLiveData<LessonPlanViewSummaryResponse?>
    var getlpeditReport: MutableLiveData<LessonPlanEditResponse?>
    var isupdatelessonplan: MutableLiveData<LessonPlanUpdateResponse?>
    var islessonplandelete: MutableLiveData<LPDeleteResponse?>
    var getcouponmenu: MutableLiveData<CouponMenuResponse?>
    var getCouponsSummary: MutableLiveData<CampaignResponse?>
    var getCouponsCategorySummary: MutableLiveData<CampaignResponse?>
    var getmycoupons: MutableLiveData<TicketSummaryResponse?>


    init {
        client_auth = RestClient()
        isDashBoard = MutableLiveData()
        isGetAds = MutableLiveData()
        isGetGlobalVariables = MutableLiveData()
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
        isGetAssignmentReport = MutableLiveData()
        isAssignmentDelete = MutableLiveData()
        isNoticeBoardReport = MutableLiveData()
        isGetDailyCollectionReport = MutableLiveData()
        isSendText = MutableLiveData()
        isSendHomeWork = MutableLiveData()
        isSendAssignment = MutableLiveData()
        isSendVoice = MutableLiveData()
        isUpdateStatusArchive = MutableLiveData()
        isAcademicYear = MutableLiveData()
        isUpdateStatusCommunication = MutableLiveData()
        isHomeWorkDetailsData = MutableLiveData()
        isDetailedPendingReport = MutableLiveData()
        isDetailedWisePendingReport = MutableLiveData()
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
        isStudentReportList = MutableLiveData()
        IsGetEventReport = MutableLiveData()
        IsGetHolidayReport = MutableLiveData()
        isSendAbsenteeSMS = MutableLiveData()
        isStudentAttendanceReportForSchool = MutableLiveData()
        getabsenteescountbydate = MutableLiveData()
        getabsenteesstudentbydate = MutableLiveData()
        sendnotice = MutableLiveData()
        sendevent = MutableLiveData()
        isSendAttachment = MutableLiveData()
        getleaverequest = MutableLiveData()
        isleaverequestapprove = MutableLiveData()
        getlpStaffReport = MutableLiveData()
        getlpViewReport = MutableLiveData()
        getlpeditReport = MutableLiveData()
        isupdatelessonplan = MutableLiveData()
        islessonplandelete = MutableLiveData()
        getcouponmenu = MutableLiveData()
        getCouponsSummary = MutableLiveData()
        getCouponsCategorySummary = MutableLiveData()
        getmycoupons = MutableLiveData()
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

    fun isGetGlobalVariables(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetGlobalVariable(isToken)
            ?.enqueue(object : Callback<GlobalVariableResponse?> {
                override fun onResponse(
                    call: Call<GlobalVariableResponse?>, response: Response<GlobalVariableResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetGlobalVariables.postValue(response.body())
                            } else {
                                isGetGlobalVariables.postValue(response.body())
                            }
                        }
                    } else {
                    }
                }

                override fun onFailure(call: Call<GlobalVariableResponse?>, t: Throwable) {
                    isGetGlobalVariables.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetGlobalVariablesLiveData: LiveData<GlobalVariableResponse?>
        get() = isGetGlobalVariables

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
                    else{
                        isGetStaffList.postValue(null)
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
                    else{
                        isGetSubjectList.postValue(null)
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
                    else{
                        isGetStandardSection.postValue(null)
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
                    else{
                        isGetStudentList.postValue(null)
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
                    else{
                        isGetCommmunicationlist.postValue(null)
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
                    else{
                        isGetCommmunicationlistload.postValue(null)
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
                    else{
                        isGetGroupList.postValue(null)
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
                    else{
                        isGetVoiceHistory.postValue(null)
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

                            isGetTextHistory.postValue(response.body())

                        }
                    }
                    else{
                        isGetTextHistory.postValue(null)
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
                    else{
                        isGetHomeWorkReport.postValue(null)
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

    fun isGetAssignmentReport(
        isToken: String, isAcademicYearId: Int, activity: Activity
    ) {
        RestClient.apiInterfaces.isGetAssignmentReport(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<AssignmentResponse?> {
                override fun onResponse(
                    call: Call<AssignmentResponse?>,
                    response: Response<AssignmentResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetAssignmentReport.postValue(response.body())
                            } else {
                                isGetAssignmentReport.postValue(response.body())
                            }
                        }
                    } else {
                        isGetAssignmentReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<AssignmentResponse?>, t: Throwable) {
                    isGetAssignmentReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isGetAssignmentReportLiveData: LiveData<AssignmentResponse?>
        get() = isGetAssignmentReport

    fun isDeleteAssignment(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        RestClient.apiInterfaces.isAssignmentDelete(isToken, jsonObject)
            ?.enqueue(object : Callback<LPDeleteResponse?> {
                override fun onResponse(
                    call: Call<LPDeleteResponse?>,
                    response: Response<LPDeleteResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isAssignmentDelete.postValue(response.body())
                            } else {
                                isAssignmentDelete.postValue(response.body())
                            }
                        }
                    } else {
                        isAssignmentDelete.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LPDeleteResponse?>, t: Throwable) {
                    isAssignmentDelete.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isDeleteAssignmentLiveData: LiveData<LPDeleteResponse?>
        get() = isAssignmentDelete


    fun isNoticeBoardReport(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.isNoticeBoardReport(isToken)
            ?.enqueue(object : Callback<NoticeBoardResponse?> {
                override fun onResponse(
                    call: Call<NoticeBoardResponse?>,
                    response: Response<NoticeBoardResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isNoticeBoardReport.postValue(response.body())
                            } else {
                                isNoticeBoardReport.postValue(response.body())
                            }
                        }
                    }
                    else{
                        isNoticeBoardReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<NoticeBoardResponse?>, t: Throwable) {
                    isNoticeBoardReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isNoticeBoardReportLiveData: LiveData<NoticeBoardResponse?>
        get() = isNoticeBoardReport


    fun IsGetEventReport(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.IsGetEventReport(isToken)
            ?.enqueue(object : Callback<EventResponse?> {
                override fun onResponse(
                    call: Call<EventResponse?>,
                    response: Response<EventResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                IsGetEventReport.postValue(response.body())
                            } else {
                                IsGetEventReport.postValue(response.body())
                            }
                        }
                    }
                    else{
                        IsGetEventReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<EventResponse?>, t: Throwable) {
                    IsGetEventReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val IsGetEventReportLiveData: LiveData<EventResponse?>
        get() = IsGetEventReport

    fun IsGetHolidayReport(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.IsGetHolidayReport(isToken)
            ?.enqueue(object : Callback<HolidayResponse?> {
                override fun onResponse(
                    call: Call<HolidayResponse?>,
                    response: Response<HolidayResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                IsGetHolidayReport.postValue(response.body())
                            } else {
                                IsGetHolidayReport.postValue(response.body())
                            }
                        }
                    }
                    else{
                        IsGetHolidayReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<HolidayResponse?>, t: Throwable) {
                    IsGetHolidayReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val IsGetHolidayReportLiveData: LiveData<HolidayResponse?>
        get() = IsGetHolidayReport



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
                    else{
                        isGetDailyCollectionReport.postValue(null)
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
                    else{
                        isGetSchoolStrengthReport.postValue(null)
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


    fun isDetailedPendingReport(isToken: String, isAcademicYearId: Int, activity: Activity) {
        RestClient.apiInterfaces.isDetailedPendingReport(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<FeePendingReportResponse?> {
                override fun onResponse(
                    call: Call<FeePendingReportResponse?>,
                    response: Response<FeePendingReportResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isDetailedPendingReport.postValue(response.body())
                            } else {
                                isDetailedPendingReport.postValue(response.body())
                            }
                        }
                    }
                    else{
                        isDetailedPendingReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<FeePendingReportResponse?>, t: Throwable) {
                    isDetailedPendingReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isDetailedPendingReportLiveData: LiveData<FeePendingReportResponse?>
        get() = isDetailedPendingReport


    fun isDetailedWisePendingReport(isToken: String, isAcademicYearId: Int, activity: Activity) {
        RestClient.apiInterfaces.isDetailedWisePendingReport(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<FeePendingReportResponse?> {
                override fun onResponse(
                    call: Call<FeePendingReportResponse?>,
                    response: Response<FeePendingReportResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isDetailedWisePendingReport.postValue(response.body())
                            } else {
                                isDetailedWisePendingReport.postValue(response.body())
                            }
                        }
                    }
                    else{
                        isDetailedWisePendingReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<FeePendingReportResponse?>, t: Throwable) {
                    isDetailedWisePendingReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isDetailedWisePendingReportLiveData: LiveData<FeePendingReportResponse?>
        get() = isDetailedWisePendingReport


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

    fun isSendAssignment(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.isAssignmentSend(isToken, jsonObject)
            ?.enqueue(object : Callback<HomeWorkSendResponse?> {
                override fun onResponse(
                    call: Call<HomeWorkSendResponse?>, response: Response<HomeWorkSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isSendAssignment.postValue(response.body())
                    } else {
                        isSendAssignment.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<HomeWorkSendResponse?>, t: Throwable) {
                    isSendAssignment.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isSendAssignmentLiveData: LiveData<HomeWorkSendResponse?>
        get() = isSendAssignment


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
                    else{
                        isAcademicYear.postValue(null)
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
                    else{
                        isHomeWorkDetailsData.postValue(null)
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


    //    //get HomeworkDetails
    fun homework_list_archive(isToken: String, activity: Activity) {
        Log.d("GetHomeworkData", isToken.toString())
        RestClient.apiInterfaces.homework_list_archive(isToken)
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
                    else{
                        isHomeWorkDetailsData.postValue(null)
                    }
                }

                override fun onFailure(call: Call<GetHomeworkData?>, t: Throwable) {
                    t.printStackTrace()
                    isHomeWorkDetailsData.postValue(null)
                    Log.d("GetHomeworkData", "Response,No Data Found")
                }
            })
    }

    val isHomeWorkDetailsListLiveData: LiveData<GetHomeworkData?>
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
                    else{
                        isPunchAttendance.postValue(null)
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
                    else{
                        isAddLocation.postValue(null)
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
                    else{
                        isRemoveLocation.postValue(null)
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
                    else{
                        isUpdateLocation.postValue(null)
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
                    else{
                        isStaffLocations.postValue(null)
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
                    else{
                        isLocationHistory.postValue(null)
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

    fun getPunchHistory(isToken: String, isDate: String, staff_id: String, activity: Activity) {
        RestClient.apiInterfaces.getPunchHistory(isToken, isDate, isDate, staff_id)
            ?.enqueue(object : Callback<PunchHistoryResponse?> {
                override fun onResponse(
                    call: Call<PunchHistoryResponse?>, response: Response<PunchHistoryResponse?>
                ) {
                    Log.d(
                        "punch_history_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
//                        if (response.body() != null) {
                        val status = response.body()!!.status
                        if (status) {
                            isPunchHistory.postValue(response.body())
                        } else {
                            isPunchHistory.postValue(response.body())
                        }
//                        }
                    }
                    else{
                        isPunchHistory.postValue(null)
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
                    else{
                        isStaffAttendanceReport.postValue(null)
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
                    else{
                        isStaffWiseAttendanceReport.postValue(null)
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
                else{
                    isStaffWiseAttendanceReportList.postValue(null)
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


    fun getStudentReportList(
        isToken: String,
        isAcademicYearId: Int,
        class_id: Int? = null,
        section_id: Int? = null,
        activity: Activity
    ) {
        RestClient.apiInterfaces.getStudentReport(isToken,isAcademicYearId, class_id, section_id)
            ?.enqueue(object : Callback<GetStudentReportData?> {
                override fun onResponse(
                    call: Call<GetStudentReportData?>,
                    response: Response<GetStudentReportData?>
                ) {
                    Log.d(
                        "GetStudentReport Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetStudentReportData", response.body().toString())
                                isStudentReportList.postValue(response.body())
                            } else {
                                Log.d("GetStudentReportData", response.body().toString())
                                isStudentReportList.postValue(response.body())
                            }
                        }
                    }
                    else{
                        isStudentReportList.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<GetStudentReportData?>,
                    t: Throwable
                ) {
                    isStudentReportList.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isStudentReportLiveData: LiveData<GetStudentReportData?>
        get() = isStudentReportList




    fun getabsenteescountbydate(
        isToken: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.getabsenteescountbydate(isToken)
            ?.enqueue(object : Callback<AbsenteesResponse?> {
                override fun onResponse(
                    call: Call<AbsenteesResponse?>,
                    response: Response<AbsenteesResponse?>
                ) {
                    Log.d(
                        "GetStudentReport Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetStudentReportData", response.body().toString())
                                getabsenteescountbydate.postValue(response.body())
                            } else {
                                Log.d("GetStudentReportData", response.body().toString())
                                getabsenteescountbydate.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getabsenteescountbydate.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<AbsenteesResponse?>,
                    t: Throwable
                ) {
                    getabsenteescountbydate.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val getabsenteescountbydateLiveData: LiveData<AbsenteesResponse?>
        get() = getabsenteescountbydate




    fun getabsenteesstudentbydate(
        isToken: String,
        absent_on: String? = null,
        section_id: String? = null,
        activity: Activity

    ) {
        RestClient.apiInterfaces.getabsenteesstudentbydate(isToken, absent_on, section_id)
            ?.enqueue(object : Callback<AbsenteeStudentsResponse?> {
                override fun onResponse(
                    call: Call<AbsenteeStudentsResponse?>,
                    response: Response<AbsenteeStudentsResponse?>
                ) {
                    Log.d(
                        "GetStudentReport Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetStudentReportData", response.body().toString())
                                getabsenteesstudentbydate.postValue(response.body())
                            } else {
                                Log.d("GetStudentReportData", response.body().toString())
                                getabsenteesstudentbydate.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getabsenteesstudentbydate.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<AbsenteeStudentsResponse?>,
                    t: Throwable
                ) {
                    getabsenteesstudentbydate.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val getabsenteesstudentbydateLiveData: LiveData<AbsenteeStudentsResponse?>
        get() = getabsenteesstudentbydate


    fun isUpdateSendAbsenteeSMS(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.UpdateSendAbsenteeSMS(isToken, jsonObject)
            ?.enqueue(object : Callback<SendAbsenteeSMSResponse?> {
                override fun onResponse(
                    call: Call<SendAbsenteeSMSResponse?>,
                    response: Response<SendAbsenteeSMSResponse?>
                ) {
                    Log.d(
                        "SendAbsenteeSMS", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isSendAbsenteeSMS.postValue(response.body())
                            } else {
                                isSendAbsenteeSMS.postValue(response.body())
                            }
                        }
                    }
                    else{
                        isSendAbsenteeSMS.postValue(null)
                    }
                }

                override fun onFailure(call: Call<SendAbsenteeSMSResponse?>, t: Throwable) {
                    isSendAbsenteeSMS.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSendAbsenteeSMSLiveData: LiveData<SendAbsenteeSMSResponse?>
        get() = isSendAbsenteeSMS



    fun getStudentAttendanceReportForSchool(
        isToken: String,
        section_id: String,
        from_date: String,
        to_date: String,
        class_id: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.isGetStudentAttendanceReportForSchool(isToken,section_id,from_date,to_date,class_id)
            ?.enqueue(object : Callback<StudentAttendanceReportDataResponse?> {
                override fun onResponse(
                    call: Call<StudentAttendanceReportDataResponse?>,
                    response: Response<StudentAttendanceReportDataResponse?>
                ) {
                    Log.d(
                        "GetStudentAttendanceReport Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetStudentAttendanceReportData", response.body().toString())
                                isStudentAttendanceReportForSchool.postValue(response.body())
                            } else {
                                Log.d("GetStudentAttendanceReportData", response.body().toString())
                                isStudentAttendanceReportForSchool.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StudentAttendanceReportDataResponse?>,
                    t: Throwable
                ) {
                    isStudentAttendanceReportForSchool.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isStudentAttendanceReportLiveData: LiveData<StudentAttendanceReportDataResponse?>
        get() = isStudentAttendanceReportForSchool





    fun sendnotice(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.sendnotice(isToken, jsonObject)
            ?.enqueue(object : Callback<NoticeBoardSendResponse?> {
                override fun onResponse(
                    call: Call<NoticeBoardSendResponse?>, response: Response<NoticeBoardSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        sendnotice.postValue(response.body())
                    } else {
                        sendnotice.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<NoticeBoardSendResponse?>, t: Throwable) {
                    sendnotice.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val sendnoticeLiveData: LiveData<NoticeBoardSendResponse?>
        get() = sendnotice


    fun sendevent(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.sendevent(isToken, jsonObject)
            ?.enqueue(object : Callback<EventSendResponse?> {
                override fun onResponse(
                    call: Call<EventSendResponse?>, response: Response<EventSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        sendevent.postValue(response.body())
                    } else {
                        sendevent.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<EventSendResponse?>, t: Throwable) {
                    sendevent.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val sendeventLiveData: LiveData<EventSendResponse?>
        get() = sendevent



    fun sendAttachment(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.sendAttachment(isToken, jsonObject)
            ?.enqueue(object : Callback<NoticeBoardSendResponse?> {
                override fun onResponse(
                    call: Call<NoticeBoardSendResponse?>, response: Response<NoticeBoardSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isSendAttachment.postValue(response.body())
                    } else {
                        isSendAttachment.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<NoticeBoardSendResponse?>, t: Throwable) {
                    isSendAttachment.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val sendAttachmentLiveData: LiveData<NoticeBoardSendResponse?>
        get() = isSendAttachment



    fun getleaverequest(
        isToken: String, member_type: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getleaverequest(isToken, member_type)
            ?.enqueue(object : Callback<LeaveRequestResponse?> {
                override fun onResponse(
                    call: Call<LeaveRequestResponse?>, response: Response<LeaveRequestResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                getleaverequest.postValue(response.body())
                            } else {
                                getleaverequest.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getleaverequest.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LeaveRequestResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val leaverequestLiveData: LiveData<LeaveRequestResponse?>
        get() = getleaverequest





    fun isleaverequestapprove(
        isToken: String, request: LeaveApproveRequest, activity: Activity
    ) {
        RestClient.apiInterfaces.isleaverequestapprove(isToken, request)
            ?.enqueue(object : Callback<LeaveActionResponse?> {
                override fun onResponse(
                    call: Call<LeaveActionResponse?>, response: Response<LeaveActionResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isleaverequestapprove.postValue(response.body())
                            } else {
                                isleaverequestapprove.postValue(response.body())
                            }
                        }
                    }
                    else{
                        isleaverequestapprove.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LeaveActionResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isleaverequestapproveLiveData: LiveData<LeaveActionResponse?>
        get() = isleaverequestapprove





    fun getlpStaffReport(
        isToken: String, request_type: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getlpStaffReport(isToken, request_type)
            ?.enqueue(object : Callback<AllClassResponse?> {
                override fun onResponse(
                    call: Call<AllClassResponse?>, response: Response<AllClassResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                getlpStaffReport.postValue(response.body())
                            } else {
                                getlpStaffReport.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getlpStaffReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<AllClassResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isgetlpStaffReportLiveData: LiveData<AllClassResponse?>
        get() = getlpStaffReport



    fun getlpViewReport(
        isToken: String, section_subject_id: String, lesson_plan_status: Int, activity: Activity
    ) {
        RestClient.apiInterfaces.getlpViewReport(isToken, section_subject_id, lesson_plan_status)
            ?.enqueue(object : Callback<LessonPlanViewSummaryResponse?> {
                override fun onResponse(
                    call: Call<LessonPlanViewSummaryResponse?>, response: Response<LessonPlanViewSummaryResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                getlpViewReport.postValue(response.body())
                            } else {
                                getlpViewReport.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getlpViewReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LessonPlanViewSummaryResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isgetlpViewReportLiveData: LiveData<LessonPlanViewSummaryResponse?>
        get() = getlpViewReport



    fun getlpeditReport(
        isToken: String, particular_id: String, request_type: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getlpeditReport(isToken, particular_id, request_type)
            ?.enqueue(object : Callback<LessonPlanEditResponse?> {
                override fun onResponse(
                    call: Call<LessonPlanEditResponse?>, response: Response<LessonPlanEditResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                getlpeditReport.postValue(response.body())
                            } else {
                                getlpeditReport.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getlpeditReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LessonPlanEditResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isgetlpeditReportLiveData: LiveData<LessonPlanEditResponse?>
        get() = getlpeditReport



    fun isupdatelessonplan(isToken: String, requestBody: RequestBody, activity: Activity) {
        RestClient.apiInterfaces.isupdatelessonplan(isToken, requestBody)
            ?.enqueue(object : Callback<LessonPlanUpdateResponse?> {
                override fun onResponse(
                    call: Call<LessonPlanUpdateResponse?>,
                    response: Response<LessonPlanUpdateResponse?>
                ) {
                    Log.d("isGetCountryList", "${response.code()} - $response")
                    if (response.code() == 200 && response.body() != null) {
                        isupdatelessonplan.postValue(response.body())
                    } else {
                        isupdatelessonplan.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LessonPlanUpdateResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isupdatelessonplanLiveData: LiveData<LessonPlanUpdateResponse?>
        get() = isupdatelessonplan




    fun islessonplandelete(isToken: String,requestBody: RequestBody, activity: Activity) {
        RestClient.apiInterfaces.islessonplandelete(isToken, requestBody)
            ?.enqueue(object : Callback<LPDeleteResponse?> {
                override fun onResponse(
                    call: Call<LPDeleteResponse?>,
                    response: Response<LPDeleteResponse?>
                ) {
                    Log.d("isGetCountryList", "${response.code()} - $response")
                    if (response.code() == 200 && response.body() != null) {
                        islessonplandelete.postValue(response.body())
                    } else {
                        islessonplandelete.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LPDeleteResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val islessonplandeleteLiveData: LiveData<LPDeleteResponse?>
        get() = islessonplandelete




    fun getcouponmenu(
       parentname: String, apiKey: String
    ) {
        RestClient.couponApiInterfaces.getcouponmenu(parentname, apiKey)
            ?.enqueue(object : Callback<CouponMenuResponse?> {
                override fun onResponse(
                    call: Call<CouponMenuResponse?>, response: Response<CouponMenuResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                          response.body()?.data?.let {
                          getcouponmenu.postValue(response.body())
                          } ?: run {
                          getcouponmenu.postValue(response.body())
                      }


                        }
                    }
                    else{
                        getcouponmenu.postValue(null)
                    }
                }

                override fun onFailure(call: Call<CouponMenuResponse?>, t: Throwable) {
                    getcouponmenu.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getcouponmenuLiveData: LiveData<CouponMenuResponse?>
        get() = getcouponmenu



    fun getCouponsSummary(
        mobile_no:String,parentname: String, apiKey: String
    ) {
        RestClient.couponApiInterfaces.getCouponsSummary(mobile_no,parentname, apiKey)
            ?.enqueue(object : Callback<CampaignResponse?> {
                override fun onResponse(
                    call: Call<CampaignResponse?>, response: Response<CampaignResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            response.body()?.data?.let {
                                getCouponsSummary.postValue(response.body())
                            } ?: run {
                                getCouponsSummary.postValue(response.body())
                            }


                        }
                    }
                    else{
                        getCouponsSummary.postValue(null)
                    }
                }

                override fun onFailure(call: Call<CampaignResponse?>, t: Throwable) {
                    getCouponsSummary.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getCouponsSummaryLiveData: LiveData<CampaignResponse?>
        get() = getCouponsSummary

    fun getCouponsCategorySummary(
        category_id: String,
        mobile_no: String,
        parentName: String,
        apiKey: String
    ) {
        val request = CategorySummaryRequest(
            category_id = category_id,
            mobile_no = mobile_no
        )
        RestClient.couponApiInterfaces.getCouponsCategorySummary(parentName,apiKey,request)
            ?.enqueue(object : Callback<CampaignResponse?> {
                override fun onResponse(
                    call: Call<CampaignResponse?>, response: Response<CampaignResponse?>
                ) {
                    if (response.code() == 200) {
                        getCouponsCategorySummary.postValue(response.body())
                    } else {
                        getCouponsCategorySummary.postValue(null)
                    }
                }

                override fun onFailure(call: Call<CampaignResponse?>, t: Throwable) {
                    getCouponsCategorySummary.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val getCouponsCategorySummaryLiveData: LiveData<CampaignResponse?>
        get() = getCouponsCategorySummary


    fun getmycouponsSummary(
        coupon_status: String,
        mobile_no: String,
        parentName: String,
        apiKey: String
    ) {
        val request = MyCouponSummaryRequest(
            coupon_status = coupon_status,
            mobile_no = mobile_no
        )
        RestClient.couponApiInterfaces.getmycoupons(parentName,apiKey,request)
            ?.enqueue(object : Callback<TicketSummaryResponse?> {
                override fun onResponse(
                    call: Call<TicketSummaryResponse?>, response: Response<TicketSummaryResponse?>
                ) {
                    if (response.code() == 200) {
                        getmycoupons.postValue(response.body())
                    } else {
                        getmycoupons.postValue(null)
                    }
                }

                override fun onFailure(call: Call<TicketSummaryResponse?>, t: Throwable) {
                    getmycoupons.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val getmycouponsSummaryLiveData: LiveData<TicketSummaryResponse?>
        get() = getmycoupons
}