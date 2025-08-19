package com.vs.schoolmessenger.Parent.RequestLeave

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestUpdate
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter_New
import com.vs.schoolmessenger.databinding.ActivityNewLeaveRequestBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NewLeaveRequest : BaseActivity<ActivityNewLeaveRequestBinding>(),
    CustomCalendarFragment.CalendarDateListener, View.OnClickListener {

    private var fromDate: LocalDate? = null
    private var toDate: LocalDate? = null
    private val leaveCategories = mutableListOf<String>()
    var isFromSession = ""
    var isToSession = ""
    var isLeaveCategoryType = ""
    var RequestEdit = false
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private var originalReason: String = ""
    private var originalLeaveFrom: String = ""
    private var originalLeaveTo: String = ""
    private var originalLeaveType: String = ""
    private var originalFromSession: String = ""
    private var originalToSession: String = ""

    private val Session = listOf(
        "First Half", "Second Half",
    )

    override fun getViewBinding(): ActivityNewLeaveRequestBinding {
        return ActivityNewLeaveRequestBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbarBlue()

        binding.imgBack.setOnClickListener(this)
        binding.btnupdate.setOnClickListener(this)
        binding.btnApplyLeave.setOnClickListener(this)
        binding.lblParentToolBar.text = getString(R.string.new_leave)
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.lblStudentName.text = isChildDetails?.name ?: ""
        binding.lblStudentName.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        binding.lblStudentSection.setTextColor(ContextCompat.getColor(this, R.color.white))

        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.imgBack.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )

        loadFromCalendar()
        isFromSpinner()
        isToSpinner()
        loadLeaveCategories()
        validateDateAndSession(showError = true)

        binding.etLeaveReason.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateDateAndSession(showError = true)
            }
        })

        appViewModel!!.isleaverequestupdate?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@NewLeaveRequest)
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

        appViewModel!!.isLeaveRequest?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@NewLeaveRequest)
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

        appViewModel!!.getLeaveCategories?.observe(this) { response ->
            Constant.hideLoading(this@NewLeaveRequest)
            if (response != null) {
                if (response.status) {
                    leaveCategories.add("Select a leave type") // Default
                    leaveCategories.addAll(response.data)
                    isLeaveCategorySpinner()
                    getIntentValuesIfEditing()
                } else {
                    Constant.showDataValidation(
                        response.status.toString(), response.message, this
                    )
                }
            }
        }

        binding.lnrFromDate.setOnClickListener {
            loadFromCalendar()
        }

        binding.lnrToDate.setOnClickListener {
            loadToCalendar(fromDate ?: LocalDate.now())
        }

        binding.FromDone.setOnClickListener {
            binding.calendarFromFragmentContainer.visibility = View.GONE
            binding.FromDone.visibility = View.GONE
            binding.activityMain.visibility = View.GONE
            binding.lblSelect.text = ""
        }

        binding.ToDone.setOnClickListener {
            binding.calendarToFragmentContainer.visibility = View.GONE
            binding.ToDone.visibility = View.GONE
            binding.activityMain.visibility = View.GONE
            binding.lblSelect.text = ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadFromCalendar() {
        val today = LocalDate.now()
        val minFromDate = today.minusMonths(1)
        val maxFromDate = today.plusYears(1)


        val fromFragment = CustomCalendarFragment.newInstance(
            minDate = minFromDate.toString(),
            maxDate = maxFromDate.toString(),
            selectedDate = fromDate?.toString(),
            tag = "FROM_DATE"
        )

        supportFragmentManager.beginTransaction()
            .replace(binding.calendarFromFragmentContainer.id, fromFragment, "FROM_CALENDAR")
            .commit()

        binding.calendarFromFragmentContainer.visibility = View.VISIBLE
        binding.calendarToFragmentContainer.visibility = View.GONE
        binding.activityMain.visibility = View.VISIBLE
        binding.FromDone.visibility = View.VISIBLE
        binding.ToDone.visibility = View.GONE
        binding.lblSelect.text = getString(R.string.select_from_date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadToCalendar(fromDate: LocalDate) {
        val maxToDate = fromDate.plusYears(1)

        val toFragment = CustomCalendarFragment.newInstance(
            minDate = fromDate.toString(),
            maxDate = maxToDate.toString(),
            selectedDate = toDate?.toString(),
            tag = "TO_DATE"
        )

        supportFragmentManager.beginTransaction()
            .replace(binding.calendarToFragmentContainer.id, toFragment, "TO_CALENDAR")
            .commit()

        binding.calendarFromFragmentContainer.visibility = View.GONE
        binding.activityMain.visibility = View.VISIBLE
        binding.calendarToFragmentContainer.visibility = View.VISIBLE
        binding.FromDone.visibility = View.GONE
        binding.ToDone.visibility = View.VISIBLE
        binding.lblSelect.text = getString(R.string.select_to_date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDateSelected(date: String, tag: String) {
        val selected = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

        when (tag) {
            "FROM_DATE" -> {
                fromDate = selected
                binding.tvFromDate.text = formatDate(selected)
            }

            "TO_DATE" -> {
                toDate = selected
                binding.tvToDate.text = formatDate(selected)
            }
        }
        validateDateAndSession(showError = true)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")
        return formatter.format(date)
    }


    private fun isFromSpinner() {

        val adapter = SpinnerLoadingAdapter_New(this, Session)
        binding.isFromSession.adapter = adapter
        binding.isFromSession.setSelection(0)
        isFromSession = Session[0]

        binding.isFromSession.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()
                isFromSession = Session[position]
                validateDateAndSession(showError = true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isLeaveCategorySpinner() {
        val adapter = SpinnerLoadingAdapter_New(this, leaveCategories)

        // hide first item from dropdown which we are using it as a hint
        adapter.enableFirstItemAsHint()

        binding.isLeaveCategories.adapter = adapter
        binding.isLeaveCategories.setSelection(0)
        isLeaveCategoryType = leaveCategories[0]
        Log.d("isLeaveCategoryType", isLeaveCategoryType)

        binding.isLeaveCategories.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    if (position > 0) {
                        isLeaveCategoryType = leaveCategories[position]
                        validateDateAndSession(showError = true)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isToSpinner() {

        val adapter = SpinnerLoadingAdapter_New(this, Session)
        binding.isToSession.adapter = adapter

        // Set default selection to Session 2 (index 1 if exists)
        val defaultToSessionIndex = if (Session.size > 1) 1 else 0
        binding.isToSession.setSelection(defaultToSessionIndex)
        isToSession = Session[defaultToSessionIndex]

        binding.isToSession.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()
                isToSession = Session[position]
                validateDateAndSession(showError = true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.btnApplyLeave -> {
                Constant.showSendConfirmationDialog(
                    this,
                    getString(R.string.confirmation),
                    getString(R.string.permission_ok),
                    getString(R.string.Cancel),
                    "",
                    getString(R.string.Are_you_sure_you_want_to_apply_for_leave)
                ) { confirmed ->
                    if (confirmed) {
                        Constant.showLoading(this)
                        isApplyLeave()
                    }
                }
            }

            R.id.btnupdate -> {
                if (hasChangesMade()) {
                    Constant.showSendConfirmationDialog(
                        this,
                        getString(R.string.confirmation),
                        getString(R.string.permission_ok),
                        getString(R.string.Cancel),
                        "",
                        getString(R.string.Are_you_sure_you_want_to_update_this_request)
                    ) { confirmed ->
                        if (confirmed) {
                            Constant.showLoading(this)
                            isUpdateLeaveReq()
                        }
                    }
                } else {
                    Constant.showErrorAlert(
                        this,
                        getString(R.string.alert),
                        getString(R.string.no_changes_made)
                    )
                }
            }
        }
    }

    private fun isApplyLeave() {
        val jsonObject = JsonObject().apply {
            addProperty(
                APIKeyNames.leave_from,
                fromDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: ""
            )
            addProperty(
                APIKeyNames.leave_to,
                toDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: ""
            )
            addProperty(APIKeyNames.reason, binding.etLeaveReason.text.toString().trim())
            addProperty(APIKeyNames.f_session, if (isFromSession == "First Half") "FH" else "SH")
            addProperty(APIKeyNames.t_session, if (isToSession == "First Half") "FH" else "SH")
            addProperty(APIKeyNames.leave_type, isLeaveCategoryType)


        }

        Log.d("isApplyLeave", jsonObject.toString())

        appViewModel?.isSendLeaveRequestApply(isAccessToken!!, jsonObject, this)
    }


    private fun isUpdateLeaveReq() {
        val updatedRequest = LeaveRequestUpdate(
            id = intent.getStringExtra("isId") ?: "",
            leave_from = fromDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "",
            leave_to = toDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "",
            reason = binding.etLeaveReason.text.toString().trim(),
            f_session = if (isFromSession == "First Half") "FH" else "SH",
            t_session = if (isToSession == "First Half") "FH" else "SH",
            leave_type = isLeaveCategoryType
        )
        appViewModel?.isleaverequestupdate(isAccessToken!!, updatedRequest, this)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getIntentValuesIfEditing() {
        RequestEdit = intent.getBooleanExtra("isRequestEdit", false)

        if (RequestEdit) {
            binding.lblParentToolBar.text = getString(R.string.edit_leave_request)
            binding.btnApplyLeave.visibility = View.GONE
            binding.btnupdate.visibility = View.VISIBLE
            originalReason = intent.getStringExtra("isReason") ?: ""
            originalLeaveFrom = intent.getStringExtra("isLeaveFrom") ?: ""
            originalLeaveTo = intent.getStringExtra("isLeaveTo") ?: ""
            originalLeaveType = intent.getStringExtra("isLeaveType") ?: Session[0]
            originalFromSession = intent.getStringExtra("isFromSession") ?: Session[0]
            originalToSession = intent.getStringExtra("isToSession") ?: Session[1]

            binding.etLeaveReason.setText(originalReason)

            val backendFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val displayFormat = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")

            try {
                fromDate = LocalDate.parse(originalLeaveFrom, backendFormat)
                binding.tvFromDate.text = displayFormat.format(fromDate)
                loadFromCalendar()//here were setting the fromDate in from date calendar by deafult

                toDate = LocalDate.parse(originalLeaveTo, backendFormat)
                binding.tvToDate.text = displayFormat.format(toDate)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val fromSessionIndex = Session.indexOf(originalFromSession)
            if (fromSessionIndex != -1) binding.isFromSession.setSelection(fromSessionIndex)

            val toSessionIndex = Session.indexOf(originalToSession)
            if (toSessionIndex != -1) binding.isToSession.setSelection(toSessionIndex)

            Log.d("leaveCategories", leaveCategories.toString())
            val leaveType = leaveCategories.indexOf(originalLeaveType)
            Log.d("leaveType", leaveType.toString())
            if (leaveType != -1) binding.isLeaveCategories.setSelection(leaveType)

            isFromSession = originalFromSession
            isToSession = originalToSession
            isLeaveCategoryType = originalLeaveType
            Log.d("isLeaveCategoryType", isLeaveCategoryType)

            validateDateAndSession(showError = true)
        }
    }

    private fun hasChangesMade(): Boolean {

        val currentReason = binding.etLeaveReason.text.toString().trim()
        val currentFrom = fromDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: ""
        val currentTo = toDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: ""
        val currentFSession = isFromSession
        val currentTSession = isToSession
        val currentLeaveCatoryType = isLeaveCategoryType

        return originalReason != currentReason ||
                originalLeaveFrom != currentFrom ||
                originalLeaveTo != currentTo ||
                originalFromSession != currentFSession ||
                originalToSession != currentTSession ||
                originalLeaveType != currentLeaveCatoryType
    }


    private fun validateDateAndSession(showError: Boolean = true): Boolean {
        val errors = mutableListOf<String>()
        val reason = binding.etLeaveReason.text.toString().trim()

        // Validate Leave Type
        if (isLeaveCategoryType.isNullOrBlank() || isLeaveCategoryType == "Select a leave type") {
            errors.add("Leave type is required.")
        }

        // Validate Reason
        if (reason.isEmpty()) {
            errors.add("Reason is required.")
        }

        // Validate Dates
        val isFromDateInvalid =
            fromDate == null || binding.tvFromDate.text == getString(R.string.select_date)
        val isToDateInvalid =
            toDate == null || binding.tvToDate.text == getString(R.string.select_date)

        if (isFromDateInvalid || isToDateInvalid) {
            errors.add("Both From and To dates are required.")
        }


        // Validate Session (only if dates are valid)
        if (!isFromDateInvalid && !isToDateInvalid && fromDate != null && toDate != null) {
            if (toDate!!.isBefore(fromDate)) {
                errors.add("To Date cannot be before From Date.")
            }

            val fromIndex = Session.indexOf(isFromSession)
            val toIndex = Session.indexOf(isToSession)

            if (fromDate == toDate && fromIndex > toIndex) {
                errors.add("From session cannot be after To session on the same day.")
            }
        }

        // Show error if any
        if (errors.isNotEmpty()) {
            if (showError) {
                binding.lblErrorMessage.text = errors.joinToString("\n")
            }
            disableButtons()
            return false
        }

        // Clear error
        binding.lblErrorMessage.text = ""

        // Calculate leave days
        var totalDays = 0f
        val fromIndex = Session.indexOf(isFromSession)
        val toIndex = Session.indexOf(isToSession)

        if (fromDate == toDate) {
            totalDays = if (fromIndex == toIndex) 0.5f else 1f
        } else {
            val daysBetween = ChronoUnit.DAYS.between(fromDate, toDate).toInt() + 1
            totalDays = daysBetween.toFloat()

            if (fromIndex > 0) totalDays -= 0.5f
            if (toIndex < Session.lastIndex) totalDays -= 0.5f
        }

        val formattedDays =
            if (totalDays % 1 == 0f) totalDays.toInt().toString() else totalDays.toString()
        val dayText = if (totalDays == 1f) "Day" else "Days"

        binding.btnApplyLeave.text = "Apply for $formattedDays $dayText Leave"
        binding.btnupdate.text = "Update for $formattedDays $dayText Leave"

        enableButtons()
        return true
    }


    private fun disableButtons() {
        val grayDrawable =
            ContextCompat.getDrawable(this, R.drawable.background_radius_button)?.mutate()
        grayDrawable?.setTint(Color.GRAY)
        binding.btnApplyLeave.background = grayDrawable
        binding.btnupdate.background = grayDrawable
        binding.btnApplyLeave.isEnabled = false
        binding.btnupdate.isEnabled = false
        binding.btnApplyLeave.text = "Apply Leave"
        binding.btnupdate.text = "Update Leave"
    }

    private fun enableButtons() {
        val normalDrawable =
            ContextCompat.getDrawable(this, R.drawable.background_radius_button)?.mutate()
        binding.btnApplyLeave.background = normalDrawable
        binding.btnupdate.background = normalDrawable
        binding.btnApplyLeave.isEnabled = true
        binding.btnupdate.isEnabled = true
    }

    private fun loadLeaveCategories() {
        appViewModel!!.getLeaveCategories(isAccessToken!!)
    }


}
