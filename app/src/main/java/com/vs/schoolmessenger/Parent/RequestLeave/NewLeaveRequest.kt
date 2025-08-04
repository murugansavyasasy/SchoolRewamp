package com.vs.schoolmessenger.Parent.RequestLeave

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestUpdate
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.ActivityNewLeaveRequestBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NewLeaveRequest : BaseActivity<ActivityNewLeaveRequestBinding>(),
    CustomCalendarFragment.CalendarDateListener, View.OnClickListener {

    private var fromDate: LocalDate? = null
    private var toDate: LocalDate? = null
    var isFromSession = ""
    var isToSession = ""
    var RequestEdit=false
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private var originalReason: String = ""
    private var originalLeaveFrom: String = ""
    private var originalLeaveTo: String = ""
    private var originalFromSession: String = ""
    private var originalToSession: String = ""
    private var leaveRequestId: String = ""
//    private var leaveFromDate: String = ""
//    private var leaveToDate: String = ""
//    private var reasonText: String = ""


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
        binding.lblParentToolBar.text = getString(R.string.leave_requests)
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.lblStudentName.text = isChildDetails?.name ?: ""
        binding.lblStudentName.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.lblStudentSection.text = isChildDetails?.standard_name+ " - " +isChildDetails?.section_name
        binding.lblStudentSection.setTextColor(ContextCompat.getColor(this, R.color.white))

        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.imgBack.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)

        // Initially show From Date calendar only
        loadFromCalendar()
        isFromSpinner()
        isToSpinner()
        getIntentValuesIfEditing()
        validateDateAndSession(showError = true)

        binding.lnrFromDate.setOnClickListener {
            loadFromCalendar()
        }

        binding.lnrToDate.setOnClickListener {
            loadToCalendar(fromDate ?: LocalDate.now())
        }

        binding.FromDone.setOnClickListener {
            binding.calendarFromFragmentContainer.visibility = View.GONE
            binding.FromDone.visibility = View.GONE
            binding.activityMain.visibility=View.GONE
            binding.lblSelect.text = ""
        }

        binding.ToDone.setOnClickListener {
            binding.calendarToFragmentContainer.visibility = View.GONE
            binding.ToDone.visibility = View.GONE
            binding.activityMain.visibility=View.GONE
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
        binding.activityMain.visibility=View.VISIBLE
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
        binding.activityMain.visibility=View.VISIBLE
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

        val adapter = SpinnerLoadingAdapter(this, Session)
        binding.isFromSession.adapter = adapter

        // Set default selection to Session 1 (index 0)
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

    private fun isToSpinner() {

        val adapter = SpinnerLoadingAdapter(this, Session)
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

            R.id.btnupdate -> {

//                var hasError = false
//
//                // Validate date/session
//                if (!validateDateAndSession(showError = true)) {
//                    hasError = true
//                }
//
//                // Validate leave reason
//                if (binding.etLeaveReason.text.toString().trim().isEmpty()) {
//                    hasError = true
//                    Constant.errorAlert(this, getString(R.string.alert), "Please enter a reason for leave.")
//                }
//
//                // Set button background based on error status
//                if (hasError) {
//                    binding.btnupdate.setBackgroundColor(Color.GRAY)
//                    return
//                } else {
//                    binding.btnupdate.setBackgroundResource(R.drawable.bg_green_radious)
//                }

                if (binding.etLeaveReason.text.toString().trim().isEmpty()) {

                    return
                }


                // Proceed if changes are made
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
                    Constant.errorAlert(this, getString(R.string.alert), getString(R.string.no_changes_made))
                }
            }
        }
    }


    private fun isUpdateLeaveReq() {
    val updatedRequest = LeaveRequestUpdate(
        id = intent.getStringExtra("requestId") ?: "",
        leave_from = fromDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "",
        leave_to = toDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "",
        reason = binding.etLeaveReason.text.toString().trim(),
        f_session = if (isFromSession == "First Half") "FH" else "SH",
        t_session = if (isToSession == "First Half") "FH" else "SH"
    )

    appViewModel?.isleaverequestupdate(isAccessToken!!, updatedRequest, this)
}



    @RequiresApi(Build.VERSION_CODES.O)
    private fun getIntentValuesIfEditing() {
        RequestEdit = intent.getBooleanExtra("isRequestEdit", false)

        if (RequestEdit) {
            binding.btnApplyLeave.visibility = View.GONE
            binding.linearLayout9.visibility = View.VISIBLE

            originalReason = intent.getStringExtra("isReason") ?: ""
            originalLeaveFrom = intent.getStringExtra("isLeaveFrom") ?: ""
            originalLeaveTo = intent.getStringExtra("isLeaveTo") ?: ""
            originalFromSession = intent.getStringExtra("isFromSession") ?: Session[0]
            originalToSession = intent.getStringExtra("isToSession") ?: Session[1]

            binding.etLeaveReason.setText(originalReason)

            val backendFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val displayFormat = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")

            try {
                fromDate = LocalDate.parse(originalLeaveFrom, backendFormat)
                binding.tvFromDate.text = displayFormat.format(fromDate)

                toDate = LocalDate.parse(originalLeaveTo, backendFormat)
                binding.tvToDate.text = displayFormat.format(toDate)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val fromSessionIndex = Session.indexOf(originalFromSession)
            if (fromSessionIndex != -1) binding.isFromSession.setSelection(fromSessionIndex)

            val toSessionIndex = Session.indexOf(originalToSession)
            if (toSessionIndex != -1) binding.isToSession.setSelection(toSessionIndex)

            isFromSession = originalFromSession
            isToSession = originalToSession

            validateDateAndSession(showError = true)
        }
    }

    private fun hasChangesMade(): Boolean {
        val initialReason = intent.getStringExtra("isReason") ?: ""
        val initialFrom = intent.getStringExtra("isLeaveFrom") ?: ""
        val initialTo = intent.getStringExtra("isLeaveTo") ?: ""
        val initialFSession = intent.getStringExtra("isFromSession") ?: Session[0]
        val initialTSession = intent.getStringExtra("isToSession") ?: Session[1]

        val currentReason = binding.etLeaveReason.text.toString().trim()
        val currentFrom = fromDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: ""
        val currentTo = toDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: ""
        val currentFSession = isFromSession
        val currentTSession = isToSession

        return initialReason != currentReason ||
                initialFrom != currentFrom ||
                initialTo != currentTo ||
                initialFSession != currentFSession ||
                initialTSession != currentTSession
    }


    private fun validateDateAndSession(showError: Boolean = true): Boolean {
        val errors = mutableListOf<String>()

        if (fromDate == null || toDate == null) {
            if (showError) {
                binding.lblErrorMessage.text = ""
                binding.btnApplyLeave.text = "Apply Leave"
                val drawable = ContextCompat.getDrawable(this, R.drawable.background_radius_button)?.mutate()
                drawable?.setTint(Color.GRAY)
                binding.btnApplyLeave.background = drawable
                val drawable2 = ContextCompat.getDrawable(this, R.drawable.bg_green_radious)?.mutate()
                drawable2?.setTint(Color.GRAY)
                binding.btnupdate.background = drawable2
                binding.btnupdate.isEnabled=false
                binding.btnApplyLeave.isEnabled=false
            }
            return false
        }

        if (toDate!!.isBefore(fromDate)) {
            errors.add("To Date cannot be before From Date.")
        }

        val fromIndex = Session.indexOf(isFromSession)
        val toIndex = Session.indexOf(isToSession)

        if (fromDate == toDate && fromIndex > toIndex) {
            errors.add("From session cannot be after To session on the same day.")
        }

        if (errors.isNotEmpty()) {
            if (showError) {
                binding.lblErrorMessage.text = errors.joinToString("\n")
                binding.btnApplyLeave.text = "Apply Leave"
                val drawable = ContextCompat.getDrawable(this, R.drawable.background_radius_button)?.mutate()
                drawable?.setTint(Color.GRAY)
                binding.btnApplyLeave.background = drawable
                val drawable2 = ContextCompat.getDrawable(this, R.drawable.bg_green_radious)?.mutate()
                drawable2?.setTint(Color.GRAY)
                binding.btnupdate.background = drawable2
                binding.btnupdate.isEnabled=false
                binding.btnApplyLeave.isEnabled=false
            }
            return false
        }

        // On success
        if (showError) {
            binding.lblErrorMessage.text = ""

            var totalDays = 0f
            if (fromDate == toDate) {
                totalDays = if (fromIndex == toIndex) 0.5f else 1f
            } else {
                val daysBetween = ChronoUnit.DAYS.between(fromDate, toDate).toInt() + 1
                totalDays = daysBetween.toFloat()

                if (fromIndex > 0) totalDays -= 0.5f
                if (toIndex < Session.lastIndex) totalDays -= 0.5f
            }

            val formattedDays = if (totalDays % 1 == 0f) totalDays.toInt().toString() else totalDays.toString()
            val dayText = if (totalDays == 1f) "Day" else "Days"

            binding.btnApplyLeave.text = "Apply for $formattedDays $dayText Leave"
            binding.btnApplyLeave.setBackgroundResource(R.drawable.background_radius_button) // Restore original background
            binding.btnupdate.setBackgroundResource(R.drawable.bg_green_radious) // Restore original background
            binding.btnupdate.isEnabled=true
            binding.btnApplyLeave.isEnabled=true
        }

        return true
    }




}
