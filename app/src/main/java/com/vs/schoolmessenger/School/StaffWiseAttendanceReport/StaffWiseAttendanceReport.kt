package com.vs.schoolmessenger.School.StaffWiseAttendanceReport

import android.app.Dialog
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SubjectLoadAdapter.SubjectLoadAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.PunchHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.StaffAttendanceReportAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.YearLoadingAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchTimingsData
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportData
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.AttendanceReportClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
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
    private var yearList: List<AcademicYear>? = null
    private var lblName: TextView? = null
    private var lblDate: TextView? = null
    private var lblSchoolName: TextView? = null
    private var lblDesignation: TextView? = null
    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
//        binding.rlaStaff.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        getStaffAttendanceReport(Constant.getCurrentDate(), "", "")
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        appViewModel!!.isStaffWiseAttendanceReport?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
                    binding.lytNoRecordFound.visibility = View.GONE
                    val isStaffReport = response.data
                    isLoadData(isStaffReport)
                } else {
                    binding.recycleAttendanceReportsToday.visibility = View.GONE
                    binding.lytNoRecordFound.visibility = View.VISIBLE
                    binding.lblNoRecords.text = response!!.message
                }
            } else {
                binding.recycleAttendanceReportsToday.visibility = View.GONE
                binding.lytNoRecordFound.visibility = View.VISIBLE
                binding.lblNoRecords.text = getString(R.string.no_data_found)
            }
        }

        appViewModel!!.isStaffWiseAttendanceReportList?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
                    binding.lytNoRecordFound.visibility = View.GONE
                    val isStaffReport = response.data
                    isLoadData(isStaffReport)
                } else {
                    binding.recycleAttendanceReportsToday.visibility = View.GONE
                    binding.lytNoRecordFound.visibility = View.VISIBLE
                    binding.lblNoRecords.text = response!!.message
                }
            } else {
                binding.recycleAttendanceReportsToday.visibility = View.GONE
                binding.lytNoRecordFound.visibility = View.VISIBLE
                binding.lblNoRecords.text = getString(R.string.no_data_found)
            }
        }
        appViewModel!!.isGetStaffList?.observe(this) { response ->
            Constant.hideLoading(this@StaffWiseAttendanceReport)
            if (response != null) {
                binding.rlaStaff.visibility = View.VISIBLE
                isGetStaffListData = response.data
                isLoadStaff(isGetStaffListData)
//                binding.lblStaff.text = isGetStaffListData!![0].name
                isStaffId = isGetStaffListData!![0].id
                getStaffAttendanceReport("", isSelectedYear!!, selectedMonthNumber!!)
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

    private fun isLoadData(isStaffReport: List<StaffAttendanceReportData>) {
//        Constant.executeAfterDelay {
        isStaffAttendanceReportAdapter = StaffAttendanceReportAdapter(
            isStaffReport, this, this, Constant.isShimmerViewDisable
        )
        binding.recycleAttendanceReportsToday.adapter = isStaffAttendanceReportAdapter
        // }
    }

    private fun isLoadYear(isAcademicYear: List<AcademicYear>?) {
        val uniqueYears =
            isAcademicYear!!.mapNotNull { it.year.split("-").firstOrNull() }.distinct()
        val currentYear =
            isAcademicYear.find { it.current_academic_year }?.year?.split("-")?.firstOrNull()
        val sortedYears = if (currentYear != null) {
            listOf(currentYear) + uniqueYears.filter { it != currentYear }
        } else {
            uniqueYears
        }

        val adapter = YearLoadingAdapter(this, sortedYears)
        binding.spinnerYears.adapter = adapter

        binding.spinnerYears.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()

                binding.lytNoRecordFound.visibility = View.GONE
                isSelectedYear = parent.getItemAtPosition(position).toString()
                isLoadMonth()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isLoadMonth() {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)

        val reorderedMonths = listOf(months[currentMonthIndex]) +
                months.filterIndexed { index, _ -> index != currentMonthIndex }

        val adapter = SpinnerLoadingAdapter(this, reorderedMonths)
        binding.spinnerMonths.adapter = adapter

        binding.spinnerMonths.setSelection(0)

        binding.spinnerMonths.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    isMonthLoaded = reorderedMonths[position]
                    binding.lytNoRecordFound.visibility = View.GONE
                    selectedMonthNumber = String.format("%02d", months.indexOf(isMonthLoaded) + 1)
                    isGetStaffList()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }


//    private fun isLoadYear(yearList: List<AcademicYear>) {
//        val uniqueYears = yearList.mapNotNull { it.year.split("-").firstOrNull() }.distinct()
//        val currentYear =
//            yearList.find { it.current_academic_year }?.year?.split("-")?.firstOrNull()
//        val sortedYears = if (currentYear != null) {
//            listOf(currentYear) + uniqueYears.filter { it != currentYear }
//        } else {
//            uniqueYears
//        }
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortedYears)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.spinnerYears.adapter = adapter
//
//
//        binding.spinnerYears.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                binding.lytNoRecordFound.visibility = View.GONE
//                isSelectedYear = parent.getItemAtPosition(position).toString()
//                isLoadMonth()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//    }


//    fun isLoadMonth() {
//        val months = listOf(
//            "January", "February", "March", "April", "May", "June",
//            "July", "August", "September", "October", "November", "December"
//        )
//
//        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
//        val currentMonthName = months[currentMonthIndex]
//
//        val updatedMonths: List<String>
//        val selectedIndex: Int
//
//        if (isLoadingFirstTime) {
//            updatedMonths = months
//            selectedIndex = currentMonthIndex
//            isLoadingFirstTime = false
//        } else if (isMonthLoaded.isNotEmpty()) {
//            updatedMonths = listOf(isMonthLoaded) + months.filter { it != isMonthLoaded }
//            selectedIndex = 0
//        } else {
//            updatedMonths = months
//            selectedIndex = currentMonthIndex
//        }
//
//        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, updatedMonths)
//        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.spinnerMonths.adapter = monthAdapter
//        binding.spinnerMonths.setSelection(selectedIndex)
//
//        binding.spinnerMonths.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                isMonthLoaded = updatedMonths[position]
//                binding.lytNoRecordFound.visibility = View.GONE
//                selectedMonthNumber = String.format("%02d", months.indexOf(isMonthLoaded) + 1)
//                isGetStaffList()
//
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isBackgroundChange(btnClick: TextView) {
        binding.btnCreate.background = null
        binding.btnHistory.background = null
        binding.lytNoRecordFound.visibility = View.GONE
        binding.lnrParent.setBackgroundResource(R.drawable.bg_light_blue)
        btnClick.setBackgroundResource(R.drawable.white_bg_radius)

        if (btnClick == binding.btnCreate) {
            binding.lblSelectStaff.visibility = View.GONE
            binding.lnrSpinners.visibility = View.GONE
            binding.rlaStaff.visibility = View.GONE
            getStaffAttendanceReport(Constant.getCurrentDate(), "", "")
        }

        if (btnClick == binding.btnHistory) {
            binding.lblSelectStaff.visibility = View.VISIBLE
            binding.lnrSpinners.visibility = View.VISIBLE
            binding.rlaStaff.visibility = View.VISIBLE
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
            lblNoRecordsFound!!.text = getString(R.string.Punch_History_found)
            lblNoRecordsFound!!.visibility = View.VISIBLE
        }
    }

    private fun isLoadStaff(isGetStaffListData: List<NameAndIds>?) {
        val adapter = SubjectLoadAdapter(this, isGetStaffListData)
        binding.spinnerStaffList.adapter = adapter
        binding.spinnerStaffList.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    isGetStaffListData!![position]
                    isStaffId = isGetStaffListData!!.get(position).id
                    getStaffAttendanceReport("", isSelectedYear!!, selectedMonthNumber!!)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnHistory -> {
                binding.btnHistory.isEnabled = false
                binding.btnCreate.isEnabled = true
                isLoadYear(Constant.isAcademicYearList)
                isTodayList = false
                isBackgroundChange(binding.btnHistory)
            }

            R.id.btnCreate -> {
                binding.btnHistory.isEnabled = true
                binding.btnCreate.isEnabled = false
                isTodayList = true
                isBackgroundChange(binding.btnCreate)
            }
        }
    }

    private fun isGetStaffList() {
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
        binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
        isStaffAttendanceReportAdapter =
            StaffAttendanceReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.recycleAttendanceReportsToday.layoutManager = LinearLayoutManager(this)
        binding.recycleAttendanceReportsToday.adapter = isStaffAttendanceReportAdapter


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

    private fun isPunchHistory(data: StaffAttendanceReportData) {
        isAccessToken?.let {
            appViewModel?.getPunchHistory(it, data.date, data.staff_id, this)
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