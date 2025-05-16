package com.vs.schoolmessenger.School.StaffWiseAttendanceReport

import android.app.Dialog
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.PunchHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.StaffAttendanceReportAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchTimingsData
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportData
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.AttendanceReportClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StaffAttendanceReportBinding
import java.util.Calendar

class StaffWiseAttendanceReport : BaseActivity<StaffAttendanceReportBinding>(),
    View.OnClickListener, AttendanceReportClickListener {

    override fun getViewBinding(): StaffAttendanceReportBinding {
        return StaffAttendanceReportBinding.inflate(layoutInflater)
    }

    var isGetStaffListData: List<NameAndIds>? = null

    private var isStaffAttendanceReportAdapter: StaffAttendanceReportAdapter? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null
    private var rcyPunchList: RecyclerView? = null
    private var lblNoRecordsFound: TextView? = null
    private var isPunchHistoryAdapter: PunchHistoryAdapter? = null
    private var isStaffId: Int? = null
    private var isTodayList = true
    private var isSelectedYear: String? = null
    private var selectedMonthNumber: String? = null
    private var isMonthLoaded = ""
    private var isLoadingFirstTime = true


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
        binding.rlaStaff.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = "Staffwise Attendance Report"
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        getStaffAttendanceReport(Constant.getCurrentDate(), "", "")

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        appViewModel!!.isStaffWiseAttendanceReport?.observe(this) { response ->
            if (response != null && response.status) {
                binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
                binding.lytNoRecordFound.visibility = View.GONE
                val isStaffReport = response.data
                isLoadData(isStaffReport)
            } else {
                binding.recycleAttendanceReportsToday.visibility = View.GONE
                binding.lytNoRecordFound.visibility = View.VISIBLE
                binding.lblNoRecords.text = response!!.message
            }
        }

        appViewModel!!.isStaffWiseAttendanceReportList?.observe(this) { response ->
            if (response != null && response.status) {
                binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
                binding.lytNoRecordFound.visibility = View.GONE
                val isStaffReport = response.data
                isLoadData(isStaffReport)
            } else {
                binding.recycleAttendanceReport.visibility = View.GONE
                binding.lytNoRecordFound.visibility = View.VISIBLE
                binding.lblNoRecords.text = response!!.message
            }
        }

        appViewModel!!.isGetStaffList?.observe(this) { response ->
            Constant.hideLoading(this@StaffWiseAttendanceReport)

            if (response != null) {
                binding.rlaStaff.visibility = View.VISIBLE
                isGetStaffListData = response.data
                binding.lblStaff.text = isGetStaffListData!![0].name
                isStaffId = isGetStaffListData!![0].id
                isLoadYear()
                isLoadMonth()
            }
        }
    }

    private fun isLoadData(isStaffReport: List<StaffAttendanceReportData>) {

        if (isStaffReport.isNotEmpty()) {
            Constant.executeAfterDelay {
                binding.lytNoRecordFound.visibility = View.GONE
                if (isTodayList) {
                    binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
                    binding.recycleAttendanceReport.visibility = View.GONE
                    isStaffAttendanceReportAdapter =
                        StaffAttendanceReportAdapter(
                            isStaffReport,
                            this,
                            this,
                            Constant.isShimmerViewDisable
                        )
                    binding.recycleAttendanceReportsToday.adapter = isStaffAttendanceReportAdapter
                } else {
                    binding.recycleAttendanceReport.visibility = View.VISIBLE
                    binding.recycleAttendanceReportsToday.visibility = View.GONE
                    isStaffAttendanceReportAdapter =
                        StaffAttendanceReportAdapter(
                            isStaffReport,
                            this,
                            this,
                            Constant.isShimmerViewDisable
                        )
                    binding.recycleAttendanceReport.adapter = isStaffAttendanceReportAdapter
                }
            }
        } else {
            binding.recycleAttendanceReport.visibility = View.GONE
            binding.recycleAttendanceReportsToday.visibility = View.GONE
            binding.lytNoRecordFound.visibility = View.VISIBLE
            binding.lblNoRecords.text = "No list found!"
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

    private fun isLoadYear() {
        val years = (2025 downTo 2001).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYears.adapter = adapter
        binding.spinnerYears.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.lytNoRecordFound.visibility = View.GONE
                isSelectedYear = parent.getItemAtPosition(position).toString()
                isLoadMonth()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    fun isLoadMonth() {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        val currentMonthName = months[currentMonthIndex]

        val updatedMonths: List<String>
        val selectedIndex: Int

        if (isLoadingFirstTime) {
            updatedMonths = months
            selectedIndex = currentMonthIndex
            isLoadingFirstTime = false
        } else if (isMonthLoaded.isNotEmpty()) {
            updatedMonths = listOf(isMonthLoaded) + months.filter { it != isMonthLoaded }
            selectedIndex = 0
        } else {
            updatedMonths = months
            selectedIndex = currentMonthIndex
        }

        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, updatedMonths)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonths.adapter = monthAdapter
        binding.spinnerMonths.setSelection(selectedIndex)

        binding.spinnerMonths.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                isMonthLoaded = updatedMonths[position]
                binding.lytNoRecordFound.visibility = View.GONE
                selectedMonthNumber = String.format("%02d", months.indexOf(isMonthLoaded) + 1)
                getStaffAttendanceReport("", isSelectedYear!!, selectedMonthNumber!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isBackgroundChange(btnClick: TextView) {
        binding.btnCreate.background = null
        binding.btnHistory.background = null
        binding.lytNoRecordFound.visibility = View.GONE
        binding.lnrParent.setBackgroundResource(R.drawable.bg_light_blue)
        btnClick.setBackgroundResource(R.drawable.white_bg_radius)

        if (btnClick == binding.btnCreate) {
            binding.rytTodays.visibility = View.VISIBLE
            binding.rytAttendanceHistorySceen.visibility = View.GONE
            getStaffAttendanceReport(Constant.getCurrentDate(), "", "")
        }

        if (btnClick == binding.btnHistory) {
            isGetStaffList()
            binding.rytTodays.visibility = View.GONE
            binding.rytAttendanceHistorySceen.visibility = View.VISIBLE
        }
    }

    fun isLoadPunchHistoryData(data: List<PunchTimingsData>) {
        if (data.isNotEmpty()) {
            rcyPunchList!!.visibility = View.VISIBLE
            lblNoRecordsFound!!.visibility = View.GONE
//            Constant.executeAfterDelay {
            isPunchHistoryAdapter = PunchHistoryAdapter(
                data, this, Constant.isShimmerViewDisable
            )
            rcyPunchList!!.layoutManager = LinearLayoutManager(this)
            rcyPunchList!!.adapter = isPunchHistoryAdapter
//            }
        } else {
            rcyPunchList!!.visibility = View.GONE
            lblNoRecordsFound!!.text = "No Punch History found!"
            lblNoRecordsFound!!.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnHistory -> {
                isTodayList = false
                isBackgroundChange(binding.btnHistory)

            }

            R.id.btnCreate -> {
                isTodayList = true
                isBackgroundChange(binding.btnCreate)
            }

            R.id.rlaStaff -> {
                isDropDownLoadData(
                    binding.rlaStaff, this, isGetStaffListData
                ) { selectStaffId ->
                    binding.lblStaff.text = selectStaffId.first
                    isStaffId = selectStaffId.second
                    getStaffAttendanceReport("", isSelectedYear!!, selectedMonthNumber!!)
                }
            }
        }
    }

    private fun isGetStaffList() {
        Constant.showLoading(this@StaffWiseAttendanceReport)
        appViewModel!!.isGetStaffList(
            isAccessToken!!, this
        )
    }

    fun getStaffAttendanceReport(
        isCurrentDate: String,
        selectedYear: String,
        selectedMonthNumber: String
    ) {
        binding.lytNoRecordFound.visibility = View.GONE
        if (isTodayList) {
            binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
            isStaffAttendanceReportAdapter =
                StaffAttendanceReportAdapter(null, this, this, Constant.isShimmerViewShow)
            binding.recycleAttendanceReportsToday.layoutManager = LinearLayoutManager(this)
            binding.recycleAttendanceReportsToday.adapter = isStaffAttendanceReportAdapter
        } else {
            binding.recycleAttendanceReport.visibility = View.VISIBLE
            isStaffAttendanceReportAdapter =
                StaffAttendanceReportAdapter(null, this, this, Constant.isShimmerViewShow)
            binding.recycleAttendanceReport.layoutManager = LinearLayoutManager(this)
            binding.recycleAttendanceReport.adapter = isStaffAttendanceReportAdapter
        }

        if (isTodayList) {
            isAccessToken?.let {
                appViewModel?.getStaffWiseAttendanceReport(it, isCurrentDate, this)
            }
        } else {
            isAccessToken?.let {
                appViewModel?.getStaffWiseAttendanceReportList(
                    it,
                    selectedYear + "-" + selectedMonthNumber, isStaffId!!,
                    this
                )
            }
        }
    }

    private fun isLocationHistory(data: StaffAttendanceReportData) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.punch_history, null)

        rcyPunchList = view.findViewById<RecyclerView>(R.id.rcyPunchList)
        lblNoRecordsFound = view.findViewById<TextView>(R.id.lblNoRecordsFound)
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

    private fun isPunchHistory(data: StaffAttendanceReportData) {

        isAccessToken?.let {
            appViewModel?.getPunchHistory(it, data.date, this)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onItemClick(data: StaffAttendanceReportData) {
        isLocationHistory(data)
    }
}