package com.vs.schoolmessenger.School.Hostel.Fragement

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Parent.RequestLeave.CustomCalendarFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Adapter.RoomAttendance.RoomAttendanceAdapter
import com.vs.schoolmessenger.School.Hostel.Listner.RoomAttendanceListener
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelAttendanceSessionType.SessionData
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelAttendanceSessionType.getHostelAttendanceSession
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelRoomAttendanceStudentList.RoomStudentAttendanceData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter_New
import com.vs.schoolmessenger.databinding.RoomAttendanceBinding
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RoomAttendanceFragment : Fragment(), CustomCalendarFragment.CalendarDateListener,
    RoomAttendanceListener {

    private var _binding: RoomAttendanceBinding? = null
    private val binding get() = _binding!!
    private var isSelectedDate: LocalDate? = null
    var isFromSession = ""
    var isSelectedSessionID = -1
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: RoomAttendanceAdapter
    private var appViewModel: App? = null

    private var sessionList: List<SessionData> = ArrayList()
    private var sessionNames: MutableList<String> = ArrayList()

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

        Log.d("isSelectedAcademicYear",Constant.isSelectedHostelRoomData?.isSelectedAcademicYear?.toString()?:"")

        val count = Constant.isSelectedHostelRoomData?.current_occupancy ?: 0
        val bedCount = Constant.isSelectedHostelRoomData?.total_beds ?: 0
        binding.lblRoomNo.text = "$count ${if (count == 1) "${getString(R.string.student)}" else "${getString(R.string.students)}"}" +" • "+
                "$bedCount ${if (bedCount == 1) "${getString(R.string.Bed)}" else "${getString(R.string.Beds)}"}"

        binding.lblRoomNo.text= "Room No ${Constant.isSelectedHostelRoomData?.id ?:"00"}"

//        loadSelectedDateCalendar()
        //here we are loading the cure
        if (isSelectedDate == null) {
            isSelectedDate = LocalDate.now()
            onDateSelected(isSelectedDate.toString(), Constant.FROM_DATE)
        }


        appViewModel!!.hotelMarkAttendance?.observe(requireActivity()) { response ->
            Constant.hideLoading(requireActivity())
            if (response != null) {
                if (response.status) {
                    Log.d("hotelMarkAttendance", response.message)
                    Constant.showDataValidation(getString(R.string.success), response.message, requireActivity())
                } else {
                    Constant.showDataValidationNoDashboardRedirect(getString(R.string.fail), response.message, requireActivity())
                }
            } else {
                Constant.showDataValidationNoDashboardRedirect(
                    getString(R.string.fail),
                    getString(R.string.something_went_wrong_please_try_again_later),
                    requireActivity()
                )
            }
        }

//        isGetHostelAttendanceSession()
        binding.lnrFromDate.setOnClickListener {
            loadSelectedDateCalendar()
        }

        binding.FromDone.setOnClickListener {
            binding.calendarFromFragmentContainer.visibility = View.GONE
            binding.FromDone.visibility = View.GONE
            binding.activityMain.visibility = View.GONE
            isGetRoomAttendance() // once if we click done only that particular day list will be open

        }

        binding.lblMarkAttendance.setOnClickListener {

            if (isSelectedSessionID == -1) {
                Toast.makeText(requireContext(), "Please select session", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val finalList = mAdapter.getUpdatedList()
            val isAllMarked = finalList.all {
                it.status == "PRESENT" || it.status == "ABSENT"
            }

            if (!isAllMarked) {
                Toast.makeText(requireContext(), "Please make sure all students are marked", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Constant.showSendConfirmationDialog(
                requireActivity(),
                getString(R.string.confirmation),
                getString(R.string.permission_ok),
                getString(R.string.Cancel),
                "",
                getString(R.string.are_you_sure_want_to_submit_the_attendance)
            ) { confirmed ->
                if (confirmed) {
                    val studentArray = JsonArray()

                    finalList.forEach {
                        val studentObj = JsonObject()
                        studentObj.addProperty("student_id", it.id)
                        studentObj.addProperty("status", it.status)
                        studentArray.add(studentObj)
                    }

                    val finalJson = JsonObject().apply {
                        addProperty("hostel_id", Constant.isSelectedHostelFromHostelListData?.id.toString())
                        addProperty("session_type_id", isSelectedSessionID)
                        addProperty("attendance_date", Constant.formatToUi2(isSelectedDate.toString()))
                        addProperty("room_id",Constant.isSelectedHostelRoomData?.id?:"0")
                        addProperty("academic_year_id", Constant.isSelectedHostelRoomData?.isSelectedAcademicYear?.toString()?:"")
                        add("student_details", studentArray)
                    }

                    Log.d("FINAL_JSON", finalJson.toString())

                    Constant.showLoading(requireActivity())
                    appViewModel?.hostelMarkAttendance(isAccessToken!!,finalJson, requireActivity())
                }
            }

        }

        appViewModel?.getHostelAttendanceRoomStudentList?.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.status) {
                    if (response.data.isNotEmpty()) {

                        binding.rcRoomAttendance.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE
                        val data=response.data
                        isLoadRoomAttendance(data as List<RoomStudentAttendanceData>)
                        binding.lblMarkAttendance.alpha=1f
                        binding.lblMarkAttendance.isEnabled=true
                    }
                    else
                    {
                        binding.rcRoomAttendance.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text =response.message
                        binding.lblMarkAttendance.alpha=0.4f
                        binding.lblMarkAttendance.isEnabled=false
                    }

                } else {
                    binding.rcRoomAttendance.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message
                    binding.lblMarkAttendance.alpha=0.4f
                    binding.lblMarkAttendance.isEnabled=false
                }

            } else {

                binding.rcRoomAttendance.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblMarkAttendance.alpha=0.4f
                binding.lblMarkAttendance.isEnabled=false
                binding.lblErrorMessage.text = getString(R.string.Something_went_wrong_Please_try_again)
            }
        }



        appViewModel?.getHostelAttendanceSession?.observe(viewLifecycleOwner) { response ->
            Constant.hideLoading(requireActivity())

            sessionList = if (response != null && response.status && !response.data.isNullOrEmpty())
            {
                response.data
            }
            else {
                listOf(SessionData(-1, "Please select a session"))
            }
            sessionNames.clear()
            sessionNames.addAll(sessionList.map { it.name })

            isFromSpinner()
        }

        isGetHostelSession()


    }

    private fun isFromSpinner() {

        val adapter = SpinnerLoadingAdapter_New(requireContext(), sessionNames)
        binding.isFromSession.adapter = adapter

        if (sessionNames.isNotEmpty()) {
            binding.isFromSession.setSelection(0)
            isFromSession = sessionNames[0]
            isSelectedSessionID = sessionList[0].id

        }

        binding.isFromSession.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()

                isFromSession = sessionNames[position]

                isSelectedSessionID = sessionList[position].id

                isGetRoomAttendance()

                Log.d("Session", "Name: $isFromSession, ID: $isSelectedSessionID")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadSelectedDateCalendar() {
        val today = LocalDate.now()
        val minFromDate =  today.minusYears(1)
        val maxFromDate = today


        val fromFragment = CustomCalendarFragment.newInstance(
            minDate = minFromDate.toString(),
            maxDate = maxFromDate.toString(),
            selectedDate = isSelectedDate?.toString(),
            tag = Constant.FROM_DATE
        )

        childFragmentManager.beginTransaction()
            .replace(binding.calendarFromFragmentContainer.id, fromFragment, "FROM_CALENDAR")
            .commit()

        binding.calendarFromFragmentContainer.visibility = View.VISIBLE
        binding.activityMain.visibility = View.VISIBLE
        binding.FromDone.visibility = View.VISIBLE
        binding.lblSelect.text = getString(R.string.select_date)
    }

    private fun isLoadRoomAttendance(newData: List<RoomStudentAttendanceData>) {
        UpdateProgressAndCountOfAttendance(newData)
        mAdapter =
            RoomAttendanceAdapter(newData, requireContext(),this, Constant.isShimmerViewDisable)
        binding.rcRoomAttendance.adapter = mAdapter
    }

    private fun isGetRoomAttendance() {

        mAdapter = RoomAttendanceAdapter(null, requireContext(),this, Constant.isShimmerViewShow)

        binding.rcRoomAttendance.layoutManager = LinearLayoutManager(requireContext())
        binding.rcRoomAttendance.isNestedScrollingEnabled = false
        binding.rcRoomAttendance.adapter = mAdapter

//        val dummyData = getDummyStudentAttendance()
//        isLoadRoomAttendance(dummyData)

        appViewModel!!.isGetHostelAttendanceRoomStudentList(isAccessToken!!,Constant.isSelectedHostelFromHostelListData?.id.toString(),
            Constant.isSelectedHostelRoomData?.id.toString(), Constant.isSelectedHostelRoomData?.isSelectedAcademicYear?.toString()?:"",
            Constant.formatToUi2(isSelectedDate.toString()),isSelectedSessionID.toString(), requireActivity())
    }

//    private fun isGetHostelAttendanceSession() {
//        val dummyData = getDummySessionResponse()
//        sessionList=dummyData.data
//        sessionNames.clear()
//        sessionNames.addAll(sessionList.map { it.name })
//        isFromSpinner()
//    }

    private fun isGetHostelSession(){
        Constant.showLoading(requireActivity())
        appViewModel!!.isGetHostelAttendanceSessionDetails(isAccessToken!!, requireActivity())
    }


//    private fun getDummyStudentAttendance(): List<RoomStudentAttendanceData> {
//
//        return listOf(
//
//            RoomStudentAttendanceData(
//                student_id = "16583170",
//                student_name = "Aarav Sharma",
//                admission_no = "SS-40",
//                roll_no = "",
//                gender = "male",
//                class_id = "32890",
//                class_name = "III",
//                section_id = "91744",
//                section_name = "A",
//                primary_mobile = "9876543210",
//                status = "PRESENT"
//            ),
//
//            RoomStudentAttendanceData(
//                student_id = "16570557",
//                student_name = "Diya Patel",
//                admission_no = "SS-34",
//                roll_no = "",
//                gender = "female",
//                class_id = "32890",
//                class_name = "III",
//                section_id = "91744",
//                section_name = "A",
//                primary_mobile = "9123456780",
//                status = "ABSENT"
//            ),
//
//            RoomStudentAttendanceData(
//                student_id = "16630660",
//                student_name = "Rohan Kumar",
//                admission_no = "SS-41",
//                roll_no = "",
//                gender = "male",
//                class_id = "32890",
//                class_name = "III",
//                section_id = "91744",
//                section_name = "A",
//                primary_mobile = "9988776655",
//                status = "PRESENT"
//            ),
//
//            RoomStudentAttendanceData(
//                student_id = "16525594",
//                student_name = "Ananya Singh",
//                admission_no = "VS-33",
//                roll_no = "",
//                gender = "female",
//                class_id = "34468",
//                class_name = "XI",
//                section_id = "95015",
//                section_name = "A",
//                primary_mobile = "9012345678",
//                status = "ABSENT"
//            ),
//
//            RoomStudentAttendanceData(
//                student_id = "16525595",
//                student_name = "Vivaan Gupta",
//                admission_no = "VS-34",
//                roll_no = "",
//                gender = "male",
//                class_id = "34468",
//                class_name = "XI",
//                section_id = "95015",
//                section_name = "A",
//                primary_mobile = "9098765432",
//                status = "PRESENT"
//            ),
//
//            RoomStudentAttendanceData(
//                student_id = "16525596",
//                student_name = "Ishaan Verma",
//                admission_no = "VS-35",
//                roll_no = "",
//                gender = "male",
//                class_id = "34468",
//                class_name = "XI",
//                section_id = "95015",
//                section_name = "A",
//                primary_mobile = "9876501234",
//                status = "PRESENT"
//            ),
//
//            RoomStudentAttendanceData(
//                student_id = "16525597",
//                student_name = "Meera Nair",
//                admission_no = "VS-36",
//                roll_no = "",
//                gender = "female",
//                class_id = "34468",
//                class_name = "XI",
//                section_id = "95015",
//                section_name = "A",
//                primary_mobile = "9123450098",
//                status = "ABSENT"
//            ),
//
//            RoomStudentAttendanceData(
//                student_id = "16525598",
//                student_name = "Aditya Kapoor",
//                admission_no = "VS-37",
//                roll_no = "",
//                gender = "male",
//                class_id = "34468",
//                class_name = "XI",
//                section_id = "95015",
//                section_name = "A",
//                primary_mobile = "9988112233",
//                status = "PRESENT"
//            )
//        )
//    }

//    private fun getDummySessionResponse(): getHostelAttendanceSession {
//        return getHostelAttendanceSession(
//            status = true,
//            message = "Attendance sessions loaded.",
//            data = listOf(
//                SessionData(1, "Morning"),
//                SessionData(2, "Evening")
//            )
//        )
//    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(Constant.EEE_comma_dd_MMM_yyyy)
        return formatter.format(date)
    }


    override fun onDateSelected(date: String, tag: String) {
        val selected = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

        when (tag) {
            Constant.FROM_DATE -> {
                isSelectedDate = selected
                Log.d("isSelectedDate",isSelectedDate.toString())

                binding.tvSelectedDate.text = formatDate(selected)
                Log.d("SelectedDate",formatDate(selected))
            }
        }
    }

    fun UpdateProgressAndCountOfAttendance(list: List<RoomStudentAttendanceData>){
        val total = list.size
        val presentCount = list.count { it.status.equals("Present", true) }

        binding.lblMarkedPercentage.text = "$presentCount/$total"


        val percentage = if (total > 0) {
            (presentCount * 100) / total
        } else 0

        binding.proAttendanceProgressBar.progress = percentage
    }

    override fun onAttendanceChanged(list: List<RoomStudentAttendanceData>) {
        UpdateProgressAndCountOfAttendance(list)
    }
}