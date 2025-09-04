package com.vs.schoolmessenger.School.PTM.Activity

import android.app.Dialog
import android.app.TimePickerDialog
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.CustomCalendar
import com.vs.schoolmessenger.School.PTM.Adapter.SectionAndStandardAdapter
import com.vs.schoolmessenger.School.PTM.Adapter.SelectedDatesAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.CreateSlotsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateSlots : BaseActivity<CreateSlotsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): CreateSlotsBinding {
        return CreateSlotsBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private lateinit var selectedDatesAdapter: SelectedDatesAdapter
    private var isStaffDetails: StaffDetails? = null
    private var appViewModel: App? = null
    var isValidAcademicYear = false
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    var isAcademicYear: List<AcademicYear>? = null
    private val selectedDates = ArrayList<String>()
    var startCalendar: Calendar? = null
    var endCalendar: Calendar? = null
    var isSlotsCount = 1

    private val itemsCategory = listOf(
        "Select Slot Duration", "10", "15", "20", "30", "Custom"
    )
    var isSlotDuration = ""


    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.lblOnline.setOnClickListener(this)
        binding.lblPhoneCall.setOnClickListener(this)
        binding.rytPickFromTime.setOnClickListener(this)
        binding.rytToTime.setOnClickListener(this)
        binding.rytPickDurationBreak.setOnClickListener(this)
        binding.rytPickDate.setOnClickListener(this)
        binding.imgCountUp.setOnClickListener(this)
        binding.imgCountDown.setOnClickListener(this)
        binding.lblPerson.setOnClickListener(this)
        binding.lblFiveMin.setOnClickListener(this)
        binding.lblTenMin.setOnClickListener(this)
        binding.lblTwentyMin.setOnClickListener(this)
        binding.lblThirtyMin.setOnClickListener(this)

        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name

        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)
        isValidAcademicYear = isAcademicYear?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYear!![0].id
        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
        isGetStandardSection()

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcySectionAndStandardList.visibility = View.VISIBLE
                    loadSectionStandard(response.data)
                } else {
                    binding.rcySectionAndStandardList.visibility = View.GONE
                }
            }
        }

        binding.switchBreak.setOnClickListener {
            if (binding.switchBreak.isChecked()) {
                binding.rytNeedBreak.visibility = View.VISIBLE
            } else {
                binding.rytNeedBreak.visibility = View.GONE
            }
        }

        isLoadSlotDuration()
    }

    private fun loadSectionStandard(data: List<Standard>) {
        val standardSectionList = mutableListOf<StandardSection>()

        for (standard in data) {
            for (section in standard.sections) {
                standardSectionList.add(
                    StandardSection(
                        standardName = standard.name,
                        sectionName = section.name
                    )
                )
            }
        }

        val adapter =
            SectionAndStandardAdapter(standardSectionList, this, Constant.isShimmerViewDisable)
        binding.rcySectionAndStandardList.layoutManager = GridLayoutManager(this, 5)
        binding.rcySectionAndStandardList.adapter = adapter
    }


    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter
        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                val selectedOption = isAcademicYear!![position]
                isAcademicYearId = selectedOption.id
                isCurrentAcademicYear = selectedOption.current_academic_year
                Log.d(
                    "DropdownMenu",
                    "Clicked Standard Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                )
                isGetStandardSection()

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isGetStandardSection() {
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblPerson -> {
                isChangeTheBackRound(binding.lblPerson)
            }

            R.id.lblOnline -> {
                isChangeTheBackRound(binding.lblOnline)
            }

            R.id.lblPhoneCall -> {
                isChangeTheBackRound(binding.lblPhoneCall)
            }
            R.id.rytPickDate -> {
                showCalendarDialog()
            }

            R.id.lblFiveMin -> {
                isChangeTheBackRoundBreakDuration(binding.lblFiveMin)
            }

            R.id.lblTenMin -> {
                isChangeTheBackRoundBreakDuration(binding.lblTenMin)
            }

            R.id.lblTwentyMin -> {
                isChangeTheBackRoundBreakDuration(binding.lblTwentyMin)
            }

            R.id.lblThirtyMin -> {
                isChangeTheBackRoundBreakDuration(binding.lblThirtyMin)
            }

            R.id.imgCountUp -> {
                isSlotsCount++
                binding.lblSlotsCount.text = isSlotsCount.toString()
            }

            R.id.imgCountDown -> {
                isSlotsCount--
                binding.lblSlotsCount.text = isSlotsCount.toString()
            }

            R.id.rytPickDurationBreak -> {
                isLoadSlotDuration()
            }

            R.id.rytPickFromTime -> {
                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        startCalendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                        }
                        binding.lblFromTime.text =
                            SimpleDateFormat(
                                "hh:mm a",
                                Locale.getDefault()
                            ).format(startCalendar!!.time)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }

            R.id.rytToTime -> {
                if (startCalendar == null) {
                    Toast.makeText(this, "Please select Start Time first", Toast.LENGTH_SHORT)
                        .show()
                    return
                }

                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        endCalendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                        }

                        // Validation
                        if (endCalendar!!.before(startCalendar)) {
                            Toast.makeText(
                                this,
                                "End Time cannot be before Start Time",
                                Toast.LENGTH_SHORT
                            ).show()
                            endCalendar = null
                        } else {
                            binding.lblToTime.text =
                                SimpleDateFormat(
                                    "hh:mm a",
                                    Locale.getDefault()
                                ).format(endCalendar!!.time)
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }
        }
    }

    fun isLoadSlotDuration() {
        val adapter = SpinnerLoadingAdapter(this, itemsCategory)
        binding.spinnerSlotDuration.adapter = adapter

        binding.spinnerSlotDuration.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    isSlotDuration = itemsCategory[position]
                    if (isSlotDuration == "Custom") {
                        binding.rytSlotCustomEdit.visibility = View.VISIBLE
                    } else {
                        binding.rytSlotCustomEdit.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun showCalendarDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_calendar)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val calendarView = dialog.findViewById<CustomCalendar>(R.id.customCalendar)
        val btnSave = dialog.findViewById<TextView>(R.id.btnSaveCalendar)

        // restore previously saved selection
        calendarView.setSelectedDates(selectedDates)

        btnSave.setOnClickListener {
            selectedDates.clear()
            selectedDates.addAll(calendarView.getSelectedDates())

            // now update your RecyclerView/GridView adapter
            selectedDatesAdapter = SelectedDatesAdapter(selectedDates) { date ->
                selectedDates.remove(date)
                selectedDatesAdapter.notifyDataSetChanged()
                calendarView.setSelectedDates(selectedDates) // keep sync with calendar
            }
            binding.rcySelectedDate.layoutManager = GridLayoutManager(this, 3)
            binding.rcySelectedDate.adapter = selectedDatesAdapter

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun isChangeTheBackRoundBreakDuration(isSelectedTextView: TextView) {
        binding.lblFiveMin.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblTenMin.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblTwentyMin.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblThirtyMin.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        isSelectedTextView.setBackgroundDrawable(this.getDrawable(R.drawable.bg_light_blue))
    }

    private fun isChangeTheBackRound(isSelectedTextView: TextView) {
        binding.lblPerson.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblOnline.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblPhoneCall.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        isSelectedTextView.setBackgroundDrawable(this.getDrawable(R.drawable.bg_light_blue))
    }
}