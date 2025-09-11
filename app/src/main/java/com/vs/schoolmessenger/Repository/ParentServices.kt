package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Dashboard.Settings.Notification.NotificationResponse
import com.vs.schoolmessenger.Parent.Assignment.Model.AssignmentSubmitResponse
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentResponse
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.MySubmittedAssignmentsResponse
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentResponse
import com.vs.schoolmessenger.Parent.Attendance.AttendanceReport.ChildAttendanceResponse
import com.vs.schoolmessenger.Parent.Attendance.Model.getStudentStats
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificatesListResponse
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificatesTypesResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.PauketPoints.PauketPointsResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.PauketPoints.SpentPointsModel
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel.ExamResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.ExamMarksResponse
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamTimeTableResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ProgressCardResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel.AnswerResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.InteractionWithStaffResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.QuestionModelResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.Request.QuestionModelRequest
import com.vs.schoolmessenger.Parent.LSRW.Model.LSRWSkillSubmitResponse
import com.vs.schoolmessenger.Parent.LSRW.Model.LsrwSkillResponse
import com.vs.schoolmessenger.Parent.PTM.DataClass.AvailableSlotsResponse
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingHistoryResponse
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingResponse
import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotDetailsResponse
import com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion.GetQuizQuestions
import com.vs.schoolmessenger.Parent.QuizExam.Model.MySubmission.GetMySubmission
import com.vs.schoolmessenger.Parent.QuizExam.Model.QuizExamList.GetQuizExamList
import com.vs.schoolmessenger.Parent.QuizExam.Model.SubmitQuiz.SubmitQuizResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestApplyResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.GetLeaveCategoriesData
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDelete
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDeleteResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestUpdate
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveUpdateResponse
import com.vs.schoolmessenger.Parent.Timetable.TimeTableResponse
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentResponse
import com.vs.schoolmessenger.School.LSRW.Model.LsrwSkillSendResponse
import com.vs.schoolmessenger.School.LSRW.SubmissionStudentListModel.StudentSubmissionLsrwResponse
import com.vs.schoolmessenger.Utils.SharedPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParentServices {
    var client_auth: RestClient
    var isChildAttendanceReport: MutableLiveData<ChildAttendanceResponse?>
    var isAttachmentResponse: MutableLiveData<AttachmentReportResponse?>
    var isAttachmentResponseArchive: MutableLiveData<AttachmentResponse?>
    var isLeaveRequestApplyResponse: MutableLiveData<LeaveRequestApplyResponse?>
    var isCertificatetypes: MutableLiveData<CertificatesTypesResponse?>
    var isNotificationResponse: MutableLiveData<NotificationResponse?>
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
    var isLeaveCategories: MutableLiveData<GetLeaveCategoriesData?>
    var isAssignmentlist: MutableLiveData<ParentAssignmentResponse?>
    var isSubmitAssignment: MutableLiveData<AssignmentSubmitResponse?>
    var isStudentStats: MutableLiveData<getStudentStats?>
    var getassignmentmysubmissionlist: MutableLiveData<MySubmittedAssignmentsResponse?>
    var isSlotBookingStudent: MutableLiveData<StatusMessageModel?>
    var isStudentSlotResponse: MutableLiveData<MeetingResponse?>
    var isAvailableSlotsResponse: MutableLiveData<AvailableSlotsResponse?>
    var isSlotCancelByStudent: MutableLiveData<StatusMessageModel?>
    var isSlotDetailsHistory: MutableLiveData<MeetingHistoryResponse?>
    var isQuizExamList: MutableLiveData<GetQuizExamList?>
    var isGetQuestions: MutableLiveData<GetQuizQuestions?>
    var isSubmitQuiz: MutableLiveData<SubmitQuizResponse?>
    var isGetMySubmission: MutableLiveData<GetMySubmission?>
    var islsrwSkilllist: MutableLiveData<LsrwSkillResponse?>
    var islsrwSkillSubmit: MutableLiveData<LSRWSkillSubmitResponse?>
    var isGetPauketPoints: MutableLiveData<PauketPointsResponse?>
    var isSpentPoints: MutableLiveData<SpentPointsModel?>

    init {
        client_auth = RestClient()
        isChildAttendanceReport = MutableLiveData()
        isAttachmentResponse = MutableLiveData()
        isAttachmentResponseArchive = MutableLiveData()
        isLeaveRequestApplyResponse = MutableLiveData()
        isCertificatetypes = MutableLiveData()
        isNotificationResponse = MutableLiveData()
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
        isLeaveCategories = MutableLiveData()
        isAssignmentlist = MutableLiveData()
        isSubmitAssignment = MutableLiveData()
        isStudentStats = MutableLiveData()
        getassignmentmysubmissionlist = MutableLiveData()
        isSlotBookingStudent = MutableLiveData()
        isStudentSlotResponse = MutableLiveData()
        isAvailableSlotsResponse = MutableLiveData()
        isSlotCancelByStudent = MutableLiveData()
        isSlotDetailsHistory = MutableLiveData()
        isQuizExamList = MutableLiveData()
        isGetQuestions = MutableLiveData()
        isSubmitQuiz = MutableLiveData()
        isGetMySubmission = MutableLiveData()
        islsrwSkilllist = MutableLiveData()
        islsrwSkillSubmit = MutableLiveData()
        isGetPauketPoints = MutableLiveData()
        isSpentPoints = MutableLiveData()
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
            ?.enqueue(object : Callback<AttachmentReportResponse?> {
                override fun onResponse(
                    call: Call<AttachmentReportResponse?>,
                    response: Response<AttachmentReportResponse?>
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
                    call: Call<AttachmentReportResponse?>,
                    t: Throwable
                ) {
                    isAttachmentResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isAttachmentResponseLiveData: LiveData<AttachmentReportResponse?>
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


    fun isNotifications(isToken: String, deviceType: String) {
        RestClient.apiInterfaces.getNotifications(isToken, deviceType)
            ?.enqueue(object : Callback<NotificationResponse?> {
                override fun onResponse(
                    call: Call<NotificationResponse?>,
                    response: Response<NotificationResponse?>
                ) {
                    Log.d("isNotifications Response", "${response.code()} - $response")

                    if (response.code() == 200) {
                        response.body()?.let {
                            isNotificationResponse.postValue(it)
                        }
                    }
                }

                override fun onFailure(call: Call<NotificationResponse?>, t: Throwable) {
                    isNotificationResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }



    val isNotificationResponseLiveData: LiveData<NotificationResponse?>
        get() = isNotificationResponse


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
                    } else {
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
                    } else {
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
                    } else {
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
        RestClient.apiInterfaces.getstaffanswers(
            isToken, staff_id,
            subject_id,
            offset,
            is_class_teacher,
        )
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
                    } else {
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
                    } else {
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
        RestClient.apiInterfaces.getviewmarks(isToken, exam_id)
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
                    } else {
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
                    call: Call<LeaveRequestDeleteResponse?>,
                    response: Response<LeaveRequestDeleteResponse?>
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
        RestClient.apiInterfaces.getProgressMarks(isToken, exam_id)
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
                    } else {
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
        RestClient.apiInterfaces.isHomeWorkComplete(isToken, jsonObject)
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


    fun getLeaveCategories(
        isToken: String,
    ) {
        RestClient.apiInterfaces.getleavecategories(isToken)
            ?.enqueue(object : Callback<GetLeaveCategoriesData?> {
                override fun onResponse(
                    call: Call<GetLeaveCategoriesData?>,
                    response: Response<GetLeaveCategoriesData?>
                ) {

                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isLeaveCategories.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isLeaveCategories.postValue(response.body())
                            }
                        }
                    } else {
                        isLeaveCategories.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<GetLeaveCategoriesData?>,
                    t: Throwable
                ) {
                    isLeaveCategories.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getLeaveCategoriesLiveData: LiveData<GetLeaveCategoriesData?>
        get() = isLeaveCategories


    fun isAssignmentlist(
        isToken: String
    ) {
        RestClient.apiInterfaces.isAssignmentlist(isToken)
            ?.enqueue(object : Callback<ParentAssignmentResponse?> {
                override fun onResponse(
                    call: Call<ParentAssignmentResponse?>,
                    response: Response<ParentAssignmentResponse?>
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
                                isAssignmentlist.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isAssignmentlist.postValue(response.body())
                            }
                        }
                    } else {
                        isAssignmentlist.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<ParentAssignmentResponse?>,
                    t: Throwable
                ) {
                    isAssignmentlist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isAssignmentlistLiveData: LiveData<ParentAssignmentResponse?>
        get() = isAssignmentlist


    fun isSubmitAssignment(
        isToken: String,
        jsonObject: JsonObject,
        activity: Activity
    ) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.isSubmitAssignment(isToken, jsonObject)
            ?.enqueue(object : Callback<AssignmentSubmitResponse?> {
                override fun onResponse(
                    call: Call<AssignmentSubmitResponse?>,
                    response: Response<AssignmentSubmitResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isSubmitAssignment.postValue(response.body())
                    } else {
                        isSubmitAssignment.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<AssignmentSubmitResponse?>, t: Throwable) {
                    isSubmitAssignment.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isSubmitAssignmentLiveData: LiveData<AssignmentSubmitResponse?>
        get() = isSubmitAssignment


    fun isStudentStats(
        isToken: String,
    ) {
        RestClient.apiInterfaces.getStudentStats(isToken)
            ?.enqueue(object : Callback<getStudentStats?> {
                override fun onResponse(
                    call: Call<getStudentStats?>,
                    response: Response<getStudentStats?>
                ) {

                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetStudentStatsData", response.body().toString())
                                isStudentStats.postValue(response.body())
                            } else {
                                Log.d("GetStudentStatsData", response.body().toString())
                                isStudentStats.postValue(response.body())
                            }
                        }
                    } else {
                        isStudentStats.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<getStudentStats?>,
                    t: Throwable
                ) {
                    isStudentStats.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isStudentStatsLiveData: LiveData<getStudentStats?>
        get() = isStudentStats


    fun getassignmentmysubmissionlist(
        isToken: String,
        id: String
    ) {
        RestClient.apiInterfaces.getassignmentmysubmissionlist(isToken, id)
            ?.enqueue(object : Callback<MySubmittedAssignmentsResponse?> {
                override fun onResponse(
                    call: Call<MySubmittedAssignmentsResponse?>,
                    response: Response<MySubmittedAssignmentsResponse?>
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
                                getassignmentmysubmissionlist.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getassignmentmysubmissionlist.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<MySubmittedAssignmentsResponse?>,
                    t: Throwable
                ) {
                    getassignmentmysubmissionlist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getassignmentmysubmissionlistLiveData: LiveData<MySubmittedAssignmentsResponse?>
        get() = getassignmentmysubmissionlist


    fun isSlotBookingStudent(
        isToken: String,
        jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isBookingForStudent(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>,
                    response: Response<StatusMessageModel?>
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
                                isSlotBookingStudent.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isSlotBookingStudent.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StatusMessageModel?>,
                    t: Throwable
                ) {
                    isSlotBookingStudent.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSlotBookingStudentLiveData: LiveData<StatusMessageModel?>
        get() = isSlotBookingStudent


    fun isSlotAvailableForStudent(
        isToken: String,
        event_date: String,
        subject_id: String,
        class_teacher_id: String,
    ) {
        RestClient.apiInterfaces.isSlotsAvailabilityForStudent(
            isToken,
            event_date,
            subject_id,
            class_teacher_id
        )
            ?.enqueue(object : Callback<MeetingResponse?> {
                override fun onResponse(
                    call: Call<MeetingResponse?>,
                    response: Response<MeetingResponse?>
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
                                isStudentSlotResponse.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isStudentSlotResponse.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<MeetingResponse?>,
                    t: Throwable
                ) {
                    isStudentSlotResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isStudentSlotResponseLiveData: LiveData<MeetingResponse?>
        get() = isStudentSlotResponse

    fun isAvailableSlotsCountForStudent(
        isToken: String,
    ) {
        RestClient.apiInterfaces.isAvailableSlotsCountForStudent(isToken)
            ?.enqueue(object : Callback<AvailableSlotsResponse?> {
                override fun onResponse(
                    call: Call<AvailableSlotsResponse?>,
                    response: Response<AvailableSlotsResponse?>
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
                                isAvailableSlotsResponse.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isAvailableSlotsResponse.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AvailableSlotsResponse?>,
                    t: Throwable
                ) {
                    isAvailableSlotsResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isAvailableSlotsResponseLiveData: LiveData<AvailableSlotsResponse?>
        get() = isAvailableSlotsResponse


    fun isASlotCancelByStudent(
        isToken: String,
        jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isCancelByStudent(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>,
                    response: Response<StatusMessageModel?>
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
                                isSlotCancelByStudent.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isSlotCancelByStudent.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StatusMessageModel?>,
                    t: Throwable
                ) {
                    isSlotCancelByStudent.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSlotCancelByStudentLiveData: LiveData<StatusMessageModel?>
        get() = isSlotCancelByStudent


    fun isSlotHistoryForStudent(
        isToken: String
    ) {
        RestClient.apiInterfaces.isSlotHistoryForStudent(isToken)
            ?.enqueue(object : Callback<MeetingHistoryResponse?> {
                override fun onResponse(
                    call: Call<MeetingHistoryResponse?>,
                    response: Response<MeetingHistoryResponse?>
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
                                isSlotDetailsHistory.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isSlotDetailsHistory.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<MeetingHistoryResponse?>,
                    t: Throwable
                ) {
                    isSlotDetailsHistory.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSlotDetailsHistoryLiveData: LiveData<MeetingHistoryResponse?>
        get() = isSlotDetailsHistory



    fun isQuizExamList(
        isToken: String,
        type: String,
        status_type: String,
    ) {
        RestClient.apiInterfaces.isQuizExamList(isToken,type,status_type)
            ?.enqueue(object : Callback<GetQuizExamList?> {
                override fun onResponse(
                    call: Call<GetQuizExamList?>,
                    response: Response<GetQuizExamList?>
                ) {
                    Log.d(
                        "GetQuizExamList Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetQuizExamListData", response.body().toString())
                                isQuizExamList.postValue(response.body())
                            } else {
                                Log.d("GetQuizExamListData", response.body().toString())
                                isQuizExamList.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetQuizExamList?>,
                    t: Throwable
                ) {
                    isQuizExamList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isQuizExamListLiveData: LiveData<GetQuizExamList?>
        get() = isQuizExamList



    fun isGetQuestions(
        isToken: String,
        id: String,
    ) {
        RestClient.apiInterfaces.isGetQuestion(isToken,id)
            ?.enqueue(object : Callback<GetQuizQuestions?> {
                override fun onResponse(
                    call: Call<GetQuizQuestions?>,
                    response: Response<GetQuizQuestions?>
                ) {
                    Log.d(
                        "GetQuizQuestions Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetQuizQuestionsData", response.body().toString())
                                isGetQuestions.postValue(response.body())
                            } else {
                                Log.d("GetQuizQuestionsData", response.body().toString())
                                isGetQuestions.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetQuizQuestions?>,
                    t: Throwable
                ) {
                    isGetQuestions.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetQuestionLiveData: LiveData<GetQuizQuestions?>
        get() = isGetQuestions


    fun isSubmitQuiz(
        isToken: String,
        jsonObject: JsonObject,
    ) {
        RestClient.apiInterfaces.isSubmitQuiz(isToken,jsonObject)
            ?.enqueue(object : Callback<SubmitQuizResponse?> {
                override fun onResponse(
                    call: Call<SubmitQuizResponse?>,
                    response: Response<SubmitQuizResponse?>
                ) {
                    Log.d(
                        "SubmitQuizResponse Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("SubmitQuizResponse", response.body().toString())
                                isSubmitQuiz.postValue(response.body())
                            } else {
                                Log.d("SubmitQuizResponse", response.body().toString())
                                isSubmitQuiz.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<SubmitQuizResponse?>,
                    t: Throwable
                ) {
                    isSubmitQuiz.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSubmitQuizLiveData: LiveData<SubmitQuizResponse?>
        get() = isSubmitQuiz





    fun isSpentPoints(
        isToken: String,
        jsonObject: JsonObject,
    ) {
        RestClient.apiInterfaces.isSpentPoints(isToken,jsonObject)
            ?.enqueue(object : Callback<SpentPointsModel?> {
                override fun onResponse(
                    call: Call<SpentPointsModel?>,
                    response: Response<SpentPointsModel?>
                ) {
                    Log.d(
                        "SubmitQuizResponse Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("SubmitQuizResponse", response.body().toString())
                                isSpentPoints.postValue(response.body())
                            } else {
                                Log.d("SubmitQuizResponse", response.body().toString())
                                isSpentPoints.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<SpentPointsModel?>,
                    t: Throwable
                ) {
                    isSpentPoints.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSpentPointsLiveData: LiveData<SpentPointsModel?>
        get() = isSpentPoints




    fun isGetMySubmission(
        isToken: String,
        id: String,
    ) {
        RestClient.apiInterfaces.isGetMySubmission(isToken,id)
            ?.enqueue(object : Callback<GetMySubmission?> {
                override fun onResponse(
                    call: Call<GetMySubmission?>,
                    response: Response<GetMySubmission?>
                ) {
                    Log.d(
                        "GetMySubmission Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMySubmissionData", response.body().toString())
                                isGetMySubmission.postValue(response.body())
                            } else {
                                Log.d("GetMySubmissionData", response.body().toString())
                                isGetMySubmission.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetMySubmission?>,
                    t: Throwable
                ) {
                    isGetMySubmission.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isMySubmissionLiveData: LiveData<GetMySubmission?>
        get() = isGetMySubmission



    fun islsrwSkilllist(
        isToken: String
    ) {
        RestClient.apiInterfaces.islsrwSkilllist(isToken)
            ?.enqueue(object : Callback<LsrwSkillResponse?> {
                override fun onResponse(
                    call: Call<LsrwSkillResponse?>,
                    response: Response<LsrwSkillResponse?>
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
                                islsrwSkilllist.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                islsrwSkilllist.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<LsrwSkillResponse?>,
                    t: Throwable
                ) {
                    islsrwSkilllist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val islsrwSkilllistLiveData: LiveData<LsrwSkillResponse?>
        get() = islsrwSkilllist




    fun islsrwSkillSubmit(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.islsrwSkillSubmit(isToken, jsonObject)
            ?.enqueue(object : Callback<LSRWSkillSubmitResponse?> {
                override fun onResponse(
                    call: Call<LSRWSkillSubmitResponse?>, response: Response<LSRWSkillSubmitResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        islsrwSkillSubmit.postValue(response.body())
                    } else {
                        islsrwSkillSubmit.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<LSRWSkillSubmitResponse?>, t: Throwable) {
                    islsrwSkillSubmit.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val islsrwSkillSubmitLiveData: LiveData<LSRWSkillSubmitResponse?>
        get() = islsrwSkillSubmit






    fun isGetPauketPoints(
        isToken: String,
        mobile_number: Long,
        user_type: Int,
    ) {
        RestClient.apiInterfaces.isGetPauketPoints(isToken,mobile_number,user_type)
            ?.enqueue(object : Callback<PauketPointsResponse?> {
                override fun onResponse(
                    call: Call<PauketPointsResponse?>,
                    response: Response<PauketPointsResponse?>
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
                                isGetPauketPoints.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isGetPauketPoints.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<PauketPointsResponse?>,
                    t: Throwable
                ) {
                    isGetPauketPoints.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetPauketPointsLiveData: LiveData<PauketPointsResponse?>
        get() = isGetPauketPoints




}