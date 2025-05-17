package com.vs.schoolmessenger.School.DailyCollection
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.DailyCollectionBinding
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class DailyCollection : BaseActivity<DailyCollectionBinding>(), View.OnClickListener, OnDateSelectedListener {

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var selectedDateTarget: Int = 0
    private var selectedType: String = "1"
    private var to_Date: String? = null
    private var from_Date: String? = null

    private var mAdapter: DcfAdapter? = null

    override fun getViewBinding(): DailyCollectionBinding {
        return DailyCollectionBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.className.setOnClickListener(this)
        binding.modeName.setOnClickListener(this)
        binding.categoryName.setOnClickListener(this)
        binding.linearLayout3.setOnClickListener(this)
        binding.linearLayout5.setOnClickListener(this)


        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val currentDate = dateFormat.format(calendar.time)
        to_Date = currentDate
        binding.fromDate3.text = currentDate

        calendar.add(Calendar.YEAR, -1)
        val oneYearAgoDate = dateFormat.format(calendar.time)
        from_Date = oneYearAgoDate
        binding.fromDate2.text = oneYearAgoDate

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val staffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = staffDetails?.access_token

        isGetDailyCollection()

        appViewModel?.isGetDailyCollectionReport?.observe(this) { response ->
            Log.d("response++", response.toString())



            if (response != null && response.status) {
                isLoadDailyCollectionData(response.data)
            } else {

                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.totalsummary1.visibility = View.GONE
            }
        }

    }



    private fun isLoadDailyCollectionData(data: List<DailyCollectionItem>?) {
        val flatList = mutableListOf<DisplayItem>()

        if (data.isNullOrEmpty()) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.totalsummary1.visibility = View.GONE
            return
        }

        data.forEach { item ->
            if (!item.category.isNullOrEmpty()) {
                flatList.add(DisplayItem.Header(item.category ?: "Unknown", item.total ?: "0"))
            }

            item.fee_data?.forEach { fee ->
                flatList.add(DisplayItem.Fee(fee.type_name ?: "Unknown", fee.amount ?: "0"))
            }
        }

        if (flatList.isEmpty()) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.totalsummary1.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.totalsummary1.visibility = View.VISIBLE

            mAdapter = DcfAdapter(flatList, this)
            binding.totalsummary1.layoutManager = LinearLayoutManager(this)
            binding.totalsummary1.adapter = mAdapter
        }
    }


    private fun isGetDailyCollection() {
        if (from_Date.isNullOrEmpty() || to_Date.isNullOrEmpty()) return

        binding.totalsummary1.visibility = View.GONE
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE


        mAdapter?.clearData()
        mAdapter = DcfAdapter(emptyList(), this)
        binding.totalsummary1.layoutManager = LinearLayoutManager(this)
        binding.totalsummary1.adapter = mAdapter


        appViewModel?.isGetDailyCollectionReport(
            isAccessToken ?: "",
            selectedType,
            from_Date ?: "",
            to_Date ?: "",
            this
        )

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.class_name -> {
                selectedType = "2"
                binding.className.setBackgroundResource(R.drawable.custom_category_background)
                binding.className.setTextColor(Color.WHITE)
                binding.modeName.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.categoryName.setBackgroundResource(R.drawable.custom_rounded_background2)
                isGetDailyCollection()
            }

            R.id.mode_name -> {
                selectedType = "3"
                binding.modeName.setBackgroundResource(R.drawable.custom_category_background)
                binding.modeName.setTextColor(Color.WHITE)
                binding.className.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.categoryName.setBackgroundResource(R.drawable.custom_rounded_background2)
                isGetDailyCollection()
            }

            R.id.category_name -> {
                selectedType = "1"
                binding.categoryName.setBackgroundResource(R.drawable.custom_category_background)
                binding.categoryName.setTextColor(Color.WHITE)
                binding.modeName.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.className.setBackgroundResource(R.drawable.custom_rounded_background2)
                isGetDailyCollection()
            }

            R.id.linear_layout3 -> {
                selectedDateTarget = R.id.linear_layout3
                showDatePickerDialog(this, this)
            }

            R.id.linear_layout5 -> {
                selectedDateTarget = R.id.linear_layout5
                showDatePickerDialog(this, this)
            }
        }
    }

    override fun onDateSelected(date: String) {
        when (selectedDateTarget) {
            R.id.linear_layout3 -> {
                binding.fromDate2.text = date
                from_Date = date
            }

            R.id.linear_layout5 -> {
                binding.fromDate3.text = date
                to_Date = date
            }
        }
        isGetDailyCollection()
    }
}
