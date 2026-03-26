package com.vs.schoolmessenger.Parent.Hostel


import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity

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
    private var allowPastTime = false   // true = allow past, false = block

    // TIME GAP (minutes)
    private var timeGapMinutes = 60



    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

//        val txtEndDate = Constant.convertDateFormat(txtEndDate!!)
//        val txtStartDate = Constant.convertDateFormat(txtStartDate!!)

        binding.lblSubmitRequest.setBackgroundTintList(
            ContextCompat.getColorStateList(this, R.color.PrimaryColor)
        )

        binding.toolbarLayout.consStudentDetails.visibility = View.VISIBLE
        binding.toolbarLayout.imgCall.visibility = View.GONE
        binding.toolbarLayout.rlaSpinner.visibility = View.GONE
        binding.toolbarLayout.lblMenuName.visibility = View.GONE

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        binding.lblFromDate.setOnClickListener(this)
        binding.lblToDate.setOnClickListener(this)
        binding.selectedFromDate.setOnClickListener(this)
        binding.selectedToDate.setOnClickListener(this)

        val (_, dayOfWeek, fullDate, _) = Constant.getCurrentDateInfo2()
        txtStartDate = fullDate
        txtEndDate = fullDate
        binding.selectedFromDate.text = txtStartDate
        binding.selectedToDate.text = txtEndDate
        setInitialTime()

        binding.selectedFromTime.setOnClickListener(this)
        binding.selectedToTime.setOnClickListener(this)


        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.imgBack.setOnClickListener(this)


    }


    override fun onDateSelected(date: String) {
        when (selectedDateField) {
            1 -> binding.selectedFromDate.text = date
            2 -> binding.selectedToDate.text = date
        }
    }

    private fun getCurrentCal(): Calendar = Calendar.getInstance()

    private fun formatTime(cal: Calendar): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }

    private fun parseTime(time: String): Calendar {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = sdf.parse(time) ?: Date()
        return Calendar.getInstance().apply { this.time = date }
    }

    private fun validateFromTime() {

        if (allowPastTime) {
            updateToTime()
            return   //  skip all validations
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
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

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val defaultCal = Calendar.getInstance()

                // Use previously selected date if available
                if (!txtStartDate.isNullOrEmpty()) {
                    try {
                        defaultCal.time = sdf.parse(txtStartDate!!) ?: Date()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                //  Min Date = 1 year before today
                val minCal = Calendar.getInstance()
                minCal.add(Calendar.YEAR, -1)
                val minDate = minCal.timeInMillis

                // Max Date = No restriction (future allowed)
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

                    //  End Date = Today OR Start Date (if start > today)
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

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
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

                val now = getCurrentCal()

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

                val selectedDate = sdf.parse(txtStartDate ?: "") ?: Date()
                val today = sdf.parse(sdf.format(Date())) ?: Date()

                //  IF allowPastTime = false → block past date
                if (!allowPastTime && selectedDate.before(today)) {
                    Toast.makeText(this, "Cannot select time for past date", Toast.LENGTH_SHORT).show()
                    return
                }

                val picker = android.app.TimePickerDialog(
                    this,
                    { _, hour, minute ->

                        val selectedCal = Calendar.getInstance()

                        selectedCal.set(Calendar.HOUR_OF_DAY, hour)
                        selectedCal.set(Calendar.MINUTE, minute)
                        selectedCal.set(Calendar.SECOND, 0)
                        selectedCal.set(Calendar.MILLISECOND, 0)

                        if (!allowPastTime && selectedDate == today) {

                            val nowCal = Calendar.getInstance()

                            selectedCal.set(Calendar.YEAR, nowCal.get(Calendar.YEAR))
                            selectedCal.set(Calendar.MONTH, nowCal.get(Calendar.MONTH))
                            selectedCal.set(Calendar.DAY_OF_MONTH, nowCal.get(Calendar.DAY_OF_MONTH)) // ✅ FIX

                            if (selectedCal.time.before(now.time)) {
                                Toast.makeText(this, "Past time not allowed", Toast.LENGTH_SHORT).show()
                                return@TimePickerDialog
                            }
                        }

                        txtFromTime = formatTime(selectedCal)
                        binding.selectedFromTime.text = txtFromTime

                        updateToTime()

                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
                )

                picker.show()
            }

            R.id.selectedToTime -> {

                if (txtFromTime.isNullOrEmpty()) {
                    Toast.makeText(this, "Select From Time first", Toast.LENGTH_SHORT).show()
                    return
                }

                val now = getCurrentCal()

                val picker = android.app.TimePickerDialog(
                    this,
                    { _, hour, minute ->

                        val selected = Calendar.getInstance()
                        selected.set(Calendar.HOUR_OF_DAY, hour)
                        selected.set(Calendar.MINUTE, minute)

                        txtToTime = formatTime(selected)
                        binding.selectedToTime.text = txtToTime
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
                )

                picker.show()
            }
        }
    }
}