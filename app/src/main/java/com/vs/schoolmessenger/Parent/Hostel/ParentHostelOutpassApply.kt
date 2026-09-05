package com.vs.schoolmessenger.Parent.Hostel


import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDetails.getParentHostelDetailsData

import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentHostelOutpassApplyBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ParentHostelOutpassApply : BaseActivity<ParentHostelOutpassApplyBinding>(),
    View.OnClickListener,OnDateSelectedListener {

    override fun getViewBinding(): ParentHostelOutpassApplyBinding {
        return ParentHostelOutpassApplyBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var selectedDateField: Int = 0
    private var txtStartDate: String? = null
    private var txtEndDate: String? = null

    private var txtFromTime: String? = null
    private var txtToTime: String? = null

    //CONTROL PAST TIME
    private var allowPastTime = false  // true = allow past, false = block

    // TIME GAP (minutes)
    private var timeGapMinutes = 60
    private var isParentHostelDetails: List<getParentHostelDetailsData> = listOf()




    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )


        binding.lblSubmitRequest.setBackgroundTintList(
            ContextCompat.getColorStateList(this, R.color.PrimaryColor)
        )

        binding.toolbarLayout.consStudentDetails.visibility = View.GONE
        binding.toolbarLayout.imgCall.visibility = View.GONE
        binding.toolbarLayout.rlaSpinner.visibility = View.GONE
        binding.toolbarLayout.lblMenuName.visibility = View.VISIBLE
        binding.toolbarLayout.imgSearchIcon.visibility= View.GONE

        binding.toolbarLayout.lblMenuName.text= getString(R.string.new_outpass_request)

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        binding.lblFromDate.setOnClickListener(this)
        binding.lblToDate.setOnClickListener(this)
        binding.selectedFromDate.setOnClickListener(this)
        binding.selectedToDate.setOnClickListener(this)


        val isHostelDetails = intent.getSerializableExtra("PARENT_HOSTEL_LIST") as? ArrayList<getParentHostelDetailsData> ?: arrayListOf()
        isParentHostelDetails=isHostelDetails

        val (_, dayOfWeek, fullDate, _) = Constant.getCurrentDateInfo2()
        txtStartDate = fullDate
        txtEndDate = fullDate
        binding.selectedFromDate.text = txtStartDate
        binding.selectedToDate.text = txtEndDate
        setInitialTime()

        binding.selectedFromTime.setOnClickListener(this)
        binding.selectedToTime.setOnClickListener(this)
        binding.lblSubmitRequest.setOnClickListener(this)


        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        appViewModel!!.applyHostelOutpass?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    Log.d("applyHostelOutpass", response.message)
                    Constant.showParentDataValidation(getString(R.string.success), response.message,this)
                } else {
                    Log.d("applyHostelOutpass", response.message)
                    Constant.showDataValidationNoDashboardRedirect(getString(R.string.fail), response.message,this)
                }
            } else {
                Constant.showDataValidationNoDashboardRedirect(
                    getString(R.string.fail),
                    getString(R.string.something_went_wrong_please_try_again_later),
                    this
                )
            }
        }

        binding.lblOutpassReason.setRequiredLabel("Reason for Outpass *")
        binding.lblFromDate.setRequiredLabel("From Date *")
        binding.lblToDate.setRequiredLabel("To Date *")
        binding.lblFromTime.setRequiredLabel("From Time *")
        binding.lblToTime.setRequiredLabel("To Time *")
        binding.lblEmergerncyContact.setRequiredLabel("Emergency Contact Number *")

        binding.lblSubmitRequest.setOnClickListener {

            // VALIDATION
            if (txtStartDate.isNullOrEmpty()) {
                Toast.makeText(this, "Please select From Date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (txtEndDate.isNullOrEmpty()) {
                Toast.makeText(this, "Please select To Date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (txtFromTime.isNullOrEmpty()) {
                Toast.makeText(this, "Please select From Time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (txtToTime.isNullOrEmpty()) {
                Toast.makeText(this, "Please select To Time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (binding.edtReason.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (binding.edtContactNumber.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter contact number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // FORMAT DATE + TIME (dd-MM-yyyy hh:mm a)
            val apiDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH)
            val inputDateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH)

            val fromDateTime = inputDateFormat.parse("$txtStartDate $txtFromTime")
            val toDateTime = inputDateFormat.parse("$txtEndDate $txtToTime")

            val outDate = apiDateFormat.format(fromDateTime!!)
            val inDate = apiDateFormat.format(toDateTime!!)

            Constant.showSendConfirmationDialog(
                this,
                getString(R.string.confirmation),
                getString(R.string.permission_ok),
                getString(R.string.Cancel),
                "",
                getString(R.string.are_you_sure_want_to_apply_outpass)
            ) { confirmed ->

                if (confirmed) {

                    val finalJson = JsonObject().apply {
                        addProperty("hostel_id", isParentHostelDetails.firstOrNull()?.hostel_id?:"")
                        addProperty("room_id",isParentHostelDetails.firstOrNull()?.room_id?:"")
                        addProperty("out_date", outDate)
                        addProperty("in_date", inDate)
                        addProperty("emergency_contact", binding.edtContactNumber.text.toString().trim())
                        addProperty("reason", binding.edtReason.text.toString().trim())
                    }

                    Log.d("FINAL_JSON", finalJson.toString())

                    Constant.showLoading(this)

                    appViewModel?.applyHostelOutpass(
                        isAccessToken ?: "",
                        finalJson,
                        this
                    )
                }
            }
        }

    }

    fun TextView.setRequiredLabel(text: String) {
        val spannable = SpannableString(text)

        if (text.endsWith("*")) {
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, android.R.color.holo_red_dark)),
                text.length - 1,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, android.R.color.black)),
                0,
                text.length - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        this.text = spannable
    }

    override fun onDateSelected(date: String) {
        when (selectedDateField) {
            1 -> binding.selectedFromDate.text = date
            2 -> binding.selectedToDate.text = date
        }
    }

    private fun getCurrentCal(): Calendar = Calendar.getInstance()

    private fun formatTime(cal: Calendar): String {
        return SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(cal.time)
    }

    private fun parseTime(time: String): Calendar {
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val date = sdf.parse(time) ?: Date()
        return Calendar.getInstance().apply { this.time = date }
    }

    private fun validateFromTime() {

        if (allowPastTime) {
            updateToTime()
            return   //  skip all validations
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        val selectedDate = sdf.parse(txtStartDate ?: "") ?: Date()
        val today = sdf.parse(sdf.format(Date())) ?: Date()

        val now = getCurrentCal()

        // BLOCK past date ONLY when allowPastTime = false
        if (selectedDate.before(today)) {
            Toast.makeText(this, "Past date not allowed", Toast.LENGTH_SHORT).show()

            txtStartDate = sdf.format(today)
            binding.selectedFromDate.text = txtStartDate

            txtFromTime = formatTime(now)
            binding.selectedFromTime.text = txtFromTime

            updateToTime()
            return
        }

        //  BLOCK past time (only today)
        if (selectedDate == today) {

            if (!txtFromTime.isNullOrEmpty()) {

                val fromCal = parseTime(txtFromTime!!)
                fromCal.set(Calendar.YEAR, now.get(Calendar.YEAR))
                fromCal.set(Calendar.MONTH, now.get(Calendar.MONTH))
                fromCal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

                if (fromCal.time.before(now.time)) {
                    Toast.makeText(this, "From time is past, resetting", Toast.LENGTH_SHORT).show()

                    txtFromTime = formatTime(now)
                    binding.selectedFromTime.text = txtFromTime
                }
            }
        }

        updateToTime()
    }

    private fun setInitialTime() {

        val now = getCurrentCal()

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val todayStr = sdf.format(Date())

        // If today → use current time
        if (txtStartDate == todayStr) {
            txtFromTime = formatTime(now)
        } else {
            // future date → you can still use current time
            txtFromTime = formatTime(now)
        }

        binding.selectedFromTime.text = txtFromTime

        // Auto set To Time
        updateToTime()
    }

    private fun updateToTime() {

        if (txtFromTime.isNullOrEmpty()) return

        val cal = parseTime(txtFromTime!!)
        cal.add(Calendar.MINUTE, timeGapMinutes)

        txtToTime = formatTime(cal)
        binding.selectedToTime.text = txtToTime
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblFromDate, R.id.selectedFromDate -> {

                selectedDateField = 1

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                val defaultCal = Calendar.getInstance()

                if (!txtStartDate.isNullOrEmpty()) {
                    try {
                        defaultCal.time = sdf.parse(txtStartDate!!) ?: Date()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                //  Min Date = Today (No past allowed)
                val minCal = Calendar.getInstance()
                minCal.set(Calendar.HOUR_OF_DAY, 0)
                minCal.set(Calendar.MINUTE, 0)
                minCal.set(Calendar.SECOND, 0)
                minCal.set(Calendar.MILLISECOND, 0)

                val minDate = minCal.timeInMillis

                // Max Date = No restriction
                val maxDate = Long.MAX_VALUE

                Constant.DatePicker(
                    context = this,
                    dateFormatType = false,
                    defaultDate = defaultCal,
                    minDate = minDate,
                    maxDate = maxDate
                ) { selectedDate ->

                    txtStartDate = Constant.covertDateFormate(selectedDate)
                    binding.selectedFromDate.text = txtStartDate

                    val startDate = sdf.parse(txtStartDate!!) ?: Date()
                    val today = Calendar.getInstance().time

                    txtEndDate = if (startDate.after(today)) {
                        sdf.format(startDate)
                    } else {
                        sdf.format(today)
                    }

                    binding.selectedToDate.text = txtEndDate

                    validateFromTime()
                }
            }

            R.id.lblToDate, R.id.selectedToDate -> {

                selectedDateField = 2

                if (txtStartDate.isNullOrEmpty()) {
                    Toast.makeText(
                        this,
                        getString(R.string.please_select_a_start_date_first),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                val startDate = sdf.parse(txtStartDate!!) ?: Date()

                val cal = Calendar.getInstance()
                cal.time = startDate

                // Min date = From Date
                val minDate = cal.timeInMillis

                // No max restriction (allow future)
                val maxDate = Long.MAX_VALUE

                val defaultEndDate = try {
                    sdf.parse(txtEndDate ?: "") ?: startDate
                } catch (e: Exception) {
                    startDate
                }

                val defaultCal = Calendar.getInstance()
                defaultCal.time = defaultEndDate

                Constant.DatePicker(
                    context = this,
                    dateFormatType = false,
                    defaultDate = defaultCal,
                    minDate = minDate,
                    maxDate = maxDate
                ) { selectedDate ->

                    txtEndDate = Constant.covertDateFormate(selectedDate)
                    binding.selectedToDate.text = txtEndDate
                }
            }


            R.id.selectedFromTime -> {

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

                val selectedDate = sdf.parse(txtStartDate ?: "") ?: Date()
                val today = sdf.parse(sdf.format(Date())) ?: Date()

                if (!allowPastTime && selectedDate.before(today)) {
                    Toast.makeText(this, "Cannot select time for past date", Toast.LENGTH_SHORT).show()
                    return
                }

                //  USE PREVIOUS TIME OR CURRENT TIME
                val defaultCal = if (!txtFromTime.isNullOrEmpty()) {
                    parseTime(txtFromTime!!)
                } else {
                    getCurrentCal()
                }

                val picker = android.app.TimePickerDialog(
                    this,
                    { _, hour, minute ->

                        val selectedCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (!allowPastTime && selectedDate == today) {

                            val nowCal = Calendar.getInstance()

                            selectedCal.set(Calendar.YEAR, nowCal.get(Calendar.YEAR))
                            selectedCal.set(Calendar.MONTH, nowCal.get(Calendar.MONTH))
                            selectedCal.set(Calendar.DAY_OF_MONTH, nowCal.get(Calendar.DAY_OF_MONTH))

                            if (selectedCal.time.before(nowCal.time)) {
                                Toast.makeText(this, "Past time not allowed", Toast.LENGTH_SHORT).show()
                                return@TimePickerDialog
                            }
                        }

                        txtFromTime = formatTime(selectedCal)
                        binding.selectedFromTime.text = txtFromTime

                        updateToTime()

                    },
                    defaultCal.get(Calendar.HOUR_OF_DAY),
                    defaultCal.get(Calendar.MINUTE),
                    false
                )

                picker.show()
            }

            R.id.selectedToTime -> {

                if (txtFromTime.isNullOrEmpty()) {
                    Toast.makeText(this, "Select From Time first", Toast.LENGTH_SHORT).show()
                    return
                }

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

                val fromDate = sdf.parse(txtStartDate ?: "") ?: Date()
                val toDate = sdf.parse(txtEndDate ?: "") ?: Date()

                val isSameDay = sdf.format(fromDate) == sdf.format(toDate)

                //  USE PREVIOUS TIME OR CURRENT
                val defaultCal = if (!txtToTime.isNullOrEmpty()) {
                    parseTime(txtToTime!!)
                } else {
                    getCurrentCal()
                }

                val picker = android.app.TimePickerDialog(
                    this,
                    { _, hour, minute ->

                        val selectedCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        // ALWAYS enforce for same day
                        if (isSameDay) {

                            val fromCal = parseTime(txtFromTime!!)

                            val baseCal = Calendar.getInstance()

                            selectedCal.set(Calendar.YEAR, baseCal.get(Calendar.YEAR))
                            selectedCal.set(Calendar.MONTH, baseCal.get(Calendar.MONTH))
                            selectedCal.set(Calendar.DAY_OF_MONTH, baseCal.get(Calendar.DAY_OF_MONTH))

                            fromCal.set(Calendar.YEAR, baseCal.get(Calendar.YEAR))
                            fromCal.set(Calendar.MONTH, baseCal.get(Calendar.MONTH))
                            fromCal.set(Calendar.DAY_OF_MONTH, baseCal.get(Calendar.DAY_OF_MONTH))

                            if (selectedCal.time.before(fromCal.time)) {
                                Toast.makeText(this, "To time cannot be before From time", Toast.LENGTH_SHORT).show()
                                return@TimePickerDialog
                            }
                        }

                        txtToTime = formatTime(selectedCal)
                        binding.selectedToTime.text = txtToTime

                    },
                    defaultCal.get(Calendar.HOUR_OF_DAY),
                    defaultCal.get(Calendar.MINUTE),
                    false
                )

                picker.show()
            }
        }
    }
}