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
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference


class DailyCollection : BaseActivity<DailyCollectionBinding>(), View.OnClickListener,
    OnDateSelectedListener {

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var selectedDateTarget: Int = 0
    private var selectedType: String = "1"
    private var to_Date: String? = null
    private var from_Date: String? = null

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
        val isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token

        isGetDailyCollection()
        appViewModel?.isGetDailyCollectionReport?.observe(this) { response ->
            Log.d("response++",response.toString())
            if (response!!.status) {
                isLoadDailyCollectionData(response.data)
                Log.d("GetDailyCollectionRespone", response.data.toString())
                Log.d("Access Token",isAccessToken.toString())

            }
        }


    }

    private fun isLoadDailyCollectionData(data: List<DailyCollectionItem>?) {
        val flatList = mutableListOf<DisplayItem>()
        data?.forEach { item ->
            flatList.add(DisplayItem.Header(item.category, item.total))
            item.fee_data.forEach { fee ->
                flatList.add(DisplayItem.Fee(fee.type_name, fee.amount))
            }
        }

        mAdapter = DcfAdapter(flatList, this, Constant.isShimmerViewDisable)
        binding.totalsummary1.adapter = mAdapter
    }

    fun isGetDailyCollection() {
        if (from_Date.isNullOrEmpty() || to_Date.isNullOrEmpty()) return

        mAdapter = DcfAdapter(emptyList(), this, Constant.isShimmerViewShow)
        binding.totalsummary1.layoutManager = LinearLayoutManager(this)
        binding.totalsummary1.isNestedScrollingEnabled = false
        binding.totalsummary1.adapter = mAdapter

        appViewModel!!.isGetDailyCollectionReport(
            isAccessToken!!,
            selectedType,
            from_Date!!,
            to_Date!!,
            this
        )
    }

//BS
//    private fun isLoadDailyCollectionData(newData: List<DailyCollectionItem>?) {
//        Log.d("isLoadDailyCollectionData", newData.toString())
//
//        if (!newData.isNullOrEmpty()) {
//            // Load category and total into Activity layout
//            val firstItem = newData[0]
//            binding.totalValue.text = firstItem.total
//
//            // Load fee_data into RecyclerView
//            mAdapter = DcfAdapter(firstItem.fee_data, this, this, Constant.isShimmerViewDisable)
//            binding.totalsummary1.adapter = mAdapter
//        }
//    }
//
//
//    fun isGetDailyCollection() {
//        if (from_Date.isNullOrEmpty() || to_Date.isNullOrEmpty()) return
//
//        mAdapter = DcfAdapter(null, this, this, Constant.isShimmerViewShow)
//        binding.totalsummary1.layoutManager = LinearLayoutManager(this)
//        binding.totalsummary1.isNestedScrollingEnabled = false
//        binding.totalsummary1.adapter = mAdapter
//
//        appViewModel!!.isGetDailyCollectionReport(
//            isAccessToken!!, selectedType, from_Date!!, to_Date!!, this
//        )
//    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.class_name -> {
                selectedType="2"
                binding.className.setBackgroundResource(R.drawable.custom_category_background)
                binding.className.setTextColor(Color.WHITE)
                binding.modeName.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.categoryName.setBackgroundResource(R.drawable.custom_rounded_background2)
                isGetDailyCollection()

            }
            R.id.mode_name -> {
                selectedType="3"
                binding.modeName.setBackgroundResource(R.drawable.custom_category_background)
                binding.modeName.setTextColor(Color.WHITE)
                binding.className.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.categoryName.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.className.setHintTextColor(ContextCompat.getColor(this, R.color.grey))
                isGetDailyCollection()

            }

            R.id.category_name -> {
                selectedType="1"
                binding.categoryName.setBackgroundResource(R.drawable.custom_category_background)
                binding.categoryName.setTextColor(Color.WHITE)
                binding.modeName.background = null
                binding.className.background = null
                isGetDailyCollection()


            }

            R.id.image_view -> {
                //track which button is clicked and taking the id
                selectedDateTarget = R.id.image_view
                showDatePickerDialog(this, this)
            }

            R.id.image_view1 -> {
                selectedDateTarget = R.id.image_view1
                showDatePickerDialog(this, this)
            }


        }

    }

    override fun onDateSelected(date: String) {
        when (selectedDateTarget) {
            R.id.image_view -> {
                binding.fromDate2.text = date
                from_Date=binding.fromDate2.text.toString()
                Log.d("Selected Date", "From Date: $date")
            }
            R.id.image_view1 -> {
                binding.fromDate3.text = date
                to_Date=binding.fromDate2.text.toString()
                Log.d("Selected Date", "To Date: $date")
            }
        }
        isGetDailyCollection()

    }
}

//Existing code --Default
//package com.vs.schoolmessenger.School.DailyCollection
//
//import android.graphics.Color
//import android.util.Log
//import android.view.View
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.databinding.DailyCollectionBinding
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModelProvider
//import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
//import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
//import com.vs.schoolmessenger.Repository.App
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.SharedPreference
//
//
//class DailyCollection : BaseActivity<DailyCollectionBinding>(), View.OnClickListener {
//
//    private var isAccessToken: String? = null
//    private var appViewModel: App? = null
//
//
//    var mAdapter: DcfAdapter? = null
//    override fun getViewBinding(): DailyCollectionBinding {
//        return DailyCollectionBinding.inflate(layoutInflater)
//    }
//
//    override fun setupViews() {
//        super.setupViews()
//        setupToolbar()
//        binding.className.setOnClickListener(this)
//        binding.modeName.setOnClickListener (this)
//        binding.categoryName.setOnClickListener(this)
//        appViewModel = ViewModelProvider(this).get(App::class.java)
//        appViewModel?.init()
//        val isChildDetails = SharedPreference.getChildDetails(this)
//        isAccessToken = isChildDetails?.access_token
////        isGetDailyCollection()
////        appViewModel?.isGetDailyCollectionReport?.observe(this) { response ->
////            Log.d("response++",response.toString())
////            if (response!!.status) {
////                isloadhomeworkData(response.data)
////                Log.d("GetHomeWorkDetails", response.data.toString())
////                Log.d("Access Token",isAccessToken.toString())
////            }
////        }
//
//
//    }
//
////    private fun isloadhomeworkData(newData: List<DailyCollectionItem>?) {
////
////        Log.d("GetHomeworkDataWise", newData.toString())
////
////        mAdapter =
////            DcfAdapter(newData, this, this, Constant.isShimmerViewDisable)
////        binding.totalsummary1.adapter = mAdapter
////
////    }
//
////    fun isGetDailyCollection() {
////        mAdapter = DcfAdapter(null, this, this, Constant.isShimmerViewShow)
////        binding.totalsummary1.layoutManager = LinearLayoutManager(this)
////        binding.totalsummary1.isNestedScrollingEnabled = false
////        binding.totalsummary1.adapter = mAdapter
////        appViewModel!!.isGetDailyCollectionReport(
////            isAccessToken!!, this
////        )
////    }
//
//    override fun onClick(p0: View?) {
//        when (p0?.id) {
//            R.id.class_name -> {
//                binding.className.setBackgroundResource(R.drawable.custom_category_background)
//                binding.className.setTextColor(Color.WHITE)
//                binding.modeName.setBackgroundResource(R.drawable.custom_rounded_background2)
//                binding.categoryName.setBackgroundResource(R.drawable.custom_rounded_background2)
//
//            }
//            R.id.mode_name -> {
//                binding.modeName.setBackgroundResource(R.drawable.custom_category_background)
//                binding.modeName.setTextColor(Color.WHITE)
//                binding.className.setBackgroundResource(R.drawable.custom_rounded_background2)
//                binding.categoryName.setBackgroundResource(R.drawable.custom_rounded_background2)
//                binding.className.setHintTextColor(ContextCompat.getColor(this, R.color.grey))
//            }
//
//            R.id.category_name -> {
//                binding.categoryName.setBackgroundResource(R.drawable.custom_category_background)
//                binding.categoryName.setTextColor(Color.WHITE)
//                binding.modeName.background = null
//                binding.className.background = null
//            }
//        }
//
//    }
//    }