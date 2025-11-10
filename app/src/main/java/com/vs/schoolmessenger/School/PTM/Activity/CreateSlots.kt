package com.vs.schoolmessenger.School.PTM.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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
import com.vs.schoolmessenger.School.PTM.DataClass.ValidatedSlot
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.CreateSlotsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    var breakAfterSlots = ""
    private var bottomSheetDialog: BottomSheetDialog? = null
    private val itemsCategory = listOf(
        "Select Slot Duration", "10 mins", "15 mins", "20 mins", "30 mins", "Custom"
    )
    private lateinit var isSlotCreateValues: MutableList<Pair<String, List<SlotAvailability>>>
    var isSlotDuration = ""
    var isMeetingMode = ""
    var isBreakDuration = "0"
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

        binding.lblMenuName.text = Constant.isSelectedMenuName
        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                if (response.status && response.data.isNotEmpty()) {
                    binding.rcySectionAndStandardList.visibility = View.VISIBLE
                    loadSectionStandard(response.data)
                } else {
                    binding.rcySectionAndStandardList.visibility = View.GONE
                    selectedDates.clear()
                    selectedSlots = emptyList()
                    isSelectedList.clear()
                    Constant.showTopAlertPopup1(
                        response.message?:"No standards found for selected academic year",
                        this,
                        false
                    )
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
                isBreakDuration = "5"
                binding.rytNeedBreak.visibility = View.VISIBLE
            } else {
                binding.rytNeedBreak.visibility = View.GONE
                isBreakDuration = "0"
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

        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }

        binding.rcySectionAndStandardList.layoutManager = flexboxLayoutManager
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
                        val now = Calendar.getInstance()
                        val chosenTime = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        // Format today and compare properly
                        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val todayStr = sdf.format(now.time)

                        // Normalize selectedDates format to dd-MM-yyyy
                        val normalizedSelectedDates = selectedDates.map {
                            try {
                                // Try to parse any format like dd-MMM-yyyy or yyyy-MM-dd
                                val parsed =
                                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it)
                                sdf.format(parsed!!)
                            } catch (e: Exception) {
                                it
                            }
                        }

                        // Case: multiple dates including today
                        if (normalizedSelectedDates.contains(todayStr)) {
                            val nowTime = Calendar.getInstance().apply {
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            if (chosenTime.before(nowTime)) {
                                Toast.makeText(
                                    this,
                                    "Cannot select past time when today is selected",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TimePickerDialog
                            }
                        }

                        // Save and display
                        startCalendar = chosenTime
                        binding.lblFromTime.text =
                            SimpleDateFormat(
                                "hh:mm a",
                                Locale.getDefault()
                            ).format(startCalendar!!.time)

                        // Reset end time
                        binding.lblToTime.text = "End with"
                        endCalendar = null
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
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        when {
                            endCalendar!!.before(startCalendar) -> {
                                Toast.makeText(
                                    this,
                                    "End Time cannot be before Start Time",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.lblToTime.text = "End with"
                                endCalendar = null
                            }

                            endCalendar!!.timeInMillis == startCalendar!!.timeInMillis -> {
                                Toast.makeText(
                                    this,
                                    "Start Time and End Time cannot be the same",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.lblToTime.text = "End with"
                                endCalendar = null
                            }

                            else -> {
                                binding.lblToTime.text = SimpleDateFormat(
                                    "hh:mm a",
                                    Locale.getDefault()
                                ).format(endCalendar!!.time)
                            }
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
                jsonObject.addProperty("from_time", formatTimeWithAMPM(meetingData.fromTime))
                jsonObject.addProperty("to_time", formatTimeWithAMPM(meetingData.toTime))
                jsonObject.addProperty("duration", meetingData.slotDuration)
                jsonObject.addProperty("event_link", meetingData.meetingLink)
                jsonObject.addProperty("break_time", meetingData.break_time)
                jsonObject.addProperty("meeting_mode", meetingData.meetingMode)

                // Prepare class-section JSON
                val isStdSecJsonArray = JsonArray()
                for (section in meetingData.selectedSections) {
                    val isStdSecJsonArrayObj = JsonObject()
                    isStdSecJsonArrayObj.addProperty("section_id", section.section_id)
                    isStdSecJsonArrayObj.addProperty("class_id", section.class_id)
                    isStdSecJsonArray.add(isStdSecJsonArrayObj)
                }

                // Break duration in minutes
                val breakMinutes =
                    if (!meetingData.break_time.isNullOrEmpty() && meetingData.break_time != "0") {
                        meetingData.break_time.toInt()
                    } else 0

                if (binding.switchBreak.isChecked()) {
                    breakAfterSlots = binding.lblSlotsCount.text.toString()
                } else {
                    breakAfterSlots = "0"
                }
                // Debug logs (helpful during testing)
                Log.d(
                    "SlotCalc",
                    "from=${formatTimeWithAMPM(meetingData.fromTime)} to=${
                        formatTimeWithAMPM(meetingData.toTime)
                    } " +
                            "slotDuration=${meetingData.slotDuration} breakMinutes=$breakMinutes breakAfterSlots=$breakAfterSlots"
                )

                // Generate time slots
                val slotsTiming = splitIntoSlots(
                    startTime = formatTimeWithAMPM(meetingData.fromTime),
                    endTime = formatTimeWithAMPM(meetingData.toTime),
                    slotDurationMinutes = meetingData.slotDuration.toInt(),
                    breakMinutes = breakMinutes,
                    breakAfterSlots = breakAfterSlots.toInt()
                )

                // Build slots JSON array
                val isSlotsDateJsonArray = JsonArray()
                for (slot in slotsTiming) {
                    val slotObj = JsonObject()
                    slotObj.addProperty("from_time", slot.fromTime)
                    slotObj.addProperty("to_time", slot.toTime)
                    isSlotsDateJsonArray.add(slotObj)
                }

                jsonObject.add("slots", isSlotsDateJsonArray)
                jsonObject.add("std_sec_details", isStdSecJsonArray)
                jsonArray.add(jsonObject)
            }

            Log.d("jsonArray", jsonArray.toString())
            appViewModel!!.isSlotValidationForStaff(isAccessToken!!, jsonArray)
        }
    }

    // --- slot generator ---
    fun splitIntoSlots(
        startTime: String,
        endTime: String,
        slotDurationMinutes: Int,
        breakMinutes: Int,
        breakAfterSlots: Int = 0 // 0 => no break
    ): List<SlotTiming> {
        val slots = mutableListOf<SlotTiming>()
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val startDate = try {
            sdf.parse(startTime)
        } catch (e: Exception) {
            null
        }
        val endDate = try {
            sdf.parse(endTime)
        } catch (e: Exception) {
            null
        }
        if (startDate == null || endDate == null) {
            Log.e(
                "splitIntoSlots",
                "Invalid start or end time format. start=$startTime end=$endTime"
            )
            return slots
        }

        // Defensive: if slot duration <=0 or start >= end, nothing to do
        if (slotDurationMinutes <= 0 || !startDate.before(endDate)) return slots

        var cursor = Calendar.getInstance().apply { time = startDate }
        val endCal = Calendar.getInstance().apply { time = endDate }

        var slotCounter = 0

        while (cursor.before(endCal)) {
            val slotStart = cursor.time

            // compute slot end
            val slotEndCal = Calendar.getInstance().apply { time = slotStart }
            slotEndCal.add(Calendar.MINUTE, slotDurationMinutes)

            // if slot end is after overall end -> stop
            if (slotEndCal.time.after(endCal.time)) break

            // add slot
            slots.add(
                SlotTiming(
                    fromTime = sdf.format(slotStart),
                    toTime = sdf.format(slotEndCal.time)
                )
            )
            slotCounter++

            // Advance cursor: normally to the end of this slot
            cursor.time = slotEndCal.time

            // If we need to add a break after every N slots
            if (breakAfterSlots > 0 && slotCounter % breakAfterSlots == 0 && breakMinutes > 0) {
                // add break minutes
                cursor.add(Calendar.MINUTE, breakMinutes)

                // If cursor now exceeds end time, break the loop
                if (!cursor.before(endCal)) break
            }
        }

        Log.d(
            "splitIntoSlots",
            "Generated ${slots.size} slots. breakAfterSlots=$breakAfterSlots breakMinutes=$breakMinutes"
        )
        return slots
    }


    data class SlotTiming(
        val fromTime: String,
        val toTime: String
    )

    private fun formatTimeWithAMPM(time: String): String {
        return try {
            // If already includes AM/PM
            if (time.contains("AM", true) || time.contains("PM", true)) {
                return time.trim()
            }

            val inputFormats = listOf(
                SimpleDateFormat("HH:mm", Locale.getDefault()),
                SimpleDateFormat("H:mm", Locale.getDefault())
            )

            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            var date: Date? = null
            for (format in inputFormats) {
                try {
                    date = format.parse(time)
                    if (date != null) break
                } catch (_: Exception) {
                }
            }

            date?.let { outputFormat.format(it) } ?: time
        } catch (e: Exception) {
            time
        }
    }

    fun isCreateSlots() {
        val jsonArray = JsonArray()
        val meetingData = validateMeetingInputs()

        for (i in isSlotCreateValues.indices) {
            val date = isSlotCreateValues[i].first
            val slotsForDate = isSlotCreateValues[i].second

            val jsonObject = JsonObject().apply {
                addProperty("date", date)
                addProperty("event_name", meetingData!!.purpose)
                addProperty("from_time", formatTimeWithAMPM(meetingData.fromTime))
                addProperty("to_time", formatTimeWithAMPM(meetingData.toTime))
                addProperty("duration", meetingData.slotDuration.toInt())
                addProperty("event_link", meetingData.meetingLink)
                addProperty("break_time", isBreakDuration.toInt())
                addProperty("meeting_mode", meetingData.meetingMode)
            }

            val isStdSecJsonArray = JsonArray()
            for (section in meetingData!!.selectedSections) {
                val isStdSecJsonObject = JsonObject()
                isStdSecJsonObject.addProperty("section_id", section.section_id)
                isStdSecJsonObject.addProperty("class_id", section.class_id)
                isStdSecJsonArray.add(isStdSecJsonObject)
            }
            jsonObject.add("std_sec_details", isStdSecJsonArray)

            val isSlotsDateJsonArray = JsonArray()
            for (slot in slotsForDate) {
                val isSlotsDateJsonObject = JsonObject()
                isSlotsDateJsonObject.addProperty("from_time", formatTimeWithAMPM(slot.slot_from))
                isSlotsDateJsonObject.addProperty("to_time", formatTimeWithAMPM(slot.slot_to))
                isSlotsDateJsonArray.add(isSlotsDateJsonObject)
            }
            jsonObject.add("slots", isSlotsDateJsonArray)

            jsonArray.add(jsonObject)
        }

        Log.d("isCreateSlots", jsonArray.toString())
        appViewModel!!.isSlotCreating(isAccessToken!!, jsonArray)
    }


    private fun isShowAvailableSlot(data: List<ValidatedSlot>) {
        // 1. Keep all slots (Available + Not Available) for display
        val allSlotsList = data.filter { it.slots.isNotEmpty() }

        if (allSlotsList.isEmpty()) {
            Constant.showTopAlertPopup1("No slots found for selected date(s) and time", this, false)
            return
        }

        // ✅ Prevent reopening if already open
        if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing) {
            return  // Exit immediately if already showing
        }

        // 2. Setup bottom sheet
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.checkslot_create, null)
        bottomSheetDialog!!.setContentView(view)

        val rcySlotDate = view.findViewById<RecyclerView>(R.id.rcySlotDate)
        val lblCreateSlot = view.findViewById<TextView>(R.id.lblCreateSlot)
        val imgClose = view.findViewById<ImageView>(R.id.imgClose)

        // 3. Prepare grouped data (date → slots)
        val groupedData = allSlotsList.map { slot ->
            AvailableSlotGroup(slot.date, slot.slots.toMutableList())
        }

        // 4. Adapter handles selection; you’ll track only available slot selections
        val adapter = CheckAvailableSlotsDate(this, groupedData) { updatedList ->
            selectedSlots = updatedList
        }

        rcySlotDate.layoutManager = GridLayoutManager(this, 1)
        rcySlotDate.adapter = adapter

        // 5. Handle create slot click
        lblCreateSlot?.setOnClickListener {
            val availableSlots = selectedSlots.filter { (_, slot) ->
                slot.slot_availablity.equals("Available", true)
            }

            if (availableSlots.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please select at least one available slot",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Confirm Slot Creation")
            dialogBuilder.setMessage("Are you sure you want to create slots for the selected dates?")
            dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
                isSlotCreateValues = availableSlots
                    .groupBy { it.first } // date
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

        // 6. Show bottom sheet full height
        bottomSheetDialog!!.show()

        val bottomSheet =
            bottomSheetDialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
            val behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = true
            behavior.skipCollapsed = true
        }

        bottomSheetDialog!!.setOnDismissListener {
            bottomSheetDialog = null
        }

        imgClose.setOnClickListener { bottomSheetDialog!!.dismiss() }
    }

    fun dismissBottomSheet() {
        bottomSheetDialog?.dismiss()
    }


    private fun validateMeetingInputs(): MeetingCreationData? {
        if (binding.edtPurPose.text.toString().isEmpty()) {

            binding.edtPurPose.error = "Enter the Purpose of meeting"
            return null
        }

        if (isMeetingMode.isEmpty()) {
            Toast.makeText(this, "Select meeting mode", Toast.LENGTH_SHORT).show()
            return null
        }

        if (isOnlineMeeting && binding.edtMobileOrLink.text.toString().isEmpty()) {

            binding.edtMobileOrLink.error = "Paste the meeting link"
            return null
        }

        if (isSelectedList.isEmpty()) {
            Toast.makeText(this, "Select section and standard", Toast.LENGTH_SHORT).show()
            return null
        }

        if (selectedDates.isEmpty()) {
            Toast.makeText(this, "Kindly select the date", Toast.LENGTH_SHORT).show()
            return null
        }

        if (binding.lblFromTime.text.toString() == "Start with") {
            Toast.makeText(this, "Kindly select the start time", Toast.LENGTH_SHORT).show()
            return null
        }

        if (binding.lblToTime.text.toString() == "End with") {
            Toast.makeText(this, "Kindly select the end time", Toast.LENGTH_SHORT).show()
            return null
        }
        if (isSlotDuration == "Select Slot Duration") {
            Toast.makeText(this, "Kindly select the slot duration", Toast.LENGTH_SHORT).show()
            return null
        }

        if (isSlotDurationCustom && binding.edtSlotCustomDuration.text.toString().isEmpty()) {

            binding.edtSlotCustomDuration.error = "Enter the slot duration"
            return null
        }

        if (binding.edtSlotCustomDuration.text.toString() == "0") {
            Toast.makeText(this, "Minutes should greater then zero", Toast.LENGTH_SHORT).show()
            return null
        }

        if (binding.switchBreak.isChecked() && isBreakDuration.isEmpty()) {
            Toast.makeText(this, "Select the break duration", Toast.LENGTH_SHORT).show()
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
            break_time = isBreakDuration
        )
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
                    isSlotDuration=itemsCategory.get(position)

                    if (position != 0 && itemsCategory[position] != "Custom") {
                        val parts = itemsCategory[position].split(" ")
                        val number = parts[0]
                        isSlotDuration = number
                    }
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
            val selected = calendarView.getSelectedDates()
            val validDates = ArrayList<String>()

            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val todayStr = sdf.format(Calendar.getInstance().time)
            val now = Calendar.getInstance()

            for (dateStr in selected) {
                try {
                    if (dateStr == todayStr) {
                        // Check if From or To time already picked and in past
                        if (startCalendar != null && startCalendar!!.before(now)) {
                            Toast.makeText(
                                this,
                                "Cannot select today because From Time is already past",
                                Toast.LENGTH_SHORT
                            ).show()
                            continue
                        }
                        if (endCalendar != null && endCalendar!!.before(now)) {
                            Toast.makeText(
                                this,
                                "Cannot select today because To Time is already past",
                                Toast.LENGTH_SHORT
                            ).show()
                            continue
                        }
                    }
                    validDates.add(dateStr)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            selectedDates.clear()
            selectedDates.addAll(validDates)

            selectedDatesAdapter = SelectedDatesAdapter(selectedDates) { date ->
                selectedDates.remove(date)
                selectedDatesAdapter.notifyDataSetChanged()
                calendarView.setSelectedDates(selectedDates)
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
        val isBreak = isSelectedTextView.text.toString().split(" ")[0]
        println(isBreak)
        isBreakDuration = if (binding.switchBreak.isChecked()) {
            isBreak
        } else {
            "0"
        }
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
            R.id.lblOnline -> "Virtual"
            R.id.lblPerson -> "In Person"
            R.id.lblPhoneCall -> "Phone Call"
            else -> ""
        }
        if (isMeetingMode.equals("Virtual", ignoreCase = true)) {
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

