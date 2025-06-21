package com.vs.schoolmessenger.Parent.RequestLeave

import android.os.Build
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LeaveRequestBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LeaveRequest : BaseActivity<LeaveRequestBinding>(), View.OnClickListener,
    LeaveRequestClickListener {

    override fun getViewBinding(): LeaveRequestBinding {
        return LeaveRequestBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    lateinit var mAdapter: LeaveRequestAdapter
    private var fromDateMillis: Long = 0L
    private var toDateMillis: Long = 0L
    private var totalLeaveDays: Int = 0
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rytStartDate.setOnClickListener(this)
        binding.rytStart.setOnClickListener(this)
        binding.rytEnd.setOnClickListener(this)
        binding.rytEndDate.setOnClickListener(this)
        binding.txtStartDate.setOnClickListener(this)
        binding.txtEndDate.setOnClickListener(this)
        binding.lnrStartCalendar.setOnClickListener(this)
        binding.lnrEndCalendar.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        val (dayOnly, dayOfWeek, fullDate, slashDate, customFormat) = Constant.getCurrentDateInfo()

        val today = Calendar.getInstance()
        val formattedToday = dateFormat.format(today.time)

        // Set default FROM date(current date)
        binding.txtStartDate.text = Constant.covertDateFormate(formattedToday)

        binding.lblDay.text = dayOnly
//        binding.lblDate.text = dayOnly

        // Parse millis from formatted date
        val parsedDate = dateFormat.parse(formattedToday)
        fromDateMillis = parsedDate?.time ?: today.timeInMillis

        // Set default TO date
        binding.txtEndDate.text = Constant.covertDateFormate(formattedToday)
        binding.lblEndDay.text = dayOnly
//        binding.lblEndDate.text = dayOnly
        toDateMillis = parsedDate?.time ?: today.timeInMillis

        // Set default leave days = 1
        totalLeaveDays = 1
        binding.lblTotalDays.text = "$totalLeaveDays Days"


        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Leave_Request)
        binding.toolbarLayout.lnrParent.visibility = View.VISIBLE
        isAccessToken = isChildDetails?.access_token

        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails.standard_name + " - " + isChildDetails.section_name

        binding.toolbarLayout.lblLeftSideBar.text = resources.getText(R.string.History)
        binding.toolbarLayout.lblRightSideBar.text = resources.getText(R.string.Create)
        binding.rlaCreateLeaveRequest.visibility = View.VISIBLE


        appViewModel?.getleaverequest?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyLeaveRequestHistory.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                isloadleaverequestData(response.data)
            } else {
                binding.rcyLeaveRequestHistory.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: "No data found"
            }
        }


        appViewModel!!.isLeaveRequest?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@LeaveRequest)
                    Constant.showDataValidation(
                        resources.getString(R.string.success),
                        response.message,
                        this
                    )
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail),
                        response.message,
                        this
                    )
                }
            }
        }

        binding.toolbarLayout.lblRightSideBar.setOnClickListener {
            isBackRoundChange(binding.toolbarLayout.lblRightSideBar)
            binding.rlaCreateLeaveRequest.visibility = View.VISIBLE
            binding.rlaHistory.visibility = View.GONE
        }

        binding.toolbarLayout.lblLeftSideBar.setOnClickListener {
            binding.rlaHistory.visibility = View.VISIBLE
            binding.rlaCreateLeaveRequest.visibility = View.GONE
            isBackRoundChange(binding.toolbarLayout.lblLeftSideBar)
            isGetLeaveRequestList()

        }

        Constant.editTextCounter(this, binding.txtDesc, 500, binding.lbTextCount)

    }


    private fun isLeaveRequestApply() {
        var from_date = Constant.convertDateFormat(binding.txtStartDate.text.toString())
        var to_date = Constant.convertDateFormat(binding.txtEndDate.text.toString())
        Log.d("FromAndToDateComing", from_date + " " + to_date)
        var reason = binding.txtDesc.text.trim()


        if (reason.isEmpty()) {
            binding.txtDesc.error = "Description is required"
            binding.txtDesc.requestFocus()
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.leave_from, from_date)
            addProperty(APIKeyNames.leave_to, to_date)
            addProperty(APIKeyNames.reason, reason.toString())

        }
        appViewModel?.isSendLeaveRequestApply(isAccessToken!!, jsonObject, this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnNext -> {
                isLeaveRequestApply()
            }


            R.id.txtStartDate, R.id.rytStartDate, R.id.lnrStartCalendar -> {
                val todayMillis = Calendar.getInstance().timeInMillis
                Constant.handleRestrictDatePicker(
                    this,
                    minDate = todayMillis,
                    preSelectedDateMillis = fromDateMillis

                ) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.txtStartDate.text = Constant.covertDateFormate(selectedDate)
                    val fromDate = dateFormat.parse(selectedDate)
                    fromDateMillis = fromDate?.time ?: 0L

//                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val result = getDayAndDate(selectedDate, dateFormat)

                    result?.let { (dayOfWeek, dayOfMonth) ->
                        binding.lblDay.text = dayOfWeek
//                        binding.lblDate.text = dayOfMonth
                        binding.lblEndDay.text = dayOfWeek
//                        binding.lblEndDate.text = dayOfMonth
                    }

                    // Auto-set To Date = From Date
                    toDateMillis = fromDateMillis
                    binding.txtEndDate.text = Constant.covertDateFormate(selectedDate)

                    // If TO date already selected, recalculate difference
                    if (toDateMillis >= fromDateMillis) {
                        val diffInMillis = toDateMillis - fromDateMillis
                        totalLeaveDays = ((diffInMillis / (1000 * 60 * 60 * 24)) + 1).toInt()
                    } else {
                        // Default 1 day
                        toDateMillis = fromDateMillis
                        totalLeaveDays = 1
                    }

                    binding.lblTotalDays.text = "$totalLeaveDays Days"

                }
            }


            R.id.txtEndDate, R.id.rytEndDate, R.id.lnrEndCalendar -> {

                Constant.handleRestrictDatePicker(
                    this,
                    minDate = fromDateMillis,
                    preSelectedDateMillis = toDateMillis
                )
                { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.txtEndDate.text = Constant.covertDateFormate(selectedDate)
                    val toDate = dateFormat.parse(selectedDate)
                    toDateMillis = toDate?.time ?: 0L

                    if (fromDateMillis != 0L && toDateMillis >= fromDateMillis) {
                        val diffInMillis = toDateMillis - fromDateMillis
                        totalLeaveDays = ((diffInMillis / (1000 * 60 * 60 * 24)) + 1).toInt()
                    } else {
                        totalLeaveDays = 1
                        fromDateMillis = toDateMillis // fallback to same date
                    }

                    binding.lblTotalDays.text = "$totalLeaveDays Days"
//                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val result = getDayAndDate(selectedDate, dateFormat)
                    result?.let { (dayOfWeek, dayOfMonth) ->
                        binding.lblEndDay.text = dayOfWeek
//                        binding.lblEndDate.text = dayOfMonth
                    }
                }
            }
        }

    }


    fun getDayAndDate(dateString: String, dateFormat: SimpleDateFormat): Pair<String, String>? {
        val dateObj = dateFormat.parse(dateString)
        return dateObj?.let {
            val calendar = Calendar.getInstance().apply { time = it }

            val dayOfWeek =
                SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)  // e.g., "Sat"
            val dayOfMonth =
                String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))         // e.g., "14"

            Pair(dayOfWeek, dayOfMonth)
        }
    }


    private fun isloadleaverequestData(newData: List<LeaveData>?) {
        mAdapter =
            LeaveRequestAdapter(
                newData,
                this,
                this,
                Constant.isShimmerViewDisable
            )
        binding.rcyLeaveRequestHistory.adapter = mAdapter
    }

    private fun isGetLeaveRequestList() {
        mAdapter = LeaveRequestAdapter(
            null,
            this,
            this,
            Constant.isShimmerViewShow
        )
        binding.rcyLeaveRequestHistory.layoutManager = LinearLayoutManager(this)
        binding.rcyLeaveRequestHistory.isNestedScrollingEnabled = false
        binding.rcyLeaveRequestHistory.adapter = mAdapter
        appViewModel!!.getleaverequest(
            isAccessToken!!, "STUDENT", this
        )
    }

    private fun isBackRoundChange(isClickingId: TextView) {

        if (isClickingId == binding.toolbarLayout.lblRightSideBar) {
            binding.toolbarLayout.lblLeftSideBar.background = null
            binding.toolbarLayout.lblLeftSideBar.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.dark_blue
                )
            )
        }
        if (isClickingId == binding.toolbarLayout.lblLeftSideBar) {
            binding.toolbarLayout.lblRightSideBar.background = null
            binding.toolbarLayout.lblRightSideBar.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.dark_blue
                )
            )
        }

        isClickingId.background =
            ContextCompat.getDrawable(this, R.drawable.bg_gradient_parent_clickbar)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))

    }

    override fun onItemImageClick(data: LeaveRequestHistoryData) {

    }

}