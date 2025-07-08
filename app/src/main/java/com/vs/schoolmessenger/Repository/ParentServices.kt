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
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestApplyResponse
import com.vs.schoolmessenger.Parent.Timetable.TimeTableResponse
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


}