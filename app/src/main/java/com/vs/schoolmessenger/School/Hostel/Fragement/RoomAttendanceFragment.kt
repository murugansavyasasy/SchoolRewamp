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
import com.vs.schoolmessenger.School.Hostel.Adapter.RoomAttendance.RoomAttendanceAdapter
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getAttendanceHistoryData
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.RoomStudentAttendanceData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.RoomAttendanceBinding

class RoomAttendanceFragment : Fragment() {

    private var _binding: RoomAttendanceBinding? = null
    private val binding get() = _binding!!
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: RoomAttendanceAdapter

    private var appViewModel: App? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = RoomAttendanceBinding.inflate(inflater, container, false)
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


        appViewModel?.getleaverequest?.observe(viewLifecycleOwner) { response ->

            if (response != null) {

                if (response.status) {

                    val dummyData = getDummyStudentAttendance()

                    if (dummyData.isNotEmpty()) {

                        binding.rcRoomAttendance.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE

                        isLoadRoomAttendance(dummyData)

                    } else {

                        binding.rcRoomAttendance.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text = getString(R.string.no_data_found)
                    }

                } else {

                    binding.rcRoomAttendance.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message
                }

            } else {

                binding.rcRoomAttendance.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        isGetRoomAttendance()
    }

    private fun isLoadRoomAttendance(newData: List<RoomStudentAttendanceData>?) {
        mAdapter =
            RoomAttendanceAdapter(newData, requireContext(), Constant.isShimmerViewDisable)
        binding.rcRoomAttendance.adapter = mAdapter
    }

    private fun isGetRoomAttendance() {

        mAdapter = RoomAttendanceAdapter(null, requireContext(), Constant.isShimmerViewDisable)

        binding.rcRoomAttendance.layoutManager = LinearLayoutManager(requireContext())
        binding.rcRoomAttendance.isNestedScrollingEnabled = false
        binding.rcRoomAttendance.adapter = mAdapter

        val dummyData = getDummyStudentAttendance()
        isLoadRoomAttendance(dummyData)
    }

    private fun getDummyStudentAttendance(): List<RoomStudentAttendanceData> {

        val list = ArrayList<RoomStudentAttendanceData>()

        list.add(
            RoomStudentAttendanceData(
                name = "Aarav Sharma",
                studentId = "s1",
                parentPhone = "9876543210",
                status = "Present"
            )
        )

        list.add(
            RoomStudentAttendanceData(
                name = "Diya Patel",
                studentId = "s2",
                parentPhone = "9123456780",
                status = "Absent"
            )
        )

        list.add(
            RoomStudentAttendanceData(
                name = "Rohan Kumar",
                studentId = "s3",
                parentPhone = "9988776655",
                status = "Present"
            )
        )

        list.add(
            RoomStudentAttendanceData(
                name = "Ananya Singh",
                studentId = "s4",
                parentPhone = "9012345678",
                status = "Absent"
            )
        )

        list.add(
            RoomStudentAttendanceData(
                name = "Vivaan Gupta",
                studentId = "s5",
                parentPhone = "9098765432",
                status = "Present"
            )
        )

        list.add(
            RoomStudentAttendanceData(
                name = "Ishaan Verma",
                studentId = "s6",
                parentPhone = "9876501234",
                status = "Present"
            )
        )

        list.add(
            RoomStudentAttendanceData(
                name = "Meera Nair",
                studentId = "s7",
                parentPhone = "9123450098",
                status = "Absent"
            )
        )

        list.add(
            RoomStudentAttendanceData(
                name = "Aditya Kapoor",
                studentId = "s8",
                parentPhone = "9988112233",
                status = "Present"
            )
        )

        return list
    }
   

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}