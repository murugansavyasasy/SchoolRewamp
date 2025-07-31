package com.vs.schoolmessenger.Parent.RequestLeave

import android.os.Bundle

import android.widget.Toast
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.EventsHolidays.CalendarFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ActivityNewLeaveRequestBinding
import java.text.SimpleDateFormat
import java.util.*

class NewLeaveRequest : BaseActivity<ActivityNewLeaveRequestBinding>() {

    private var isSelectingFromDate = true
    private val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    private var fromDate: Date = Calendar.getInstance().time
    private var toDate: Date = Calendar.getInstance().time

    override fun getViewBinding(): ActivityNewLeaveRequestBinding {
        return ActivityNewLeaveRequestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewLeaveRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbarBlue()

        loadCalendarFragment()


        val today = Calendar.getInstance().time
        fromDate = today
        toDate = today

        binding.tvFromDate.text = dateFormat.format(fromDate)
        binding.tvToDate.text = dateFormat.format(toDate)

//        binding.back.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
//
//        binding.customCalendar.onDateSelected = { selectedDate ->
//            if (isSelectingFromDate) {
//                fromDate = selectedDate
//                binding.tvFromDate.text = dateFormat.format(fromDate)
//                isSelectingFromDate = false
//            } else {
//                toDate = selectedDate
//                binding.tvToDate.text = dateFormat.format(toDate)
//                isSelectingFromDate = true
//                updateLeaveDurationButton()
//            }
//        }

//        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
//            val calendar = Calendar.getInstance()
//            calendar.set(year, month, dayOfMonth)
//            val selected = calendar.time
//
//            if (isSelectingFromDate) {
//                fromDate = selected
//                binding.tvFromDate.text = dateFormat.format(fromDate)
////                binding.tvCalendarTitle.text = "Select To Date"
//                isSelectingFromDate = false
//            } else {
//                toDate = selected
//                binding.tvToDate.text = dateFormat.format(toDate)
////                binding.tvCalendarTitle.text = "Select From Date"
//                isSelectingFromDate = true
//                updateLeaveDurationButton()
//            }
//        }

        binding.btnApplyLeave.setOnClickListener {
            if (fromDate.after(toDate)) {
                Toast.makeText(this, "From Date should be before To Date", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val days = calculateDaysBetween(fromDate, toDate)
            Toast.makeText(this, "Leave request for $days day(s) submitted!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun calculateDaysBetween(start: Date, end: Date): Int {
        val diffMillis = end.time - start.time
        return ((diffMillis / (1000 * 60 * 60 * 24)) + 1).toInt()
    }

    private fun updateLeaveDurationButton() {
        val days = calculateDaysBetween(fromDate, toDate)
        if (days > 0) {
            binding.btnApplyLeave.text = "Apply for ($days day${if (days > 1) "s" else ""}) leave"
        } else {
            binding.btnApplyLeave.text = "Invalid date range"
        }
    }

    private fun loadCalendarFragment() {
        val fragment = CalendarFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.calendarFragmentContainer, fragment)
            .commit()
    }
}
