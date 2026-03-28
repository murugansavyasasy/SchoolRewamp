package com.vs.schoolmessenger.School.StaffLeaveRequest

import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestAdapter
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestClickListener
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestHistoryData
import com.vs.schoolmessenger.Parent.RequestLeave.MonthWiseLeaveData
import com.vs.schoolmessenger.Parent.RequestLeave.MonthWiseLeaveHistoryAdapter


import android.content.Intent
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Parent.Attendance.Attendance
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDelete
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.School.StaffLeaveRequest.Adapter.MonthWiseStaffLeaveHistoryAdapter
import com.vs.schoolmessenger.School.StaffLeaveRequest.Listner.StaffLeaveRequestClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LeaveRequestBinding
import com.vs.schoolmessenger.databinding.StaffLeaveHistoryBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.view.isVisible
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory.StaffLeaveData
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory.StaffMonthWiseLeaveData

class StaffLeaveHistory : BaseActivity<StaffLeaveHistoryBinding>(), View.OnClickListener,
    StaffLeaveRequestClickListener {

    override fun getViewBinding(): StaffLeaveHistoryBinding {
        return StaffLeaveHistoryBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    lateinit var mAdapter: MonthWiseStaffLeaveHistoryAdapter
    var isDeletedId = ""

    private var fromDateMillis: Long = 0L
    private var toDateMillis: Long = 0L
    private var totalLeaveDays: Int = 0
    private val dateFormat = SimpleDateFormat(Constant.dd_MM_yyyy, Locale.getDefault())
    private var selectedStatus: String = Constant.All_
    private var originalLeaveList: List<StaffMonthWiseLeaveData> = emptyList()
    private var isLeaveList: List<StaffMonthWiseLeaveData> = emptyList()

    var userDetails: UserDetails? = null
    private var isStaffDetails: StaffDetails? = null


    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblParentToolBar.text = "Staff Leave history"
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
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
        binding.toolbarLayout.imgBack.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )

        val (dayOnly, _, _, _, _) = Constant.getCurrentDateInfo()

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
        binding.lblTotalDays.text = "${getString(R.string.No_of_Days)} - $totalLeaveDays"

        binding.rlaCreateLeaveRequest.visibility = View.GONE
        binding.rlaHistory.visibility = View.VISIBLE

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.isVisible) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtVideoMenu1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtVideoMenu1.windowToken, 0)
            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtVideoMenu1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtVideoMenu1.windowToken, 0)
            }
        }

        isGetLeaveRequestList()

        binding.txtVideoMenu1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
                Log.d("Search", s.toString())
            }
        })



        appViewModel?.getStaffleaverequesthistory?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyLeaveRequestHistory.visibility = View.VISIBLE
                binding.lytList.visibility = View.GONE
                originalLeaveList = response.data
                isLeaveList = response.data
                isloadleaverequestData(isLeaveList)
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            } else {
                binding.rcyLeaveRequestHistory.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                binding.rytSearch1.visibility = View.GONE
            }
        }


        appViewModel!!.isStaffleaverequestdelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@StaffLeaveHistory)
                    Log.d("isStaffleaverequestdelete", response.message)
                    Constant.showDataValidationNoDashboardRedirect(
                        resources.getString(R.string.success), response.message, this
                    )
                    onLeaveDeletedSuccess(isDeletedId)
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        binding.btncancel.setOnClickListener {
            val intent = Intent(this, StaffLeaveHistory::class.java)
            startActivity(intent)
            finish()
        }

        binding.rlaHistory.visibility = View.VISIBLE
        binding.tabLayoutStatus.visibility = View.VISIBLE
        binding.rlaCreateLeaveRequest.visibility = View.GONE


        binding.tabLayoutStatus.removeAllTabs()

        val tabTitles = listOf("All", "Approved", "Rejected", "Waiting")

        val tabStatusMap = mapOf(
            "All" to Constant.All_,
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
                binding.txtVideoMenu1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtVideoMenu1.windowToken, 0)
                val selectedTitle = tab.text.toString()
                selectedStatus = tabStatusMap[selectedTitle] ?: Constant.All_

                // filter from originalLeaveList, not isLeaveList
                val tabFilteredList = if (selectedStatus == Constant.All_) {
                    originalLeaveList
                } else {
                    originalLeaveList.mapNotNull { monthWiseLeave ->
                        val filteredDetails =
                            monthWiseLeave.details.filter { it.status == selectedStatus }
                        if (filteredDetails.isNotEmpty()) monthWiseLeave.copy(details = filteredDetails) else null
                    }
                }

                isLeaveList = tabFilteredList
                mAdapter.updateData(isLeaveList)

                if (mAdapter.itemCount == 0) {
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.rytSearch1.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                    binding.rcyLeaveRequestHistory.visibility = View.GONE
                } else {
                    binding.lytList.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    binding.rcyLeaveRequestHistory.visibility = View.VISIBLE
                }
            }


            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


    }

    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        // Step 1: Filter by selected tab status
        val statusFilteredList = if (selectedStatus.equals(Constant.All_, ignoreCase = true)) {
            originalLeaveList
        } else {
            originalLeaveList.mapNotNull { monthData ->
                val filteredDetails = monthData.details.filter {
                    it.status.equals(selectedStatus, ignoreCase = true)
                }
                if (filteredDetails.isNotEmpty()) {
                    monthData.copy(details = filteredDetails)
                } else null
            }
        }

        // Step 2: Apply search filter
        val searchFilteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            statusFilteredList
        } else {
            statusFilteredList.mapNotNull { monthWiseLeave ->
                val filteredDetails = monthWiseLeave.details.filter { leave ->
                    val appliedOn = try {
                        Constant.convertDateTimeFormat(leave.applied_on ?: "")
                    } catch (e: Exception) {
                        leave.applied_on
                    }

                    val leaveFrom = try {
                        Constant.convertDateTimeFormat(leave.from_date ?: "")
                    } catch (e: Exception) {
                        leave.from_date
                    }

                    val leaveTo = try {
                        Constant.convertDateTimeFormat(leave.to_date ?: "")
                    } catch (e: Exception) {
                        leave.to_date
                    }

                    val fieldsToSearch = listOf(
                        (leave.staff_name ?: "").lowercase(),
                        (appliedOn ?: "").lowercase(),
                        (leave.no_of_days.toString() ?: "").lowercase(),
                        (leaveFrom ?: "").lowercase(),
                        (leaveTo ?: "").lowercase(),
                        (leave.leave_type ?: "").lowercase(),
                        (leave.reason ?: "").lowercase(),
                        (leave.from_session ?: "").lowercase(),
                        (leave.to_session ?: "").lowercase(),
                    )

                    searchWords.all { word ->
                        fieldsToSearch.any { field -> field.contains(word.lowercase()) }
                    }
                }


                if (filteredDetails.isNotEmpty()) {
                    monthWiseLeave.copy(details = filteredDetails) // keep month
                } else null
            }
        }

        isLeaveList = searchFilteredList

        // 🔹 Update UI
        if (isLeaveList.isNotEmpty()) {
            binding.rcyLeaveRequestHistory.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
            mAdapter.updateData(isLeaveList)
        } else {
            binding.rcyLeaveRequestHistory.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        }
    }


    fun ShowData() {
        binding.rcyLeaveRequestHistory.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }


    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
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
                    getDayAndDate(selectedDate, dateFormat)
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
                    binding.lblTotalDays.text =
                        "${getString(R.string.No_of_Days)} - $totalLeaveDays"
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

                    binding.lblTotalDays.text =
                        "${getString(R.string.No_of_Days)} - $totalLeaveDays"
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
            String.format(Constant.time02d, calendar.get(Calendar.DAY_OF_MONTH)) // e.g., "18"
        }
    }


    private fun isloadleaverequestData(newData: List<StaffMonthWiseLeaveData>?) {
        binding.rcyLeaveRequestHistory.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
        mAdapter = MonthWiseStaffLeaveHistoryAdapter(
            newData, this, this, Constant.isShimmerViewDisable
        )
        binding.rcyLeaveRequestHistory.adapter = mAdapter
    }

    private fun isGetLeaveRequestList() {
        mAdapter = MonthWiseStaffLeaveHistoryAdapter(
            null, this, this, Constant.isShimmerViewShow
        )
        binding.rcyLeaveRequestHistory.layoutManager = LinearLayoutManager(this)
        binding.rcyLeaveRequestHistory.isNestedScrollingEnabled = false
        binding.rcyLeaveRequestHistory.adapter = mAdapter

        appViewModel!!.getStaffleaverequest(
            isAccessToken!!, "", this
        )


    }

    private fun onLeaveDeletedSuccess(deletedId: String) {
        //  Remove from adapter (current filtered list)
        mAdapter.removeItemById(deletedId)

        //  Remove from both lists (original + filtered)
        originalLeaveList = originalLeaveList.mapNotNull { monthData ->
            val updatedDetails = monthData.details.filterNot { it.id == deletedId }
            if (updatedDetails.isNotEmpty()) monthData.copy(details = updatedDetails) else null
        }

        isLeaveList = isLeaveList.mapNotNull { monthData ->
            val updatedDetails = monthData.details.filterNot { it.id == deletedId }
            if (updatedDetails.isNotEmpty()) monthData.copy(details = updatedDetails) else null
        }

        //  Optional: refresh UI if current list becomes empty
        if (isLeaveList.isEmpty()) {
            binding.rcyLeaveRequestHistory.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.rytSearch1.visibility = View.GONE

        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            binding.rytSearch1.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
            binding.rcyLeaveRequestHistory.visibility = View.VISIBLE
        }
    }


    override fun onItemImageClick(data: LeaveRequestHistoryData) {

    }


    override fun onItemDeleteClick(data: StaffLeaveData) {
        val request = LeaveRequestDelete(
            id = data.id
        )
        Constant.showSendConfirmationDialog(
            this,
            getString(R.string.confirmation),
            getString(R.string.permission_ok),
            getString(R.string.Cancel),
            "",
            getString(R.string.are_you_sure_you_want_to_cancel_leave_request)
        ) { confirmed ->
            if (confirmed) {
                Constant.showLoading(this)
                isDeletedId = data.id
                appViewModel?.isStaffleaverequestdelete(isAccessToken!!, request, this)
            }
        }
    }


    override fun onItemEditClick(data: StaffLeaveData) {
        val intent = Intent(this, StaffLeaveRequest::class.java)
        intent.putExtra(Constant.isReason, data.reason)
        intent.putExtra(Constant.isIdValue, data.id)
        intent.putExtra(Constant.isLeaveTo, data.to_date)
        intent.putExtra(Constant.isLeaveFrom, data.from_date)
        intent.putExtra(Constant.isFromSession, data.from_session)
        intent.putExtra(Constant.isToSession, data.to_session)
        intent.putExtra(Constant.isLeaveType, data.leave_type)
        intent.putExtra(Constant.isLeaveTypeID, data.leave_type_id)
        intent.putExtra(Constant.isStaffRequestEdit, true)
        startActivity(intent)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, StaffLeaveRequest::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}