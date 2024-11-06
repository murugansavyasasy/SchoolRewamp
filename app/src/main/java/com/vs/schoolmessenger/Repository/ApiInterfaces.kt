package com.vs.schoolmessenger.Repository
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Splash.VersionCheckResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterfaces {

    @GET(APIMethods.Country)
    fun Getcountrylist(@Query(RequestKeys.Req_id) AppId: Int): Call<JsonArray?>?

    @POST(APIMethods.VersionCheck)
    fun VersionCheck(@Body jsonObject: JsonObject): Call<VersionCheckResponse?>?

}