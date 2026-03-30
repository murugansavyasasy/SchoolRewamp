package com.vs.schoolmessenger.School.Hostel.Fragement

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Adapter.AdminRequests.StatusWiseAdminRequest
import com.vs.schoolmessenger.School.Hostel.Adapter.OutpassRequest.StatusWiseOutpassRequest
import com.vs.schoolmessenger.School.Hostel.BottomSheet
import com.vs.schoolmessenger.School.Hostel.Listner.OutpassRequestClickListner
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.AdminRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.StatusWiseAdminRequestData
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.OutpassRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.StatusWiseOutpassRequestData
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelRoomAttendanceStudentList.RoomStudentAttendanceData
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveApproveRequest
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AdminRequestsBinding
import com.vs.schoolmessenger.databinding.OutpassRequestFragmentBinding
import java.util.Calendar


class OutPassRequestFragment : Fragment(), OutpassRequestClickListner,View.OnClickListener{

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: StatusWiseOutpassRequest
    private var appViewModel: App? = null
    private var _binding: OutpassRequestFragmentBinding? = null
    private val binding get() = _binding!!
    private var currentYear: Int = 0
    private var currentMonth: Int = 0

    lateinit var request: LeaveApproveRequest
    var isApproveRejectId = ""
    var isApprovedOrRejectedSuccessful = false
    private var pendingApprovalCallback: ((Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = OutpassRequestFragmentBinding.inflate(inflater, container, false)
        _binding?.imgClose?.setOnClickListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        isStaffDetails = SharedPreference.getStaffDetails(requireContext())
        isAccessToken = isStaffDetails?.access_token

        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1

        binding.lblPendingRequest.text = Constant.isHostelName?:""





        appViewModel?.hotelSchoolOutpassRequest?.observe(viewLifecycleOwner) { response ->

            if (response != null) {

                if (response.status) {
                    if (response.data.isNotEmpty()) {

                        binding.rcOutpassRequest.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE

                        isLoadAttendanceHistory(response.data)

                    } else {

                        binding.rcOutpassRequest.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text = getString(R.string.no_data_found)


                    }

                } else {

                    binding.rcOutpassRequest.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message


                }

            } else {

                binding.rcOutpassRequest.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        appViewModel!!.schoolHostelOutpassUpdateStatus?.observe(requireActivity()) { response ->
            Constant.hideLoadingAny(requireView())

            if (response != null && response.status) {
                isApprovedOrRejectedSuccessful = true
                pendingApprovalCallback?.invoke(true)
                pendingApprovalCallback = null
                Constant.showDataValidationNoDashboardRedirect(
                    getString(R.string.success),
                    response.message,
                    requireActivity()
                )
                isGetAttendanceHistory()

            } else {
                isApprovedOrRejectedSuccessful = false
                pendingApprovalCallback?.invoke(false)
                pendingApprovalCallback = null
                Constant.showDataValidation(
                    getString(R.string.fail),
                    response?.message ?: getString(R.string.Something_went_wrong_Please_try_again),
                    requireActivity()
                )
            }
        }

        isGetAttendanceHistory()
    }
    private fun isLoadAttendanceHistory(newData: List<StatusWiseOutpassRequestData>?) {
        binding.rcOutpassRequest.visibility = View.VISIBLE
        mAdapter = StatusWiseOutpassRequest(
            newData, requireActivity(),this, Constant.isShimmerViewDisable
        )
        binding.rcOutpassRequest.adapter = mAdapter
    }

    private fun isGetAttendanceHistory() {
        mAdapter = StatusWiseOutpassRequest(
            null, requireActivity(),this,Constant.isShimmerViewShow
        )
        binding.rcOutpassRequest.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcOutpassRequest.isNestedScrollingEnabled = false
        binding.rcOutpassRequest.adapter = mAdapter

        appViewModel!!.isGetHostelSchoolOutpassRequestList(isAccessToken!!,currentYear.toString(),currentMonth.toString(),Constant.isSelectedHostelFromHostelListData?.id.toString(),Constant.isSelectedAcademicYear?:"",requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onApproveClicked(
        data: OutpassRequestWiseData,
        position: Int,
        isButtonClick: Boolean,
        resultCallback: (Boolean) -> Unit
    ) {

        Log.d("isStatus", isButtonClick.toString())
        isApproveRejectId = data.id.toString()
        var isMessage = ""
        if (isButtonClick) {
            request = LeaveApproveRequest(id = data.id.toString(), is_approve = true)
            isMessage = getString(R.string.are_you_sure_you_want_to_approve_this_outpass_request)
        } else {
            request = LeaveApproveRequest(id = data.id.toString(), is_approve = false)
            isMessage = getString(R.string.are_you_sure_you_want_to_reject_this_outpass_request)
        }
        Constant.showSendConfirmationDialog(
            requireActivity(),
            getString(R.string.confirmation),
            getString(R.string.permission_ok),
            getString(R.string.Cancel),
            "",
            isMessage
        ) { confirmed ->
            if (confirmed) {
                Constant.showLoadingAny(requireView())
                pendingApprovalCallback = resultCallback // store it for later
                appViewModel?.schoolHostelOutpassUpdateStatus(isAccessToken!!, request, requireActivity())
            } else {
                resultCallback(false) // user cancelled
            }
        }
    }
    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.imgClose -> {
                (parentFragment as? BottomSheet)?.closeSheet()
            }
        }
    }
}