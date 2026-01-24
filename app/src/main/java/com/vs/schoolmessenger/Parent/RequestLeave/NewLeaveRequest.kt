package com.vs.schoolmessenger.Parent.RequestLeave

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
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
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter_New_2
import com.vs.schoolmessenger.databinding.ActivityNewLeaveRequestBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NewLeaveRequest : BaseActivity<ActivityNewLeaveRequestBinding>(),
    CustomCalendarFragment.CalendarDateListener, View.OnClickListener {

    private var fromDate: LocalDate? = null
    private var toDate: LocalDate? = null

    private val leaveCategories = mutableListOf<getCatorgiesData>()

    var isFromSession = ""
    var isToSession = ""
    var isLeaveCategoryType = ""
    var isLeaveCatoryID = 0
    var RequestEdit = false
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private var originalReason: String = ""
    private var originalLeaveFrom: String = ""
    private var originalLeaveTo: String = ""
    private var originalLeaveType: String = ""
    private var originalLeaveID: Int = 0
    private var originalFromSession: String = ""
    private var originalToSession: String = ""

    private val Session = listOf(
        "First Half", "Second Half",
    )

    override fun getViewBinding(): ActivityNewLeaveRequestBinding {
        return ActivityNewLeaveRequestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setupToolbarBlueWhite()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnupdate.setOnClickListener(this)
        binding.btnApplyLeave.setOnClickListener(this)
        binding.lblParentToolBar.text = getString(R.string.new_leave)
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentName.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        binding.toolbarLayout.lblStudentSection.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )

        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setColorFilter(
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
            Constant.hideLoading(this@NewLeaveRequest)
            if (response != null) {
                if (response.status) {
                    Log.d("isleaverequestupdate", response.message)
                    Constant.showRedirecttoMenu(
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    Constant.showRedirecttoMenu(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        appViewModel!!.isLeaveRequest?.observe(this) { response ->
            Constant.hideLoading(this@NewLeaveRequest)
            if (response != null) {
                if (response.status) {
                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_apply_leave)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                        addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)
                    Constant.showParentDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    Constant.showParentDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        appViewModel!!.getLeaveCategories?.observe(this) { response ->
            Constant.hideLoading(this@NewLeaveRequest)
            if (response != null) {
                if (response.status) {
                    leaveCategories.clear()
                    leaveCategories.add(
                        getCatorgiesData(
                            0,
                            Constant.Select_a_leave_type
                        )
                    ) // Default
                    leaveCategories.addAll(response.data)
                    isLeaveCategorySpinner()
                    getIntentValuesIfEditing()
                } else {
                    Constant.showParentDataValidation(
                        response.status.toString(), response.message, this
                    )
                }
            }
        }

        binding.lnrFromDate.setOnClickListener {
            loadFromCalendar()
        }
        binding.lnrFromDateImg.setOnClickListener {
            loadFromCalendar()
        }

        binding.lnrToDate.setOnClickListener {
            loadToCalendar(fromDate ?: LocalDate.now())
        }

        binding.lnrToDateImg.setOnClickListener {
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

    private fun loadFromCalendar() {
        val today = LocalDate.now()
        val minFromDate = today.minusMonths(1)
        val maxFromDate = today.plusYears(1)


        val fromFragment = CustomCalendarFragment.newInstance(
            minDate = minFromDate.toString(),
            maxDate = maxFromDate.toString(),
            selectedDate = fromDate?.toString(),
            tag = Constant.FROM_DATE
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

    private fun loadToCalendar(fromDate: LocalDate) {
        val maxToDate = fromDate.plusYears(1)

        val toFragment = CustomCalendarFragment.newInstance(
            minDate = fromDate.toString(),
            maxDate = maxToDate.toString(),
            selectedDate = toDate?.toString(),
            tag = Constant.TO_DATE
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

    override fun onDateSelected(date: String, tag: String) {
        val selected = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

        when (tag) {
            Constant.FROM_DATE -> {
                fromDate = selected
                binding.tvFromDate.text = formatDate(selected)
            }

            Constant.TO_DATE -> {
                toDate = selected
                binding.tvToDate.text = formatDate(selected)
            }
        }
        validateDateAndSession(showError = true)
    }


    private fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(Constant.EEE_comma_dd_MMM_yyyy)
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
        val adapter = SpinnerLoadingAdapter_New_2(this, leaveCategories)

        adapter.enableFirstItemAsHint()

        binding.isLeaveCategories.adapter = adapter
        binding.isLeaveCategories.setSelection(0)

        // Default selected
        isLeaveCategoryType = leaveCategories[0].name
        isLeaveCatoryID = leaveCategories[0].id

        binding.isLeaveCategories.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    if (position > 0) {
                        isLeaveCategoryType = leaveCategories[position].name
                        isLeaveCatoryID = leaveCategories[position].id
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
                Log.d("hasChangesMade", hasChangesMade().toString())
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
                fromDate?.format(DateTimeFormatter.ofPattern(Constant.ddMMyyyy)) ?: ""
            )
            addProperty(
                APIKeyNames.leave_to,
                toDate?.format(DateTimeFormatter.ofPattern(Constant.ddMMyyyy)) ?: ""
            )
            addProperty(APIKeyNames.reason, binding.etLeaveReason.text.toString().trim())
            addProperty(
                APIKeyNames.f_session,
                if (isFromSession == Constant.First_Half) Constant.firstHalf else Constant.secondHalf
            )
            addProperty(
                APIKeyNames.t_session,
                if (isToSession == Constant.First_Half) Constant.firstHalf else Constant.secondHalf
            )
            addProperty(APIKeyNames.leave_type, isLeaveCatoryID)


        }

        Log.d("isApplyLeave", jsonObject.toString())

        appViewModel?.isSendLeaveRequestApply(isAccessToken!!, jsonObject, this)
    }


    private fun isUpdateLeaveReq() {
        val updatedRequest = LeaveRequestUpdate(
            id = intent.getStringExtra(Constant.isIdValue) ?: "",
            leave_from = fromDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "",
            leave_to = toDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "",
            reason = binding.etLeaveReason.text.toString().trim(),
            f_session = if (isFromSession == Constant.First_Half) Constant.firstHalf else Constant.secondHalf,
            t_session = if (isToSession == Constant.First_Half) Constant.firstHalf else Constant.secondHalf,
            leave_type = isLeaveCatoryID
        )
        appViewModel?.isleaverequestupdate(isAccessToken!!, updatedRequest, this)
    }


    private fun getIntentValuesIfEditing() {
        RequestEdit = intent.getBooleanExtra(Constant.isRequestEdit, false)

        if (RequestEdit) {
            binding.lblParentToolBar.text = getString(R.string.edit_leave_request)
            binding.btnApplyLeave.visibility = View.GONE
            binding.btnupdate.visibility = View.VISIBLE
            originalReason = intent.getStringExtra(Constant.isReason) ?: ""
            originalLeaveFrom = intent.getStringExtra(Constant.isLeaveFrom) ?: ""
            originalLeaveTo = intent.getStringExtra(Constant.isLeaveTo) ?: ""
            originalLeaveType = intent.getStringExtra(Constant.isLeaveType) ?: Constant.Others2
            originalLeaveID = intent.getIntExtra(Constant.isLeaveTypeID, 0)
            originalFromSession = intent.getStringExtra(Constant.isFromSession) ?: Session[0]
            originalToSession = intent.getStringExtra(Constant.isToSession) ?: Session[1]

            binding.etLeaveReason.setText(originalReason)

            val backendFormat = DateTimeFormatter.ofPattern(Constant.ddMMyyyy)
            val displayFormat = DateTimeFormatter.ofPattern(Constant.EEE_comma_dd_MMM_yyyy)

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
//            val leaveType = leaveCategories.indexOf(originalLeaveType)
            val leaveTypeIndex = leaveCategories.indexOfFirst { it.id == originalLeaveID }
            if (leaveTypeIndex != -1) {
                binding.isLeaveCategories.setSelection(leaveTypeIndex)
                isLeaveCatoryID = leaveCategories[leaveTypeIndex].id
                originalLeaveID = leaveCategories[leaveTypeIndex].id
                isLeaveCategoryType = leaveCategories[leaveTypeIndex].name
            }
//            Log.d("leaveType", leaveType.toString())
//            if (leaveType != -1) binding.isLeaveCategories.setSelection(leaveType)
//            isLeaveCatoryID = originalLeaveID

            isFromSession = originalFromSession
            isToSession = originalToSession

            Log.d("isLeaveCategoryType", isLeaveCategoryType)

            validateDateAndSession(showError = true)
        }
    }

    private fun hasChangesMade(): Boolean {

        val currentReason = binding.etLeaveReason.text.toString().trim()
        val currentFrom = fromDate?.format(DateTimeFormatter.ofPattern(Constant.ddMMyyyy)) ?: ""
        val currentTo = toDate?.format(DateTimeFormatter.ofPattern(Constant.ddMMyyyy)) ?: ""
        val currentFSession = isFromSession
        val currentTSession = isToSession
        val currentLeaveCatoryID = isLeaveCatoryID

        Log.d("isLeaveFinalID", isLeaveCatoryID.toString())
        Log.d("isLeaveFinalID", currentTSession.toString())
        Log.d("isLeaveFinalID", currentFSession.toString())
        Log.d("isLeaveFinalID", currentTo.toString())
        Log.d("isLeaveFinalID", currentFrom.toString())
        Log.d("isLeaveFinalID", currentReason.toString())
        Log.d("isLeaveFinalID", originalReason.toString())
        Log.d("isLeaveFinalID", originalLeaveFrom.toString())
        Log.d("isLeaveFinalID", originalLeaveTo.toString())
        Log.d("isLeaveFinalID", originalFromSession.toString())
        Log.d("isLeaveFinalID", originalToSession.toString())
        Log.d("isLeaveFinalID", originalLeaveID.toString())


        return originalReason != currentReason ||
                originalLeaveFrom != currentFrom ||
                originalLeaveTo != currentTo ||
                originalFromSession != currentFSession ||
                originalToSession != currentTSession ||
                originalLeaveID != currentLeaveCatoryID
    }


    private fun validateDateAndSession(showError: Boolean = true): Boolean {
        val errors = mutableListOf<String>()
        val reason = binding.etLeaveReason.text.toString().trim()

//        // Validate Leave Type
//        if (isLeaveCategoryType.isNullOrBlank() || isLeaveCategoryType == Constant.Select_a_leave_type) {
//            errors.add(getString(R.string.leave_type_is_required))
//        }
        if (isLeaveCatoryID < 0 || isLeaveCategoryType == Constant.Select_a_leave_type) {
            errors.add(getString(R.string.leave_type_is_required))
        }

        // Validate Reason
        if (reason.isEmpty()) {
            errors.add(getString(R.string.reason_is_required))
        }

        // Validate Dates
        val isFromDateInvalid =
            fromDate == null || binding.tvFromDate.text == getString(R.string.select_date)
        val isToDateInvalid =
            toDate == null || binding.tvToDate.text == getString(R.string.select_date)

        if (isFromDateInvalid || isToDateInvalid) {
            errors.add(getString(R.string.both_from_and_to_dates_are_required))
        }


        // Validate Session (only if dates are valid)
        if (!isFromDateInvalid && !isToDateInvalid && fromDate != null && toDate != null) {
            if (toDate!!.isBefore(fromDate)) {
                errors.add(getString(R.string.to_date_cannot_be_before_from_date))
            }

            val fromIndex = Session.indexOf(isFromSession)
            val toIndex = Session.indexOf(isToSession)

            if (fromDate == toDate && fromIndex > toIndex) {
                errors.add(getString(R.string.from_session_cannot_be_after_to_session_on_the_same_day))
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
        val dayText = if (totalDays == 1f) getString(R.string.Day) else getString(R.string.days)

        binding.btnApplyLeave.text =
            "${getString(R.string.Apply_for)} $formattedDays $dayText ${getString(R.string.Leave)}"
        binding.btnupdate.text =
            "${getString(R.string.Update_for)} $formattedDays $dayText ${getString(R.string.Leave)}"

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
        binding.btnApplyLeave.text = getString(R.string.apply_leave)
        binding.btnupdate.text = getString(R.string.update_leave)
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
        Constant.showLoading(this)
        appViewModel!!.getLeaveCategories(isAccessToken!!, this)
    }


}
