package com.vs.schoolmessenger.School.PTM.Activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import android.view.inputmethod.InputMethodManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.CheckAvailableSlotsDate
import com.vs.schoolmessenger.School.PTM.Adapter.CustomCalendar
import com.vs.schoolmessenger.School.PTM.Adapter.SectionAndStandardAdapter
import com.vs.schoolmessenger.School.PTM.Adapter.SelectedClassSection
import com.vs.schoolmessenger.School.PTM.Adapter.SelectedDatesAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.AvailableSlotGroup
import com.vs.schoolmessenger.School.PTM.DataClass.MeetingCreationData
import com.vs.schoolmessenger.School.PTM.DataClass.SlotAvailability
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.School.PTM.DataClass.TimeSlot
import com.vs.schoolmessenger.School.PTM.DataClass.ValidatedSlot
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
    private var selectedSlots: List<Pair<String, SlotAvailability>> = emptyList()
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
    private var bottomSheetDialog: BottomSheetDialog? = null
    private val itemsCategory = listOf(
        "Select Slot Duration", "10", "15", "20", "30", "Custom"
    )
    private lateinit var isSlotCreateValues: MutableList<Pair<String, List<SlotAvailability>>>
    var isSlotDuration = ""
    var isMeetingMode = ""
    var isBreakDuration = ""
    private var isSelectedList: MutableList<SelectedClassSection> = mutableListOf()
    var isSlotDurationCustom = false
    var isOnlineMeeting = false

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
        binding.lblCheckAvailability.setOnClickListener(this)
        binding.lblPerson.setOnClickListener(this)
        binding.lblFiveMin.setOnClickListener(this)
        binding.lblTenMin.setOnClickListener(this)
        binding.lblTwentyMin.setOnClickListener(this)
        binding.lblThirtyMin.setOnClickListener(this)

        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name

        isChangeTheBackRound(binding.lblPerson)
        isChangeTheBackRoundBreakDuration(binding.lblFiveMin)

        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)
        isValidAcademicYear = isAcademicYear?.any { it.current_academic_year } == true
        isAcademicYearId = isAcademicYear!![0].id
        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
        isGetStandardSection()

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }


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

            appViewModel!!.isPtmSlotCreate?.observe(this) { response ->
                Constant.hideLoading(this)

                if (response != null) {
                    bottomSheetDialog?.dismiss()
                    if (response.status) {
                        Constant.showTopAlertPopup(response.message, this)
                    } else {
                        Constant.showTopAlertPopup("Slot creation failed!", this)
                    }
                }

        }

        appViewModel!!.isSlotValidation?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    isShowAvailableSlot(response.data)
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
                        standardId = standard.id.toString(),
                        standardName = standard.name,
                        sectionId = section.id.toString(),
                        sectionName = section.name
                    )
                )
            }
        }

        val adapter = SectionAndStandardAdapter(
            standardSectionList,
            this,
            Constant.isShimmerViewDisable
        ) { selectedList ->
            isSelectedList = selectedList.toMutableList()
        }
        binding.rcySectionAndStandardList.layoutManager = GridLayoutManager(this, 4)
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
                isSelectedList.clear()
                isGetStandardSection()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isGetStandardSection() {
        appViewModel!!.isGetStandardSection(isAccessToken!!, isAcademicYearId, this)
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
                if (isSlotsCount > 1) {
                    isSlotsCount--
                }
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

                        if (endCalendar!!.before(startCalendar)) {
                            Toast.makeText(
                                this,
                                "End Time cannot be before Start Time",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lblToTime.text = "End with"
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

            R.id.lblCheckAvailability -> {
                    isCheckAvailableSlots()
            }
        }
    }

    fun isCheckAvailableSlots() {
        val meetingData = validateMeetingInputs()
        if (meetingData != null) {
            val jsonArray = JsonArray()
            for (i in meetingData.selectedDates.indices) {
                val jsonObject = JsonObject()
                jsonObject.addProperty("date", meetingData.selectedDates[i])
                jsonObject.addProperty("event_name", meetingData.purpose)
                jsonObject.addProperty("from_time", meetingData.fromTime)
                jsonObject.addProperty("to_time", meetingData.toTime)
                jsonObject.addProperty("duration", meetingData.slotDuration)
                jsonObject.addProperty("event_link", meetingData.meetingLink)
                jsonObject.addProperty("break_time", meetingData.breakDuration)
                jsonObject.addProperty("meeting_mode", meetingData.meetingMode)


                val isStdSecJsonArray = JsonArray()
                for (i in meetingData.selectedSections.indices) {
                    val isStdSecJsonObject = JsonObject()
                    isStdSecJsonObject.addProperty(
                        "section_id",
                        meetingData.selectedSections.get(i).section_id
                    )
                    isStdSecJsonObject.addProperty(
                        "class_id",
                        meetingData.selectedSections.get(i).class_id
                    )
                    isStdSecJsonArray.add(isStdSecJsonObject)
                }

                val slotsTiming = splitIntoSlots(
                    binding.lblFromTime.text.toString(),
                    binding.lblToTime.text.toString(),
                    isSlotDuration.toInt()
                )


                val isSlotsDateJsonArray = JsonArray()

                for (i in slotsTiming.indices) {
                    val isSlotsDateJsonObject = JsonObject()
                    isSlotsDateJsonObject.addProperty("from_time", slotsTiming.get(i).fromTime)
                    isSlotsDateJsonObject.addProperty("to_time", slotsTiming.get(i).toTime)
                    isSlotsDateJsonArray.add(isSlotsDateJsonObject)
                }

                jsonObject.add("slots", isSlotsDateJsonArray)
                jsonObject.add("std_sec_details", isStdSecJsonArray)
                jsonArray.add(jsonObject)
            }

            Log.d("jsonArray", jsonArray.toString())
            appViewModel!!.isSlotValidationForStaff(
                isAccessToken!!, jsonArray
            )
        }
    }

    fun isCreateSlots() {
        val jsonArray = JsonArray()
        val meetingData = validateMeetingInputs() ?: return

        for (i in isSlotCreateValues.indices) {

            val date = isSlotCreateValues[i].first
            val slotsForDate = isSlotCreateValues[i].second

            val jsonObject = JsonObject().apply {
                addProperty("date", date)
                addProperty("event_name", meetingData.purpose)
                addProperty("from_time", meetingData.fromTime)
                addProperty("to_time", meetingData.toTime)
                addProperty("duration", meetingData.slotDuration.toInt())
                addProperty("event_link", meetingData.meetingLink)
                addProperty("break_time", 0)
                addProperty("meeting_mode", meetingData.meetingMode)
            }

            val isStdSecJsonArray = JsonArray()
            for (section in meetingData.selectedSections) {
                val isStdSecJsonObject = JsonObject()
                isStdSecJsonObject.addProperty("section_id", section.section_id)
                isStdSecJsonObject.addProperty("class_id", section.class_id)
                isStdSecJsonArray.add(isStdSecJsonObject)
            }
            jsonObject.add("std_sec_details", isStdSecJsonArray)
            val isSlotsDateJsonArray = JsonArray()
            for (slot in slotsForDate) {
                val isSlotsDateJsonObject = JsonObject()
                isSlotsDateJsonObject.addProperty("from_time", slot.slot_from)
                isSlotsDateJsonObject.addProperty("to_time", slot.slot_to)
                isSlotsDateJsonArray.add(isSlotsDateJsonObject)
            }
            jsonObject.add("slots", isSlotsDateJsonArray)

            jsonArray.add(jsonObject)
        }

        Log.d("isCreateSlots", jsonArray.toString())

        appViewModel!!.isSlotCreating(
            isAccessToken!!, jsonArray
        )
    }


    private fun isShowAvailableSlot(data: List<ValidatedSlot>) {
        val availableSlotsOnly = data.map { slot ->
            val filteredSlots = slot.slots.filter { it.slot_availablity.equals("Available", true) }
            slot.copy(slots = filteredSlots)
        }.filter { it.slots.isNotEmpty() }
        if (availableSlotsOnly.isEmpty()) {
            Toast.makeText(this, "No available slots for selected date(s)", Toast.LENGTH_SHORT).show()
            return
        }

        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.checkslot_create, null)
        bottomSheetDialog!!.setContentView(view)

        val isRcySlotDate = view.findViewById<RecyclerView>(R.id.rcySlotDate)
        val lblCreateSlot = view.findViewById<TextView>(R.id.lblCreateSlot)

        val groupedData = availableSlotsOnly.map { slot ->
            AvailableSlotGroup(slot.date, slot.slots.toMutableList())
        }

        val adapter = CheckAvailableSlotsDate(this, groupedData) { updatedList ->
            selectedSlots = updatedList
        }

        isRcySlotDate.layoutManager = GridLayoutManager(this, 1)
        isRcySlotDate.adapter = adapter

        lblCreateSlot?.setOnClickListener {
            val availableSlots = selectedSlots.filter { (_, slot) ->
                slot.slot_availablity.equals("Available", true)
            }

            if (availableSlots.isEmpty()) {
                Toast.makeText(this, "Please select at least one available slot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dialogBuilder = android.app.AlertDialog.Builder(this)
            dialogBuilder.setTitle("Confirm Slot Creation")
            dialogBuilder.setMessage("Are you sure you want to create slots for the selected dates?")
            dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
                isSlotCreateValues = availableSlots
                    .groupBy { it.first }
                    .map { (date, slots) -> date to slots.map { it.second } }
                    .toMutableList()
                Constant.showLoading(this)
                isCreateSlots()
                dialog.dismiss()
            }
            dialogBuilder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.create().show()
        }


        bottomSheetDialog!!.show()

        val bottomSheet = bottomSheetDialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = true
            behavior.skipCollapsed = true
        }
    }


    private fun validateMeetingInputs(): MeetingCreationData? {
        if (binding.edtPurPose.text.toString().isEmpty()) {
            Toast.makeText(this, "Enter the Purpose of meeting", Toast.LENGTH_SHORT).show()
            return null
        }

        if (isMeetingMode.isEmpty()) {
            Toast.makeText(this, "Select meeting mode", Toast.LENGTH_SHORT).show()
            return null
        }

        if (isOnlineMeeting && binding.edtMobileOrLink.text.toString().isEmpty()) {
            Toast.makeText(this, "Paste the meeting link", Toast.LENGTH_SHORT).show()
            return null
        }

        if (isSelectedList.isEmpty()) {
            Toast.makeText(this, "Select section and standard", Toast.LENGTH_SHORT).show()
            return null
        }

        if (selectedDates.isEmpty()) {
            Toast.makeText(this, "Please select choose the date", Toast.LENGTH_SHORT).show()
            return null
        }

        if (binding.lblFromTime.text.toString() == "Start with") {
            Toast.makeText(this, "Please choose the starting time", Toast.LENGTH_SHORT).show()
            return null
        }

        if (binding.lblToTime.text.toString() == "End with") {
            Toast.makeText(this, "Please choose the end time", Toast.LENGTH_SHORT).show()
            return null
        }

        if (isSlotDuration == "Select Slot Duration") {
            Toast.makeText(this, "Please choose the slot duration", Toast.LENGTH_SHORT).show()
            return null
        }

        if (isSlotDurationCustom && binding.edtSlotCustomDuration.text.toString().isEmpty()) {
            Toast.makeText(this, "Enter the slot duration", Toast.LENGTH_SHORT).show()
            return null
        }

        if (binding.switchBreak.isChecked() && isBreakDuration.isEmpty()) {
            Toast.makeText(this, "Choose the break duration", Toast.LENGTH_SHORT).show()
            return null
        }
        if (isSlotDurationCustom) {
            isSlotDuration = binding.edtSlotCustomDuration.text.toString()
        }

        return MeetingCreationData(
            purpose = binding.edtPurPose.text.toString(),
            meetingMode = isMeetingMode,
            meetingLink = binding.edtMobileOrLink.text.toString(),
            selectedSections = isSelectedList,
            selectedDates = selectedDates,
            fromTime = binding.lblFromTime.text.toString(),
            toTime = binding.lblToTime.text.toString(),
            slotDuration = isSlotDuration,
            slotsCount = binding.lblSlotsCount.text.toString(),
            breakDuration = isBreakDuration
        )
    }


    fun splitIntoSlots(start: String, end: String, durationMinutes: Int): List<TimeSlot> {
        val slots = ArrayList<TimeSlot>()
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val startDate = sdf.parse(start)
        val endDate = sdf.parse(end)
        if (startDate != null && endDate != null) {
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            while (calendar.time.before(endDate)) {
                val from = sdf.format(calendar.time)
                calendar.add(Calendar.MINUTE, durationMinutes)
                val to = if (calendar.time.before(endDate) || calendar.time == endDate) {
                    sdf.format(calendar.time)
                } else {
                    sdf.format(endDate)
                }
                slots.add(TimeSlot(from, to))
            }
        }
        return slots
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
                        isSlotDurationCustom = true
                    } else {
                        binding.rytSlotCustomEdit.visibility = View.GONE
                        isSlotDurationCustom = false
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showCalendarDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_calendar)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val calendarView = dialog.findViewById<CustomCalendar>(R.id.customCalendar)
        val btnSave = dialog.findViewById<TextView>(R.id.btnSaveCalendar)
        calendarView.setOnCancelListener {
            dialog.dismiss()
        }
        calendarView.setSelectedDates(selectedDates)
        btnSave.setOnClickListener {
            selectedDates.clear()
            selectedDates.addAll(calendarView.getSelectedDates())
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun isChangeTheBackRoundBreakDuration(isSelectedTextView: TextView) {
        binding.lblFiveMin.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblTenMin.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblTwentyMin.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblThirtyMin.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        isSelectedTextView.setBackgroundDrawable(this.getDrawable(R.drawable.green_bg_radius))
        isBreakDuration = isSelectedTextView.text.toString()
    }

    private fun isChangeTheBackRound(isSelectedTextView: TextView) {
        binding.lblPerson.apply {
            setBackgroundResource(R.drawable.gray_bg_radius)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        binding.lblOnline.apply {
            setBackgroundResource(R.drawable.gray_bg_radius)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        binding.lblPhoneCall.apply {
            setBackgroundResource(R.drawable.gray_bg_radius)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        isSelectedTextView.apply {
            setBackgroundResource(R.drawable.bg_button_blue_color)
            setTextColor(ContextCompat.getColor(context, R.color.white))
        }

        isMeetingMode = when (isSelectedTextView.id) {
            R.id.lblOnline -> "Online"
            R.id.lblPerson -> "Person"
            R.id.lblPhoneCall -> "Phone Call"
            else -> ""
        }
        if (isMeetingMode.equals("Online", ignoreCase = true)) {
            binding.lblLinkOrNumber.visibility = View.VISIBLE
            binding.edtMobileOrLink.visibility = View.VISIBLE
            binding.lblLinkOrNumber.text = "Paste the meeting link"
            binding.edtMobileOrLink.hint = "Paste the meeting link here"
            isOnlineMeeting = true
        } else {
            binding.lblLinkOrNumber.visibility = View.GONE
            binding.edtMobileOrLink.visibility = View.GONE
            isOnlineMeeting = false
        }

    }
    }

