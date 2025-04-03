package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.StaffListResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIdsResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.StandardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppServices {

    var client_auth: RestClient
    var isDashBoard: MutableLiveData<DashboardResponse?>
    var isGetAds: MutableLiveData<AdsResponse?>
    var isGetStaffList: MutableLiveData<StaffListResponse?>
    var isGetSubjectList: MutableLiveData<NameAndIdsResponse?>
    var isGetStandardSection: MutableLiveData<StandardResponse?>
    var isGetStudentList: MutableLiveData<NameAndIdsResponse?>
    var isGetGroupList: MutableLiveData<NameAndIdsResponse?>

    init {
        client_auth = RestClient()
        isDashBoard = MutableLiveData()
        isGetAds = MutableLiveData()
        isGetStaffList = MutableLiveData()
        isGetSubjectList = MutableLiveData()
        isGetStandardSection = MutableLiveData()
        isGetStudentList = MutableLiveData()
        isGetGroupList = MutableLiveData()
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
            ?.enqueue(object : Callback<StaffListResponse?> {
                override fun onResponse(
                    call: Call<StaffListResponse?>, response: Response<StaffListResponse?>
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

                override fun onFailure(call: Call<StaffListResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetStaffListLiveData: LiveData<StaffListResponse?>
        get() = isGetStaffList

    fun isGetSubjectList(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.getSubjectList(isToken)
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


    fun isGetStandardSection(isToken: String, isSection: String, activity: Activity) {
        RestClient.apiInterfaces.getStandard(isToken,isSection)
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


    fun isGetStudentList(isToken: String, isSection: String, activity: Activity) {
        RestClient.apiInterfaces.getStudentList(isToken,isSection)
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


    fun isGetGroupList(isToken:String,activity: Activity) {
        RestClient.apiInterfaces.isGroupList(isToken)
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
                    } else {
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






}