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
import com.vs.schoolmessenger.School.Communication.TextSendResponse
import com.vs.schoolmessenger.School.Communication.VoiceDetails
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
    var isSendText: MutableLiveData<TextSendResponse?>
    var isSendVoice: MutableLiveData<TextSendResponse?>
    var isUpdateStatusArchive: MutableLiveData<StatusArchiveResponse?>
    var isAcademicYear: MutableLiveData<AcademicYearResponse?>
    var isUpdateStatusCommunication: MutableLiveData<StatusArchiveResponse?>


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
        isSendText = MutableLiveData()
        isSendVoice = MutableLiveData()
        isUpdateStatusArchive = MutableLiveData()
        isAcademicYear = MutableLiveData()
        isUpdateStatusCommunication = MutableLiveData()
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

    fun isGetSubjectList(isToken: String, isAcademicYearId: Int, isSection: String, activity: Activity) {
        RestClient.apiInterfaces.getSubjectList(isToken,isAcademicYearId,isSection)
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
        RestClient.apiInterfaces.getStandard(isToken,isAcademicYearId)
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


    fun isGetStudentList(isToken: String, isSection: String, isAcademicYearId: Int, activity: Activity) {
        RestClient.apiInterfaces.getStudentList(isToken, isSection,isAcademicYearId)
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
                    call: Call<VoiceDataResponse?>,
                    response: Response<VoiceDataResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
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
                    call: Call<VoiceDataResponse?>,
                    response: Response<VoiceDataResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
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
        RestClient.apiInterfaces.isGroupList(isToken,isAcademicYearId)
            ?.enqueue(object : Callback<NameAndIdsResponse?> {
                override fun onResponse(
                    call: Call<NameAndIdsResponse?>,
                    response: Response<NameAndIdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
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
                    call: Call<VoiceDetails?>,
                    response: Response<VoiceDetails?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
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


    fun isSendText(isToken:String,jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.isSendText(isToken,jsonObject)
            ?.enqueue(object : Callback<TextSendResponse?> {
                override fun onResponse(
                    call: Call<TextSendResponse?>,
                    response: Response<TextSendResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isSendText.postValue(response.body())
                            } else {
                                isSendText.postValue(response.body())
                            }
                        }
                    } else {

                    }
                }

                override fun onFailure(call: Call<TextSendResponse?>, t: Throwable) {
                    isSendText.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSendTextLiveData: LiveData<TextSendResponse?>
        get() = isSendText



    fun isUpdateStatusArchive(isToken: String, jsonObject: JsonObject, activity: Activity) {
//        val request = StatusArchiveModelRequest(isToken, jsonObject)

        RestClient.apiInterfaces.isUpdateStatusArchive(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusArchiveResponse> {
                override fun onResponse(
                    call: Call<StatusArchiveResponse>,
                    response: Response<StatusArchiveResponse>
                ) {
                    Log.d("isUpdateStatusArchive", "${response.code()} - ${response.body()}")

                    if (response.code() == 200 && response.body() != null) {
                        isUpdateStatusArchive.postValue(response.body())
                    } else {
                        isUpdateStatusArchive.postValue(null)
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
                    call: Call<StatusArchiveResponse>,
                    response: Response<StatusArchiveResponse>
                ) {
                    Log.d("isUpdateStatusArchive", "${response.code()} - ${response.body()}")

                    if (response.code() == 200 && response.body() != null) {
                        isUpdateStatusCommunication.postValue(response.body())
                    } else {
                        isUpdateStatusCommunication.postValue(null)
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



    fun isSendVoice(isToken:String,jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.isSendVoice(isToken,jsonObject)
            ?.enqueue(object : Callback<TextSendResponse?> {
                override fun onResponse(
                    call: Call<TextSendResponse?>,
                    response: Response<TextSendResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isSendVoice.postValue(response.body())
                            } else {
                                isSendVoice.postValue(response.body())
                            }
                        }
                    } else {

                    }
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
                    call: Call<AcademicYearResponse?>,
                    response: Response<AcademicYearResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
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

}