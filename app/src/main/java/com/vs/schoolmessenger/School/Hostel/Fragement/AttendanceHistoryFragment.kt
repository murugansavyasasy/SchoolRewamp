package com.vs.schoolmessenger.School.Hostel.Fragement

import android.app.DatePickerDialog
import android.content.Context
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
import com.vs.schoolmessenger.School.Hostel.Adapter.AttendanceHistory.AttendanceHistoryAdapter
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getAttendanceHistoryData
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getRoomData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HostelAttendanceHistoryBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AttendanceHistoryFragment : Fragment(), OnDateSelectedListener, View.OnClickListener {

    private var _binding: HostelAttendanceHistoryBinding? = null
    private val binding get() = _binding!!
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: AttendanceHistoryAdapter

    private var selectedDateField: Int = 0
    private var appViewModel: App? = null
    private var lastSelectedDate: Calendar? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = HostelAttendanceHistoryBinding.inflate(inflater, container, false)
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

        val (_, dayOfWeek, fullDate, _) = Constant.getCurrentDateInfo2()
        binding.lblDay.text = dayOfWeek
        binding.txtStartDate.text = fullDate
        lastSelectedDate = Calendar.getInstance()



        appViewModel?.hotelSchoolAttendanceReport?.observe(viewLifecycleOwner) { response ->

            if (response != null) {

                if (response.status) {

                    if (response.data.isNotEmpty()) {

                        binding.rcRoomAvailability.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE

                        isLoadAttendanceHistory(response.data)

                    } else {

                        binding.rcRoomAvailability.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text = getString(R.string.no_data_found)
                    }

                } else {

                    binding.rcRoomAvailability.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message
                }

            } else {

                binding.rcRoomAvailability.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        isGetAttendanceHistory()
    }

    private fun isLoadAttendanceHistory(newData: List<getRoomData>?) {
        mAdapter =
            AttendanceHistoryAdapter(newData, requireContext(), Constant.isShimmerViewDisable)
        binding.rcRoomAvailability.adapter = mAdapter
    }

    private fun isGetAttendanceHistory() {

        mAdapter = AttendanceHistoryAdapter(null, requireContext(), Constant.isShimmerViewShow)

        binding.rcRoomAvailability.layoutManager = LinearLayoutManager(requireContext())
        binding.rcRoomAvailability.isNestedScrollingEnabled = false
        binding.rcRoomAvailability.adapter = mAdapter

        appViewModel!!.isGetHostelSchoolAttendanceReport(isAccessToken!!, Constant.isSelectedHostelFromHostelListData?.id.toString(),Constant.convertDateFormat(binding.txtStartDate.text.toString()),Constant.isSelectedAcademicYear?:"", requireActivity())

//        val dummyData = getDummyFloorWiseRoomAvailabilityData()
//        isLoadAttendanceHistory(dummyData)
    }

//    private fun getDummyFloorWiseRoomAvailabilityData(): List<getAttendanceHistoryData> {
//
//        val list = ArrayList<getAttendanceHistoryData>()
//
//        list.add(
//            getAttendanceHistoryData(
//                date = "Tuesday, Mar 3",
//                year = "2026",
//                attendancePercentage = 91,
//                totalStudents = 23,
//                presentStudents = 21,
//                absentStudents = 2,
//                roomsMarked = 6,
//                totalRooms = 8
//            )
//        )
//
//        list.add(
//            getAttendanceHistoryData(
//                date = "Wednesday, Mar 4",
//                year = "2026",
//                attendancePercentage = 88,
//                totalStudents = 25,
//                presentStudents = 22,
//                absentStudents = 3,
//                roomsMarked = 5,
//                totalRooms = 8
//            )
//        )
//
//        list.add(
//            getAttendanceHistoryData(
//                date = "Thursday, Mar 5",
//                year = "2026",
//                attendancePercentage = 95,
//                totalStudents = 20,
//                presentStudents = 19,
//                absentStudents = 1,
//                roomsMarked = 8,
//                totalRooms = 8
//            )
//        )
//
//        return list
//    }

    override fun onDateSelected(date: String) {
        when (selectedDateField) {
            1 -> binding.txtStartDate.text = date
        }
    }

    fun showDatePicker11(
        context: Context,
        dateFormatType: Boolean,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = lastSelectedDate ?: Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                // Save for next time
                lastSelectedDate = selectedCalendar

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedCalendar.time)
                onDateSelected(formattedDate)
            },
            year, month, day
        )

        // Prevent past dates
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.imgClose -> {
            }

            R.id.rytStartDate -> {

                selectedDateField = 1
                showDatePicker11(requireActivity(), false) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.txtStartDate.text =
                        Constant.covertDateFormate(selectedDate) // 13 may 2222
                    val (_, formattedDate) = Constant.getDayAndDateOnly2(binding.txtStartDate.text.toString())// 13 Monday
                    binding.lblDay.text = formattedDate

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}