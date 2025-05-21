package com.vs.schoolmessenger.School.AbsenteesMarking

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
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
    View.OnClickListener, OnDateSelectedListener {

    lateinit var mAdapter: AttendanceStudentReportAdapter
    private lateinit var studentsList: List<StudentAttendanceReportData>
    private var appViewModel: App? = null
    var isSection: List<Section>? = null
    private var isAccessToken: String? = null
    private var isStandardName: String? = null
    private var isSectionName: String? = null

    var isValidAcademicYear = false
    var isAcademicYear: List<AcademicYear>? = null
    private var isStaffDetails: StaffDetails? = null
    var isAcademicYearId = -1
    var isSelectedDate: String? = null
    var isCurrentAcademicYear = true
    var isSectionId = -1
    var isGetStandard: List<Standard>? = null
    private var isClassID: Int? = null
    private var isSectionID: Int? = null


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
        binding.imgBack.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.rlaSection.setOnClickListener(this)
        binding.rlaAttendanceType.setOnClickListener(this)
        binding.btnAbsent.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
        binding.lblDate.setOnClickListener(this)
        binding.rlaStandardReport.setOnClickListener(this)
        binding.rlaSectionReport.setOnClickListener(this)
        binding.rlaDayDatePicker.setOnClickListener(this)
        binding.dropdownAcademicYear.setOnClickListener(this)
        binding.rlaFullDay.setOnClickListener(this)
        binding.rlaHalfDay.setOnClickListener(this)
        binding.rlaSecondHalf.setOnClickListener(this)
        binding.rlaFirstHalf.setOnClickListener(this)


        appViewModel!!.isSendAbsenteeSMS?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.hideLoading(this@AttendanceMark)
//                val dialogRootView = view as ViewGroup
//                showTopAlertPopup(response.message, dialogRootView, -1, response.status, "isUpdate")
            }
        }





        isStaffDetails = SharedPreference.getStaffDetails(this)
        //Setting the first "Get All Student" as Default text in the Sort Option
        isAccessToken = isStaffDetails!!.access_token
        Log.d("isAccessToken", isStaffDetails!!.access_token)
        isGetAcademicYear()

//Initiallt itself we are assigning the day,date,full date in textview
        val (dayOnly, dayOfWeek, fullDate) = getCurrentDateInfo()
        binding.lblDate1.text = dayOnly
        binding.lblDay.text = dayOfWeek
        binding.lblDatePick.text = fullDate
/////////////
        //start here!!!!!!!!!!!!
        /////////

        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.class_id, isClassID)
            addProperty(APIKeyNames.section_id, isSectionID)
            addProperty(APIKeyNames.all_present, true)
            addProperty(APIKeyNames.session_type, true)
            addProperty(APIKeyNames.attendance_date, true)
            addProperty(APIKeyNames.student_id, true)
        }
        appViewModel?.isUpdateSendAbsenteeSMS(isAccessToken!!, jsonObject, this)






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
                }
                else {
                    Constant.showDataValidation("Error",response.message, this)
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
                            isClassID = isGetStandard!!.get(0).id
                            Log.d("isClassID", isClassID.toString())
//                        isSectionId = isGetStandard!!.get(0).sections.get(0).id
                            isSectionID = isGetStandard!!.get(0).sections.get(0).id

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
                }
                else {
                    binding.selectClassSection.visibility=View.GONE
                    binding.lnrClasses.visibility = View.GONE
                    Constant.showDataValidation("Error",response.message, this)
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
            isClassID = null
            isSectionID = null
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
        isClassID = standard.id
        isSection = standard.sections

        binding.lblStandard.text = standard.name
        isStandardName=standard.name



        val sections = standard.sections
        if (!sections.isNullOrEmpty()) {
            val defaultSection = sections[0]
            isSectionID = defaultSection.id
            binding.lblSection.text = defaultSection.name
            isSectionName= defaultSection.name
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
            isSectionID = null
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

            R.id.rlaDayDatePicker -> {
                showDayDatePickerDialog(this) { dayOnly, dayOfWeek, fullDate, slashDate ->
                    binding.lblDate1.text = dayOnly         // we get Date
                    binding.lblDay.text = dayOfWeek     // we get Day
                    binding.lblDatePick.text = fullDate       //we get Day Date Month Year
                    isSelectedDate = slashDate //Day/Month/Year
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

            R.id.rlaFullDay->{
                binding.radioButtonHalfDay.isEnabled=false
                binding.radioButtonFullDay.isEnabled=true
                binding.radioButtonHalfDay.isChecked=false
                binding.radioButtonFullDay.isChecked=true
                binding.lnrClasses2.visibility=View.GONE
                binding.sessionHeader.visibility=View.GONE
            }
            R.id.rlaHalfDay->{
                binding.radioButtonFullDay.isEnabled=false
                binding.radioButtonHalfDay.isEnabled=true
                binding.radioButtonHalfDay.isChecked=true
                binding.radioButtonFullDay.isChecked=false
                binding.lnrClasses2.visibility=View.VISIBLE
                binding.sessionHeader.visibility=View.VISIBLE
            }
            R.id.rlaFirstHalf->{
                binding.radioButtonSecondHalf.isEnabled=false
                binding.radioButtonFirstHalf.isEnabled=true
                binding.radioButtonFirstHalf.isChecked=true
                binding.radioButtonSecondHalf.isChecked=false

            }
            R.id.rlaSecondHalf->{
                binding.radioButtonFirstHalf.isEnabled=false
                binding.radioButtonSecondHalf.isEnabled=true
                binding.radioButtonSecondHalf.isChecked=true
                binding.radioButtonFirstHalf.isChecked=false
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
                loadData()
            }

            R.id.btnAbsent -> {

                val intent = Intent(this, AbsenteesStudentMark::class.java)
                Log.d("ComingStandardName",isStandardName.toString())
                Log.d("ComingSectionName",isSectionName.toString())

                intent.putExtra(Constant.isStandardName,isStandardName)
                intent.putExtra(Constant.isSectionName,isSectionName)
                intent.putExtra(Constant.isAccessToken,isAccessToken!!.toString() )
                intent.putExtra(Constant.isAcademicYearId, isAcademicYearId)
                intent.putExtra(Constant.isSectionId,isSectionID)


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
                        isSectionID = selectedOption.second
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
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedCal.time)

                onDateSelected(dayOnly, dayOfWeek, fullDate, slashDate)
            },
            year, month, day
        )

        //  Disallow future dates — only today and past
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    fun getCurrentDateInfo(): Triple<String, String, String> {
        val calendar = Calendar.getInstance()

        val dayOnly = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
        val fullDate =
            SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault()).format(calendar.time)

        return Triple(dayOnly, dayOfWeek, fullDate)
    }


    override fun onDateSelected(date: String) {
    }

    fun loadData() {
        studentsList = listOf(

            StudentAttendanceReportData(
                "Murugan", "76979871",
                "Present"
            ),

            StudentAttendanceReportData(
                "Sathish", "22439234",
                "Absent"
            ),
            StudentAttendanceReportData(
                "Saran Raj", "259411563",
                "Present"
            ),
            StudentAttendanceReportData(
                "Chanthru", "216098214",
                "Absent"
            ),
            StudentAttendanceReportData(
                "Ramesh", "90509568",
                "Present"
            ),
            StudentAttendanceReportData(
                "Lakshmanan Narayanan", "90509568",
                "Absent"
            ),
            StudentAttendanceReportData(
                "Gunal", "90509568",
                "Present"
            ),
            StudentAttendanceReportData(
                "Lakshmanan", "90509568",
                "Absent"
            ),
            StudentAttendanceReportData(
                "Narayanan", "90509568",
                "Present"
            ), StudentAttendanceReportData(
                "Gunal", "90509568",
                "Present"
            ),
            StudentAttendanceReportData(
                "Lakshmanan", "90509568",
                "Absent"
            ), StudentAttendanceReportData(
                "Gunal", "90509568",
                "Present"
            ),
            StudentAttendanceReportData(
                "Lakshmanan", "90509568",
                "Absent"
            )
        )


        mAdapter = AttendanceStudentReportAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
        binding.rcyAttendanceReport.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter =
                AttendanceStudentReportAdapter(studentsList, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyAttendanceReport.adapter = mAdapter
        }
    }
}


//Existing Code
//package com.vs.schoolmessenger.School.AbsenteesMarking
//
//import android.content.Intent
//import android.icu.util.Calendar
//import android.util.Log
//import android.view.View
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.OnDateSelectedListener
//import com.vs.schoolmessenger.databinding.AttendanceMarkBinding
//
//class AttendanceMark : BaseActivity<AttendanceMarkBinding>(),
//    View.OnClickListener, OnDateSelectedListener {
//
//    lateinit var mAdapter: AttendanceStudentReportAdapter
//    private lateinit var studentsList: List<StudentAttendanceReportData>
//
//    override fun getViewBinding(): AttendanceMarkBinding {
//        return AttendanceMarkBinding.inflate(layoutInflater)
//    }
//
//    private val itemsSection = listOf(
//        "A",
//        "B",
//        "C",
//        "D",
//        "E",
//        "F",
//        "G",
//        "H",
//    )
//    private val itemsStandard = listOf(
//        "V",
//        "VI",
//        "VII",
//        "VIII",
//        "IX",
//        "X",
//        "XI",
//        "XII"
//    )
//    private val itemsAttendanceType = listOf(
//        "Full day",
//        "Half day"
//    )
//
//
//    override fun setupViews() {
//        super.setupViews()
//        setupToolbar()
//        binding.imgBack.setOnClickListener(this)
//        binding.rlaStandard.setOnClickListener(this)
//        binding.rlaSection.setOnClickListener(this)
//        binding.rlaAttendanceType.setOnClickListener(this)
//        binding.btnAbsent.setOnClickListener(this)
//        binding.btnCreate.setOnClickListener(this)
//        binding.btnHistory.setOnClickListener(this)
//        binding.lblDate.setOnClickListener(this)
//        binding.rlaStandardReport.setOnClickListener(this)
//        binding.rlaSectionReport.setOnClickListener(this)
//
//        val currentDate = Calendar.getInstance()
//
//        // Set the maximum date to today
//        binding.calendarView.maxDate = currentDate.timeInMillis
//
//        // Set the minimum date to 30 days before today
//        currentDate.add(Calendar.DAY_OF_MONTH, -30) // Subtract 30 days
//        binding.calendarView.minDate = currentDate.timeInMillis
//
//        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
//            val date = "$dayOfMonth-${month + 1}-$year"
//            Log.d("SelectedDate", date)
//        }
//    }
//
//
//    override fun onClick(p0: View?) {
//        when (p0?.id) {
//            R.id.imgBack -> {
//                onBackPressed()
//            }
//
//            R.id.lblDate -> {
//                showDatePickerDialog(this, this)
//            }
//
//            R.id.rlaStandardReport -> {
//                showDropdownMenuSort(
//                    binding.lblStandardReport,
//                    this,
//                    itemsStandard
//                ) { selectedOption ->
//                    binding.lblStandardReport.text = selectedOption
//                }
//            }
//
//            R.id.rlaSectionReport -> {
//                showDropdownMenuSort(
//                    binding.lblSectionReport,
//                    this,
//                    itemsSection
//                ) { selectedOption ->
//                    binding.lblSectionReport.text = selectedOption
//                }
//            }
//
//
//            R.id.btnCreate -> {
//                isBackRoundChange(binding.btnCreate)
//                binding.rlaAttendanceReport.visibility = View.GONE
//                binding.rlaAttendanceMark.visibility = View.VISIBLE
//            }
//
//            R.id.btnHistory -> {
//                isBackRoundChange(binding.btnHistory)
//                binding.rlaAttendanceReport.visibility = View.VISIBLE
//                binding.rlaAttendanceMark.visibility = View.GONE
//                loadData()
//            }
//
//            R.id.btnAbsent -> {
//                startActivity(Intent(this, AbsenteesStudentMark::class.java))
//            }
//
//            R.id.rlaStandard -> {
//                showDropdownMenuSort(
//                    binding.lblStandard,
//                    this,
//                    itemsStandard
//                ) { selectedOption ->
//                    binding.lblStandard.text = selectedOption
//                }
//            }
//
//            R.id.rlaSection -> {
//                showDropdownMenuSort(
//                    binding.lblSection,
//                    this,
//                    itemsSection
//                ) { selectedOption ->
//                    binding.lblSection.text = selectedOption
//                }
//            }
//
//            R.id.rlaAttendanceType -> {
//                showDropdownMenuSort(
//                    binding.lblAttendanceType,
//                    this,
//                    itemsAttendanceType
//                ) { selectedOption ->
//                    binding.lblAttendanceType.text = selectedOption
//                }
//            }
//        }
//    }
//
//    private fun isBackRoundChange(isClickingId: TextView) {
//
//        if (isClickingId == binding.btnCreate) {
//            binding.btnHistory.background = null
//            binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
//        }
//
//        if (isClickingId == binding.btnHistory) {
//            binding.btnCreate.background = null
//            binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
//
//        }
//
//        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.bg_blue)
//        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))
//
//    }
//
//    override fun onDateSelected(date: String) {
//        binding.lblDate.text = date
//    }
//
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
//            // Set GridLayoutManager (2 columns in this case)
//            binding.rcyAttendanceReport.adapter = mAdapter
//        }
//    }
//}
