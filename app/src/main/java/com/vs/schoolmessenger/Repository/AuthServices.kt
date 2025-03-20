package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Country.CountryResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordCreationResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordResetResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.PasswordUpdateResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetailsResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationResponse
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpSendResponse
import com.vs.schoolmessenger.Auth.OTP.OtpResponse
import com.vs.schoolmessenger.Auth.Splash.VersionCheckResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthServices {
    var client_auth: RestClient
    var isCountryList: MutableLiveData<CountryResponse?>
    var isVersionCheck: MutableLiveData<VersionCheckResponse?>
    var isValidationUser: MutableLiveData<UserValidationResponse?>
    var isOtpResponse: MutableLiveData<OtpResponse?>
    var isUserDetails: MutableLiveData<UserDetailsResponse?>
    var isPasswordChange: MutableLiveData<PasswordCreationResponse?>
    var isForgetPassword: MutableLiveData<ForgetOtpSendResponse?>
    var isPasswordReset: MutableLiveData<PasswordResetResponse?>
    var isCreatePassWord: MutableLiveData<PasswordResetResponse?>

    init {
        client_auth = RestClient()
        isCountryList = MutableLiveData()
        isVersionCheck = MutableLiveData()
        isValidationUser = MutableLiveData()
        isOtpResponse = MutableLiveData()
        isUserDetails = MutableLiveData()
        isPasswordChange = MutableLiveData()
        isForgetPassword = MutableLiveData()
        isPasswordReset = MutableLiveData()
        isCreatePassWord = MutableLiveData()
    }

    fun isCountryList() {
        RestClient.apiInterfaces.isCountry()
            ?.enqueue(object : Callback<CountryResponse?> {
                override fun onResponse(
                    call: Call<CountryResponse?>,
                    response: Response<CountryResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isCountryList.postValue(response.body())
                            } else {
                                isCountryList.postValue(response.body())
                            }
                        }
                    } else if (response.code() == 400) {
                        if (response.body() != null) {
                            isCountryList.postValue(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<CountryResponse?>, t: Throwable) {
                    isCountryList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isCountryListLiveData: LiveData<CountryResponse?>
        get() = isCountryList


    fun isVersionCheck(jsonObject: JsonObject,activity: Activity) {
        RestClient.apiInterfaces.isVersionCheck(jsonObject)
            ?.enqueue(object : Callback<VersionCheckResponse?> {
                override fun onResponse(
                    call: Call<VersionCheckResponse?>,
                    response: Response<VersionCheckResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isVersionCheck.postValue(response.body())
                            } else {
                                isVersionCheck.postValue(response.body())
                            }
                        }
                    } else if (response.code() == 400) {
                        if (response.body() != null) {
                            isVersionCheck.postValue(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<VersionCheckResponse?>, t: Throwable) {
                    isVersionCheck.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isVersionCheckLiveData: LiveData<VersionCheckResponse?>
        get() = isVersionCheck

    fun isValidateUser(jsonObject: JsonObject,activity: Activity) {
        RestClient.apiInterfaces.isValidateUser(jsonObject)
            ?.enqueue(object : Callback<UserValidationResponse?> {
                override fun onResponse(
                    call: Call<UserValidationResponse?>,
                    response: Response<UserValidationResponse?>
                ) {
                    Log.d(
                        "isPasswordUpdateCheck",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isValidationUser.postValue(response.body())
                            } else {
                                isValidationUser.postValue(response.body())
                            }
                        }
                    } else {
                            isValidationUser.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<UserValidationResponse?>, t: Throwable) {
                    Log.d("isException",t.toString())
                    isValidationUser.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isUserValidationLiveData: LiveData<UserValidationResponse?>
        get() = isValidationUser


    fun isValidateOtp(jsonObject: JsonObject,activity: Activity) {
        RestClient.apiInterfaces.isValidateOtp(jsonObject)
            ?.enqueue(object : Callback<OtpResponse?> {
                override fun onResponse(
                    call: Call<OtpResponse?>,
                    response: Response<OtpResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isOtpResponse.postValue(response.body())
                            } else {
                                isOtpResponse.postValue(response.body())
                            }
                        }
                    } else if (response.code() == 400) {
                        if (response.body() != null) {
                            isOtpResponse.postValue(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<OtpResponse?>, t: Throwable) {
                    isOtpResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isOtpResponseLiveData: LiveData<OtpResponse?>
        get() = isOtpResponse

//    fun isUserDetails(isMobileNumber:String,isPassword:String,isDeviceType:String,isSecureId:String,activity: Activity) {
//        RestClient.apiInterfaces.isUserDetails(isMobileNumber,isPassword,isDeviceType,isSecureId)
//            ?.enqueue(object : Callback<UserDetailsResponse?> {
//                override fun onResponse(
//                    call: Call<UserDetailsResponse?>,
//                    response: Response<UserDetailsResponse?>
//                ) {
//                    Log.d(
//                        "isGetCountryList",
//                        response.code().toString() + " - " + response.toString()
//                    )
//                    if (response.code() == 200) {
//                        if (response.body() != null) {
//                            val status = response.body()!!.status
//                            if (status) {
//                                isUserDetails.postValue(response.body())
//                            } else {
//                                isUserDetails.postValue(response.body())
//                            }
//                        }
//                    } else if (response.code() == 400) {
//                        if (response.body() != null) {
//                            isUserDetails.postValue(response.body())
//                        }
//                    }
//                }
//
//                override fun onFailure(call: Call<UserDetailsResponse?>, t: Throwable) {
//                    isUserDetails.postValue(null)
//                    t.printStackTrace()
//                }
//            })
//    }
//
//    val isUserDetailsLiveData: LiveData<UserDetailsResponse?>
//        get() = isUserDetails

    fun isPasswordChange(jsonObject: JsonObject,activity: Activity) {
        RestClient.apiInterfaces.isPasswordChange(jsonObject)
            ?.enqueue(object : Callback<PasswordCreationResponse?> {
                override fun onResponse(
                    call: Call<PasswordCreationResponse?>,
                    response: Response<PasswordCreationResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isPasswordChange.postValue(response.body())
                            } else {
                                isPasswordChange.postValue(response.body())
                            }
                        }
                    } else if (response.code() == 400) {
                        if (response.body() != null) {
                            isPasswordChange.postValue(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<PasswordCreationResponse?>, t: Throwable) {
                    isPasswordChange.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isPasswordChangeLiveData: LiveData<PasswordCreationResponse?>
        get() = isPasswordChange

    fun isForgetPassword(jsonObject: JsonObject,activity: Activity) {
        RestClient.apiInterfaces.isForgetPassword(jsonObject)
            ?.enqueue(object : Callback<ForgetOtpSendResponse?> {
                override fun onResponse(
                    call: Call<ForgetOtpSendResponse?>,
                    response: Response<ForgetOtpSendResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isForgetPassword.postValue(response.body())
                            } else {
                                isForgetPassword.postValue(response.body())
                            }
                        }
                    } else if (response.code() == 400) {
                        if (response.body() != null) {
                            isForgetPassword.postValue(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<ForgetOtpSendResponse?>, t: Throwable) {
                    isForgetPassword.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isForgetPasswordLiveData: LiveData<ForgetOtpSendResponse?>
        get() = isForgetPassword

    fun isResetPassword(jsonObject: JsonObject,activity: Activity) {
        RestClient.apiInterfaces.isResetPassword(jsonObject)
            ?.enqueue(object : Callback<PasswordResetResponse?> {
                override fun onResponse(
                    call: Call<PasswordResetResponse?>,
                    response: Response<PasswordResetResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isPasswordReset.postValue(response.body())
                            } else {
                                isPasswordReset.postValue(response.body())
                            }
                        }
                    } else if (response.code() == 400) {
                        if (response.body() != null) {
                            isPasswordReset.postValue(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<PasswordResetResponse?>, t: Throwable) {
                    isPasswordReset.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isPasswordResetLiveData: LiveData<PasswordResetResponse?>
        get() = isPasswordReset



    fun isCreateNewPassword(jsonObject: JsonObject,activity: Activity) {
        RestClient.apiInterfaces.isCreateNewPassword(jsonObject)
            ?.enqueue(object : Callback<PasswordResetResponse?> {
                override fun onResponse(
                    call: Call<PasswordResetResponse?>,
                    response: Response<PasswordResetResponse?>
                ) {
                    Log.d(
                        "isGetCountryList",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isCreatePassWord.postValue(response.body())
                            } else {
                                isCreatePassWord.postValue(response.body())
                            }
                        }
                    } else if (response.code() == 400) {
                        if (response.body() != null) {
                            isCreatePassWord.postValue(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<PasswordResetResponse?>, t: Throwable) {
                    isCreatePassWord.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isCreatePassWordLiveData: LiveData<PasswordResetResponse?>
        get() = isCreatePassWord




    
}