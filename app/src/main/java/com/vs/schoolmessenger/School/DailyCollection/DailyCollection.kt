package com.vs.schoolmessenger.School.DailyCollection

import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.DailyCollectionBinding
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference


class DailyCollection : BaseActivity<DailyCollectionBinding>(), View.OnClickListener {

    private var isAccessToken: String? = null
    private var appViewModel: App? = null


    var mAdapter: DcfAdapter? = null
    override fun getViewBinding(): DailyCollectionBinding {
        return DailyCollectionBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.className.setOnClickListener(this)
        binding.modeName.setOnClickListener (this)
        binding.categoryName.setOnClickListener(this)
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
//        isGetDailyCollection()
//        appViewModel?.isGetDailyCollectionReport?.observe(this) { response ->
//            Log.d("response++",response.toString())
//            if (response!!.status) {
//                isloadhomeworkData(response.data)
//                Log.d("GetHomeWorkDetails", response.data.toString())
//                Log.d("Access Token",isAccessToken.toString())
//            }
//        }


    }

//    private fun isloadhomeworkData(newData: List<DailyCollectionItem>?) {
//
//        Log.d("GetHomeworkDataWise", newData.toString())
//
//        mAdapter =
//            DcfAdapter(newData, this, this, Constant.isShimmerViewDisable)
//        binding.totalsummary1.adapter = mAdapter
//
//    }

//    fun isGetDailyCollection() {
//        mAdapter = DcfAdapter(null, this, this, Constant.isShimmerViewShow)
//        binding.totalsummary1.layoutManager = LinearLayoutManager(this)
//        binding.totalsummary1.isNestedScrollingEnabled = false
//        binding.totalsummary1.adapter = mAdapter
//        appViewModel!!.isGetDailyCollectionReport(
//            isAccessToken!!, this
//        )
//    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.class_name -> {
                binding.className.setBackgroundResource(R.drawable.custom_category_background)
                binding.className.setTextColor(Color.WHITE)
                binding.modeName.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.categoryName.setBackgroundResource(R.drawable.custom_rounded_background2)

            }
            R.id.mode_name -> {
                binding.modeName.setBackgroundResource(R.drawable.custom_category_background)
                binding.modeName.setTextColor(Color.WHITE)
                binding.className.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.categoryName.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.className.setHintTextColor(ContextCompat.getColor(this, R.color.grey))
            }

            R.id.category_name -> {
                binding.categoryName.setBackgroundResource(R.drawable.custom_category_background)
                binding.categoryName.setTextColor(Color.WHITE)
                binding.modeName.background = null
                binding.className.background = null
            }
        }

    }
    }