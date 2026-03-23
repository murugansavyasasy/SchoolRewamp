package com.vs.schoolmessenger.Parent.Hostel

import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelDetailedAttendance.HostelDetailedAttendanceAdpater
import com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelInformation.HostelInfoAdapter
import com.vs.schoolmessenger.Parent.Hostel.Adapter.OutpassRequestList.OutpassRequestList
import com.vs.schoolmessenger.Parent.Hostel.Model.DetailedAttendanceRecords.DayAttendance
import com.vs.schoolmessenger.Parent.Hostel.Model.DetailedAttendanceRecords.getHostelDetailedAttendance
import com.vs.schoolmessenger.Parent.Hostel.Model.OutpassRequestList.OutpassRequestData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.selctedHotelDetails
import com.vs.schoolmessenger.School.Hostel.SchoolHostelDashboard
import com.vs.schoolmessenger.Utils.Constant

import com.vs.schoolmessenger.Utils.SharedPreference

import com.vs.schoolmessenger.databinding.ParentHostelDashboardBinding


class ParentHostelDashboard : BaseActivity<ParentHostelDashboardBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): ParentHostelDashboardBinding {
        return ParentHostelDashboardBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private lateinit var mAdapter: HostelDetailedAttendanceAdpater
    lateinit var nAdapter: OutpassRequestList
    lateinit var oAdapter: HostelInfoAdapter



    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )


        binding.toolbarLayout.lblInitialName.visibility = View.VISIBLE
        binding.toolbarLayout.imgCall.visibility = View.VISIBLE
        binding.toolbarLayout.lblClassAndRoomDetails.visibility = View.VISIBLE
        binding.toolbarLayout.lblHostelName.visibility = View.VISIBLE
        binding.toolbarLayout.lblName.visibility = View.VISIBLE

        binding.toolbarLayout.lblToday.visibility = View.GONE
        binding.toolbarLayout.lblDate.visibility = View.GONE
        binding.toolbarLayout.rlaSpinner.visibility = View.GONE

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblSeeMore.setOnClickListener(this)
        binding.lblApplyNewOutpassRequest.setOnClickListener(this)

        isGetOutpassRequest()

//        appViewModel?.isLeaveRequest?.observe(this) { response ->
//
//            if (response != null) {
//
//                if (response.status) {
//
//                    if (response.data != null && response.data.days.isNotEmpty()) {
//
//                        binding.recyclerMain.visibility = View.VISIBLE
//                        binding.lblErrorMessage.visibility = View.GONE
//                        binding.imgNoDataFound.visibility = View.GONE
//
//                        //  Setup Header
//                        setupHeader(response.data.sessions)
//
//                        //  Load Data
//                        isLoadAttendance(response.data.days)
//
//                    } else {
//                        showNoData(getString(R.string.no_data_found))
//                    }
//
//                } else {
//                    showNoData(response.message)
//                }
//
//            } else {
//                showNoData(getString(R.string.Something_went_wrong_Please_try_again))
//            }
//        }


//        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
//        binding.toolbarLayout.lblStudentSection.text =
//            isChildDetails.standard_name + " - " + isChildDetails.section_name

        isGetAttendance()
        isGetHotelInformation()

    }


//    private fun showNoData(message: String) {
//        binding.rcDetailedAttendanceRecords.visibility = View.GONE
//        binding.lblErrorMessage.visibility = View.VISIBLE
//        binding.imgNoDataFound.visibility = View.VISIBLE
//        binding.lblErrorMessage.text = message
//    }

    private fun isLoadOutpassRequest(newData: List<OutpassRequestData>) {

        if (newData.size > 3) {
            binding.lblSeeMore.visibility = View.VISIBLE
        } else {
            binding.lblSeeMore.visibility = View.GONE
        }

        // Show only first 3 items if size > 3
        val displayList = if (newData.size > 3) { newData.take(3) }
        else { newData }

        binding.rcOutpassRequest.visibility = View.VISIBLE

        nAdapter = OutpassRequestList(
            displayList,
            this,
            Constant.isShimmerViewDisable
        )

        binding.rcOutpassRequest.adapter = nAdapter
    }

    private fun isGetOutpassRequest() {
        nAdapter = OutpassRequestList(null, this, Constant.isShimmerViewShow)
        binding.rcOutpassRequest.layoutManager = LinearLayoutManager(this)
        binding.rcOutpassRequest.isNestedScrollingEnabled = true
        binding.rcOutpassRequest.adapter = nAdapter
        val dummyData = getDummyOutpassRequestListData()
        isLoadOutpassRequest(dummyData)
    }




    private fun getDummyHostelInfoList(): List<String> {
        return listOf(
            "Warden Name: Saranraj",
            "Address: 3/18 Kaver Street",
            "Contact: 9876543210",
            "Capacity: 50 Beds",
            "Mess Timing: 07:00 AM - 09:00 PM"
        )
    }

    private fun isLoadHotelInformation(Data: List<String>)
    {
        oAdapter = HostelInfoAdapter(Data, this, Constant.isShimmerViewDisable)
        binding.rcHostelInformation.adapter = oAdapter
    }


    private fun isGetHotelInformation()
    {
        oAdapter = HostelInfoAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcHostelInformation.layoutManager = LinearLayoutManager(this)
        binding.rcHostelInformation.adapter = oAdapter

        val dummyData=getDummyHostelInfoList()
        isLoadHotelInformation(dummyData)
    }



    private fun isGetAttendance() {

        mAdapter = HostelDetailedAttendanceAdpater(
            null,
            this,
            true,
            binding.headerScroll
        )

        binding.rcDetailedAttendanceRecords.layoutManager = LinearLayoutManager(this)
        binding.rcDetailedAttendanceRecords.adapter = mAdapter

        // 🔥 Header scroll sync
        binding.headerScroll.viewTreeObserver.addOnScrollChangedListener {
            mAdapter.syncScroll(binding.headerScroll.scrollX)
        }

        val dummyData = getDummyApiResponse()

        setupHeader(dummyData.sessions)
        isLoadAttendance(dummyData.days)
    }

    private fun isLoadAttendance(list: List<DayAttendance>) {

        mAdapter = HostelDetailedAttendanceAdpater(
            list,
            this,
            false,
            binding.headerScroll
        )

        binding.rcDetailedAttendanceRecords.layoutManager = LinearLayoutManager(this)
        binding.rcDetailedAttendanceRecords.adapter = mAdapter
    }

    private fun setupHeader(sessions: List<String>) {

        val columnWidth = AttendanceUIConfig.columnWidth(this)
        val paddingH = AttendanceUIConfig.paddingH(this)
        val paddingV = AttendanceUIConfig.paddingV(this)

        binding.headerContainer.removeAllViews()

        sessions.forEach {

            val tv = TextView(this)
            tv.layoutParams = LinearLayout.LayoutParams(
                columnWidth,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tv.setPadding(paddingH, paddingV, paddingH, paddingV)
            tv.text = it
            tv.gravity = Gravity.CENTER
            tv.setTextAppearance(this, R.style.CustomTextStylePoppinsBold)

            binding.headerContainer.addView(tv)
        }
    }

    private fun getDummyOutpassRequestListData(): List<OutpassRequestData> {
        val list = ArrayList<OutpassRequestData>()

        list.add(
            OutpassRequestData(
                reason = "I am sick",
                fromDateToDate = "11 Jan 2020 - 13 Jan 2020",
                requestTime = "11 Jan 2020 at 04:00 AM",
                status = Constant.waiting_for_approval
            )
        )
        list.add(
            OutpassRequestData(
                reason = "Family function",
                fromDateToDate = "14 Jan 2020 - 16 Jan 2020",
                requestTime = "14 Jan 2020 at 09:30 AM",
                status = "Approved"
            )
        )
        list.add(
            OutpassRequestData(
                reason = "Medical checkup",
                fromDateToDate = "17 Jan 2020 - 17 Jan 2020",
                requestTime = "17 Jan 2020 at 11:00 AM",
                status = "Rejected"
            )
        )
        list.add(
            OutpassRequestData(
                reason = "Personal work",
                fromDateToDate = "18 Jan 2020 - 19 Jan 2020",
                requestTime = "18 Jan 2020 at 02:15 PM",
                status = Constant.waiting_for_approval
            )
        )
        list.add(
            OutpassRequestData(
                reason = "Festival leave",
                fromDateToDate = "20 Jan 2020 - 23 Jan 2020",
                requestTime = "20 Jan 2020 at 06:45 AM",
                status = "Approved"
            )
        )
        list.add(
            OutpassRequestData(
                reason = "Emergency",
                fromDateToDate = "24 Jan 2020 - 25 Jan 2020",
                requestTime = "24 Jan 2020 at 01:00 AM",
                status = Constant.waiting_for_approval
            )
        )
        list.add(
            OutpassRequestData(
                reason = "Vacation",
                fromDateToDate = "26 Jan 2020 - 28 Jan 2020",
                requestTime = "26 Jan 2020 at 10:30 AM",
                status = "Rejected"
            )
        )
        list.add(
            OutpassRequestData(
                reason = "Exam preparation",
                fromDateToDate = "29 Jan 2020 - 31 Jan 2020",
                requestTime = "29 Jan 2020 at 08:00 AM",
                status = Constant.waiting_for_approval
            )
        )
        list.add(
            OutpassRequestData(
                reason = "Marriage function",
                fromDateToDate = "01 Feb 2020 - 03 Feb 2020",
                requestTime = "01 Feb 2020 at 05:20 PM",
                status = "Approved"
            )
        )
        list.add(
            OutpassRequestData(
                reason = "Health issue",
                fromDateToDate = "04 Feb 2020 - 05 Feb 2020",
                requestTime = "04 Feb 2020 at 07:10 AM",
                status = Constant.waiting_for_approval
            )
        )

        return list
    }
    fun getDummyApiResponse(): getHostelDetailedAttendance {

        val sessions = listOf(
            "1st Hour",
            "Morining Attendance",
            "3st Hour",
            "4st Hour",
            "Morining Attendance",
            "Morining Attendance",
            "7st Hour",
            "8st Hour",
            "9st Hour"
        )

        val statusOptions = listOf("Present", "Absent", "Not Taken")

        val days = ArrayList<DayAttendance>()

        for (i in 1..30) {

            val status = sessions.map {
                statusOptions.random()
            }

            days.add(
                DayAttendance(
                    dayLabel = "Day $i",
                    status = status
                )
            )
        }

        return getHostelDetailedAttendance(
            sessions = sessions,
            days = days
        )
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.lblSeeMore->{
                val intent = Intent(this, ParentOutpassrequestList::class.java)
                startActivity(intent)
            }
            R.id.lblApplyNewOutpassRequest->{
                val intent = Intent(this, ParentHostelOutpassApply::class.java)
                startActivity(intent)
            }

        }
    }

}