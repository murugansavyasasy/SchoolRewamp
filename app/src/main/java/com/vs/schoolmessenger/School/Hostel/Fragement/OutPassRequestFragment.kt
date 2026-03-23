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
import com.vs.schoolmessenger.School.Hostel.Adapter.OutpassRequest.StatusWiseOutpassRequest
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.AdminRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.StatusWiseAdminRequestData
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.StatusWiseOutpassRequestData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AdminRequestsBinding
import com.vs.schoolmessenger.databinding.OutpassRequestFragmentBinding


class OutPassRequestFragment : Fragment() {


    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: StatusWiseOutpassRequest

    private var appViewModel: App? = null
    private var _binding: OutpassRequestFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = OutpassRequestFragmentBinding.inflate(inflater, container, false)
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




//        appViewModel?.getleaverequest?.observe(viewLifecycleOwner) { response ->
//
//            if (response != null) {
//
//                if (response.status) {
//                    val dummyData = getDummyLeaveRequestData()
//                    if (dummyData.isNotEmpty()) {
//
//                        binding.rcOutpassRequest.visibility = View.VISIBLE
//                        binding.imgNoDataFound.visibility = View.GONE
//                        binding.lblErrorMessage.visibility = View.GONE
//
//                        isLoadAttendanceHistory(dummyData)
//
//                    } else {
//
//                        binding.rcOutpassRequest.visibility = View.GONE
//                        binding.lblErrorMessage.visibility = View.VISIBLE
//                        binding.imgNoDataFound.visibility = View.VISIBLE
//                        binding.lblErrorMessage.text = getString(R.string.no_data_found)
//                    }
//
//                } else {
//
//                    binding.rcOutpassRequest.visibility = View.GONE
//                    binding.lblErrorMessage.visibility = View.VISIBLE
//                    binding.imgNoDataFound.visibility = View.VISIBLE
//                    binding.lblErrorMessage.text = response.message
//                }
//
//            } else {
//
//                binding.rcOutpassRequest.visibility = View.GONE
//                binding.lblErrorMessage.visibility = View.VISIBLE
//                binding.imgNoDataFound.visibility = View.VISIBLE
//                binding.lblErrorMessage.text =
//                    getString(R.string.Something_went_wrong_Please_try_again)
//            }
//        }

        isGetAttendanceHistory()
    }

    private fun isLoadAttendanceHistory(newData: List<StatusWiseOutpassRequestData>?) {
        binding.rcOutpassRequest.visibility = View.VISIBLE
        mAdapter = StatusWiseOutpassRequest(
            newData, requireActivity(), Constant.isShimmerViewDisable
        )
        binding.rcOutpassRequest.adapter = mAdapter
    }

    private fun isGetAttendanceHistory() {
        mAdapter = StatusWiseOutpassRequest(
            null, requireActivity(), Constant.isShimmerViewShow
        )
        binding.rcOutpassRequest.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcOutpassRequest.isNestedScrollingEnabled = false
        binding.rcOutpassRequest.adapter = mAdapter
//        val dummyData = getDummyLeaveRequestData()
//        isLoadAttendanceHistory(dummyData)
    }

//    private fun getDummyLeaveRequestData(): List<StatusWiseOutpassRequestData> {
//
//        val leaveList = ArrayList<StatusWiseOutpassRequestData>()
//
//        val pending = listOf(
//
//            OutpassRequestWiseData(
//                roomNumber = "101",
//                roomTitle = "Room 101",
//                studentName = "Aarav Sharma",
//                issueDescription = "Tap not working in bathroom",
//                InDate = "Mar 4 2026",
//                OutDate = "Mar 5 2026",
//                OutTime = "10:30 AM",
//                InTime = "10:30 AM",
//                Destination = "Library",
//                status = Constant.waiting_for_approval
//            ),
//
//            OutpassRequestWiseData(
//                roomNumber = "102",
//                roomTitle = "Room 102",
//                studentName = "Rohit",
//                issueDescription = "Tap not working in bathroom",
//                InDate = "Mar 4 2026",
//                OutDate = "Mar 5 2026",
//                OutTime = "10:30 AM",
//                InTime = "10:30 AM",
//                Destination = "Mess",
//                status = Constant.waiting_for_approval
//            )
//        )
//
//        val approved = listOf(
//
//            OutpassRequestWiseData(
//                roomNumber = "101",
//                roomTitle = "Room 101",
//                studentName = "Aarav Sharma",
//                issueDescription = "Tap not working in bathroom",
//                InDate = "Mar 4 2026",
//                OutDate = "Mar 5 2026",
//                OutTime = "10:30 AM",
//                InTime = "10:30 AM",
//                Destination = "Mess",
//                status = Constant.approved
//
//            ),
//
//            OutpassRequestWiseData(
//                roomNumber = "102",
//                roomTitle = "Room 102",
//                studentName = "Rohit",
//                issueDescription = "Tap not working in bathroom",
//                InDate = "Mar 4 2026",
//                OutDate = "Mar 5 2026",
//                OutTime = "10:30 AM",
//                InTime = "10:30 AM",
//                Destination = "Mess",
//                status = Constant.approved
//            )
//        )
//
//        val rejected = listOf(
//
//            OutpassRequestWiseData(
//                roomNumber = "101",
//                roomTitle = "Room 101",
//                studentName = "Sharma",
//                issueDescription = "Need to improve the food quality ",
//                InDate = "Mar 4 2026",
//                OutDate = "Mar 5 2026",
//                OutTime = "10:30 AM",
//                InTime = "10:30 AM",
//                Destination = "Mess",
//                status = Constant.rejected
//            ),
//
//            OutpassRequestWiseData(
//                roomNumber = "102",
//                roomTitle = "Room 102",
//                studentName = "Rohit Kohli",
//                issueDescription = "Need to have TV in the mess",
//                InDate = "Mar 4 2026",
//                OutDate = "Mar 5 2026",
//                OutTime = "10:30 AM",
//                InTime = "10:30 AM",
//                Destination = "Mess",
//                status = Constant.rejected
//            )
//        )
//
//        leaveList.add(
//            StatusWiseOutpassRequestData(
//                Status = Constant.waiting_for_approval,
//                StatusWiseData = pending
//            )
//        )
//        leaveList.add(
//            StatusWiseOutpassRequestData(
//                Status = Constant.approved,
//                StatusWiseData = approved
//            )
//        )
//        leaveList.add(
//            StatusWiseOutpassRequestData(
//                Status = Constant.rejected,
//                StatusWiseData = rejected
//            )
//        )
//
//        return leaveList
//    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}