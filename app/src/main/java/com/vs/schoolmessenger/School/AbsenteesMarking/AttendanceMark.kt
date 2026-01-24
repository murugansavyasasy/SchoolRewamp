package com.vs.schoolmessenger.School.AbsenteesMarking

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.support.annotation.DrawableRes
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardDropDownListAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingAdapter.AttendanceStudentReportAdapter
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.StudentAttendanceReportData
import com.vs.schoolmessenger.School.AbsenteesMarking.CustomCalendarFragement.CustomCalendarFragment
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.SectionDropDownListAdapter
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AttendanceMarkBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AttendanceMark : BaseActivity<AttendanceMarkBinding>(),
    CustomCalendarFragment.CalendarDateListener, View.OnClickListener {


    lateinit var mAdapter: AttendanceStudentReportAdapter
    private lateinit var studentsList: List<StudentAttendanceReportData>
    private var appViewModel: App? = null
    var isSection: List<Section>? = null
    private var isAccessToken: String? = null
    private var isStandardName: String? = null
    private var isSectionName: String? = null
    private var isSelectedIds: List<String>? = null
    var isValidAcademicYear = false
    var isAcademicYear: List<AcademicYear>? = null
    var AllPresent = ""
    var SessionType = ""
    var AttendanceType = ""
    var fromDate = ""
    var toDate = ""
    private var isSelectedDate: LocalDate? = null

    private var isStaffDetails: StaffDetails? = null
    var isAcademicYearId = -1

    var SelectedDate: String? = null
    var isCurrentAcademicYear = true
    var isSectionId = -1
    var isGetStandard: List<Standard>? = null
    private var isStandardId: Int? = null
    private var SectionID: Int? = null
    private var callApi = false
    private var hasUserSelectedSection = false


    override fun getViewBinding(): AttendanceMarkBinding {
        return AttendanceMarkBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        AttendanceType = Constant.fullDay
        isSelectedDate = LocalDate.now()
        binding.AttendanceSelectedDate.text = Constant.formatToPretty(isSelectedDate.toString())
        SelectedDate = Constant.formatToUi2(isSelectedDate.toString())
        binding.imgInfo.setColorFilter(
            ContextCompat.getColor(this, R.color.PrimaryColor),
            PorterDuff.Mode.SRC_IN
        )

        styleLabel(
            binding.lblFullDay,
            R.drawable.mild_gray_radius,
            R.color.PrimaryColor,
            R.color.white
        )
        styleLabel(binding.lblHalfDay, R.drawable.mild_gray_radius, R.color.gray, R.color.black)

        binding.rlaStandard.setOnClickListener(this)
        binding.rlaSection.setOnClickListener(this)
        binding.btnAbsent.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)
        binding.btnSelectPresent.setOnClickListener(this)

        binding.lblFullDay.setOnClickListener(this)
        binding.lblHalfDay.setOnClickListener(this)
        binding.lblFirstHalf.setOnClickListener(this)
        binding.lblSecondHalf.setOnClickListener(this)
        updateActionButtonsState()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        Log.d("isAccessToken", isStaffDetails!!.access_token)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        loadFromCalendar()

        binding.imgSearchicon.setOnClickListener {
            if (binding.rytSearchbox.visibility == View.VISIBLE) {
                binding.rytSearchbox.visibility = View.GONE
                binding.txtSearchBox.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.toolbarLayout.imgSearch.windowToken, 0)
            } else {
                binding.rytSearchbox.visibility = View.VISIBLE
                binding.txtSearchBox.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.toolbarLayout.imgSearch.windowToken, 0)

            }
        }

        binding.imgInfo.setOnClickListener {
            showCustomPopupMenu()
        }


        binding.lnrTabOneName.setOnClickListener {
            binding.lnrTabOneName.isEnabled = false
            binding.lnrTabTwoName.isEnabled = true
            binding.line1.setBackgroundResource(R.color.iconBlue)
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.line2.setBackgroundResource(R.color.athens_gray)
            callApi = false
            binding.txtSearchBox.text.clear()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.txtSearchBox.windowToken, 0)
            binding.calendarFromFragmentContainer.visibility = View.VISIBLE
            binding.lnrAttendanceReport.visibility = View.GONE
            binding.rytSearchbox.visibility = View.GONE
            loadFromCalendar()
            isGetStandardSection()
        }

        binding.lnrTabTwoName.setOnClickListener {
            binding.lnrTabOneName.isEnabled = true
            binding.lnrTabTwoName.isEnabled = false
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.line2.setBackgroundResource(R.color.iconBlue)
            binding.line1.setBackgroundResource(R.color.athens_gray)
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.txtSearchBox.windowToken, 0)
            callApi = true
            binding.rytSearchbox.visibility = View.GONE
            binding.btnAbsent.visibility = View.GONE
            binding.lnrClasses2.visibility = View.GONE
            binding.lnrClasses1.visibility = View.GONE
            binding.lblAttendanceOptions.visibility = View.GONE
            binding.rcyAttendanceReport.visibility = View.VISIBLE
            binding.calendarFromFragmentContainer.visibility = View.VISIBLE
            binding.lnrAttendanceReport.visibility = View.VISIBLE
            loadData()
        }


        binding.txtSearchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TEXTCOMING", "Text changed to: ${s.toString()}")
                filter(s.toString())
            }
        })


        appViewModel!!.isSendAbsenteeSMS?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@AttendanceMark)
                    Log.d("isSendAbsenteeSMS", response.message)
                    Constant.showDataValidation(
                        resources.getString(R.string.success),
                        response.message,
                        this
                    )
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail),
                        response.message,
                        this
                    )
                }
            } else {
                Constant.showDataValidation(
                    getString(R.string.fail),
                    getString(R.string.something_went_wrong_please_try_again_later),
                    this
                )
            }
        }

        isAcademicYear = isAcademicYearList
        isValidAcademicYear =
            isAcademicYear?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYear!![0].id
        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
        isLoadAcademicYear(isAcademicYear)
        isGetStandardSection()


        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            Constant.hideLoading(this@AttendanceMark)
            if (response != null) {
                if (response.status) {
                    isGetStandard = response.data
                    isGetStandard?.size?.let {
                        if (it > 0) {
                            binding.lnrClasses.visibility = View.VISIBLE
                            isStandardId = isGetStandard!!.get(0).id
                            if (isGetStandard!!.get(0).sections.size > 0) {
                                isSection = isGetStandard!!.get(0).sections
                                SectionID = isGetStandard!!.get(0).sections.get(0).id
                            } else {
                                Toast.makeText(
                                    this,
                                    getString(R.string.no_section_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            isLoadStandard(isGetStandard)
                            val firstStandard = isGetStandard!![0]
                            binding.calendarFromFragmentContainer.visibility = View.VISIBLE
                            binding.lnrClasses.visibility = View.VISIBLE
                            binding.btnAbsent.visibility = View.VISIBLE
                            binding.lblAttendanceOptions.visibility = View.VISIBLE
                            binding.lnrClasses1.visibility = View.VISIBLE
                            binding.lytNoDataFound1.visibility = View.GONE
                            //To Assign Standard and Section in early to use in AbsenteesStudentMark.kt
                            updateStandardAndSection(firstStandard)
                            if (callApi) {
                                binding.btnAbsent.visibility = View.GONE
                                binding.lnrClasses2.visibility = View.GONE
                                binding.lnrClasses1.visibility = View.GONE
                                binding.lblAttendanceOptions.visibility = View.GONE
                                loadData()
                                ShowData()
                            }
                        } else {
                            if (callApi) {
                                binding.btnAbsent.visibility = View.GONE
                                binding.lnrClasses2.visibility = View.GONE
                                binding.lnrClasses1.visibility = View.GONE
                                binding.lblAttendanceOptions.visibility = View.GONE
                                binding.lnrAttendanceReport.visibility = View.GONE
                            }
                            isStandardId = null
                            SectionID = null
                            binding.lnrClasses.visibility = View.GONE
                            binding.btnAbsent.visibility = View.GONE
                            binding.lblAttendanceOptions.visibility = View.GONE
                            binding.lnrClasses1.visibility = View.GONE
                            binding.lnrClasses2.visibility = View.GONE
                            binding.lytNoDataFound1.visibility = View.VISIBLE
                            binding.noDataFound1.text = getString(R.string.no_standard_found)
                        }
                    }
                } else {
                    isStandardId = null
                    SectionID = null
                    if (callApi) {
                        binding.btnAbsent.visibility = View.GONE
                        binding.lnrClasses2.visibility = View.GONE
                        binding.lnrClasses1.visibility = View.GONE
                        binding.lblAttendanceOptions.visibility = View.GONE
                        binding.lnrAttendanceReport.visibility = View.GONE
                    }
                    binding.lnrClasses.visibility = View.GONE
                    binding.btnAbsent.visibility = View.GONE
                    binding.lblAttendanceOptions.visibility = View.GONE
                    binding.lnrClasses1.visibility = View.GONE
                    binding.lnrClasses2.visibility = View.GONE
                    binding.lytNoDataFound1.visibility = View.VISIBLE
                    binding.noDataFound1.text = response.message
                }

            }
        }

        appViewModel!!.isGetStudentAttendanceReportData?.observe(this) { response ->
            Constant.hideLoading(this@AttendanceMark)
            if (response != null) {
                if (response.status) {
                    studentsList = response.data.get(0).attd_report

                    if (response.data.get(0).holiday_message != "") {
                        binding.marqueeText.visibility = View.VISIBLE
                        binding.marqueeText.isSelected = true
                        setMarqueeText(
                            binding.marqueeText,
                            "📢 ${response.data.get(0).holiday_message}"
                        )
                    } else {
                        binding.marqueeText.visibility = View.GONE
                    }

                    studentsList?.size?.let {
                        if (it > 0) {
//                            studentsList = isStudentAttendanceReportResponseData
                            ShowData()
                            loadStudentReport(studentsList)
                            binding.imgSearchicon.visibility = View.VISIBLE
                            binding.rytInfoDetails.visibility = View.VISIBLE
                            binding.lnrAttendancePercentageRate.visibility = View.VISIBLE
                        } else {
                            if (response.message == Constant.This_day_is_marked_as_a_holiday) {
                                ErrorMessage(response.message, R.drawable.no_holiday_message)
                            } else {
                                ErrorMessage(response.message, R.drawable.no_attendance_taken)
                            }
                            binding.rcyAttendanceReport.visibility = View.GONE
                            binding.imgSearchicon.visibility = View.GONE
                            binding.rytInfoDetails.visibility = View.GONE
                            binding.lnrAttendancePercentageRate.visibility = View.GONE
                        }
                    }

                } else {
                    binding.marqueeText.visibility = View.GONE

                    if (response.message == Constant.Attendance_has_not_been_taken_yet) {
                        ErrorMessage(response.message, R.drawable.no_attendance_taken)
                    } else {
                        ErrorMessage(response.message, R.drawable.no_holiday_message)
                    }
                    binding.rcyAttendanceReport.visibility = View.GONE
                    binding.imgSearchicon.visibility = View.GONE
                    binding.rytInfoDetails.visibility = View.GONE
                    binding.lnrAttendancePercentageRate.visibility = View.GONE
                }
            } else {
                binding.rcyAttendanceReport.visibility = View.GONE
                ErrorMessage(
                    getString(R.string.something_went_wrong_please_try_again_later),
                    R.drawable.no_search_message
                )
                binding.imgSearchicon.visibility = View.GONE
                binding.rytInfoDetails.visibility = View.GONE
                binding.lnrAttendancePercentageRate.visibility = View.GONE
            }
        }


    }

    private fun setMarqueeText(textView: TextView, message: String) {
        textView.apply {
            text = message
            visibility = View.VISIBLE
            isSelected = true // start marquee

            //  Force marquee even if text is short
            post {
                val textWidth = paint.measureText(message)
                val viewWidth = width.toFloat()

                if (textWidth <= viewWidth) {
                    // Repeat text with spaces to make it scroll continuously
                    val repeatCount = ((viewWidth / textWidth) + 8).toInt().coerceAtLeast(3)
                    val repeatedText = (message + "     ").repeat(repeatCount)
                    text = repeatedText
                }

                // Re-enable marquee indefinitely
                isSelected = true
                marqueeRepeatLimit = -1 // -1 = infinite loop
            }
        }
    }


    private fun filter(text: String) {
        val filteredList = if (text.isBlank()) {
            studentsList
        } else {

            val searchWords = text.trim().lowercase().split("\\s+".toRegex())

            studentsList.filter { student ->
                val fieldsToSearch = listOf(
                    student.student_name.lowercase(),
                    student.admission_no.lowercase(),
                )

                // Check if ALL search words are found in ANY of the fields(feildTosearch List i.e admission_no,student_name...etc)
                searchWords.all { word ->
                    fieldsToSearch.any { field ->
                        field.contains(word)
                    }
                }
            }
        }

        if (filteredList.isNotEmpty()) {
            Log.d("Filter Coming", filteredList.toString())
            mAdapter.updateData(filteredList)
            Log.d("Filter Caming", filteredList.toString())
            ShowData()
        } else {
            binding.rcyAttendanceReport.visibility = View.GONE
            ErrorMessage(resources.getString(R.string.no_data_found), R.drawable.no_search_message)
        }
    }


    private fun isGetStandardSection() {
        Constant.showLoading(this@AttendanceMark)
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }


    private fun updateStandardAndSection(standard: Standard?) {
        if (standard == null) {
            // No Standard Found
            isStandardId = null
            SectionID = null
            isStandardName = null
            isSectionName = null

            binding.rlaSection.isEnabled = false
            binding.rlaSection.isClickable = false
            //Checking Whether to enable the Select All as present and Mark Absentees button
            updateActionButtonsState()
            return
        }
        // Set selected Standard
        isStandardId = standard.id
        isSection = standard.sections

        isStandardName = standard.name
        val sections = standard.sections
        if (!sections.isNullOrEmpty()) {
            val defaultSection = sections[0]
            SectionID = defaultSection.id
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
            isSection = null
            isSectionName = null
            binding.rlaSection.isEnabled = false
            binding.rlaSection.isClickable = false
            //Checking Whether to enable the Select All as present and Mark Absentees button
            updateActionButtonsState()
            return
        }
        //Checking Whether to enable the Select All as present and Mark Absentees button
        updateActionButtonsState()

    }

    fun ErrorMessage(ErrorMessage: String, drawableRes: Int) {
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = ErrorMessage

        Glide.with(binding.imgStudentReportImage.context)
            .load(drawableRes)
            .placeholder(drawableRes)
            .into(binding.imgStudentReportImage)
    }

    fun ShowData() {
        binding.rcyAttendanceReport.visibility = View.VISIBLE
        binding.lytNoDataFound.visibility = View.GONE
    }


    private fun loadFromCalendar() {
        val today = LocalDate.now()
        val minFromDate =
            today.minusYears(1) //LocalDate.of(2025, 9, 10)   // 10 Sep 2025 To handle the only for Specify date
        val maxFromDate = today


        val fromFragment = CustomCalendarFragment.newInstance(
            minDate = minFromDate.toString(),
            maxDate = maxFromDate.toString(),
            selectedDate = isSelectedDate?.toString(),
            tag = Constant.FROM_DATE
        )

        supportFragmentManager.beginTransaction()
            .replace(binding.calendarFromFragmentContainer.id, fromFragment, "FROM_CALENDAR")
            .commit()

        binding.calendarFromFragmentContainer.visibility = View.VISIBLE
    }

    override fun onDateSelected(date: String, tag: String) {
        val selected = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

        when (tag) {
            Constant.FROM_DATE -> {
                Log.d("selectedDate", selected.toString())
                SelectedDate = Constant.formatToUi2(selected.toString())
                isSelectedDate = selected//This Date for Fragemnt to change the next date
                binding.AttendanceSelectedDate.text = Constant.formatToPretty(selected.toString())
                if (callApi) {
                    binding.rytSearchbox.visibility = View.GONE
                    binding.txtSearchBox.text.clear()
                    loadData()
                }
            }
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnSelectPresent -> {

                Constant.showSendConfirmationDialog(
                    this,
                    getString(R.string.confirmation),
                    getString(R.string.permission_ok),
                    getString(R.string.Cancel),
                    "",
                    getString(R.string.MarkAllPresent)
                ) { confirmed ->
                    if (confirmed) {
                        Constant.showLoading(this)
                        isMarkAttendance()
                    }
                }
            }


            R.id.lblFullDay -> {
                SessionType = ""
                AttendanceType = Constant.fullDay

                styleLabel(
                    binding.lblFullDay,
                    R.drawable.mild_gray_radius,
                    R.color.PrimaryColor,
                    R.color.white
                )
                styleLabel(
                    binding.lblHalfDay,
                    R.drawable.mild_gray_radius,
                    R.color.gray,
                    R.color.black
                )
                binding.lnrClasses2.visibility = View.GONE

            }

            R.id.lblHalfDay -> {
                AttendanceType = Constant.halfDay

                styleLabel(
                    binding.lblFullDay,
                    R.drawable.mild_gray_radius,
                    R.color.gray,
                    R.color.black
                )
                styleLabel(
                    binding.lblHalfDay,
                    R.drawable.mild_gray_radius,
                    R.color.PrimaryColor,
                    R.color.white
                )
                binding.lnrClasses2.visibility = View.VISIBLE

                binding.lnrClasses2.visibility = View.VISIBLE
                SessionType = Constant.firstHalf
                styleLabel(
                    binding.lblFirstHalf,
                    R.drawable.gray_bg_radius,
                    R.color.green,
                    R.color.white
                )
                styleLabel(
                    binding.lblSecondHalf,
                    R.drawable.gray_bg_radius,
                    R.color.gray,
                    R.color.black
                )

            }

            R.id.lblFirstHalf -> {
                SessionType = Constant.firstHalf
                styleLabel(
                    binding.lblFirstHalf,
                    R.drawable.gray_bg_radius,
                    R.color.green,
                    R.color.white
                )
                styleLabel(
                    binding.lblSecondHalf,
                    R.drawable.gray_bg_radius,
                    R.color.gray,
                    R.color.black
                )
            }

            R.id.lblSecondHalf -> {
                SessionType = Constant.secondHalf

                styleLabel(
                    binding.lblFirstHalf,
                    R.drawable.gray_bg_radius,
                    R.color.gray,
                    R.color.black
                )
                styleLabel(
                    binding.lblSecondHalf,
                    R.drawable.gray_bg_radius,
                    R.color.green,
                    R.color.white
                )

            }

            R.id.btnAbsent -> {

                val intent = Intent(this, AbsenteesStudentMark::class.java)
                //We are saving the details in Data class to use in the next screen
                isSaveMarkAttendanceDetails()
                startActivity(intent)
            }

        }
        updateActionButtonsState()
    }

    private fun styleLabel(
        view: TextView,
        @DrawableRes drawableRes: Int,
        @ColorRes bgColorRes: Int,
        @ColorRes textColorRes: Int
    ) {
        val drawable = ContextCompat.getDrawable(view.context, drawableRes)?.mutate()
        if (drawable is GradientDrawable) {
            drawable.setColor(ContextCompat.getColor(view.context, bgColorRes))
        }
        view.background = drawable
        view.setTextColor(ContextCompat.getColor(view.context, textColorRes))
    }


    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter

        binding.isSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    val selectedOption = isAcademicYear!![position]
                    isAcademicYearId = selectedOption.id
                    isGetStandardSection()
                    if (callApi) {
                        binding.txtSearchBox.text.clear()
                        loadData()
                    }
                    binding.txtSearchBox.text.clear()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isLoadStandard(isStandard: List<Standard>?) {
        val adapter = StandardDropDownListAdapter(this, isStandard)
        binding.isSpinnerStandard.adapter = adapter
        binding.isSpinnerStandard.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    val selectedOption = isStandard!![position]
                    updateStandardAndSection(selectedOption)
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${isStandard[position].id}, Year = ${isStandard[position].name}"
                    )

                    hasUserSelectedSection = false
                    isSectionId = isStandard.get(position).id
                    isSection = isStandard[position].sections

                    isLoadSection(isSection)
                    if (callApi) {
                        binding.txtSearchBox.text.clear()
                        loadData()

                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isLoadSection(isSection: List<Section>?) {
        val adapter = SectionDropDownListAdapter(this, isSection)
        binding.isSpinnerSection.adapter = adapter
        binding.isSpinnerSection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    val selectedOption = isSection!![position]
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${isSection[position].id}, Year = ${isSection[position].name}"
                    )
                    if (hasUserSelectedSection) {
                        SectionID = selectedOption.id
                        isSectionName = selectedOption.name
                        if (callApi == true) {
                            binding.txtSearchBox.text.clear()
                            loadData()
                        }
                    } else {
                        // First auto-trigger — just set the flag and skip loadData
                        hasUserSelectedSection = true
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isSaveMarkAttendanceDetails() {
        val saveAttendanceData = MarkAttendanceDataSending(
            academic_year_id = isAcademicYearId,
            class_name = isStandardName!!,
            section_name = isSectionName!!,
            class_id = isStandardId.toString(),
            section_id = SectionID.toString(),
            all_present = AllPresent,
            attendance_type = AttendanceType,
            session_type = SessionType,
            attendance_date = SelectedDate!!
        )
        //We are Saving all the data in Constant as List Here
        Constant.isMarkAttendanceDataSending = saveAttendanceData
        Log.d("saveAttendanceData", saveAttendanceData.toString())
    }


    private fun updateActionButtonsState() {
        if (isStandardId != null && SectionID != null) {
            binding.btnAbsent.setBackgroundResource(R.drawable.bg_btn_blue)
            binding.btnAbsent.background.setTint(ContextCompat.getColor(this, R.color.PrimaryColor))
            binding.btnAbsent.isEnabled = true
        } else {
            binding.btnAbsent.isEnabled = false
            binding.btnAbsent.setBackgroundResource(R.drawable.bg_btn_blue)

        }
    }

    private fun isMarkAttendance() {
        if (isStandardId != null && SectionID != null &&
            ((SessionType == "" && AttendanceType == Constant.fullDay)
                    || (AttendanceType == Constant.halfDay && (SessionType == Constant.firstHalf || SessionType == Constant.secondHalf)))
            && SelectedDate != null && isSelectedIds?.size == null
        ) {
            AllPresent = Constant.allPresent
            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.class_id, isStandardId.toString())
                addProperty(APIKeyNames.section_id, SectionID.toString())
                addProperty(APIKeyNames.all_present, AllPresent)
                addProperty(APIKeyNames.attendance_type, AttendanceType)
                addProperty(APIKeyNames.session_type, SessionType)
                addProperty(APIKeyNames.attendance_date, SelectedDate)
                val studentArray = JsonArray().apply {
                    isSelectedIds?.forEach { id ->
                        add(JsonObject().apply {
                            addProperty(APIKeyNames.id_, id.toString())
                        })
                    }
                }
                add(APIKeyNames.student_id, studentArray)
                Log.d("AbsenteesList", studentArray.toString())

            }
            appViewModel?.isUpdateSendAbsenteeSMS(isAccessToken!!, jsonObject, this)
        }

    }

    fun loadData() {
        //In API,we have From Date and To Date but we actually going to pass only One date(that we getting from ID:rlaDayDatePicker) in both From and To Date
        //Till now this is our requirement(one Day picker we are using so From and To Date)
        fromDate = SelectedDate.toString()
        toDate = SelectedDate.toString()

        mAdapter = AttendanceStudentReportAdapter(null, this, Constant.isShimmerViewShow)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.isAutoMeasureEnabled = true
        binding.rcyAttendanceReport.layoutManager = layoutManager
        binding.rcyAttendanceReport.adapter = mAdapter
        binding.rcyAttendanceReport.isNestedScrollingEnabled = false
        binding.rcyAttendanceReport.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        if (SectionID != null && isStandardId != null) {
            binding.lnrAttendanceReport.visibility = View.VISIBLE
            appViewModel!!.getStudentAttendanceReport(
                isAccessToken!!,
                SectionID.toString(),
                fromDate,
                toDate,
                isStandardId.toString(),
                this
            )
        } else {
            binding.lnrAttendanceReport.visibility = View.GONE
        }


    }

    private fun loadStudentReport(studentReportData: List<StudentAttendanceReportData>) {

        // Calculate attendance stats
        var presentCount = 0
        var absentCount = 0
        var odCount = 0
        var lateCount = 0
        var validCount = 0

        var totalODStudents = 0

        for (student in studentReportData) {
            val statusParts = student.att_status
                ?.split("/")    // split P/A/OD/-
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() && it != "-" } ?: emptyList()

            // Count OD student once per student
            if (statusParts.any { it.equals("P~", true) }) {
                totalODStudents++
            }

            for (status in statusParts) {
                when {
                    status.equals("P", ignoreCase = true) -> presentCount++
                    status.equals("A", ignoreCase = true) -> absentCount++
                    status.equals("OD", ignoreCase = true) -> odCount++
                    status.equals("P~", ignoreCase = true) -> lateCount++
                }
                validCount++
            }
        }

// Calculate percentages safely


        // Combine Present + Late for percentage
        val presentPlusLateCount = presentCount + lateCount

        val presentPlusLatePercentage =
            if (validCount > 0) (presentPlusLateCount * 100f) / validCount else 0f

//        val presentPercentage = if (validCount > 0) (presentCount * 100f) / validCount else 0f
        val absentPercentage = if (validCount > 0) (absentCount * 100f) / validCount else 0f
        val odPercentage = if (validCount > 0) (odCount * 100f) / validCount else 0f
//        val latePercentage = if (validCount > 0) (lateCount * 100f) / validCount else 0f


// Format to two decimal places
//        val presentFormatted = String.format("%.1f", presentPercentage)

        val presentPlusLateFormatted = String.format("%.1f", presentPlusLatePercentage)

        val absentFormatted = String.format("%.1f", absentPercentage)
        val odFormatted = String.format("%.1f", odPercentage)
//        val lateFormatted = String.format("%.1f", latePercentage)


// Set to UI
//        binding.lblPresentRate.text = "$presentFormatted%"
        binding.lblPresentRate.text = "$presentPlusLateFormatted%"
        binding.lblAbsentRate.text = "$absentFormatted%"
        binding.lblODRate.text = "$odFormatted%"
//        binding.lblLateRate.text = "$lateFormatted%"


        // Show total OD students
        binding.lblLateRate.text =
            "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83C\uDF93" + " " + totalODStudents.toString()

        mAdapter =
            AttendanceStudentReportAdapter(studentReportData, this, Constant.isShimmerViewDisable)

        binding.rcyAttendanceReport.adapter = mAdapter

    }


    private fun showCustomPopupMenu() {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.dialog_attendance_status, null)
        val container = popupView.findViewById<LinearLayout>(R.id.containerIcons)

        val items = listOf(
            Triple("-", getString(R.string.not_taken), R.drawable.report_nottaken_icon),
            Triple("P", getString(R.string.present), R.drawable.report_present_icon),
            Triple("OD", getString(R.string.OD), R.drawable.report_od_icon),
            //            Triple("LA", getString(R.string.Late_2), R.drawable.report_latercomer_icon),
            Triple(
                "P ᴸᴬ",
                getString(R.string.present_late),
                R.drawable.report_present_icon
            ), // Late
            Triple("A", getString(R.string.absent), R.drawable.report_absent_icon),
        )

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // --- Header FN / AN block ---
        val headerTextView = TextView(this).apply {
            setPadding(18, 12, 16, 12)
            setTextColor(ContextCompat.getColor(this@AttendanceMark, android.R.color.black))

            val text = SpannableStringBuilder()

            val fnLabel = "FN : "
            val fnValue = context.getString(R.string.forenoon)
            val anLabel = " / AN : "
            val anValue = context.getString(R.string.afternoon)

            val fnLabelStart = text.length
            text.append(fnLabel)
            text.setSpan(
                AbsoluteSizeSpan(16, true),
                fnLabelStart,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val fnValueStart = text.length
            text.append(fnValue)
            text.setSpan(
                AbsoluteSizeSpan(13, true),
                fnValueStart,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val anLabelStart = text.length
            text.append(anLabel)
            text.setSpan(
                AbsoluteSizeSpan(16, true),
                anLabelStart,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val anValueStart = text.length
            text.append(anValue)
            text.setSpan(
                AbsoluteSizeSpan(13, true),
                anValueStart,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START
            this.text = text
        }

        container.addView(headerTextView)


        for ((code, title, iconRes) in items) {
            val itemView = inflater.inflate(R.layout.item_popup_icon_text, container, false)
            val txtInside = itemView.findViewById<TextView>(R.id.txtInsideIcon)
            val txtTitle = itemView.findViewById<TextView>(R.id.txtTitle)

            txtTitle.text = title

            // Background icon
            if (iconRes != 0) {
                txtInside.setBackgroundResource(iconRes)
            } else {
                txtInside.background = null
            }

            //we are Applying P ᴸᴬ special color
            if (code == "P ᴸᴬ") {

                val text = "P ᴸᴬ"
                val spannable = SpannableString(text)

                // P = white
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.white)),
                    0, 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // ᴸᴬ = dark_orange
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this, R.color.dark_orange)),
                    2, text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                txtInside.text = spannable
            } else {
                // All other status → white text
                txtInside.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                txtInside.text = code
            }

            itemView.setOnClickListener {
                popupWindow.dismiss()
            }

            container.addView(itemView)
        }

        popupWindow.setBackgroundDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_popup_round
            )
        )
        popupWindow.elevation = 10f
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(binding.imgInfo, -30, 10)
    }


}