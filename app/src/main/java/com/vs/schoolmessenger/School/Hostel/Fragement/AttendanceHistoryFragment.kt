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
import com.vs.schoolmessenger.School.Hostel.Adapter.AttendanceHistory.AttendanceHistorySessionWiseAdapter
import com.vs.schoolmessenger.School.Hostel.BottomSheet
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getAttendanceHistoryData
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getRoomData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HostelAttendanceHistoryBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AttendanceHistoryFragment : Fragment(), View.OnClickListener {

    private var _binding: HostelAttendanceHistoryBinding? = null
    private val binding get() = _binding!!
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: AttendanceHistorySessionWiseAdapter

    private var selectedDateField: Int = 0
    private var appViewModel: App? = null
    private var lastSelectedDate: Calendar? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = HostelAttendanceHistoryBinding.inflate(inflater, container, false)
        _binding?.imgClose?.setOnClickListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        binding.root.post {
            isGetAttendanceHistory()
        }
    }

    private fun setupViews() {

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        isStaffDetails = SharedPreference.getStaffDetails(requireContext())
        isAccessToken = isStaffDetails?.access_token

        binding.imgClose.setOnClickListener(this)
        binding.rytStart.setOnClickListener(this)

        val (_, dayOfWeek, fullDate, _) = Constant.getCurrentDateInfo2()
        binding.lblDay.text = dayOfWeek
        binding.txtStartDate.text = fullDate
        lastSelectedDate = Calendar.getInstance()

        binding.lblHostelName.text= Constant.isHostelName?:""


        appViewModel?.hotelSchoolAttendanceReport?.observe(viewLifecycleOwner) { response ->
            Constant.hideLoadingAny(requireView())
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
                        binding.lblErrorMessage.text = response.message
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

    }

    private fun isLoadAttendanceHistory(newData: List<getRoomData>?) {
        mAdapter =
            AttendanceHistorySessionWiseAdapter(
                newData,
                requireContext(),
                Constant.isShimmerViewDisable
            )
        binding.rcRoomAvailability.layoutManager = LinearLayoutManager(requireContext())
        binding.rcRoomAvailability.isNestedScrollingEnabled = false
        binding.rcRoomAvailability.adapter = mAdapter

    }

    private fun isGetAttendanceHistory() {
        Constant.showLoadingAny(requireView())
        appViewModel!!.isGetHostelSchoolAttendanceReport(isAccessToken!!, Constant.isSelectedHostelFromHostelListData?.id.toString(),Constant.convertDateFormat(binding.txtStartDate.text.toString()),Constant.isSelectedAcademicYear?:"", requireActivity())

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

                lastSelectedDate = selectedCalendar

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedCalendar.time)

                onDateSelected(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.imgClose -> {
                (parentFragment as? BottomSheet)?.closeSheet()
            }
            R.id.rytStart -> {

                selectedDateField = 1
                showDatePicker11(requireActivity(), false) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.txtStartDate.text =
                        Constant.covertDateFormate(selectedDate) // 13 may 2222
                    val (_, formattedDate) = Constant.getDayAndDateOnly2(binding.txtStartDate.text.toString())// 13 Monday
                    binding.lblDay.text = formattedDate
                    isGetAttendanceHistory()

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}