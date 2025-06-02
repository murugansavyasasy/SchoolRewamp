package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentResponse
import com.vs.schoolmessenger.Parent.Attendance.ChildAttendanceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParentServices {
    var client_auth: RestClient
    var isChildAttendanceReport: MutableLiveData<ChildAttendanceResponse?>
    var isAttachmentResponse: MutableLiveData<AttachmentResponse?>
    var isAttachmentResponseArchive: MutableLiveData<AttachmentResponse?>

    init {
        client_auth = RestClient()
        isChildAttendanceReport = MutableLiveData()
        isAttachmentResponse = MutableLiveData()
        isAttachmentResponseArchive = MutableLiveData()
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

}