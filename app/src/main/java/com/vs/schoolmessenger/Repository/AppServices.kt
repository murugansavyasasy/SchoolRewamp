package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.GroupList.GroupListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppServices {

    var client_auth: RestClient
    var isDashBoard: MutableLiveData<DashboardResponse?>
    var isGetAds: MutableLiveData<AdsResponse?>
    var isGetGroupList: MutableLiveData<GroupListResponse?>


    init {
        client_auth = RestClient()
        isDashBoard = MutableLiveData()
        isGetAds = MutableLiveData()
        isGetGroupList = MutableLiveData()
    }


    fun isDashBoard(isToken:String,isMemberType: String,activity: Activity) {
        RestClient.apiInterfaces.isDashBoard(isToken,isMemberType)
            ?.enqueue(object : Callback<DashboardResponse?> {
                override fun onResponse(
                    call: Call<DashboardResponse?>,
                    response: Response<DashboardResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
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


    fun isGetAds(isToken:String,isMenuId: String,activity: Activity) {
        RestClient.apiInterfaces.isGetAds(isToken,isMenuId)
            ?.enqueue(object : Callback<AdsResponse?> {
                override fun onResponse(
                    call: Call<AdsResponse?>,
                    response: Response<AdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
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


    fun isGetGroupList(isToken:String,activity: Activity) {
        RestClient.apiInterfaces.isGroupList(isToken)
            ?.enqueue(object : Callback<GroupListResponse?> {
                override fun onResponse(
                    call: Call<GroupListResponse?>,
                    response: Response<GroupListResponse?>
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

                override fun onFailure(call: Call<GroupListResponse?>, t: Throwable) {
                    isGetGroupList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetGroupLiveData: LiveData<GroupListResponse?>
        get() = isGetGroupList






}