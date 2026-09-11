package com.vs.schoolmessenger.School.ApproveStaffLeaveRequest

import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Adapter.PreviewStaffLeaveRequest.StaffLeaveHistory
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory.StaffLeaveData
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveApproveRequest
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PreviewStaffLeaveRequestBinding

class PreviewStaffLeaveRequest : BaseActivity<PreviewStaffLeaveRequestBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): PreviewStaffLeaveRequestBinding {
        return PreviewStaffLeaveRequestBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: StaffLeaveHistory
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var isRoleName = ""
    private var isStaffName = ""
    private var isPripority = ""
    lateinit var request: LeaveApproveRequest


    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()


        isRoleName = Constant.isStaffLeaveHistoryData?.role ?: ""
        isStaffName = Constant.isStaffLeaveHistoryData?.staff_name ?: ""
        isPripority = Constant.isStaffLeaveHistoryData?.priority_level?.lowercase() ?: ""

        if (isPripority=="p2"){
            binding.constStatus.visibility= View.GONE
        }
        else{
            binding.constStatus.visibility= View.VISIBLE
        }

        binding.btnCancel.setOnClickListener {
            isApproveReject(false)
        }
        binding.btnApprove.setOnClickListener {
            isApproveReject(true)
        }

        if (Constant.isStaffLeaveHistoryData?.status == Constant.waiting_for_approval){
           binding.rlaCurrentLeaveRequest.visibility= View.VISIBLE
        }
        else{
            binding.rlaCurrentLeaveRequest.visibility= View.GONE
        }


        binding.lblLeaveType.text= Constant.isStaffLeaveHistoryData?.leave_type ?: ""
        binding.lblDays.text= Constant.isStaffLeaveHistoryData?.no_of_days ?: ""
        binding.lblStartDate.text= Constant.convertDateTimeFormatDateMonth(Constant.isStaffLeaveHistoryData?.from_date ?: "")
        binding.lblEndDate.text= Constant.convertDateTimeFormatDateMonth(Constant.isStaffLeaveHistoryData?.to_date ?: "")
        binding.Reason.text= Constant.convertDateTimeFormatDateMonth(Constant.isStaffLeaveHistoryData?.reason ?: "")
        binding.lbltxtDays.text=
            if ((Constant.isStaffLeaveHistoryData?.no_of_days ?: "") == Constant.one) getString(R.string.Day) else getString(
                R.string.days
            )

        binding.toolbarLayout.lblStaffInitial.text = Constant.getInitials(isStaffName)
        binding.toolbarLayout.lblName.text = isStaffName
        binding.toolbarLayout.lblSubject.text = isRoleName

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        appViewModel!!.isstaffleaverequestapprove?.observe(this) { response ->
            Constant.hideLoading(this@PreviewStaffLeaveRequest)

            if (response != null && response.status) {
                binding.rlaCurrentLeaveRequest.visibility = View.GONE
                Constant.showDataValidationNoDashboardRedirect(
                    getString(R.string.success),
                    response.message,
                    this
                )

            } else {
                binding.rlaCurrentLeaveRequest.visibility = View.VISIBLE
                Constant.showDataValidation(
                    getString(R.string.fail),
                    response?.message ?: getString(R.string.Something_went_wrong_Please_try_again),
                    this
                )
            }
        }
        isGetLeaveRequestList()

        binding.lblPhoneNumber.text = Constant.isStaffLeaveHistoryData?.mobile_no ?: ""
        binding.lblEmail.text = Constant.isStaffLeaveHistoryData?.email ?: ""
        binding.lblAddress.text = Constant.isStaffLeaveHistoryData?.address ?: ""

        binding.rlaEntireCall.setOnClickListener {
            if (Constant.isStaffLeaveHistoryData?.mobile_no.isNullOrEmpty() || Constant.isStaffLeaveHistoryData?.mobile_no.toString() == "--") {
                Toast.makeText(this, "Invaild Mobile Number", Toast.LENGTH_SHORT).show()
            } else {
                Constant.redirectToDialPad(
                    this,
                    Constant.isStaffLeaveHistoryData?.mobile_no.toString()
                )

            }
        }

        binding.rlaEntireMail.setOnClickListener {
            if (Constant.isStaffLeaveHistoryData?.email.isNullOrEmpty() || Constant.isStaffLeaveHistoryData?.email.toString() == "--") {
                Toast.makeText(this, "Invaild Mail", Toast.LENGTH_SHORT).show()
            } else {
                Constant.redirectToMail(
                    this,
                    Constant.isStaffLeaveHistoryData?.email.toString(),
                    "",
                    ""
                )
            }
        }

        binding.toolbarLayout.imgBack.setOnClickListener(this)

        appViewModel?.getStaffleaverequesthistory?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response?.status == true && !response.data.isNullOrEmpty()) {
                    binding.rcyStaffLeaveHistory.visibility = View.VISIBLE
                    binding.nomessage.visibility = View.GONE
                    binding.txtNoData.visibility = View.GONE
                    val staff_leaveList: List<StaffLeaveData> =
                        response.data?.flatMap { it.details } ?: emptyList()
                    isloadleaverequestData(staff_leaveList)

                } else {
                    binding.rcyStaffLeaveHistory.visibility = View.GONE
                    binding.nomessage.visibility = View.VISIBLE
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
                }
            }
        }

    }

    fun isApproveReject(isButtonClick: Boolean) {
        var isMessage = ""
        if (isButtonClick) {
            request =
                LeaveApproveRequest(Constant.isStaffLeaveHistoryData?.id ?: "", is_approve = true)
            isMessage = getString(R.string.Are_you_sure_you_want_to_approve_this_request)
        } else {
            request =
                LeaveApproveRequest(Constant.isStaffLeaveHistoryData?.id ?: "", is_approve = false)
            isMessage = getString(R.string.Are_you_sure_you_want_to_reject_this_request)
        }
        Constant.showSendConfirmationDialog(
            this,
            getString(R.string.confirmation),
            getString(R.string.permission_ok),
            getString(R.string.Cancel),
            "",
            isMessage
        ) { confirmed ->
            if (confirmed) {
                Constant.showLoading(this)
                appViewModel?.isStaffleaverequestapprove(isAccessToken!!, request, this)
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()

        }
    }

    private fun isloadleaverequestData(newData: List<StaffLeaveData>?) {
        mAdapter = StaffLeaveHistory(newData, this, Constant.isShimmerViewDisable)
        binding.rcyStaffLeaveHistory.adapter = mAdapter
    }

    private fun isGetLeaveRequestList() {
        Constant.showLoading(this)
        mAdapter = StaffLeaveHistory(null, this, Constant.isShimmerViewShow)
        binding.rcyStaffLeaveHistory.layoutManager = LinearLayoutManager(this)
        binding.rcyStaffLeaveHistory.isNestedScrollingEnabled = false
        binding.rcyStaffLeaveHistory.adapter = mAdapter
        appViewModel!!.getStaffleaverequest(
            isAccessToken!!, Constant.isStaffLeaveHistoryData?.staff_id ?: "", this
        )
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }
}