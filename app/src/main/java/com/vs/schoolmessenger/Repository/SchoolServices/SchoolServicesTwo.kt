package com.vs.schoolmessenger.Repository.SchoolServices

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.ClassTestMarkResponse
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.ClassTestResponse
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.getParentHostelDashboard
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDetails.getParentHostelDetails
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.School.ClassTest.Report.DeleteClassTestExamModel.DeleteClassTestResponse
import com.vs.schoolmessenger.School.ClassTest.Report.ExamTestDeleteModel.ExamTestDeleteResponse
import com.vs.schoolmessenger.School.ClassTest.Report.Model.ExamlistModelResponse
import com.vs.schoolmessenger.School.ClassTest.Review.Model.CreateClassTestResponse
import com.vs.schoolmessenger.School.ClassTest.Subject.ModelClass.Subjectlistresponse
import com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model.ClassEntryMarkResponse
import com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model.UploadMarksClassEntryResponse
import com.vs.schoolmessenger.School.ExamReview.AnalysisSetResponseModel.AnalysisSetsResponse
import com.vs.schoolmessenger.School.ExamReview.ApiResponseModel.StudentAnalysisResponse
import com.vs.schoolmessenger.School.Homework.HomeworkSubmissionListModel.GetHomeWorkSubmissionList
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getSchoolHostelAttendanceReport
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelAttendanceSessionType.getHostelAttendanceSession
import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.getHostelDashboard
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.getHostelList
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.getSchoolHostelOutpassRequest
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassUpdateStatus.schoolHostelOutpassUpdateStatus
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelRoomAttendanceStudentList.getHostelStudentRoomAttendance
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.RoomMarkAttendance.hostelMarkAttendanceRespone
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveApproveRequest
import kotlinx.serialization.StringFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SchoolServicesTwo {
    var client_auth: RestClient

    var isGetSubmissionList: MutableLiveData<GetHomeWorkSubmissionList?>
    var isGetHostelList: MutableLiveData<getHostelList?>
    var isGetHostelDashboardDetails: MutableLiveData<getHostelDashboard?>
    var isGetHostelAttendanceSession: MutableLiveData<getHostelAttendanceSession?>
    var isGetHostelAttendanceStudentList: MutableLiveData<getHostelStudentRoomAttendance?>
    var isHostelMarkAttendance: MutableLiveData<hostelMarkAttendanceRespone?>
    var isHostelSchoolOutpassRequest: MutableLiveData<getSchoolHostelOutpassRequest?>
    var isGetSchoolHostelAttendanceReport: MutableLiveData<getSchoolHostelAttendanceReport?>
    var schoolHostelOutpassUpdateStatus: MutableLiveData<schoolHostelOutpassUpdateStatus?>
    var getParentHostelDetails: MutableLiveData<getParentHostelDetails?>
    var getParentHostelDashboard: MutableLiveData<getParentHostelDashboard?>
    var isSectionwisesubjectsdetail: MutableLiveData<Subjectlistresponse?>
    var isClassTestResponse: MutableLiveData<ClassTestResponse?>
    var isViewClassTestResponse: MutableLiveData<ClassTestMarkResponse?>
    var isCreateClasstest: MutableLiveData<CreateClassTestResponse?>
    var isgetExamreportdetails: MutableLiveData<ExamlistModelResponse?>
    var isputExamDelete: MutableLiveData<ExamTestDeleteResponse?>
    var isputClassTestDelete: MutableLiveData<DeleteClassTestResponse?>
    var isexamDetailsMark: MutableLiveData<ClassEntryMarkResponse?>
    var isexamdetailsmarkpost: MutableLiveData<UploadMarksClassEntryResponse?>
    var isExamtestAnalysis: MutableLiveData<StudentAnalysisResponse?>
    var isExamtestAnalysisSets: MutableLiveData<AnalysisSetsResponse?>


    init {
        client_auth = RestClient()

        isGetSubmissionList = MutableLiveData()
        isGetHostelList = MutableLiveData()
        isGetHostelDashboardDetails = MutableLiveData()
        isGetHostelAttendanceSession = MutableLiveData()
        isGetHostelAttendanceStudentList = MutableLiveData()
        isHostelMarkAttendance = MutableLiveData()
        isHostelSchoolOutpassRequest = MutableLiveData()
        isGetSchoolHostelAttendanceReport = MutableLiveData()
        schoolHostelOutpassUpdateStatus = MutableLiveData()
        getParentHostelDetails = MutableLiveData()
        getParentHostelDashboard = MutableLiveData()
        isSectionwisesubjectsdetail = MutableLiveData()
        isClassTestResponse = MutableLiveData()
        isViewClassTestResponse = MutableLiveData()
        isCreateClasstest = MutableLiveData()
        isgetExamreportdetails = MutableLiveData()
        isputExamDelete = MutableLiveData()
        isputClassTestDelete = MutableLiveData()
        isexamDetailsMark = MutableLiveData()
        isexamdetailsmarkpost = MutableLiveData()
        isExamtestAnalysis = MutableLiveData()
        isExamtestAnalysisSets = MutableLiveData()

    }

    fun getHomeworkSubmissionList(
        isToken: String, id: String, activity: Activity
    ) {
        RestClient.Companion.apiInterfaces.getHomeworkSubmissionList(isToken,id)
            ?.enqueue(object : Callback<GetHomeWorkSubmissionList?> {
                override fun onResponse(
                    call: Call<GetHomeWorkSubmissionList?>, response: Response<GetHomeWorkSubmissionList?>
                ) {
                    Log.d(
                        "GetHomeWorkSubmissionList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isGetSubmissionList.postValue(response.body())
                        }
                    } else {
                        isGetSubmissionList.postValue(null)
                    }
                }

                override fun onFailure(call: Call<GetHomeWorkSubmissionList?>, t: Throwable) {
                    isGetSubmissionList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isHomeworkSubmissionListLiveData: LiveData<GetHomeWorkSubmissionList?>
        get() = isGetSubmissionList

    fun getHostelList(
        isToken: String, activity: Activity
    ) {
        RestClient.Companion.apiInterfaces.getHotelList(isToken)
            ?.enqueue(object : Callback<getHostelList?> {
                override fun onResponse(
                    call: Call<getHostelList?>, response: Response<getHostelList?>
                ) {
                    Log.d(
                        "getHostelList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isGetHostelList.postValue(response.body())
                        }
                    } else {
                        isGetHostelList.postValue(null)
                    }
                }

                override fun onFailure(call: Call<getHostelList?>, t: Throwable) {
                    isGetHostelList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isHostelListLiveData: LiveData<getHostelList?>
        get() = isGetHostelList



    fun getHostelDashboardDetails(
        isToken: String, hostel_id : String,academic_year_id :String,activity: Activity
    ) {
        RestClient.Companion.apiInterfaces.getHostelDasboard(isToken,hostel_id,academic_year_id )
            ?.enqueue(object : Callback<getHostelDashboard?> {
                override fun onResponse(
                    call: Call<getHostelDashboard?>, response: Response<getHostelDashboard?>
                ) {
                    Log.d(
                        "getHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isGetHostelDashboardDetails.postValue(response.body())
                        }
                    } else {
                        isGetHostelDashboardDetails.postValue(null)
                    }
                }

                override fun onFailure(call: Call<getHostelDashboard?>, t: Throwable) {
                    isGetHostelDashboardDetails.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isHostelDashboardDetailsLiveData: LiveData<getHostelDashboard?>
        get() = isGetHostelDashboardDetails


    fun getHostelAttendanceSession(
        isToken: String,activity: Activity
    ) {
        RestClient.Companion.apiInterfaces.getHostelAttendanceSession(isToken)
            ?.enqueue(object : Callback<getHostelAttendanceSession?> {
                override fun onResponse(
                    call: Call<getHostelAttendanceSession?>, response: Response<getHostelAttendanceSession?>
                ) {
                    Log.d(
                        "getHostelAttendanceSession", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isGetHostelAttendanceSession.postValue(response.body())
                        }
                    } else {
                        isGetHostelAttendanceSession.postValue(null)
                    }
                }

                override fun onFailure(call: Call<getHostelAttendanceSession?>, t: Throwable) {
                    isGetHostelAttendanceSession.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isHostelAttendanceSessionLiveData: LiveData<getHostelAttendanceSession?>
        get() = isGetHostelAttendanceSession


    fun getHostelAttendanceRoomStudentList(
        isToken: String,hostel_id : String, room_id: String, academic_year_id :String, date : String,session_type_id:String,activity: Activity
    ) {

        RestClient.Companion.apiInterfaces.getHostelAttendanceRoomStudentList(isToken,hostel_id,room_id,academic_year_id,date,session_type_id)
            ?.enqueue(object : Callback<getHostelStudentRoomAttendance?> {
                override fun onResponse(
                    call: Call<getHostelStudentRoomAttendance?>, response: Response<getHostelStudentRoomAttendance?>
                ) {
                    Log.d(
                        "getHostelStudentRoomAttendance", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isGetHostelAttendanceStudentList.postValue(response.body())
                        }
                    } else {
                        isGetHostelAttendanceStudentList.postValue(null)
                    }
                }

                override fun onFailure(call: Call<getHostelStudentRoomAttendance?>, t: Throwable) {
                    isGetHostelAttendanceStudentList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isHostelAttendanceRoomStudentListLiveData: LiveData<getHostelStudentRoomAttendance?>
        get() = isGetHostelAttendanceStudentList

    fun hostelMarkAtttendance(
        isToken: String,
        jsonObject: JsonObject,activity: Activity
    ) {

        RestClient.Companion.apiInterfaces.hostelMarkAttendance(isToken,jsonObject)
            ?.enqueue(object : Callback<hostelMarkAttendanceRespone?> {
                override fun onResponse(
                    call: Call<hostelMarkAttendanceRespone?>, response: Response<hostelMarkAttendanceRespone?>
                ) {
                    Log.d(
                        "isHostelMarkAttendance", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isHostelMarkAttendance.postValue(response.body())
                        }
                    } else {
                        isHostelMarkAttendance.postValue(null)
                    }
                }

                override fun onFailure(call: Call<hostelMarkAttendanceRespone?>, t: Throwable) {
                    isHostelMarkAttendance.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isHotelMarkAttendanceLiveData: LiveData<hostelMarkAttendanceRespone?>
        get() = isHostelMarkAttendance



    fun getHostelSchoolOutpassRequestList(
        isToken: String, year_id  : String, month_id : String, hostel_id  :String, academic_year_id  : String, activity: Activity
    ) {

        RestClient.Companion.apiInterfaces.getHostelSchoolOutpassList(isToken,year_id,month_id,hostel_id,academic_year_id)
            ?.enqueue(object : Callback<getSchoolHostelOutpassRequest?> {
                override fun onResponse(
                    call: Call<getSchoolHostelOutpassRequest?>, response: Response<getSchoolHostelOutpassRequest?>
                ) {
                    Log.d(
                        "getSchoolHostelOutpassRequest", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isHostelSchoolOutpassRequest.postValue(response.body())
                        }
                    } else {
                        isHostelSchoolOutpassRequest.postValue(null)
                    }
                }

                override fun onFailure(call: Call<getSchoolHostelOutpassRequest?>, t: Throwable) {
                    isHostelSchoolOutpassRequest.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isHotelSchoolOutpassRequestLiveData: LiveData<getSchoolHostelOutpassRequest?>
        get() = isHostelSchoolOutpassRequest

 fun getSchoolHostelAttendanceReport(
     isToken: String, hostel_id : String, date: String, academic_year_id:String,activity: Activity
    ) {

        RestClient.Companion.apiInterfaces.getHostelSchoolAttendanceReport(isToken,hostel_id,date,academic_year_id)
            ?.enqueue(object : Callback<getSchoolHostelAttendanceReport?> {
                override fun onResponse(
                    call: Call<getSchoolHostelAttendanceReport?>, response: Response<getSchoolHostelAttendanceReport?>
                ) {
                    Log.d(
                        "getSchoolHostelAttendanceReport", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isGetSchoolHostelAttendanceReport.postValue(response.body())
                        }
                    } else {
                        isGetSchoolHostelAttendanceReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<getSchoolHostelAttendanceReport?>, t: Throwable) {
                    isGetSchoolHostelAttendanceReport.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isHotelSchoolAttendanceReportLiveData: LiveData<getSchoolHostelAttendanceReport?>
        get() = isGetSchoolHostelAttendanceReport




    fun schoolHostelOutpassUpdateStatus(
        isToken: String,
        isSchoolHostelOutpassStatus: LeaveApproveRequest,activity: Activity
    ) {

        RestClient.Companion.apiInterfaces.isSchoolHostelOutpassUpdateStatus(isToken,isSchoolHostelOutpassStatus)
            ?.enqueue(object : Callback<schoolHostelOutpassUpdateStatus?> {
                override fun onResponse(
                    call: Call<schoolHostelOutpassUpdateStatus?>, response: Response<schoolHostelOutpassUpdateStatus?>
                ) {
                    Log.d(
                        "schoolHostelOutpassUpdateStatus", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            schoolHostelOutpassUpdateStatus.postValue(response.body())
                        }
                    } else {
                        schoolHostelOutpassUpdateStatus.postValue(null)
                    }
                }

                override fun onFailure(call: Call<schoolHostelOutpassUpdateStatus?>, t: Throwable) {
                    schoolHostelOutpassUpdateStatus.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSchoolHostelOutpassUpdateStatusLiveData: LiveData<schoolHostelOutpassUpdateStatus?>
        get() = schoolHostelOutpassUpdateStatus




    fun getParentHostelDetails(
        isToken: String ,activity: Activity
    ) {

        RestClient.Companion.apiInterfaces.getParentHostelDetails(isToken)
            ?.enqueue(object : Callback<getParentHostelDetails?> {
                override fun onResponse(
                    call: Call<getParentHostelDetails?>, response: Response<getParentHostelDetails?>
                ) {
                    Log.d(
                        "getParentHostelDetails", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            getParentHostelDetails.postValue(response.body())
                        }
                    } else {
                        getParentHostelDetails.postValue(null)
                    }
                }

                override fun onFailure(call: Call<getParentHostelDetails?>, t: Throwable) {
                    getParentHostelDetails.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isParentHotelDetailsLiveData: LiveData<getParentHostelDetails?>
        get() = getParentHostelDetails



    fun isGetParentHostelDashboard(
        isToken: String,hostel_id : Int,year_id : Int,month_id  : Int,country_id  : String,activity: Activity) {

        RestClient.Companion.apiInterfaces.parentHostelDashboard(isToken,hostel_id,year_id,month_id,country_id)
            ?.enqueue(object : Callback<getParentHostelDashboard?> {
                override fun onResponse(
                    call: Call<getParentHostelDashboard?>, response: Response<getParentHostelDashboard?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            getParentHostelDashboard.postValue(response.body())
                        }
                    } else {
                        getParentHostelDashboard.postValue(null)
                    }
                }

                override fun onFailure(call: Call<getParentHostelDashboard?>, t: Throwable) {
                    getParentHostelDashboard.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isParentHotelDashBoardLiveData: LiveData<getParentHostelDashboard?>
        get() = getParentHostelDashboard





    fun isSectionwisesubjectsdetail(
        isToken: String,section_ids : String,activity: Activity) {
        RestClient.Companion.apiInterfaces.isgetSectionWiseSubjects(isToken,section_ids)
            ?.enqueue(object : Callback<Subjectlistresponse?> {
                override fun onResponse(
                    call: Call<Subjectlistresponse?>, response: Response<Subjectlistresponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isSectionwisesubjectsdetail.postValue(response.body())
                        }
                    } else {
                        isSectionwisesubjectsdetail.postValue(null)
                    }
                }

                override fun onFailure(call: Call<Subjectlistresponse?>, t: Throwable) {
                    isSectionwisesubjectsdetail.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSectionwisesubjectsdetailLiveData: LiveData<Subjectlistresponse?>
        get() = isSectionwisesubjectsdetail


    fun isClassTesFortStudent(
        isToken: String,activity: Activity) {
        RestClient.Companion.apiInterfaces.isClassTestStudent(isToken)
            ?.enqueue(object : Callback<ClassTestResponse?> {
                override fun onResponse(
                    call: Call<ClassTestResponse?>, response: Response<ClassTestResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isClassTestResponse.postValue(response.body())
                        }
                    } else {
                        isClassTestResponse.postValue(null)
                    }
                }

                override fun onFailure(call: Call<ClassTestResponse?>, t: Throwable) {
                    isClassTestResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isClassTestResponselLiveData: LiveData<ClassTestResponse?>
        get() = isClassTestResponse

    fun isViewClassTesFortStudent(
        isToken: String, isClassTestId: String, activity: Activity) {
        RestClient.Companion.apiInterfaces.isViewClassTestStudent(isToken,isClassTestId)
            ?.enqueue(object : Callback<ClassTestMarkResponse?> {
                override fun onResponse(
                    call: Call<ClassTestMarkResponse?>, response: Response<ClassTestMarkResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isViewClassTestResponse.postValue(response.body())
                        }
                    } else {
                        isViewClassTestResponse.postValue(null)
                    }
                }

                override fun onFailure(call: Call<ClassTestMarkResponse?>, t: Throwable) {
                    isViewClassTestResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isViewClassTestResponselLiveData: LiveData<ClassTestMarkResponse?>
        get() = isViewClassTestResponse




    fun isCreateClasstest(
        isToken: String,jsonArray: JsonArray) {
        RestClient.Companion.apiInterfaces.ispostCreateClasstest(isToken,jsonArray)
            ?.enqueue(object : Callback<CreateClassTestResponse?> {
                override fun onResponse(
                    call: Call<CreateClassTestResponse?>, response: Response<CreateClassTestResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isCreateClasstest.postValue(response.body())
                        }
                    } else {
                        isCreateClasstest.postValue(null)
                    }
                }

                override fun onFailure(call: Call<CreateClassTestResponse?>, t: Throwable) {
                    isCreateClasstest.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isCreateClasstestLiveData: LiveData<CreateClassTestResponse?>
        get() = isCreateClasstest




    fun isgetExamreportdetails(
        isToken: String, class_test_id: String,exam_date: String,academic_year_id: String, activity: Activity) {
        RestClient.Companion.apiInterfaces.isgetExamreportdetails(isToken,class_test_id,exam_date,academic_year_id)
            ?.enqueue(object : Callback<ExamlistModelResponse?> {
                override fun onResponse(
                    call: Call<ExamlistModelResponse?>, response: Response<ExamlistModelResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isgetExamreportdetails.postValue(response.body())
                        }
                    } else {
                        isgetExamreportdetails.postValue(null)
                    }
                }

                override fun onFailure(call: Call<ExamlistModelResponse?>, t: Throwable) {
                    isgetExamreportdetails.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isgetExamreportdetailsLiveData: LiveData<ExamlistModelResponse?>
        get() = isgetExamreportdetails


    fun isputExamDelete(
        isToken: String, jsonObject: JsonObject , activity: Activity) {
        RestClient.Companion.apiInterfaces.isputExamDelete(isToken,jsonObject)
            ?.enqueue(object : Callback<ExamTestDeleteResponse?> {
                override fun onResponse(
                    call: Call<ExamTestDeleteResponse?>, response: Response<ExamTestDeleteResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isputExamDelete.postValue(response.body())
                        }
                    } else {
                        isputExamDelete.postValue(null)
                    }
                }

                override fun onFailure(call: Call<ExamTestDeleteResponse?>, t: Throwable) {
                    isputExamDelete.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isputExamDeleteLiveData: LiveData<ExamTestDeleteResponse?>
        get() = isputExamDelete


    fun isputClassTestDelete(
        isToken: String, jsonObject: JsonObject , activity: Activity) {
        RestClient.Companion.apiInterfaces.isputClassTestDelete(isToken,jsonObject)
            ?.enqueue(object : Callback<DeleteClassTestResponse?> {
                override fun onResponse(
                    call: Call<DeleteClassTestResponse?>, response: Response<DeleteClassTestResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isputClassTestDelete.postValue(response.body())
                        }
                    } else {
                        isputClassTestDelete.postValue(null)
                    }
                }

                override fun onFailure(call: Call<DeleteClassTestResponse?>, t: Throwable) {
                    isputClassTestDelete.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isputClassTestDeleteLiveData: LiveData<DeleteClassTestResponse?>
        get() = isputClassTestDelete



    fun isexamDetailsMark(
        isToken: String, class_test_id: String ,section_id: String,class_test_subject_id: String, activity: Activity) {
        RestClient.Companion.apiInterfaces.isexamDetailsMark(isToken,class_test_id,section_id,class_test_subject_id)
            ?.enqueue(object : Callback<ClassEntryMarkResponse?> {
                override fun onResponse(
                    call: Call<ClassEntryMarkResponse?>, response: Response<ClassEntryMarkResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isexamDetailsMark.postValue(response.body())
                        }
                    } else {
                        isexamDetailsMark.postValue(null)
                    }
                }

                override fun onFailure(call: Call<ClassEntryMarkResponse?>, t: Throwable) {
                    isexamDetailsMark.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isexamDetailsMarkLiveData: LiveData<ClassEntryMarkResponse?>
        get() = isexamDetailsMark



    fun isexamdetailsmarkpost(
        isToken: String,jsonObject: JsonObject) {
        RestClient.Companion.apiInterfaces.isexamdetailsmarkpost(isToken,jsonObject)
            ?.enqueue(object : Callback<UploadMarksClassEntryResponse?> {
                override fun onResponse(
                    call: Call<UploadMarksClassEntryResponse?>, response: Response<UploadMarksClassEntryResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isexamdetailsmarkpost.postValue(response.body())
                        }
                    } else {
                        isexamdetailsmarkpost.postValue(null)
                    }
                }

                override fun onFailure(call: Call<UploadMarksClassEntryResponse?>, t: Throwable) {
                    isexamdetailsmarkpost.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isexamdetailsmarkpostLiveData: LiveData<UploadMarksClassEntryResponse?>
        get() = isexamdetailsmarkpost



    fun isExamtestAnalysis(
        isToken: String,student_id: String,analysis_set_id: String,activity: Activity) {
        RestClient.Companion.apiInterfaces.isExamtestAnalysis(isToken,student_id,analysis_set_id)
            ?.enqueue(object : Callback<StudentAnalysisResponse?> {
                override fun onResponse(
                    call: Call<StudentAnalysisResponse?>, response: Response<StudentAnalysisResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isExamtestAnalysis.postValue(response.body())
                        }
                    } else {
                        isExamtestAnalysis.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StudentAnalysisResponse?>, t: Throwable) {
                    isExamtestAnalysis.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isExamtestAnalysisLiveData: LiveData<StudentAnalysisResponse?>
        get() = isExamtestAnalysis



    fun isExamtestAnalysisSets(
        isToken: String,class_id: String,section_id: String,activity: Activity) {
        RestClient.Companion.apiInterfaces.isExamtestAnalysisSets(isToken,class_id,section_id)
            ?.enqueue(object : Callback<AnalysisSetsResponse?> {
                override fun onResponse(
                    call: Call<AnalysisSetsResponse?>, response: Response<AnalysisSetsResponse?>
                ) {
                    Log.d(
                        "getParentHostelDashboard", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            isExamtestAnalysisSets.postValue(response.body())
                        }
                    } else {
                        isExamtestAnalysisSets.postValue(null)
                    }
                }

                override fun onFailure(call: Call<AnalysisSetsResponse?>, t: Throwable) {
                    isExamtestAnalysisSets.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isExamtestAnalysisSetsLiveData: LiveData<AnalysisSetsResponse?>
        get() = isExamtestAnalysisSets

}