package com.vs.schoolmessenger.Dashboard.Fragments


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.Ads.AdsDisplayOptions
import com.vs.schoolmessenger.CommonScreens.MenuDetails.ContactDetails
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardCountData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuCountDetail
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.AutoScrollAdapterWithDots
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter
import com.vs.schoolmessenger.Parent.QuizExam.QuizExam
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.AttendanceMark
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReport
import com.vs.schoolmessenger.School.Assignment.Assignment
import com.vs.schoolmessenger.School.Attachment.Attachment
import com.vs.schoolmessenger.School.Communication.CommunicationSchool
import com.vs.schoolmessenger.School.DailyCollection.DailyCollection
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReport
import com.vs.schoolmessenger.School.Homework.HomeWork
import com.vs.schoolmessenger.School.ImportantInfo.ImportantInfo
import com.vs.schoolmessenger.School.InteractionWithStudent.InteractionWithStudent
import com.vs.schoolmessenger.School.LSRW.LsrwMain
import com.vs.schoolmessenger.School.LeaveRequests.LeaveRequests
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary.LessonPlan
import com.vs.schoolmessenger.School.MarkYourAttendance.MarkYourAttendance
import com.vs.schoolmessenger.School.MessageFromManagement.MessageFromManagement
import com.vs.schoolmessenger.School.NoticeBoard.CreateNoticeBoard

import com.vs.schoolmessenger.School.QuizExam.ExamQuiz
import com.vs.schoolmessenger.School.PTM.Activity.PTM
import com.vs.schoolmessenger.School.SchoolNeeds.SchoolNeeds
import com.vs.schoolmessenger.School.SchoolStrength.SchoolStrength
import com.vs.schoolmessenger.School.StaffWiseAttendanceReport.StaffWiseAttendanceReport
import com.vs.schoolmessenger.School.StudentReport.StudentReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.FrequentSchoollyUsedMenuItems
import com.vs.schoolmessenger.Utils.Constant.isSchoolAdItem
import com.vs.schoolmessenger.Utils.Constant.isSchoolContactDetails
import com.vs.schoolmessenger.Utils.Constant.isSchoolDashBoardData
import com.vs.schoolmessenger.Utils.Constant.isSchoolMenuCountDetails
import com.vs.schoolmessenger.Utils.Constant.isSchoolMenuDetails
import com.vs.schoolmessenger.Utils.ScrollItem
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolHomeFragmentBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class SchoolHomeFragment : Fragment(), View.OnClickListener, MenuClickListener {

    private lateinit var binding: SchoolHomeFragmentBinding

    //    private lateinit var items: List<ScrollItem>
    lateinit var isMenuAdapter: SchoolMenuAdapter
    private var isSearchVisible = false
    private var appViewModel: App? = null
    var userDetails: UserDetails? = null
    var staffDetails: StaffDetails? = null
    var isDashBoardCountData: List<DashboardCountData>? = null
    var isAdsDisplayOptions: AdsDisplayOptions? = null
    var access_token = ""
    private lateinit var allMenuItems: List<MenuDetail>
    private val isMenuItems = mutableListOf<MenuDetail>()


    private lateinit var adapter: AutoScrollAdapterWithDots
    private lateinit var layoutManager: LinearLayoutManager
    private val snapHelper = PagerSnapHelper()
    private var autoScrollHandler: Handler? = null
    private var autoScrollRunnable: Runnable? = null
    private var currentPosition = 0
    private var isUserScrolling = false
    private var isAutoScrollEnabled = true
    private var currentDotPosition = 0
    private var mobile_number = ""


    companion object {
        private const val SCROLL_DELAY = 3000L
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SchoolHomeFragmentBinding.inflate(layoutInflater)
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        dateFormat.format(currentDate)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        Constant.checkBiometricSupport(requireActivity())

        mobile_number = SharedPreference.getMobileNumber(requireActivity()).toString()
        userDetails = SharedPreference.getUserDetails(requireActivity())
        staffDetails = SharedPreference.getStaffDetails(requireActivity())


        Log.d("school_logo", staffDetails!!.school_logo)

        if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
            access_token = staffDetails!!.access_token
            binding.lblSchoolName.text = staffDetails!!.school_name
            binding.username.text = userDetails!!.staff_details[0].role

            if (staffDetails!!.school_name_regional != "") {
//                binding.lblSchoolRegionalName.visibility = View.GONE
//                binding.lblSchoolRegionalName.text = staffDetails!!.school_name_regional
            } else {
                //   binding.lblSchoolRegionalName.visibility = View.GONE
            }

        } else {
            access_token = userDetails!!.staff_details[0].access_token
            if (userDetails!!.staff_details.size > 1) {
                binding.username.text = userDetails!!.role_name
                binding.lblSchoolName.visibility = View.GONE
            } else {
                binding.lblSchoolName.visibility = View.VISIBLE
                binding.lblSchoolName.text = userDetails!!.staff_details[0].school_name
                binding.username.text = userDetails!!.staff_details[0].role
                if (staffDetails!!.school_name_regional != "") {
//                    binding.lblSchoolRegionalName.visibility = View.GONE
//                    binding.lblSchoolRegionalName.text = staffDetails!!.school_name_regional
                } else {
//                    binding.lblSchoolRegionalName.visibility = View.GONE
                }
            }
        }

        binding.imgBurgerMenu.setOnClickListener(this)

        binding.imgBurgerMenu.setOnClickListener {
            (activity as? SchoolDashboard)?.openDrawer()
        }

        appViewModel!!.isDashBoardData?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    val isDashboardResponse = response.data
                    isSchoolDashBoardData = isDashboardResponse

                    isSchoolContactDetails = isSchoolDashBoardData!![0].contactDetails

                    appViewModel!!.isDashBoardCountData(
                        access_token, Constant.staff_, requireActivity()
                    )
                    isSchoolMenuDetails = isSchoolDashBoardData!![0].menus
                    FrequentSchoollyUsedMenuItems = isSchoolDashBoardData!![0].frequently_used
                    allMenuItems = isSchoolMenuDetails!!
                    setupRecyclerView()

                }
            }
        }

        appViewModel!!.isDashBoardCountData?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    val isDashboardResponse = response.data
                    isDashBoardCountData = isDashboardResponse
                    isSchoolMenuCountDetails = isDashBoardCountData!![0].menu_details
                    isGetAds()
                }
            }
        }

        appViewModel!!.isGetAds?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    isSchoolAdItem = response.data
                    val filteredAds = response.data.filter { it.id != null }
                    isAdsDisplayOptions = isSchoolAdItem!![0].ads_display_options
                    val adList: List<AdItem> = filteredAds.map { ad ->
                        AdItem(
                            ad.id!!, ad.name ?: "", ad.content_url ?: "", ad.redirect_url ?: ""
                        )
                    }
                    isSchoolAdItem = adList
                }
                isLoadData()
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (userDetails!!.is_parent && userDetails!!.is_staff) {
                    val intent = Intent(requireActivity(), PrioritySelection::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                } else {
                    handleBackPress()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return binding.root
    }
    private fun setupRecyclerView() {
        if (!FrequentSchoollyUsedMenuItems.isNullOrEmpty()) {
            binding.autoScrollRecyclerView.visibility = View.VISIBLE

            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            binding.autoScrollRecyclerView.layoutManager = layoutManager

            adapter = AutoScrollAdapterWithDots(FrequentSchoollyUsedMenuItems!!, this)
            binding.autoScrollRecyclerView.adapter = adapter

            if (binding.autoScrollRecyclerView.onFlingListener == null) {
                snapHelper.attachToRecyclerView(binding.autoScrollRecyclerView)
            }

            currentPosition = adapter.getMiddlePosition()
            layoutManager.scrollToPosition(currentPosition)

        } else {
            binding.autoScrollRecyclerView.visibility = View.GONE
        }
    }

    private fun createSampleData(): List<ScrollItem> {
        return listOf(
            ScrollItem(R.drawable.home_work_icon_school, "Daily Homework"),
            ScrollItem(R.drawable.fee_pending_reports, "Fee Payment"),
            ScrollItem(R.drawable.attachment_icon, "Attendance"),
            ScrollItem(R.drawable.event_icon_school, "School Events"),
            ScrollItem(R.drawable.fee_details, "Grades"),
            ScrollItem(R.drawable.message_f_management, "Messages")
        )
    }

    private fun isLoadData() {

        Log.d("isMenuCountDetails", isSchoolMenuCountDetails!!.size.toString())
        isMenuAdapter = SchoolMenuAdapter(
            requireActivity(),
            this,
            isSchoolMenuDetails,
            isSchoolMenuCountDetails,
            Constant.isShimmerViewDisable
        )
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)

        binding.gridRecyclerView.layoutManager = gridLayoutManager
        binding.gridRecyclerView.adapter = isMenuAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filter(text: String) {
        val query = text.lowercase(Locale.ROOT)
        val filtered = if (query.isEmpty()) {
            allMenuItems
        } else {
            allMenuItems.filter {
                it.name.lowercase(Locale.ROOT).contains(query) == true
            }
        }
        isMenuItems.clear()
        isMenuItems.addAll(filtered)
        isMenuAdapter.updateList(isMenuItems.toList())

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
        }
    }


    private fun handleBackPress() {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.Go_Back))
            .setMessage(getString(R.string.Do_you_want_Exit))
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                requireActivity().finishAffinity()
            }.setNegativeButton(getString(R.string.No), null).show()
    }


    private fun isDashBoardData() {

        isMenuAdapter =
            SchoolMenuAdapter(requireActivity(), this, null, null, Constant.isShimmerViewShow)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)

        binding.gridRecyclerView.layoutManager = gridLayoutManager
        binding.gridRecyclerView.adapter = isMenuAdapter

        appViewModel!!.isDashBoardData(
            access_token, Constant.staff_, mobile_number, requireActivity()
        )
    }


    private fun isGetAds() {
        activity?.let { safeActivity ->
            appViewModel?.isGetAds(access_token, "102", safeActivity)
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("Loading", "Dashboard Data is Loading")
        if(isSchoolDashBoardData == null) {
            isDashBoardData()
        }
        else{
            isLoadData()
            setupRecyclerView()
        }
        Log.d("Loading", "Dashboard Data is Refreshed")
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
        Log.d("Status", "onPause")
    }

    override fun onClick(data: MenuDetail) {

        val activityClass = when (data.id) {
            Constant.M_COMMUNICATION -> CommunicationSchool::class.java
            Constant.M_ASSIGNMENT -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    Assignment::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        Assignment::class.java
                    }
                }
            }

            Constant.M_HOMEWORK -> {

                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    HomeWork::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        HomeWork::class.java
                    }
                }
            }

            Constant.M_QUIZ_EXAM -> {

                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    ExamQuiz::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        ExamQuiz::class.java
                    }
                }
            }




            Constant.M_ATTENDANCE_MARKING -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    AttendanceMark::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        AttendanceMark::class.java
                    }
                }
            }

            Constant.M_ABSENTEES_REPORT -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    AbsenteesReport::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        AbsenteesReport::class.java
                    }
                }
            }

            Constant.M_SCHOOL_STRENGTH -> {
                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    SchoolStrength::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        SchoolStrength::class.java
                    }
                }
            }

            Constant.M_NOTICEBOARD -> CreateNoticeBoard::class.java
            Constant.M_SCHOOL_CLASS_EVENTS -> {

                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    CreateEvent::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        CreateEvent::class.java
                    }
                }
            }


            Constant.M_MESSAGES_FROM_MANAGEMENT -> {
                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    MessageFromManagement::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        MessageFromManagement::class.java
                    }
                }
            }

            Constant.M_INTERACTION_WITH_STUDENT -> {
                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    InteractionWithStudent::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        InteractionWithStudent::class.java
                    }
                }
            }


            Constant.M_DAILY_COLLECTION -> {
                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    DailyCollection::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        DailyCollection::class.java
                    }
                }
            }

            Constant.M_STUDENT_REPORT -> {
                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    StudentReport::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        StudentReport::class.java
                    }
                }
            }

            Constant.M_LESSON_PLAN -> {

                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    LessonPlan::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        LessonPlan::class.java
                    }
                }
            }

            Constant.M_FEE_PENDING_REPORT -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    //go to fee pending report
                    FeePendingReport::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        //go to fee pending report
                        FeePendingReport::class.java
                    }
                }
            }

            Constant.M_MARK_YOUR_ATTENDANCE -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    //go to geometric mark attendance page
                    MarkYourAttendance::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        //go to geometric mark attendance page
                        MarkYourAttendance::class.java
                    }
                }
            }

            Constant.M_STAFF_WISE_ATTENDANCE_REPORT -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    StaffWiseAttendanceReport::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        StaffWiseAttendanceReport::class.java
                    }
                }
            }

            Constant.M_PTM -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    //go to ptm page
                    PTM::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        //go to ptm page
                        PTM::class.java
                    }
                }
            }
            Constant.M_ATTACHMENTS -> {
                Attachment::class.java
            }

            Constant.M_LEAVE_REQUEST -> {
                LeaveRequests::class.java
            }
            Constant.M_VERY_IMPORTANT_INFO -> ImportantInfo::class.java
            Constant.M_FEEDBACK -> ImportantInfo::class.java
//            Constant.M_SCHOOL_NEEDS -> SchoolNeeds::class.java
            Constant.M_SCHOOL_NEEDS -> LsrwMain::class.java
            else -> null
        }
        activityClass?.let {
            startActivity(Intent(requireActivity(), it))
        }
    }
}