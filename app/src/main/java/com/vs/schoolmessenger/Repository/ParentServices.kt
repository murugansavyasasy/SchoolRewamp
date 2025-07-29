package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentResponse
import com.vs.schoolmessenger.Parent.Attendance.ChildAttendanceResponse
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificatesListResponse
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificatesTypesResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel.ExamResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.ExamMarksResponse
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamTimeTableResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ProgressCardResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel.AnswerResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.InteractionWithStaffResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.QuestionModelResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.Request.QuestionModelRequest
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestApplyResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDelete
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDeleteResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestUpdate
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveUpdateResponse
import com.vs.schoolmessenger.Parent.Timetable.TimeTableResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParentServices {
    var client_auth: RestClient
    var isChildAttendanceReport: MutableLiveData<ChildAttendanceResponse?>
    var isAttachmentResponse: MutableLiveData<AttachmentResponse?>
    var isAttachmentResponseArchive: MutableLiveData<AttachmentResponse?>
    var isLeaveRequestApplyResponse: MutableLiveData<LeaveRequestApplyResponse?>
    var isCertificatetypes: MutableLiveData<CertificatesTypesResponse?>
    var isSendCertificateRequest: MutableLiveData<StatusMessageModel?>
    var isCertificateRequestList: MutableLiveData<CertificatesListResponse?>
    var isTimeTable: MutableLiveData<TimeTableResponse?>
    var getdetailsforchat: MutableLiveData<InteractionWithStaffResponse?>
    var getstudentdetailsforchat: MutableLiveData<InteractionWithStudentResponse?>
    var getstaffanswers: MutableLiveData<AnswerResponse?>
    var sendquestion: MutableLiveData<QuestionModelResponse?>
    var getexams: MutableLiveData<ExamTimeTableResponse?>
    var getexamslist: MutableLiveData<ExamResponse?>
    var getviewmarks: MutableLiveData<ExamMarksResponse?>
    var isleaverequestupdate: MutableLiveData<LeaveUpdateResponse?>
    var isleaverequestdelete: MutableLiveData<LeaveRequestDeleteResponse?>
    var getProgressMarks: MutableLiveData<ProgressCardResponse?>
    var isUpdateCompleteHomeWork: MutableLiveData<StatusMessageModel?>

    init {
        client_auth = RestClient()
        isChildAttendanceReport = MutableLiveData()
        isAttachmentResponse = MutableLiveData()
        isAttachmentResponseArchive = MutableLiveData()
        isLeaveRequestApplyResponse = MutableLiveData()
        isCertificatetypes = MutableLiveData()
        isSendCertificateRequest = MutableLiveData()
        isCertificateRequestList = MutableLiveData()
        isTimeTable = MutableLiveData()
        getdetailsforchat = MutableLiveData()
        getstaffanswers = MutableLiveData()
        sendquestion = MutableLiveData()
        getexams = MutableLiveData()
        getexamslist = MutableLiveData()
        getviewmarks = MutableLiveData()
        isleaverequestupdate = MutableLiveData()
        isleaverequestdelete = MutableLiveData()
        getProgressMarks = MutableLiveData()
        isUpdateCompleteHomeWork = MutableLiveData()
        getstudentdetailsforchat = MutableLiveData()
    }

    fun getChildAttendanceReport(
        isToken: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.isGetChildAttendanceReport(isToken)
            ?.enqueue(object : Callback<ChildAttendanceResponse?> {
                override fun onResponse(
                    call: Call<ChildAttendanceResponse?>,
                    response: Response<ChildAttendanceResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isChildAttendanceReport.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isChildAttendanceReport.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<ChildAttendanceResponse?>,
                    t: Throwable
                ) {
                    isChildAttendanceReport.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isChildAttendanceReportLiveData: LiveData<ChildAttendanceResponse?>
        get() = isChildAttendanceReport


    fun getAttachmentList(
        isToken: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.attachmentList(isToken)
            ?.enqueue(object : Callback<AttachmentResponse?> {
                override fun onResponse(
                    call: Call<AttachmentResponse?>,
                    response: Response<AttachmentResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isAttachmentResponse.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isAttachmentResponse.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AttachmentResponse?>,
                    t: Throwable
                ) {
                    isAttachmentResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isAttachmentResponseLiveData: LiveData<AttachmentResponse?>
        get() = isAttachmentResponse


    fun attachmentListArchive(
        isToken: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.attachmentListArchive(isToken)
            ?.enqueue(object : Callback<AttachmentResponse?> {
                override fun onResponse(
                    call: Call<AttachmentResponse?>,
                    response: Response<AttachmentResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isAttachmentResponseArchive.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isAttachmentResponseArchive.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AttachmentResponse?>,
                    t: Throwable
                ) {
                    isAttachmentResponseArchive.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isAttachmentResponseArchiveLiveData: LiveData<AttachmentResponse?>
        get() = isAttachmentResponseArchive


    fun isLeaveRequestApply(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.LeaveRequestApply(isToken, jsonObject)
            ?.enqueue(object : Callback<LeaveRequestApplyResponse?> {
                override fun onResponse(
                    call: Call<LeaveRequestApplyResponse?>,
                    response: Response<LeaveRequestApplyResponse?>
                ) {
                    Log.d(
                        "", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isLeaveRequestApplyResponse.postValue(response.body())
                            } else {
                                isLeaveRequestApplyResponse.postValue(response.body())
                            }
                        }
                    } else {
                        isLeaveRequestApplyResponse.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LeaveRequestApplyResponse?>, t: Throwable) {
                    isLeaveRequestApplyResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val leaveRequestLiveData: LiveData<LeaveRequestApplyResponse?>
        get() = isLeaveRequestApplyResponse


    fun getCertificateTypes(
        isToken: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.isGetCertificatesTypes(isToken)
            ?.enqueue(object : Callback<CertificatesTypesResponse?> {
                override fun onResponse(
                    call: Call<CertificatesTypesResponse?>,
                    response: Response<CertificatesTypesResponse?>
                ) {
                    Log.d(
                        "GetCertificate Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isCertificatetypes.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isCertificatetypes.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<CertificatesTypesResponse?>,
                    t: Throwable
                ) {
                    isCertificatetypes.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isCertificateTypesLiveData: LiveData<CertificatesTypesResponse?>
        get() = isCertificatetypes

    fun getCertificateRequestList(
        isToken: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.isGetCertificateRequests(isToken)
            ?.enqueue(object : Callback<CertificatesListResponse?> {
                override fun onResponse(
                    call: Call<CertificatesListResponse?>,
                    response: Response<CertificatesListResponse?>
                ) {
                    Log.d(
                        "certificate list Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isCertificateRequestList.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isCertificateRequestList.postValue(response.body())
                            }
                        }
                    }
                    else{
                        isCertificateRequestList.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<CertificatesListResponse?>,
                    t: Throwable
                ) {
                    isCertificateRequestList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isCertificateRequestListLiveData: LiveData<CertificatesListResponse?>
        get() = isCertificateRequestList


    fun sendCertificateRequest(
        isToken: String,
        jsonObject: JsonObject,
        activity: Activity
    ) {
        RestClient.apiInterfaces.sendCertificateRequest(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>,
                    response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "send certificate Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isSendCertificateRequest.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isSendCertificateRequest.postValue(response.body())
                            }
                        }
                    }
                    else{
                        isSendCertificateRequest.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<StatusMessageModel?>,
                    t: Throwable
                ) {
                    isSendCertificateRequest.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSendCertificateLiveData: LiveData<StatusMessageModel?>
        get() = isSendCertificateRequest


    fun getTimeTable(
        isToken: String,
        day_id: Int,
        activity: Activity
    ) {
        RestClient.apiInterfaces.isGetTimeTable(isToken, day_id)
            ?.enqueue(object : Callback<TimeTableResponse?> {
                override fun onResponse(
                    call: Call<TimeTableResponse?>,
                    response: Response<TimeTableResponse?>
                ) {
                    Log.d(
                        "Timetable list Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isTimeTable.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isTimeTable.postValue(response.body())
                            }
                        }
                    }
                    else{
                        isTimeTable.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<TimeTableResponse?>,
                    t: Throwable
                ) {
                    isTimeTable.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isTimeTableListLiveData: LiveData<TimeTableResponse?>
        get() = isTimeTable




    fun getdetailsforchat(
        isToken: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.getdetailsforchat(isToken)
            ?.enqueue(object : Callback<InteractionWithStaffResponse?> {
                override fun onResponse(
                    call: Call<InteractionWithStaffResponse?>,
                    response: Response<InteractionWithStaffResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getdetailsforchat.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getdetailsforchat.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<InteractionWithStaffResponse?>,
                    t: Throwable
                ) {
                    getdetailsforchat.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getdetailsforchatLiveData: LiveData<InteractionWithStaffResponse?>
        get() = getdetailsforchat


    fun getstudentdetailsforchat(
        isToken: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.getstudentdetailsforchat(isToken)
            ?.enqueue(object : Callback<InteractionWithStudentResponse?> {
                override fun onResponse(
                    call: Call<InteractionWithStudentResponse?>,
                    response: Response<InteractionWithStudentResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getstudentdetailsforchat.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getstudentdetailsforchat.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<InteractionWithStudentResponse?>,
                    t: Throwable
                ) {
                    getstudentdetailsforchat.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getstudentdetailsforchatLiveData: LiveData<InteractionWithStudentResponse?>
        get() = getstudentdetailsforchat



    fun getstaffanswers(
        isToken: String,
        staff_id: String,
        subject_id: String,
        offset: Int,
        is_class_teacher: Boolean,
        activity: Activity
    ) {
        RestClient.apiInterfaces.getstaffanswers(isToken,staff_id,
            subject_id,
            offset,
            is_class_teacher,)
            ?.enqueue(object : Callback<AnswerResponse?> {
                override fun onResponse(
                    call: Call<AnswerResponse?>,
                    response: Response<AnswerResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getstaffanswers.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getstaffanswers.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AnswerResponse?>,
                    t: Throwable
                ) {
                    getstaffanswers.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getstaffanswersLiveData: LiveData<AnswerResponse?>
        get() = getstaffanswers



    fun sendquestion(
        isToken: String,
        request: QuestionModelRequest
    ) {
        RestClient.apiInterfaces.sendquestion(isToken, request)
            ?.enqueue(object : Callback<QuestionModelResponse?> {
                override fun onResponse(
                    call: Call<QuestionModelResponse?>,
                    response: Response<QuestionModelResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        sendquestion.postValue(response.body())
                    } else {
                        sendquestion.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<QuestionModelResponse?>, t: Throwable) {
                    sendquestion.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val sendquestionLiveData: LiveData<QuestionModelResponse?>
        get() = sendquestion



    fun getexams(
        isToken: String
    ) {
        RestClient.apiInterfaces.getexams(isToken)
            ?.enqueue(object : Callback<ExamTimeTableResponse?> {
                override fun onResponse(
                    call: Call<ExamTimeTableResponse?>,
                    response: Response<ExamTimeTableResponse?>
                ) {
                    Log.d(
                        "certificate list Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getexams.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getexams.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getexams.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<ExamTimeTableResponse?>,
                    t: Throwable
                ) {
                    getexams.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getexamsLiveData: LiveData<ExamTimeTableResponse?>
        get() = getexams


    fun getexamslist(
        isToken: String
    ) {
        RestClient.apiInterfaces.getexamslist(isToken)
            ?.enqueue(object : Callback<ExamResponse?> {
                override fun onResponse(
                    call: Call<ExamResponse?>,
                    response: Response<ExamResponse?>
                ) {
                    Log.d(
                        "certificate list Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getexamslist.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getexamslist.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getexamslist.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<ExamResponse?>,
                    t: Throwable
                ) {
                    getexamslist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getexamslistLiveData: LiveData<ExamResponse?>
        get() = getexamslist


    fun getviewmarks(
        isToken: String,
        exam_id: String
    ) {
        RestClient.apiInterfaces.getviewmarks(isToken,exam_id)
            ?.enqueue(object : Callback<ExamMarksResponse?> {
                override fun onResponse(
                    call: Call<ExamMarksResponse?>,
                    response: Response<ExamMarksResponse?>
                ) {
                    Log.d(
                        "certificate list Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getviewmarks.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getviewmarks.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getviewmarks.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<ExamMarksResponse?>,
                    t: Throwable
                ) {
                    getviewmarks.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getviewmarksLiveData: LiveData<ExamMarksResponse?>
        get() = getviewmarks



    fun isleaverequestupdate(
        isToken: String, request: LeaveRequestUpdate, activity: Activity
    ) {
        RestClient.apiInterfaces.isleaverequestupdate(isToken, request)
            ?.enqueue(object : Callback<LeaveUpdateResponse?> {
                override fun onResponse(
                    call: Call<LeaveUpdateResponse?>, response: Response<LeaveUpdateResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isleaverequestupdate.postValue(response.body())
                            } else {
                                isleaverequestupdate.postValue(response.body())
                            }
                        }
                    } else {
                        isleaverequestupdate.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<LeaveUpdateResponse?>,
                    t: Throwable
                ) {
                    isleaverequestupdate.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isleaverequestupdateLiveData: LiveData<LeaveUpdateResponse?>
        get() = isleaverequestupdate


    fun isleaverequestdelete(
        isToken: String, request: LeaveRequestDelete, activity: Activity
    ) {
        RestClient.apiInterfaces.isleaverequestdelete(isToken, request)
            ?.enqueue(object : Callback<LeaveRequestDeleteResponse?> {
                override fun onResponse(
                    call: Call<LeaveRequestDeleteResponse?>, response: Response<LeaveRequestDeleteResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isleaverequestdelete.postValue(response.body())
                            } else {
                                isleaverequestdelete.postValue(response.body())
                            }
                        }
                    } else {
                        isleaverequestdelete.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<LeaveRequestDeleteResponse?>,
                    t: Throwable
                ) {
                    isleaverequestdelete.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isleaverequestdeleteLiveData: LiveData<LeaveRequestDeleteResponse?>
        get() = isleaverequestdelete



    fun getProgressMarks(
        isToken: String,
        exam_id: String
    ) {
        RestClient.apiInterfaces.getProgressMarks(isToken,exam_id)
            ?.enqueue(object : Callback<ProgressCardResponse?> {
                override fun onResponse(
                    call: Call<ProgressCardResponse?>,
                    response: Response<ProgressCardResponse?>
                ) {
                    Log.d(
                        "certificate list Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getProgressMarks.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getProgressMarks.postValue(response.body())
                            }
                        }
                    }
                    else{
                        getProgressMarks.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<ProgressCardResponse?>,
                    t: Throwable
                ) {
                    getProgressMarks.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getProgressMarksLiveData: LiveData<ProgressCardResponse?>
        get() = getProgressMarks


    fun isHomeWorkComplete(
        isToken: String,
        jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isHomeWorkComplete(isToken,jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )

                    isUpdateCompleteHomeWork.postValue(response.body())

                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    val isUpdateCompleteHomeWorkLiveData: LiveData<StatusMessageModel?>
        get() = isUpdateCompleteHomeWork


}