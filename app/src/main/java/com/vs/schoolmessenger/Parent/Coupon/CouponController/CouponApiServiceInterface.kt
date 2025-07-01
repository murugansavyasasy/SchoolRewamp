package com.vs.schoolmessenger.Parent.Coupon.CouponController

import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponCoin.PointsResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.CouponMenuResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CouponSummaryResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.LogactiveApiResponse.LogActiveApiResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCoupon.ActivateCouponResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCouponSummary.ActivateCouponSummaryResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummaryResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap


interface CouponAPIServiceInterface {
    @GET("get_category_list")
    fun getCategories(
        @Header("Partner-Name") parentName: String,
        @Header("api-key") apiKey: String
    ): Call<CouponMenuResponse>



    @POST("get_campaigns")
    fun getCoupons(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @QueryMap params: MutableMap<String?, String?>?
    ): Call<CouponSummaryResponse?>?

    @POST("get_campaign_details")
    fun getActivateCouponsDetails(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @QueryMap params: MutableMap<String?, String?>?
    ): Call<ActivateCouponSummaryResponse?>?

    @POST("activate_coupon")
    fun getactivateCoupon(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @QueryMap params: MutableMap<String?, String?>?
    ): Call<ActivateCouponResponse?>?

    @POST("my_coupons")
    fun getTicketCoupons(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @QueryMap params: MutableMap<String?, String?>?
    ): Call<TicketSummaryResponse?>?

    @GET("get-Points")
    fun getPointsCoupons(
        @Query("user_type") user_type: Int,
        @Query("mobile_number") mobile_number: String?
    ): Call<PointsResponse?>?


    @POST("spent-points")
    fun getlogactiveresponse(
        @Body body: MutableMap<String?, Any?>?
    ): Call<LogActiveApiResponse?>?


    @POST("get_campaigns")
    fun getCategoryCoupons(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @Body body: MutableMap<String?, String?>?
    ): Call<CouponSummaryResponse?>?
}

