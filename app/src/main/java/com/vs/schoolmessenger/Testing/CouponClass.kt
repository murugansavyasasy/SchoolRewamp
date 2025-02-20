package com.vs.schoolmessenger.Testing

import com.vs.schoolmessenger.databinding.DemoCouponBinding
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CouponClass : AppCompatActivity() {

    private lateinit var binding: DemoCouponBinding
    private lateinit var categoryAdapter: CouponMenuAdapter
    private lateinit var couponClassAdapter: CouponClassAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DemoCouponBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        fetchCategories()
        fetchCoupons()
    }

    private fun setupRecyclerView() {
        binding.recyclerView1.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun fetchCategories() {
        RetrofitClient.instance.getCategories().enqueue(object : Callback<CategoryResponse> {

            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val categoryList = response.body()!!.data.categories
                    categoryAdapter = CouponMenuAdapter(categoryList)
                    binding.recyclerView1.adapter = categoryAdapter
                } else {
                    Toast.makeText(this@CouponClass, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error: ${t.message}")
                Toast.makeText(this@CouponClass, "API call failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCoupons() {
        val headers = hashMapOf(
            "Partner-Name" to "voicesnaps",
            "api-key" to "b9634e2c3aa9b6fdc392527645c43871",
            "Content-Type" to "application/json"
        )

        val requestBody = HashMap<String, String>()
        requestBody["Mobile_no"] = "8610786768"

        RetrofitClient.instance.getCoupons(headers, requestBody).enqueue(object : Callback<CouponResponse> {
            override fun onResponse(call: Call<CouponResponse>, response: Response<CouponResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d("API_RESPONSE", response.body().toString())

                    // Extracting campaigns list correctly
                    val campaignsList = response.body()!!.data.campaigns.data // Updated this line

                    couponClassAdapter = CouponClassAdapter(campaignsList)
                    binding.recyclerView.adapter = couponClassAdapter
                } else {
                    Log.e("API_ERROR", "Response error: ${response.errorBody()?.string()}")
                    Toast.makeText(this@CouponClass, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CouponResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error: ${t.message}")
                Toast.makeText(this@CouponClass, "API call failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
