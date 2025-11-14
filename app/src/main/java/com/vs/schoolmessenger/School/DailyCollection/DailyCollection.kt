package com.vs.schoolmessenger.School.DailyCollection

import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DailyCollectionDisplayItem
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DailyData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.DailyCollectionBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DailyCollection : BaseActivity<DailyCollectionBinding>(),
    View.OnClickListener, OnDateSelectedListener {

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var selectedDateTarget: Int = 0
    private var selectedType: String = Constant.one
    private var to_Date: String? = null
    private var from_Date: String? = null
    private var mAdapter: DcfAdapter? = null
    private var isStaffDetails: StaffDetails? = null
    private var fromDateMillis: Long = 0L
    private var toDateMillis: Long = 0L
    val dateFormat = SimpleDateFormat(Constant.ddMMyyyy, Locale.getDefault())


    override fun getViewBinding(): DailyCollectionBinding {
        return DailyCollectionBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.className.setOnClickListener(this)
        binding.modeName.setOnClickListener(this)
        binding.categoryName.setOnClickListener(this)
        binding.linearLayout3.setOnClickListener(this)
        binding.linearLayout5.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name


        val calendar = Calendar.getInstance()

        val currentDate = dateFormat.format(calendar.time)
        to_Date = currentDate
        binding.fromDate3.text = Constant.convertToReadableDate1(currentDate)

        from_Date = currentDate
        binding.fromDate2.text = Constant.convertToReadableDate1(currentDate)


        // Convert currentDate string into millis
        val parsedDate = dateFormat.parse(currentDate)
        val currentMillis = parsedDate?.time ?: calendar.timeInMillis

        // Initialize fromDateMillis and toDateMillis
        fromDateMillis = currentMillis
        toDateMillis = currentMillis

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val staffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = staffDetails?.access_token

        isGetDailyCollection()

        appViewModel?.isGetDailyCollectionReport?.observe(this) { response ->
            Constant.hideLoading(this@DailyCollection)
            Log.d("response++", response.toString())

            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                return@observe
            }

            if (response.status) {
                isLoadDailyCollectionData(response.data)
                binding.relativeLayout6.visibility = View.GONE
            } else {
                showErrorUI(response.message ?: getString(R.string.no_data_available))
                binding.relativeLayout6.visibility = View.GONE
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


    private fun isLoadDailyCollectionData(data: List<DailyData>?) {
        val flatList = mutableListOf<DailyCollectionDisplayItem>()

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
                    val feeList = item.fee_data?.map { fee ->
                        DailyCollectionDisplayItem.Fee(
                            fee.type_name ?: getString(R.string.Unknown),
                            fee.amount ?: "0"
                        )
                    } ?: emptyList()

                    flatList.add(
                        DailyCollectionDisplayItem.Header(
                            item.category ?: getString(R.string.Unknown),
                            item.total ?: Constant.zero,
                            feeList
                        )
                    )
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

            val totalCollectionSum = data[0].total_collection
            binding.totalCollection.text = totalCollectionSum.toString()

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


        if (selectedType == Constant.one) {
            binding.categoryName.isEnabled = false
            binding.className.isEnabled = true
            binding.modeName.isEnabled = true
        }
        if (selectedType == Constant.two) {
            binding.className.isEnabled = false
            binding.categoryName.isEnabled = true
            binding.modeName.isEnabled = true
        }
        if (selectedType == Constant.three) {
            binding.modeName.isEnabled = false
            binding.categoryName.isEnabled = true
            binding.className.isEnabled = true
        }

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
                selectedType = Constant.two
                binding.className.setBackgroundResource(R.drawable.bg_primary)
                binding.className.setTextColor(Color.WHITE)
                binding.modeName.setTextColor(Color.BLACK)
                binding.categoryName.setTextColor(Color.BLACK)
                binding.modeName.setBackgroundResource(R.drawable.gray_bg_radius)
                binding.categoryName.setBackgroundResource(R.drawable.gray_bg_radius)

                isGetDailyCollection()
            }

            R.id.mode_name -> {
                selectedType = Constant.three
                binding.modeName.setBackgroundResource(R.drawable.bg_primary)
                binding.modeName.setTextColor(Color.WHITE)
                binding.className.setTextColor(Color.BLACK)
                binding.categoryName.setTextColor(Color.BLACK)

                binding.className.setBackgroundResource(R.drawable.gray_bg_radius)
                binding.categoryName.setBackgroundResource(R.drawable.gray_bg_radius)
                isGetDailyCollection()
            }

            R.id.category_name -> {
                selectedType = Constant.one
                binding.categoryName.setBackgroundResource(R.drawable.bg_primary)
                binding.categoryName.setTextColor(Color.WHITE)
                binding.modeName.setTextColor(Color.BLACK)
                binding.className.setTextColor(Color.BLACK)

                binding.modeName.setBackgroundResource(R.drawable.gray_bg_radius)
                binding.className.setBackgroundResource(R.drawable.gray_bg_radius)
                isGetDailyCollection()
            }

            R.id.linear_layout3 -> {
                selectedDateTarget = R.id.linear_layout3
                dailycollectionshowDatePickerDialog(
                    this,
                    this,
                    isFromDate = true,
                    fromDateMillis = fromDateMillis,
                    preSelectedDate = from_Date
                )
            }

            R.id.linear_layout5 -> {
                selectedDateTarget = R.id.linear_layout5
                dailycollectionshowDatePickerDialog(
                    this,
                    this,
                    isFromDate = false,
                    fromDateMillis = fromDateMillis,
                    preSelectedDate = to_Date
                )
            }
        }
    }

    override fun onDateSelected(date: String) {
        when (selectedDateTarget) {
            R.id.linear_layout3 -> {
                binding.fromDate2.text = Constant.convertToReadableDate1(date)
                from_Date = date

                // update fromDateMillis
                val parsed = dateFormat.parse(date)
                fromDateMillis = parsed?.time ?: 0L
            }

            R.id.linear_layout5 -> {
                binding.fromDate3.text = Constant.convertToReadableDate1(date)
                to_Date = date
            }
        }
        isGetDailyCollection()
    }

}
