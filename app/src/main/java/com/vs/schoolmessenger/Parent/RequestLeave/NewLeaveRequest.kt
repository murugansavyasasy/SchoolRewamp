package com.vs.schoolmessenger.Parent.RequestLeave

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
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
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
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null


    private val Session = listOf(
        "Session 1", " Session 2",
    )

    override fun getViewBinding(): ActivityNewLeaveRequestBinding {
        return ActivityNewLeaveRequestBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbarBlue()

        binding.imgBack.setOnClickListener(this)
        binding.lblParentToolBar.text = getString(R.string.Leave_Request)
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

                val errors = mutableListOf<String>()

                if (toDate != null && toDate!!.isBefore(fromDate)) {
                    errors.add("To Date cannot be before From Date")
                }

                if (errors.isNotEmpty()) {
                    binding.lblErrorMessage.text = errors.joinToString("\n")
                    binding.btnApplyLeave.text = "Apply Leave"
                } else {
                    binding.lblErrorMessage.text = ""
                    calculateDays()
                }
            }

            "TO_DATE" -> {
                toDate = selected
                binding.tvToDate.text = formatDate(selected)

                val errors = mutableListOf<String>()

                if (fromDate == null) {
                    errors.add("Please select From Date first")
                } else if (selected.isBefore(fromDate)) {
                    errors.add("To Date cannot be before From Date")
                }

                if (errors.isNotEmpty()) {
                    binding.lblErrorMessage.text = errors.joinToString("\n")
                    binding.btnApplyLeave.text = "Apply Leave"
                } else {
                    binding.lblErrorMessage.text = ""
                    calculateDays()
                }
            }

        }
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
                calculateDays()

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
                calculateDays()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun calculateDays() {
        binding.lblErrorMessage.text = ""

        if (fromDate == null || toDate == null) {
            binding.btnApplyLeave.text = "Apply Leave"
            return
        }

        // Collect all errors
        val errors = mutableListOf<String>()

        if (toDate!!.isBefore(fromDate)) {
            errors.add("To date should be greater than from date")
        }

        val fromIndex = Session.indexOf(isFromSession)
        val toIndex = Session.indexOf(isToSession)

        if (fromDate == toDate && fromIndex > toIndex) {
            errors.add("From session cannot be after To session on the same day.")
        }

        if (errors.isNotEmpty()) {
            // Show all errors in one TextView
            binding.lblErrorMessage.text = errors.joinToString("\n")
            binding.btnApplyLeave.text = "Apply Leave"
            return
        }

        // Valid range, calculate total days
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

    }
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }






}
