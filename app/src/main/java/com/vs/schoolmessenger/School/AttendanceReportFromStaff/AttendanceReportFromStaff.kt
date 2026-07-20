package com.vs.schoolmessenger.School.AttendanceReportFromStaff

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SubjectLoadAdapter.SubjectLoadAdapter
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.PunchHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchTimingsData
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AttendancereportFromStaffBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AttendanceReportFromStaff : BaseActivity<AttendancereportFromStaffBinding>(),
    View.OnClickListener, OnAttendanceHistoryClickListener {

    override fun getViewBinding(): AttendancereportFromStaffBinding {
        return AttendancereportFromStaffBinding.inflate(layoutInflater)
    }

    var isGetStaffListData: List<NameAndIds>? = null
    private var isAttendanceReportFromStaffAdapter: AttendanceReportFromStaffAdapter? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null
    private var rcyPunchList: RecyclerView? = null
    private var lblNoRecordsFound: TextView? = null
    private var isPunchHistoryAdapter: PunchHistoryAdapter? = null
    private var isStaffId: String? = null
    private var lblName: TextView? = null
    private var lblDate: TextView? = null
    private var lblSchoolName: TextView? = null
    private var lblDesignation: TextView? = null

    var isAllStaff = true

    private var fromDateMillis: Long = 0L
    private var toDateMillis: Long = 0L

    private var isFromDate: String = ""
    private var isToDFate: String = ""

    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
    private val apiFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        binding.linearLayout3.setOnClickListener(this)
        binding.linearLayout4.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        val calendar = Calendar.getInstance()
        fromDateMillis = calendar.timeInMillis
        toDateMillis = calendar.timeInMillis
        val displayDate = displayFormat.format(calendar.time)
        val apiDate = apiFormat.format(calendar.time)
        binding.fromDate2.text = displayDate
        binding.fromDate3.text = displayDate
        isFromDate = apiDate
        isToDFate = apiDate


        val text = getString(R.string.fromdatewithman)
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(Color.RED),
            text.length - 1,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.fromDate.text = spannable


        val text1 = getString(R.string.todatewithman)
        val spannable1 = SpannableString(text1)
        spannable1.setSpan(
            ForegroundColorSpan(Color.RED),
            text1.length - 1,
            text1.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.fromDate1.text = spannable1

        val text2 = getString(R.string.select_staff_with_man)
        val spannable2 = SpannableString(text2)
        spannable2.setSpan(
            ForegroundColorSpan(Color.RED),
            text2.length - 1,
            text2.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.lblSelectStaff.text = spannable2

        isGetStaffList()

        appViewModel!!.isGioStaffWiseAttendanceReportList?.observe(this) { response ->

            if (response != null && response.status) {

                binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
                if (isAllStaff) {
                    binding.rytSummery.visibility = View.VISIBLE
                } else {
                    binding.rytSummery.visibility = View.GONE
                }
                binding.lytNoRecordFound.visibility = View.GONE

                val overall = response.data[0].overall_stat


                setCountText(binding.tvPresent, overall.present, "Present")
                setCountText(binding.tvAbsent, overall.absent, "Absent")
                setCountText(binding.tvNotMarked, overall.not_marked, "Not Marked")

                val finalList = ArrayList<Pair<String, DateAttendanceDataClass>>()

                response.data.forEach { mainData ->
                    mainData.all_attd.forEach { (date, value) ->
                        finalList.add(Pair(date, value))
                    }
                }

                isLoadData(finalList)

            } else {
                binding.recycleAttendanceReportsToday.visibility = View.GONE
                binding.rytSummery.visibility = View.GONE
                binding.lytNoRecordFound.visibility = View.VISIBLE
                binding.lblNoRecords.text = response?.message ?: getString(R.string.no_data_found)
            }
        }

        appViewModel!!.isGetStaffList?.observe(this) { response ->
            Constant.hideLoading(this@AttendanceReportFromStaff)
            if (response != null) {
                isGetStaffListData = response.data
                isLoadStaff(isGetStaffListData)
                isStaffId = isGetStaffListData!![0].id.toString()
            }
        }

        appViewModel!!.isPunchHistory?.observe(this) { response ->
            if (response != null && response.status) {
                val historyList = response.data
                if (historyList.isNotEmpty()) {
                    val isPunchTiming = historyList.flatMap { it.timings }
                    isLoadPunchHistoryData(isPunchTiming)
                } else {
                    rcyPunchList!!.visibility = View.GONE
                    lblNoRecordsFound!!.text = response!!.message
                    lblNoRecordsFound!!.visibility = View.VISIBLE
                }
            } else {
                rcyPunchList!!.visibility = View.GONE
                lblNoRecordsFound!!.text = response!!.message
                lblNoRecordsFound!!.visibility = View.VISIBLE
            }
        }
    }

    private fun setCountText(textView: TextView, count: Int, label: String) {

        val countText = count.toString()
        val fullText = "$countText\n$label"

        val spannable = SpannableString(fullText)

        spannable.setSpan(
            AbsoluteSizeSpan(20, true),
            0,
            countText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            countText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            AbsoluteSizeSpan(12, true), // smaller size
            countText.length + 1,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            countText.length + 1,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }

    private fun isLoadData(list: List<Pair<String, DateAttendanceDataClass>>) {

        val adapter = AttendanceReportFromStaffAdapter(
            list, this, false, this, isAllStaff
        )

        binding.recycleAttendanceReportsToday.layoutManager = LinearLayoutManager(this)

        binding.recycleAttendanceReportsToday.adapter = adapter
    }

    fun isLoadPunchHistoryData(data: List<PunchTimingsData>) {
        if (data.isNotEmpty()) {
            rcyPunchList!!.visibility = View.VISIBLE
            lblNoRecordsFound!!.visibility = View.GONE
            isPunchHistoryAdapter = PunchHistoryAdapter(
                data, this, Constant.isShimmerViewDisable
            )
            rcyPunchList!!.layoutManager = LinearLayoutManager(this)
            rcyPunchList!!.adapter = isPunchHistoryAdapter

        } else {
            rcyPunchList!!.visibility = View.GONE
            lblNoRecordsFound!!.text = getString(R.string.Punch_History_found)
            lblNoRecordsFound!!.visibility = View.VISIBLE
        }
    }

    private fun isLoadStaff(isGetStaffListData: List<NameAndIds>?) {

        if (isGetStaffListData.isNullOrEmpty()) return

        val staffList = ArrayList<NameAndIds>()

        staffList.add(
            NameAndIds(
                id = 0, name = "View All Staff", ",", "", "", ""
            )
        )

        staffList.addAll(isGetStaffListData)

        val adapter = SubjectLoadAdapter(this, staffList)
        binding.spinnerStaffList.adapter = adapter

        binding.spinnerStaffList.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    val selectedItem = staffList[position]
                    if (position == 0) {
                        isStaffId = "0"
                        isAllStaff = true
                    } else {
                        isStaffId = selectedItem.id.toString()
                        isAllStaff = false
                    }
                    getStaffAttendanceReport(isAllStaff, isStaffId)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.linear_layout3 -> {
                openFromDatePicker()
            }

            R.id.linear_layout4 -> {
                openToDatePicker()
            }
        }
    }

    private fun openFromDatePicker() {

        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)

        val config = Configuration(this.resources.configuration)
        config.setLocale(Locale.ENGLISH)
        this.resources.updateConfiguration(config, this.resources.displayMetrics)

        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                Locale.setDefault(originalLocale)
                this.resources.updateConfiguration(
                    Configuration(this.resources.configuration).apply {
                        setLocale(originalLocale)
                    },
                    this.resources.displayMetrics
                )

                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)

                fromDateMillis = selectedCal.timeInMillis

                val apiDate = apiFormat.format(selectedCal.time)
                isFromDate = apiDate

                val displayDate = displayFormat.format(selectedCal.time)
                binding.fromDate2.text = displayDate

                // Auto adjust TO date if needed
                if (toDateMillis < fromDateMillis) {
                    toDateMillis = fromDateMillis
                    binding.fromDate3.text = displayDate
                    isToDFate = apiDate
                }

                getStaffAttendanceReport(isAllStaff, isStaffId)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }

    private fun openToDatePicker() {

        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)

                toDateMillis = selectedCal.timeInMillis

                val apiDate = apiFormat.format(selectedCal.time)
                isToDFate = apiDate

                val displayDate = displayFormat.format(selectedCal.time)
                binding.fromDate3.text = displayDate

                getStaffAttendanceReport(isAllStaff, isStaffId)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = fromDateMillis

        datePicker.show()
    }
    private fun isGetStaffList() {
        appViewModel!!.isGetStaffList(
            isAccessToken!!, this
        )
    }

    fun getStaffAttendanceReport(
        isAllStaff: Boolean, isStaffId: String?
    ) {
        binding.lytNoRecordFound.visibility = View.GONE
        binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
        isAttendanceReportFromStaffAdapter =
            AttendanceReportFromStaffAdapter(
                null,
                this,
                Constant.isShimmerViewShow,
                this,
                isAllStaff
            )
        binding.recycleAttendanceReportsToday.layoutManager = LinearLayoutManager(this)
        binding.recycleAttendanceReportsToday.adapter = isAttendanceReportFromStaffAdapter

        isAccessToken?.let {
            appViewModel?.getGioMetricAttendanceReport(
                it, isFromDate, isToDFate, isAllStaff, isStaffId!!, this
            )
        }
    }

    private fun isLocationHistory(data: AttendanceDetailDataClass) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.punch_history, null)

        rcyPunchList = view.findViewById<RecyclerView>(R.id.rcyPunchList)
        lblNoRecordsFound = view.findViewById<TextView>(R.id.lblNoRecordsFound)

        lblName = view.findViewById<TextView>(R.id.lblStaffName)
        lblDate = view.findViewById<TextView>(R.id.lblDate)
        lblSchoolName = view.findViewById<TextView>(R.id.lblSchoolName)
        lblDesignation = view.findViewById<TextView>(R.id.lblDesignation)

        lblDate!!.text = Constant.convertDateTimeFormat(data.date)
        lblName!!.text = data.name
        lblSchoolName!!.text = isStaffDetails!!.school_name
        lblDesignation!!.text = data.role

        val imgBack = view.findViewById<ImageView>(R.id.imgBack)
        isPunchHistory(data)

        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val width = (resources.displayMetrics.widthPixels * 0.96).toInt()
        val params = WindowManager.LayoutParams()
        params.copyFrom(dialog.window?.attributes)
        params.width = width - (2 * dpToPx(10))
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = params

        imgBack.setOnClickListener {
            dialog.dismiss()
            binding.lytNoRecordFound.visibility = View.GONE
        }

        try {
            dialog.show()
        } catch (e: Exception) {
        } finally {
        }
    }

    private fun isPunchHistory(data: AttendanceDetailDataClass) {
        isAccessToken?.let {
            appViewModel?.getPunchHistory(it, data.date, data.staff_id, this)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onAttendanceClick(data: AttendanceDetailDataClass) {

        isLocationHistory(data)
    }
}