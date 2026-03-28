package com.vs.schoolmessenger.School.AttendanceReportFromStaff

import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SubjectLoadAdapter.SubjectLoadAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.PunchHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchTimingsData
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportData
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.AttendanceReportClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AttendancereportFromStaffBinding
import com.vs.schoolmessenger.databinding.StaffAttendanceReportBinding

class AttendanceReportFromStaff : BaseActivity<AttendancereportFromStaffBinding>(),
    View.OnClickListener {

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


    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        isGetStaffList()

        appViewModel!!.isGioStaffWiseAttendanceReportList?.observe(this) { response ->

            if (response != null && response.status) {

                binding.recycleAttendanceReportsToday.visibility = View.VISIBLE
                binding.lytNoRecordFound.visibility = View.GONE

                val overall = response.data[0].overall_stat

                binding.tvPresent.text = "${overall.present}\nPresent"
                binding.tvAbsent.text = "${overall.absent}\nAbsent"
                binding.tvNotMarked.text = "${overall.not_marked}\nNot Marked"

                val finalList = ArrayList<Pair<String, DateAttendanceDataClass>>()

                response.data.forEach { mainData ->
                    mainData.all_attd.forEach { (date, value) ->
                        finalList.add(Pair(date, value))
                    }
                }

                isLoadData(finalList)

            } else {
                binding.recycleAttendanceReportsToday.visibility = View.GONE
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


    private fun isLoadData(list: List<Pair<String, DateAttendanceDataClass>>) {

        val adapter = AttendanceReportFromStaffAdapter(
            list, this, false
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


                    var isAllStaff = true

                    if (position == 0) {
                        isStaffId = ""
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

        }
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
            AttendanceReportFromStaffAdapter(null, this, Constant.isShimmerViewShow)
        binding.recycleAttendanceReportsToday.layoutManager = LinearLayoutManager(this)
        binding.recycleAttendanceReportsToday.adapter = isAttendanceReportFromStaffAdapter

        isAccessToken?.let {
            appViewModel?.getGioMetricAttendanceReport(
                it, "01-01-2025", "28-03-2026", isAllStaff, isStaffId!!, this
            )
        }
    }

    private fun isLocationHistory(data: StaffAttendanceReportData) {
        Log.d("isComing", "isComing")
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
}