package com.vs.schoolmessenger.School.Hostel.Fragement

import android.os.Bundle
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
import com.vs.schoolmessenger.School.Hostel.BottomSheet
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.AdminRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.StatusWiseAdminRequestData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AdminRequestsBinding


class AdminRequestFragment : Fragment(), View.OnClickListener {

    private var _binding: AdminRequestsBinding? = null
    private val binding get() = _binding!!

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: StatusWiseAdminRequest

    private var appViewModel: App? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = AdminRequestsBinding.inflate(inflater, container, false)
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

        binding.imgClose.setOnClickListener(this)
        binding.lblCancel.setOnClickListener(this)
        binding.lblNewRequest.setOnClickListener(this)


        appViewModel?.getleaverequest?.observe(viewLifecycleOwner) { response ->

            if (response != null) {

                if (response.status) {
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
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        isGetAttendanceHistory()
    }

    private fun isLoadAttendanceHistory(newData: List<StatusWiseAdminRequestData>?) {
        binding.rcAdminRequest.visibility = View.VISIBLE
        mAdapter = StatusWiseAdminRequest(
            newData, requireActivity(), Constant.isShimmerViewDisable
        )
        binding.rcAdminRequest.adapter = mAdapter
    }

    private fun isGetAttendanceHistory() {
        mAdapter = StatusWiseAdminRequest(
            null, requireActivity(), Constant.isShimmerViewDisable
        )
        binding.rcAdminRequest.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcAdminRequest.isNestedScrollingEnabled = false
        binding.rcAdminRequest.adapter = mAdapter
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
                status = Constant.waiting_for_approval
            ),

            AdminRequestWiseData(
                roomNumber = "102",
                roomTitle = "Room 102",
                studentName = "Rohit",
                issueDescription = "Tap not working in bathroom",
                dateTime = "Mar 5, 10:00 AM",
                status = Constant.waiting_for_approval
            )
        )

        val approved = listOf(

            AdminRequestWiseData(
                roomNumber = "101",
                roomTitle = "Room 101",
                studentName = "Aarav Sharma",
                issueDescription = "Tap not working in bathroom",
                dateTime = "Apr 4, 10:30 AM",
                status = Constant.approved
            ),

            AdminRequestWiseData(
                roomNumber = "102",
                roomTitle = "Room 102",
                studentName = "Rohit",
                issueDescription = "Tap not working in bathroom",
                dateTime = "Apr 5, 10:00 AM",
                status = Constant.approved
            )
        )

        val rejected = listOf(

            AdminRequestWiseData(
                roomNumber = "101",
                roomTitle = "Room 101",
                studentName = "Sharma",
                issueDescription = "Need to improve the food quality ",
                dateTime = "Apr 4, 10:30 AM",
                status = Constant.rejected
            ),

            AdminRequestWiseData(
                roomNumber = "102",
                roomTitle = "Room 102",
                studentName = "Rohit Kohli",
                issueDescription = "Need to have TV in the mess",
                dateTime = "Apr 5, 10:00 AM",
                status = Constant.rejected
            )
        )

        leaveList.add(
            StatusWiseAdminRequestData(
                Status = Constant.waiting_for_approval,
                StatusWiseData = pending
            )
        )
        leaveList.add(
            StatusWiseAdminRequestData(
                Status = Constant.approved,
                StatusWiseData = approved
            )
        )
        leaveList.add(
            StatusWiseAdminRequestData(
                Status = Constant.rejected,
                StatusWiseData = rejected
            )
        )

        return leaveList
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.imgClose -> {
                    (parentFragment as? BottomSheet)?.closeSheet()
            }

            R.id.lblCancel->{
                binding.groupEntireAdminRequest.visibility= View.GONE
                binding.lblNewRequest.visibility= View.VISIBLE
            }

            R.id.lblNewRequest->{
                binding.lblNewRequest.visibility= View.GONE
                binding.groupEntireAdminRequest.visibility= View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}