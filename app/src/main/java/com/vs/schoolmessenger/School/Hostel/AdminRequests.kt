package com.vs.schoolmessenger.School.Hostel


import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Adapter.AttendanceHistory.AttendanceHistoryAdapter

import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getAttendanceHistoryData

import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AdminRequestsBinding



class AdminRequests : BaseActivity<AdminRequestsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): AdminRequestsBinding {
        return AdminRequestsBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: AttendanceHistoryAdapter



    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()


        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        appViewModel?.getleaverequest?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    // Always load dummy data (Ignore API response completely)
                    val dummyData = getDummyFloorWiseRoomAvailabilityData()
                    if (dummyData.isNotEmpty()) {
                        binding.rcAdminRequest.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE

                        isLoadAttendanceHistory(dummyData)

                    } else {
                        binding.rcAdminRequest.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text = getString(R.string.no_data_found)
                    }
                } else {
                    binding.rcAdminRequest.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message
                }
            } else {
                binding.rcAdminRequest.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblErrorMessage.text = getString(R.string.Something_went_wrong_Please_try_again)
            }
        }
        isGetAttendanceHistory()





    }

    private fun isLoadAttendanceHistory(newData: List<getAttendanceHistoryData>?) {
        mAdapter = AttendanceHistoryAdapter(newData, this,Constant.isShimmerViewDisable)
        binding.rcAdminRequest.adapter = mAdapter
    }


    private fun isGetAttendanceHistory() {
//        Constant.showLoading(this)
        mAdapter = AttendanceHistoryAdapter(null,this, Constant.isShimmerViewDisable)
        binding.rcAdminRequest.layoutManager = LinearLayoutManager(this)
        binding.rcAdminRequest.isNestedScrollingEnabled = false
        binding.rcAdminRequest.adapter = mAdapter
//        appViewModel!!.getleaverequest(
//            isAccessToken!!, Constant.STAFF__, this
//        )

        val dummyData = getDummyFloorWiseRoomAvailabilityData()
        isLoadAttendanceHistory(dummyData)
    }

    private fun getDummyFloorWiseRoomAvailabilityData(): List<getAttendanceHistoryData> {

        val list = ArrayList<getAttendanceHistoryData>()

        list.add(
            getAttendanceHistoryData(
                date = "Tuesday, Mar 3",
                year = "2026",
                attendancePercentage = 91,
                totalStudents = 23,
                presentStudents = 21,
                absentStudents = 2,
                roomsMarked = 6,
                totalRooms = 8
            )
        )

        list.add(
            getAttendanceHistoryData(
                date = "Wednesday, Mar 4",
                year = "2026",
                attendancePercentage = 88,
                totalStudents = 25,
                presentStudents = 22,
                absentStudents = 3,
                roomsMarked = 5,
                totalRooms = 8
            )
        )

        list.add(
            getAttendanceHistoryData(
                date = "Thursday, Mar 5",
                year = "2026",
                attendancePercentage = 95,
                totalStudents = 20,
                presentStudents = 19,
                absentStudents = 1,
                roomsMarked = 8,
                totalRooms = 8
            )
        )

        return list
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

}