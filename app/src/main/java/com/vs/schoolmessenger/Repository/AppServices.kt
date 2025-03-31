package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vs.schoolmessenger.Dashboard.School.DashboardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppServices {

    var client_auth: RestClient
    var isDashBoard: MutableLiveData<DashboardResponse?>

    init {
        client_auth = RestClient()
        isDashBoard = MutableLiveData()
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
}