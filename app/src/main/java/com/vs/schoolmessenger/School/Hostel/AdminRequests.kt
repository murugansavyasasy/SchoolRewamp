package com.vs.schoolmessenger.School.Hostel


import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Adapter.AdminRequests.StatusWiseAdminRequest
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.AdminRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.StatusWiseAdminRequestData
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
    lateinit var mAdapter: StatusWiseAdminRequest



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
                    val dummyData = getDummyLeaveRequestData()
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

    private fun isLoadAttendanceHistory(newData: List<StatusWiseAdminRequestData>?) {
        binding.rcAdminRequest.visibility = View.VISIBLE
        mAdapter = StatusWiseAdminRequest(
            newData, this, Constant.isShimmerViewDisable
        )
        binding.rcAdminRequest.adapter = mAdapter
    }


    private fun isGetAttendanceHistory() {
//        Constant.showLoading(this)
        mAdapter = StatusWiseAdminRequest(
            null, this, Constant.isShimmerViewDisable
        )
        binding.rcAdminRequest.layoutManager = LinearLayoutManager(this)
        binding.rcAdminRequest.isNestedScrollingEnabled = false
        binding.rcAdminRequest.adapter = mAdapter

//        appViewModel!!.getleaverequest(
//            isAccessToken!!, Constant.STAFF__, this
//        )

        val dummyData = getDummyLeaveRequestData()
        isLoadAttendanceHistory(dummyData)
    }

    private fun getDummyLeaveRequestData(): List<StatusWiseAdminRequestData> {

        val leaveList = ArrayList<StatusWiseAdminRequestData>()

        val pending = listOf(

            AdminRequestWiseData(
                roomNumber = "101",
                roomTitle = "Room 101",
                studentName = "Aarav Sharma",
                issueDescription = "Tap not working in bathroom",
                dateTime = "Mar 4, 10:30 AM",
                status = "Pending"
            ),

            AdminRequestWiseData(
                roomNumber = "102",
                roomTitle = "Room 102",
                studentName = "Rohit",
                issueDescription = "Tap not working in bathroom",
                dateTime = "Mar 5, 10:00 AM",
                status = "Pending"
            )
        )

        val approved = listOf(

            AdminRequestWiseData(
                roomNumber = "101",
                roomTitle = "Room 101",
                studentName = "Aarav Sharma",
                issueDescription = "Tap not working in bathroom",
                dateTime = "Apr 4, 10:30 AM",
                status = "approved"
            ),

            AdminRequestWiseData(
                roomNumber = "102",
                roomTitle = "Room 102",
                studentName = "Rohit",
                issueDescription = "Tap not working in bathroom",
                dateTime = "Apr 5, 10:00 AM",
                status = "approved"
            )
        )

        val rejected = listOf(

            AdminRequestWiseData(
                roomNumber = "101",
                roomTitle = "Room 101",
                studentName = "Sharma",
                issueDescription = "Need to improve the food quality ",
                dateTime = "Apr 4, 10:30 AM",
                status = "rejected"
            ),

            AdminRequestWiseData(
                roomNumber = "102",
                roomTitle = "Room 102",
                studentName = "Rohit Kohli",
                issueDescription = "Need to have TV in the mess",
                dateTime = "Apr 5, 10:00 AM",
                status = "rejected"
            )
        )

        leaveList.add(
            StatusWiseAdminRequestData(
                Status = "Pending(2)",
                StatusWiseData = pending
            )
        )
        leaveList.add(
            StatusWiseAdminRequestData(
                Status = "approved(2)",
                StatusWiseData = approved
            )
        )
        leaveList.add(
            StatusWiseAdminRequestData(
                Status = "rejected(2)",
                StatusWiseData = rejected
            )
        )

        return leaveList
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

}