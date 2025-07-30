package com.vs.schoolmessenger.Parent.RequestLeave

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.Parent.ExamMark
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDelete
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestUpdate
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
import kotlin.String

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

    private var currentTab = TabType.LeaveRequest

    private enum class TabType {
        LeaveRequest, History
    }

    private val txtDesc: String = ""

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
        binding.btnupdate.setOnClickListener(this)
        binding.btncancel.setOnClickListener(this)
        val (dayOnly, dayOfWeek, fullDate, slashDate, customFormat) = Constant.getCurrentDateInfo()

        val today = Calendar.getInstance()
        val formattedToday = dateFormat.format(today.time)

        binding.txtStartDate.text = Constant.covertDateFormate(formattedToday)

        binding.lblDay.text = dayOnly

        val parsedDate = dateFormat.parse(formattedToday)
        fromDateMillis = parsedDate?.time ?: today.timeInMillis

        binding.txtEndDate.text = Constant.covertDateFormate(formattedToday)
        binding.lblEndDay.text = dayOnly
        toDateMillis = parsedDate?.time ?: today.timeInMillis

        totalLeaveDays = 1
        binding.lblTotalDays.text = "No of Days - $totalLeaveDays"


        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.lnrParent.visibility = View.GONE
        isAccessToken = isChildDetails?.access_token

        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails.standard_name + " - " + isChildDetails.section_name

        binding.toolbarLayout.lblLeftSideBar.text = resources.getText(R.string.History)
        binding.toolbarLayout.lblRightSideBar.text = "Leave Request"
        binding.rlaCreateLeaveRequest.visibility = View.GONE
        binding.rlaHistory.visibility = View.VISIBLE
        isGetLeaveRequestList()



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
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }


        appViewModel!!.isleaverequestdelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@LeaveRequest)
                    Log.d("isleaverequestdelete", response.message)
                    Constant.showDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        appViewModel!!.isleaverequestupdate?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@LeaveRequest)
                    Log.d("isleaverequestupdate", response.message)
                    Constant.showDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        binding.btncancel.setOnClickListener {
            val intent = Intent(this, LeaveRequest::class.java)
            startActivity(intent)
            finish()
        }



        binding.toolbarLayout.lblRightSideBar.setOnClickListener {

            if (currentTab == TabType.LeaveRequest) return@setOnClickListener
            currentTab = TabType.LeaveRequest

            binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.white_radious)
            binding.toolbarLayout.lblRightSideBar.setTextColor(Color.BLACK)
            binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.bg_light_green)
            binding.rlaCreateLeaveRequest.visibility = View.VISIBLE
            binding.rlaHistory.visibility = View.GONE
            binding.tabLayoutStatus.visibility = View.GONE
        }

//        binding.toolbarLayout.lblLeftSideBar.setOnClickListener {
//            if (currentTab == TabType.History) return@setOnClickListener
//            currentTab = TabType.History

            binding.rlaHistory.visibility = View.VISIBLE
            binding.tabLayoutStatus.visibility = View.VISIBLE
            binding.rlaCreateLeaveRequest.visibility = View.GONE

            binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.white_radious)
            binding.toolbarLayout.lblLeftSideBar.setTextColor(Color.BLACK)
            binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.bg_light_green)

            binding.tabLayoutStatus.removeAllTabs()

            val tabTitles = listOf("All", "Approved", "Rejected", "Waiting")

            val tabStatusMap = mapOf(
                "All" to "All",
                "Approved" to Constant.approved,
                "Rejected" to Constant.rejected,
                "Waiting" to Constant.waiting_for_approval
            )

            tabTitles.forEach { title ->
                binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText(title))
            }

            binding.tabLayoutStatus.clearOnTabSelectedListeners()

            binding.tabLayoutStatus.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val selectedTitle = tab.text.toString()
                    val filterStatus = tabStatusMap[selectedTitle] ?: "All"
                    mAdapter.filterByStatus(filterStatus)

                    if (mAdapter.itemCount == 0) {
                        binding.txtNoData.visibility = View.VISIBLE
                        binding.nomessage.visibility = View.VISIBLE
                        binding.rcyLeaveRequestHistory.visibility = View.GONE
                    } else {
                        binding.txtNoData.visibility = View.GONE
                        binding.nomessage.visibility = View.GONE
                        binding.rcyLeaveRequestHistory.visibility = View.VISIBLE
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        isGetLeaveRequestList()

//        }



        Constant.editTextCounter(this, binding.txtDesc, 500, binding.lbTextCount)

    }


    private fun isLeaveRequestApply() {
        var from_date = Constant.convertDateFormat(binding.txtStartDate.text.toString())
        var to_date = Constant.convertDateFormat(binding.txtEndDate.text.toString())
        Log.d("FromAndToDateComing", from_date + " " + to_date)
        var reason = binding.txtDesc.text.trim()


        if (reason.isEmpty()) {
            binding.txtDesc.error = getString(R.string.This_field_required)
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
                    this, minDate = todayMillis, preSelectedDateMillis = fromDateMillis

                ) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.txtStartDate.text = Constant.covertDateFormate(selectedDate)
                    val fromDate = dateFormat.parse(selectedDate)
                    fromDateMillis = fromDate?.time ?: 0L
                    val result = getDayAndDate(selectedDate, dateFormat)
                    val dayOfMonth = getDayAndDate(selectedDate, dateFormat)
                    dayOfMonth?.let {
                        binding.lblDay.text = it
                    }
                    toDateMillis = fromDateMillis
                    binding.txtEndDate.text = Constant.covertDateFormate(selectedDate)
                    if (toDateMillis >= fromDateMillis) {
                        val diffInMillis = toDateMillis - fromDateMillis
                        totalLeaveDays = ((diffInMillis / (1000 * 60 * 60 * 24)) + 1).toInt()
                    } else {
                        toDateMillis = fromDateMillis
                        totalLeaveDays = 1
                    }
                    binding.lblTotalDays.text = "No of Days - $totalLeaveDays"
                }
            }


            R.id.txtEndDate, R.id.rytEndDate, R.id.lnrEndCalendar -> {
                Constant.handleRestrictDatePicker(
                    this, minDate = fromDateMillis, preSelectedDateMillis = toDateMillis
                ) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.txtEndDate.text = Constant.covertDateFormate(selectedDate)
                    val toDate = dateFormat.parse(selectedDate)
                    toDateMillis = toDate?.time ?: 0L

                    if (fromDateMillis != 0L && toDateMillis >= fromDateMillis) {
                        val diffInMillis = toDateMillis - fromDateMillis
                        totalLeaveDays = ((diffInMillis / (1000 * 60 * 60 * 24)) + 1).toInt()
                    } else {
                        totalLeaveDays = 1
                        fromDateMillis = toDateMillis
                    }

                    binding.lblTotalDays.text = "No of Days - $totalLeaveDays"
                    val dayOfMonth = getDayAndDate(selectedDate, dateFormat)
                    dayOfMonth?.let {
                        binding.lblEndDay.text = it
                    }

                }
            }
        }

    }


    fun getDayAndDate(dateString: String, dateFormat: SimpleDateFormat): String? {
        val dateObj = dateFormat.parse(dateString)
        return dateObj?.let {
            val calendar = Calendar.getInstance().apply { time = it }
            String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) // e.g., "18"
        }
    }


    private fun isloadleaverequestData(newData: List<LeaveData>?) {
        mAdapter = LeaveRequestAdapter(
            newData, this, this, Constant.isShimmerViewDisable
        )
        binding.rcyLeaveRequestHistory.adapter = mAdapter
    }

    private fun isGetLeaveRequestList() {
        mAdapter = LeaveRequestAdapter(
            null, this, this, Constant.isShimmerViewDisable
        )
        binding.rcyLeaveRequestHistory.layoutManager = LinearLayoutManager(this)
        binding.rcyLeaveRequestHistory.isNestedScrollingEnabled = false
        binding.rcyLeaveRequestHistory.adapter = mAdapter
        appViewModel!!.getleaverequest(
            isAccessToken!!, "STUDENT", this
        )
    }


    override fun onItemImageClick(data: LeaveRequestHistoryData) {

    }


    override fun onItemDeleteClick(data: LeaveData) {
        val request = LeaveRequestDelete(
            id = data.id
        )
        appViewModel?.isleaverequestdelete(isAccessToken!!, request, this)

    }


    override fun onItemEditClick(data: LeaveData) {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val backendFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.white_radious)
        binding.toolbarLayout.lblRightSideBar.setTextColor(Color.BLACK)
        binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.bg_light_green)
        binding.rlaCreateLeaveRequest.visibility = View.VISIBLE
        binding.rlaHistory.visibility = View.GONE
        binding.txtDesc.setText(data.reason)
        binding.btnNext.visibility = View.GONE
        binding.linearLayout9.visibility = View.VISIBLE
        binding.tabLayoutStatus.visibility = View.GONE
        binding.toolbarLayout.lnrParent.visibility = View.GONE
        binding.lblHeaderTitle.setText("Edit Leave Request")

        try {
            val parsedFromDate = inputFormat.parse(data.leave_from)
            parsedFromDate?.let {
                val formattedFrom = displayFormat.format(it)
                binding.txtStartDate.text = Constant.covertDateFormate(formattedFrom)
                fromDateMillis = it.time
                val dayOnly = getDayAndDate(formattedFrom, displayFormat)
                binding.lblDay.text = dayOnly
            }

            val parsedToDate = inputFormat.parse(data.leave_to)
            parsedToDate?.let {
                val formattedTo = displayFormat.format(it)
                binding.txtEndDate.text = Constant.covertDateFormate(formattedTo)
                toDateMillis = it.time
                val dayOnly = getDayAndDate(formattedTo, displayFormat)
                binding.lblEndDay.text = dayOnly
            }

            totalLeaveDays = if (fromDateMillis <= toDateMillis) {
                (((toDateMillis - fromDateMillis) / (1000 * 60 * 60 * 24)) + 1).toInt()
            } else {
                1
            }
            binding.lblTotalDays.text = "No of Days - $totalLeaveDays"

        } catch (e: Exception) {
            Log.e("EditClickDateError", "Date parsing failed: ${e.localizedMessage}")
        }

        binding.btnupdate.setOnClickListener {
            val updatedReason = binding.txtDesc.text.toString().trim()
            val updatedStartDate = binding.txtStartDate.text.toString()
            val updatedEndDate = binding.txtEndDate.text.toString()

            var leaveFromFormatted = data.leave_from
            var leaveToFormatted = data.leave_to

            try {
                val fromDate = displayFormat.parse(updatedStartDate)
                val toDate = displayFormat.parse(updatedEndDate)
                if (fromDate != null) leaveFromFormatted = backendFormat.format(fromDate)
                if (toDate != null) leaveToFormatted = backendFormat.format(toDate)
            } catch (e: Exception) {
                Log.e("UpdateClickDateError", "Parsing updated dates failed: ${e.localizedMessage}")
            }

            val updatedRequest = LeaveRequestUpdate(
                id = data.id,
                leave_from = leaveFromFormatted,
                leave_to = leaveToFormatted,
                reason = updatedReason
            )

            appViewModel?.isleaverequestupdate(isAccessToken!!, updatedRequest, this)
        }
    }


}