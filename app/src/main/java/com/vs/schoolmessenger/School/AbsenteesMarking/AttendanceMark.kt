package com.vs.schoolmessenger.School.AbsenteesMarking

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AttendanceMarkBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceMark : BaseActivity<AttendanceMarkBinding>(),
    View.OnClickListener, OnDateSelectedListener, AbsenteesSelectionListener {


    lateinit var mAdapter: AttendanceStudentReportAdapter
    private val selectedIds = mutableListOf<String>()
    private lateinit var studentsList: List<StudentAttendanceReportData>
    private var appViewModel: App? = null
    var isSection: List<Section>? = null
    private var isAccessToken: String? = null
    private var isStandardName: String? = null
    private var isSectionName: String? = null

    var isValidAcademicYear = false
    var isAcademicYear: List<AcademicYear>? = null
    var AllPresent = ""
    var SessionType = ""
    var AttendanceType = ""
    private var isStaffDetails: StaffDetails? = null
    var isAcademicYearId = -1

    var SelectedDate: String? = null
    var isCurrentAcademicYear = true
    var isSectionId = -1
    var isGetStandard: List<Standard>? = null
    private var ClassID: Int? = null
    private var SectionID: Int? = null


    override fun getViewBinding(): AttendanceMarkBinding {
        return AttendanceMarkBinding.inflate(layoutInflater)
    }

    private val itemsSection = listOf(
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
    )
    private val itemsStandard = listOf(
        "V",
        "VI",
        "VII",
        "VIII",
        "IX",
        "X",
        "XI",
        "XII"
    )
    private val itemsAttendanceType = listOf(
        "Full day",
        "Half day"
    )


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.rlaSection.setOnClickListener(this)
        binding.rlaAttendanceType.setOnClickListener(this)
        binding.btnAbsent.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
        binding.lblDate.setOnClickListener(this)
        binding.rlaStandardReport.setOnClickListener(this)
        binding.btnSelectPresent.setOnClickListener(this)
        binding.rlaSectionReport.setOnClickListener(this)
        binding.rlaDayDatePicker.setOnClickListener(this)
        binding.dropdownAcademicYear.setOnClickListener(this)
        binding.rlaFullDay.setOnClickListener(this)
        binding.rlaHalfDay.setOnClickListener(this)
        binding.rlaSecondHalf.setOnClickListener(this)
        binding.rlaFirstHalf.setOnClickListener(this)

        binding.radioButtonFullDay.setOnClickListener(this)
        binding.radioButtonHalfDay.setOnClickListener(this)
        binding.radioButtonFirstHalf.setOnClickListener(this)
        binding.radioButtonSecondHalf.setOnClickListener(this)
        updateActionButtonsState()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        Log.d("isAccessToken", isStaffDetails!!.access_token)
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.MarkAttendance)
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name


        appViewModel!!.isSendAbsenteeSMS?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.hideLoading(this@AttendanceMark)
                Log.d("isSendAbsenteeSMS", response.message)
//                val dialogRootView = view as ViewGroup
//                showTopAlertPopup(response.message, dialogRootView, -1, response.status, "isUpdate")
            }
        }
        isGetAcademicYear()

        val (dayOnly, dayOfWeek, fullDate, slashDate) = getCurrentDateInfo()
        binding.lblDate1.text = dayOnly
        binding.lblDay.text = dayOfWeek
        binding.lblDatePick.text = fullDate
        SelectedDate = slashDate
        Constant.isSelectedDate = SelectedDate.toString()

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
//            Constant.hideLoading(this@StudentReport)
            if (response != null) {
                if (response.status) {
                    response.data.let { academicList ->
                        Log.d("AcademicYearResponse", response.data.toString())
                        val reorderedList =
                            academicList.sortedByDescending { it.current_academic_year }
                        if (isAcademicYear == reorderedList) return@observe
                        isAcademicYear = reorderedList
                        isValidAcademicYear =
                            isAcademicYear?.any { it.current_academic_year == true } == true
                        binding.dropdownAcademicYear.text = isAcademicYear!![0].year
                        isAcademicYearId = isAcademicYear!![0].id
                        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
                        Log.d("isAcademicYearId", isAcademicYearId.toString())
                        isGetStandardSection()
                    }
                } else {
                    Constant.showDataValidation("Error", response.message, this)
//                    ErrorMessage(response.message)
                }
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
//            Constant.hideLoading(this@StudentReport)
            if (response != null) {
                if (response.status) {
                    isGetStandard = response.data
                    isGetStandard?.size?.let {
                        if (it > 0) {
                            binding.lnrClasses.visibility = View.VISIBLE
                            ClassID = isGetStandard!!.get(0).id
                            Constant.isClassID = ClassID.toString()
                            Log.d("isClassID", ClassID.toString())
//                        isSectionId = isGetStandard!!.get(0).sections.get(0).id
                            SectionID = isGetStandard!!.get(0).sections.get(0).id
                            Constant.isSectionID = SectionID.toString()


                            binding.lblStandard.text = isGetStandard!!.get(0).name
                            if (isGetStandard!!.get(0).sections.size > 0) {
                                binding.lblSection.text =
                                    isGetStandard!!.get(0).sections.get(0).name
                                isSection = isGetStandard!!.get(0).sections
                                Log.d("isSectionID", isSection.toString())
                            }
                            val firstStandard = isGetStandard!![0]
                            //To Assign Standard and Section in early to use in AbsenteesStudentMark.kt
                            updateStandardAndSection(firstStandard)
//                        isGetStudentReport()
                        }
                    }
                } else {
                    binding.selectClassSection.visibility = View.GONE
                    binding.lnrClasses.visibility = View.GONE
                    Constant.showDataValidation("Error", response.message, this)
                }
            }
        }
    }

    private fun isGetStandardSection() {
        Log.d("isAcademicYearId", isAcademicYearId.toString())
//        Constant.showLoading(this@StudentReport)
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    private fun isGetAcademicYear() {
        Log.d("isGetAcademicYear", "Getting")
//        Constant.showLoading(this@StudentReport)

        appViewModel!!.isGetAcademicYear(
            isAccessToken!!, this
        )
    }

    private fun updateStandardAndSection(standard: Standard?) {
        if (standard == null) {
            // No Standard Found
            ClassID = null
            Constant.isClassID = ClassID.toString()
            SectionID = null
            Constant.isSectionID = SectionID.toString()
            isStandardName = null
            isSectionName = null

            binding.lblStandard.text = "-"
            binding.lblSection.text = "-"
            binding.rlaSection.isEnabled = false
            binding.rlaSection.isClickable = false
//            ErrorMessage(Constant.No_STANDARD_FOUND)
            return
        }
        // Set selected Standard
        ClassID = standard.id
        Constant.isClassID = ClassID.toString()
        isSection = standard.sections

        binding.lblStandard.text = standard.name
        isStandardName = standard.name


        val sections = standard.sections
        if (!sections.isNullOrEmpty()) {
            val defaultSection = sections[0]
            SectionID = defaultSection.id
            Constant.isSectionID = SectionID.toString()
            binding.lblSection.text = defaultSection.name
            isSectionName = defaultSection.name
            if (sections.size == 1) {
                // Only one section -> disable dropdown
                binding.rlaSection.isEnabled = false
                binding.rlaSection.isClickable = false
            } else {
                // Multiple sections -> enable dropdown
                binding.rlaSection.isEnabled = true
                binding.rlaSection.isClickable = true
            }

        } else {
            // No sections -> reset and disable section dropdown
            SectionID = null
            Constant.isSectionID = SectionID.toString()
            isSection = null
            isSectionName = null
            binding.lblSection.text = "-"
            binding.rlaSection.isEnabled = false
            binding.rlaSection.isClickable = false
            return
        }


        // Safe to call API now
//        isGetStudentReport()
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }


            R.id.btnSelectPresent -> {
                Constant.showLoading(this@AttendanceMark)
                //saving the data in Data Class if we are using in next screen
//                isSaveMarkAttendanceDetails()
                isMarkAttendance()
            }


            R.id.rlaDayDatePicker -> {
                showDayDatePickerDialog(this) { dayOnly, dayOfWeek, fullDate, slashDate ->
                    binding.lblDate1.text = dayOnly         // we get Date
                    binding.lblDay.text = dayOfWeek     // we get Day
                    binding.lblDatePick.text = fullDate       //we get Day Date Month Year
                    SelectedDate = slashDate //Day/Month/Year
                    Constant.isSelectedDate = SelectedDate.toString()

                }


            }

            R.id.rlaStandardReport -> {
                showDropdownMenuSort(
                    binding.lblStandardReport,
                    this,
                    itemsStandard
                ) { selectedOption ->
                    binding.lblStandardReport.text = selectedOption
                }
            }

            R.id.radioButtonFullDay, R.id.rlaFullDay -> {
                SessionType = ""
                AttendanceType = "F"
                Constant.isSessionType = SessionType
                Constant.isAttendanceType = AttendanceType
                binding.radioButtonFullDay.isChecked = true
                binding.radioButtonHalfDay.isChecked = false
                binding.radioButtonFirstHalf.isChecked = false
                binding.radioButtonSecondHalf.isChecked = false

                binding.radioButtonFirstHalf.isEnabled = false
                binding.radioButtonSecondHalf.isEnabled = false

                binding.lnrClasses2.visibility = View.GONE
                binding.sessionHeader.visibility = View.GONE
            }

            R.id.radioButtonHalfDay, R.id.rlaHalfDay -> {
                AttendanceType = "H"
                Constant.isAttendanceType = AttendanceType
                binding.radioButtonFullDay.isChecked = false
                binding.radioButtonHalfDay.isChecked = true

                binding.radioButtonFirstHalf.isEnabled = true
                binding.radioButtonSecondHalf.isEnabled = true

                binding.radioButtonFirstHalf.isChecked = false
                binding.radioButtonSecondHalf.isChecked = false

                binding.lnrClasses2.visibility = View.VISIBLE
                binding.sessionHeader.visibility = View.VISIBLE
            }

            R.id.radioButtonFirstHalf, R.id.rlaFirstHalf -> {
                SessionType = "FH"
                Constant.isSessionType = SessionType
                if (binding.radioButtonHalfDay.isChecked) {
                    binding.radioButtonFirstHalf.isChecked = true
                    binding.radioButtonSecondHalf.isChecked = false
                }
            }

            R.id.radioButtonSecondHalf, R.id.rlaSecondHalf -> {
                SessionType = "SH"
                Constant.isSessionType = SessionType
                if (binding.radioButtonHalfDay.isChecked) {
                    binding.radioButtonFirstHalf.isChecked = false
                    binding.radioButtonSecondHalf.isChecked = true
                }
            }

            R.id.rlaSectionReport -> {
                showDropdownMenuSort(
                    binding.lblSectionReport,
                    this,
                    itemsSection
                ) { selectedOption ->
                    binding.lblSectionReport.text = selectedOption
                }
            }


            R.id.btnCreate -> {
                isBackRoundChange(binding.btnCreate)
                binding.rlaAttendanceReport.visibility = View.GONE
                binding.rlaAttendanceMark.visibility = View.VISIBLE
            }

            R.id.btnHistory -> {
                isBackRoundChange(binding.btnHistory)
                binding.rlaAttendanceReport.visibility = View.VISIBLE
                binding.rlaAttendanceMark.visibility = View.GONE
             //   loadData()
            }

            R.id.btnAbsent -> {

                val intent = Intent(this, AbsenteesStudentMark::class.java)
                Log.d("ComingStandardName", isStandardName.toString())
                Log.d("ComingSectionName", isSectionName.toString())
                intent.putExtra(Constant.isStandardName, isStandardName)
                intent.putExtra(Constant.isSectionName, isSectionName)
                intent.putExtra(Constant.isAccessToken, isAccessToken!!.toString())
                intent.putExtra(Constant.isAcademicYearId, isAcademicYearId)
                intent.putExtra(Constant.isSectionId, SectionID)

                //We are saving the details in Data class to use in the next screen
                isSaveMarkAttendanceDetails()
                startActivity(intent)

            }

            R.id.rlaStandard -> {
                showStandardDropdown(
                    binding.lblStandard,
                    this,
                    isGetStandard
                ) { selectStandard, _ ->
                    updateStandardAndSection(selectStandard)
                }
            }

            R.id.rlaSection -> {
                if (!isSection.isNullOrEmpty() && binding.rlaSection.isEnabled) {
                    isDropDownLoadDataSection(
                        binding.lblSection,
                        this,
                        isSection
                    ) { selectedOption ->
                        binding.lblSection.text = selectedOption.first
                        SectionID = selectedOption.second
                        Constant.isSectionID = SectionID.toString()
                        isSectionName = selectedOption.first
//                        isGetStudentReport()
                    }
                }

            }


            R.id.dropdownAcademicYear -> {
                showAcademicDropdown(
                    binding.dropdownAcademicYear, this, isAcademicYear
                ) { selectedYear ->
                    binding.dropdownAcademicYear.text = selectedYear.year
                    isGetStandardSection()
                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
                    )
//                    isGetStudentReport()
                }
            }


            R.id.rlaAttendanceType -> {
                showDropdownMenuSort(
                    binding.lblAttendanceType,
                    this,
                    itemsAttendanceType
                ) { selectedOption ->
                    binding.lblAttendanceType.text = selectedOption
                }
            }
        }
        // Call this after any change
        updateActionButtonsState()
    }

    private fun isSaveMarkAttendanceDetails() {
        val saveAttendanceData = MarkAttendanceDataSending(
            class_id = Constant.isClassID,
            section_id = Constant.isSectionID,
            all_present = Constant.isAllPresent,
            attendance_type = Constant.isAttendanceType,
            session_type = Constant.isSessionType,
            attendance_date = Constant.isSelectedDate
        )
        //We are Saving all the data in Constant as List Here
        Constant.isMarkAttendanceDataSending = saveAttendanceData
    }

    private fun updateActionButtonsState() {
        if (binding.radioButtonFullDay.isChecked ||
            (binding.radioButtonHalfDay.isChecked &&
                    (binding.radioButtonFirstHalf.isChecked || binding.radioButtonSecondHalf.isChecked))
        ) {

            binding.btnSelectPresent.setBackgroundResource(R.drawable.rect_shadow_green)
            binding.btnAbsent.setBackgroundResource(R.drawable.rect_shadow_red)
            binding.btnSelectPresent.isEnabled = true
            binding.btnAbsent.isEnabled = true
        } else {
            binding.btnSelectPresent.isEnabled = false
            binding.btnAbsent.isEnabled = false
            binding.btnAbsent.setBackgroundResource(R.drawable.rect_shadow_gray)
            binding.btnSelectPresent.setBackgroundResource(R.drawable.rect_shadow_gray)

        }
    }


    private fun isMarkAttendance() {

        if (ClassID != null && SectionID != null && SessionType == ""
            && AttendanceType == "F" && SelectedDate != null && selectedIds.size == 0
        ) {
            AllPresent = "T"
            Constant.isAllPresent = AllPresent
            isUpdateMarkAtttendance()
        }
        if (ClassID != 0 && SectionID != 0 && SessionType != ""
            && AttendanceType == "H" && SelectedDate != null && selectedIds.size > 0
        ) {
            isUpdateMarkAtttendance()
        }
    }

    private fun isUpdateMarkAtttendance() {
        val AbsentStudentIDList = JsonArray()
        for (id in selectedIds) {
            AbsentStudentIDList.add(JsonPrimitive(id))
        }

        Log.d(
            "Parameter_for_SendAbsentessSMS",
            ClassID.toString() + "," +
                    SectionID.toString() + "," +
                    AllPresent + "," +
                    AttendanceType + "," +
                    SessionType + "," +
                    SelectedDate.toString() + "," +
                    AbsentStudentIDList.toString()
        )

        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.class_id, ClassID.toString())
            addProperty(APIKeyNames.section_id, SectionID.toString())
            addProperty(APIKeyNames.all_present, AllPresent)
            addProperty(APIKeyNames.attendance_type, AttendanceType)
            addProperty(APIKeyNames.session_type, SessionType)
            addProperty(APIKeyNames.attendance_date, SelectedDate)
            add(APIKeyNames.student_id, AbsentStudentIDList)
        }
        appViewModel?.isUpdateSendAbsenteeSMS(isAccessToken!!, jsonObject, this)

    }

    private fun isBackRoundChange(isClickingId: TextView) {

        if (isClickingId == binding.btnCreate) {
            binding.btnHistory.background = null
            binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
        }

        if (isClickingId == binding.btnHistory) {
            binding.btnCreate.background = null
            binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))

        }

        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.white_bg_radius)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.black))

    }

    fun showDayDatePickerDialog(
        context: Context,
        onDateSelected: (
            dayOnly: String,
            dayOfWeek: String,
            fullDate: String,
            slashDate: String
        ) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(selectedYear, selectedMonth, selectedDay)

                val dayOnly = String.format("%02d", selectedDay)
                val dayOfWeek =
                    SimpleDateFormat("EEE", Locale.getDefault()).format(selectedCal.time)
                val fullDate = SimpleDateFormat(
                    "EEE dd MMM yyyy",
                    Locale.getDefault()
                ).format(selectedCal.time)
                val slashDate =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedCal.time)

                onDateSelected(dayOnly, dayOfWeek, fullDate, slashDate)
            },
            year, month, day
        )

        //  Disallow future dates — only today and past
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    fun getCurrentDateInfo(): List<String> {
        val calendar = Calendar.getInstance()

        val dayOnly = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
        val fullDate =
            SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        val slashDate =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)

        return listOf(dayOnly, dayOfWeek, fullDate, slashDate)
    }

    override fun onSelectionChanged(selectedIds: List<String>) {
        Log.d("ActivitySelectedIDs", selectedIds.toString())
    }


    override fun onDateSelected(date: String) {
    }

//    fun loadData() {
//        studentsList = listOf(
//
//            StudentAttendanceReportData(
//                "Murugan", "76979871",
//                "Present"
//            ),
//
//            StudentAttendanceReportData(
//                "Sathish", "22439234",
//                "Absent"
//            ),
//            StudentAttendanceReportData(
//                "Saran Raj", "259411563",
//                "Present"
//            ),
//            StudentAttendanceReportData(
//                "Chanthru", "216098214",
//                "Absent"
//            ),
//            StudentAttendanceReportData(
//                "Ramesh", "90509568",
//                "Present"
//            ),
//            StudentAttendanceReportData(
//                "Lakshmanan Narayanan", "90509568",
//                "Absent"
//            ),
//            StudentAttendanceReportData(
//                "Gunal", "90509568",
//                "Present"
//            ),
//            StudentAttendanceReportData(
//                "Lakshmanan", "90509568",
//                "Absent"
//            ),
//            StudentAttendanceReportData(
//                "Narayanan", "90509568",
//                "Present"
//            ), StudentAttendanceReportData(
//                "Gunal", "90509568",
//                "Present"
//            ),
//            StudentAttendanceReportData(
//                "Lakshmanan", "90509568",
//                "Absent"
//            ), StudentAttendanceReportData(
//                "Gunal", "90509568",
//                "Present"
//            ),
//            StudentAttendanceReportData(
//                "Lakshmanan", "90509568",
//                "Absent"
//            )
//        )
//
//
//        mAdapter = AttendanceStudentReportAdapter(null, this, Constant.isShimmerViewShow)
//        binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
//        binding.rcyAttendanceReport.adapter = mAdapter
//        Constant.executeAfterDelay {
//            mAdapter =
//                AttendanceStudentReportAdapter(studentsList, this, Constant.isShimmerViewDisable)
//            binding.rcyAttendanceReport.adapter = mAdapter
//        }
//    }
}