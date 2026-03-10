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
import com.vs.schoolmessenger.School.Hostel.Adapter.AttendanceHistory.AttendanceHistoryAdapter
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getAttendanceHistoryData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AdminRequestsBinding

class AdminRequestFragment : Fragment(), View.OnClickListener {

    private var _binding: AdminRequestsBinding? = null
    private val binding get() = _binding!!

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: AttendanceHistoryAdapter

    private var appViewModel: App? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = AdminRequestsBinding.inflate(inflater, container, false)
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

        appViewModel?.getleaverequest?.observe(viewLifecycleOwner) { response ->

            if (response != null) {

                if (response.status) {

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
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        isGetAttendanceHistory()
    }

    private fun isLoadAttendanceHistory(newData: List<getAttendanceHistoryData>?) {

        mAdapter =
            AttendanceHistoryAdapter(newData, requireContext(), Constant.isShimmerViewDisable)

        binding.rcAdminRequest.adapter = mAdapter
    }

    private fun isGetAttendanceHistory() {

        mAdapter =
            AttendanceHistoryAdapter(null, requireContext(), Constant.isShimmerViewDisable)

        binding.rcAdminRequest.layoutManager = LinearLayoutManager(requireContext())
        binding.rcAdminRequest.isNestedScrollingEnabled = false
        binding.rcAdminRequest.adapter = mAdapter

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

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.imgClose -> {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}