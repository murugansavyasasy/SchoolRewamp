package com.vs.schoolmessenger.School.ApproveStaffLeaveRequest

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Adapter.PreviewStaffLeaveRequest.StaffLeaveHistory
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveHistory.GetStaffLeaveHistory
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PreviewStaffLeaveRequestBinding

class PreviewStaffLeaveRequest : BaseActivity<PreviewStaffLeaveRequestBinding>(),
    View.OnClickListener{

    override fun getViewBinding(): PreviewStaffLeaveRequestBinding {
        return PreviewStaffLeaveRequestBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: StaffLeaveHistory
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var staffLeaveHistory: List<GetStaffLeaveHistory>? = null
    private var userDetails: UserDetails? = null
    private var isSubjectName=""
    private var isStaffName=""



    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        isSubjectName = intent.getStringExtra(Constant.isStaffSubjectName).toString()
        isStaffName = intent.getStringExtra(Constant.isStaffName).toString()

        binding.toolbarLayout.lblStaffInitial.text = Constant.getInitials(isStaffName)
        binding.toolbarLayout.lblName.text =isStaffName
        binding.toolbarLayout.lblSubject.text =isSubjectName

        userDetails = SharedPreference.getUserDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isGetLeaveRequestList()

        binding.toolbarLayout.imgBack.setOnClickListener(this)

        appViewModel?.getleaverequest?.observe(this) {

            Constant.hideLoading(this)

            // Always load dummy data (Ignore API response completely)
            val dummyData = getDummyMonthWiseData()

            if (dummyData.isNotEmpty()) {

                binding.rcyStaffLeaveHistory.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE

                staffLeaveHistory = dummyData
                isloadleaverequestData(dummyData)

            } else {
                binding.rcyStaffLeaveHistory.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = getString(R.string.no_data_found)
            }
        }

    }

    private fun getDummyMonthWiseData(): List<GetStaffLeaveHistory> {

        return listOf(
            GetStaffLeaveHistory(
                id = "201",
                applied_on = "07-02-2026",
                staff_name = "Rahul Das",
                class_name = "8",
                section_name = "C",
                leave_from = "07-02-2026",
                leave_to = "08-02-2026",
                no_of_days = "2",
                reason = "The student needs to undergo a scheduled medical treatment and follow-up consultation as prescribed by the doctor. Due to the treatment and recovery time, attending school will not be possible.",
                status = Constant.rejected,
                updated_on = "06 Feb 2026",
                from_session = "FN",
                to_session = "AN",
                approved_by = "Vice Principal",
                leave_type = "Sick",
                leave_type_id = 3
            ),
            GetStaffLeaveHistory(
                id = "202",
                applied_on = "18-02-2026",
                staff_name = "Sneha Reddy",
                class_name = "7",
                section_name = "A",
                leave_from = "18-02-2026",
                leave_to = "19-02-2026",
                no_of_days = "2",
                reason = "The family will be travelling out of station due to personal commitments and unavoidable circumstances. The student will not be able to attend classes during the mentioned dates. Kindly approve the leave request.",
                status = Constant.waiting_for_approval,
                updated_on = "",
                from_session = "Full Day",
                to_session = "Full Day",
                approved_by = "",
                leave_type = "Casual",
                leave_type_id = 2
            ),
            GetStaffLeaveHistory(
                id = "101",
                applied_on = "02-01-2026",
                staff_name = "Arun Kumar",
                class_name = "10",
                section_name = "A",
                leave_from = "05-01-2026",
                leave_to = "06-01- 2026",
                no_of_days = "2",
                reason = "Student is suffering from high fever and viral infection. Doctor has advised complete bed rest for at least two days to recover properly. Kindly grant leave for the mentioned dates.",
                status = Constant.approved,
                updated_on = "03 Jan 2026",
                from_session = "FN",
                to_session = "AN",
                approved_by = "Principal",
                leave_type = "Sick",
                leave_type_id = 1
            ),
            GetStaffLeaveHistory(
                id = "102",
                applied_on = "10-01-2026",
                staff_name = "Priya Sharma",
                class_name = "9",
                section_name = "B",
                leave_from = "12-01-2026",
                leave_to = "12-01-2026",
                no_of_days = "1",
                reason = "We have an important family function and traditional ceremony at our hometown which requires the student's presence throughout the day. Hence requesting leave for the above mentioned date.",
                status = Constant.waiting_for_approval,
                updated_on = "",
                from_session = "Full Day",
                to_session = "Full Day",
                approved_by = "",
                leave_type = "Casual",
                leave_type_id = 2
            )
        )

    }



    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()

        }
    }


    private fun isloadleaverequestData(newData: List<GetStaffLeaveHistory>?) {
        mAdapter = StaffLeaveHistory(newData, this, Constant.isShimmerViewDisable)
        binding.rcyStaffLeaveHistory.adapter = mAdapter
    }

    private fun isGetLeaveRequestList() {
        Constant.showLoading(this)
        mAdapter = StaffLeaveHistory(null, this, Constant.isShimmerViewDisable)
        binding.rcyStaffLeaveHistory.layoutManager = LinearLayoutManager(this)
        binding.rcyStaffLeaveHistory.isNestedScrollingEnabled = false
        binding.rcyStaffLeaveHistory.adapter = mAdapter
        appViewModel!!.getleaverequest(
            isAccessToken!!, Constant.STAFF__, this
        )
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }
}