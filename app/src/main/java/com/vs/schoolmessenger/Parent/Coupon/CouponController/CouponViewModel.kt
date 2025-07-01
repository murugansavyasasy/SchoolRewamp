package com.vs.schoolmessenger.Parent.Coupon.CouponController

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.Category
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.CouponMenuResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CouponViewModel : ViewModel() {
    private val API_KEY = "33adab6a67a9eee6e72be49acfb6c100"
    private val PARTNER_NAME = "savyasasy"

    private val _categoriesLiveData = MutableLiveData<List<Category>>()
    val categoriesLiveData: LiveData<List<Category>> get() = _categoriesLiveData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchCategories() {
        _isLoading.value = true
        CouponRetrofitNetworkCall.apiService.getCategories(PARTNER_NAME, API_KEY)
            .enqueue(object : Callback<CouponMenuResponse> {
                override fun onResponse(
                    call: Call<CouponMenuResponse>,
                    response: Response<CouponMenuResponse>
                ) {
                    _isLoading.value = false
                    val list = response.body()?.data?.categories?.filterNotNull()
                    _categoriesLiveData.value = list ?: emptyList()
                }

                override fun onFailure(call: Call<CouponMenuResponse>, t: Throwable) {
                    _isLoading.value = false
                    _categoriesLiveData.value = emptyList()
                }
            })
    }
}