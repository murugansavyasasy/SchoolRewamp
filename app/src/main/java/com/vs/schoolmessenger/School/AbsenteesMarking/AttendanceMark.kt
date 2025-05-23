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
    private  var isSelectedIds: List<String>?=null
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
                Constant.showDataValidation("Success", response.message, this)

            }
        }
        isGetAcademicYear()
        val (dayOnly, dayOfWeek, fullDate, slashDate) = getCurrentDateInfo()
        binding.lblDate1.text = dayOnly
        binding.lblDay.text = dayOfWeek
        binding.lblDatePick.text = fullDate
        SelectedDate = slashDate

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            Constant.hideLoading(this@AttendanceMark)
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
                }
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            Constant.hideLoading(this@AttendanceMark)
            if (response != null) {
                if (response.status) {
                    isGetStandard = response.data
                    isGetStandard?.size?.let {
                        if (it > 0) {
                            binding.lnrClasses.visibility = View.VISIBLE
                            ClassID = isGetStandard!!.get(0).id
                            Log.d("isClassID", ClassID.toString())
//                        isSectionId = isGetStandard!!.get(0).sections.get(0).id
                            SectionID = isGetStandard!!.get(0).sections.get(0).id


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
        Constant.showLoading(this@AttendanceMark)
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    private fun isGetAcademicYear() {
        Log.d("isGetAcademicYear", "Getting")
        Constant.showLoading(this@AttendanceMark)
        appViewModel!!.isGetAcademicYear(
            isAccessToken!!, this
        )
    }

    private fun updateStandardAndSection(standard: Standard?) {
        if (standard == null) {
            // No Standard Found
            ClassID = null
            SectionID = null
            isStandardName = null
            isSectionName = null

            binding.lblStandard.text = "-"
            binding.lblSection.text = "-"
            binding.rlaSection.isEnabled = false
            binding.rlaSection.isClickable = false
            //Checking Whether to enable the Select All as present and Mark Absentees button
            updateActionButtonsState()
            return
        }
        // Set selected Standard
        ClassID = standard.id
        isSection = standard.sections

        binding.lblStandard.text = standard.name
        isStandardName = standard.name
        val sections = standard.sections
        if (!sections.isNullOrEmpty()) {
            val defaultSection = sections[0]
            SectionID = defaultSection.id
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

        }
        else
        {
            // No sections -> reset and disable section dropdown
            SectionID = null
            isSection = null
            isSectionName = null
            binding.lblSection.text = "-"
            binding.rlaSection.isEnabled = false
            binding.rlaSection.isClickable = false
            //Checking Whether to enable the Select All as present and Mark Absentees button
            updateActionButtonsState()
            return
        }
        //Checking Whether to enable the Select All as present and Mark Absentees button
        updateActionButtonsState()
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnSelectPresent -> {
                Constant.showLoading(this@AttendanceMark)
                isMarkAttendance()
            }

            R.id.rlaDayDatePicker -> {
                Constant.showDatePicker(this) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.lblDatePick.text = Constant.covertDateFormate(selectedDate)
                    SelectedDate = selectedDate.toString()
                    val parts = binding.lblDatePick.text.split(" ")
                    val day = parts[0]
                    val Date = parts[1]
                    binding.lblDay.text=day
                    binding.lblDate1.text=Date
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
                AttendanceType =Constant.fullDay
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
                AttendanceType = Constant.halfDay
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
                SessionType =Constant.firstHalf
                if (binding.radioButtonHalfDay.isChecked) {
                    binding.radioButtonFirstHalf.isChecked = true
                    binding.radioButtonSecondHalf.isChecked = false
                }
            }

            R.id.radioButtonSecondHalf, R.id.rlaSecondHalf -> {
                SessionType = Constant.secondHalf
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
            academic_year_id=isAcademicYearId,
            class_name=isStandardName!!,
            section_name=isSectionName!!,
            class_id = ClassID.toString(),
            section_id = SectionID.toString(),
            all_present = AllPresent,
            attendance_type = AttendanceType,
            session_type = SessionType,
            attendance_date = SelectedDate!!
        )
        //We are Saving all the data in Constant as List Here
        Constant.isMarkAttendanceDataSending = saveAttendanceData
    }

    private fun updateActionButtonsState() {
        if ((ClassID != null && SectionID != null)&&(binding.radioButtonFullDay.isChecked ||
            (binding.radioButtonHalfDay.isChecked &&
                    (binding.radioButtonFirstHalf.isChecked || binding.radioButtonSecondHalf.isChecked))
        )) {

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
            && AttendanceType == Constant.fullDay && SelectedDate != null && isSelectedIds?.size == null
        ) {
            AllPresent = Constant.allPresent
            Log.d(
                "Parameter_for_SendAbsentessSMS",
                ClassID.toString() + "," +
                        SectionID.toString() + "," +
                        AllPresent + "," +
                        AttendanceType + "," +
                        SessionType + "," +
                        SelectedDate.toString()
            )

            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.class_id, ClassID.toString())
                addProperty(APIKeyNames.section_id, SectionID.toString())
                addProperty(APIKeyNames.all_present, AllPresent)
                addProperty(APIKeyNames.attendance_type, AttendanceType)
                addProperty(APIKeyNames.session_type, SessionType)
                addProperty(APIKeyNames.attendance_date, SelectedDate)
                val studentArray = JsonArray().apply {
                    isSelectedIds?.forEach { id ->
                        add(JsonObject().apply {
                            addProperty("ID", id)
                        })
                    }
                }
                add(APIKeyNames.student_id, studentArray)
                Log.d("AbsenteesList",studentArray.toString())

            }
            appViewModel?.isUpdateSendAbsenteeSMS(isAccessToken!!, jsonObject, this)
        }

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
        isSelectedIds = selectedIds

    }


    override fun onDateSelected(date: String) {
    }

}