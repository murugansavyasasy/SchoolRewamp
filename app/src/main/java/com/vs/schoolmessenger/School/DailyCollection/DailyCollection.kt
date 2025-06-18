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
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.CollectionData
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DailyCollectionItem
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DisplayItem
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
    private var isStaffDetails: StaffDetails? = null
    override fun getViewBinding(): DailyCollectionBinding {
        return DailyCollectionBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.className.setOnClickListener(this)
        binding.modeName.setOnClickListener(this)
        binding.categoryName.setOnClickListener(this)
        binding.linearLayout3.setOnClickListener(this)
        binding.linearLayout5.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.daily_collection_fees)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name



        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val currentDate = dateFormat.format(calendar.time)
        to_Date = currentDate
        binding.fromDate3.text = currentDate

        from_Date = currentDate
        binding.fromDate2.text = currentDate

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val staffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = staffDetails?.access_token

        isGetDailyCollection()

        appViewModel?.isGetDailyCollectionReport?.observe(this) { response ->
            Constant.hideLoading(this@DailyCollection)
            Log.d("response++", response.toString())

            if (response == null) {
                showErrorUI("Something went wrong. Please try again.")
                return@observe
            }

            if (response.status) {
                isLoadDailyCollectionData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }


    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.totalsummary1.visibility = View.GONE
        binding.relativeLayout5.visibility = View.GONE
    }




    private fun isLoadDailyCollectionData(data: List<CollectionData>?) {
        val flatList = mutableListOf<DisplayItem>()

        if (data.isNullOrEmpty()) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.totalsummary1.visibility = View.GONE
            binding.relativeLayout5.visibility = View.GONE
            return
        }

        data.forEach { collectionData ->
            collectionData.collections.forEach { item ->
                if (!item.category.isNullOrEmpty()) {
                    flatList.add(DisplayItem.Header(item.category ?: "Unknown", item.total ?: "0"))
                }

                item.fee_data?.forEach { fee ->
                    flatList.add(DisplayItem.Fee(fee.type_name ?: "Unknown", fee.amount ?: "0"))
                }
            }
        }

        if (flatList.isEmpty()) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.totalsummary1.visibility = View.GONE
            binding.relativeLayout5.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.totalsummary1.visibility = View.VISIBLE
            binding.relativeLayout5.visibility = View.VISIBLE

            mAdapter = DcfAdapter(flatList, this)
            binding.totalsummary1.layoutManager = LinearLayoutManager(this)
            binding.totalsummary1.adapter = mAdapter

            val totalCollectionSum = data.sumOf {
                it.total_collection.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
            }
            binding.totalCollection.text = "%.2f".format(totalCollectionSum)

        }
    }


    private fun isGetDailyCollection() {

        if (from_Date.isNullOrEmpty() || to_Date.isNullOrEmpty()) return

        binding.totalsummary1.visibility = View.GONE
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.relativeLayout5.visibility = View.GONE


        mAdapter?.clearData()
        mAdapter = DcfAdapter(emptyList(), this)
        binding.totalsummary1.layoutManager = LinearLayoutManager(this)
        binding.totalsummary1.adapter = mAdapter

        Constant.showLoading(this@DailyCollection)
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
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.class_name -> {
                selectedType = "2"
                binding.className.setBackgroundResource(R.drawable.white_radious)
                binding.className.setTextColor(Color.BLACK)
                binding.modeName.setBackgroundResource(R.drawable.bg_light_blue)
                binding.categoryName.setBackgroundResource(R.drawable.bg_light_blue)

                isGetDailyCollection()
            }

            R.id.mode_name -> {
                selectedType = "3"
                binding.modeName.setBackgroundResource(R.drawable.white_radious)
                binding.modeName.setTextColor(Color.BLACK)
                binding.className.setBackgroundResource(R.drawable.bg_light_blue)
                binding.categoryName.setBackgroundResource(R.drawable.bg_light_blue)
                isGetDailyCollection()
            }

            R.id.category_name -> {
                selectedType = "1"
                binding.categoryName.setBackgroundResource(R.drawable.white_radious)
                binding.categoryName.setTextColor(Color.BLACK)
                binding.modeName.setBackgroundResource(R.drawable.bg_light_blue)
                binding.className.setBackgroundResource(R.drawable.bg_light_blue)
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
