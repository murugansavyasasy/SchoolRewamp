package com.vs.schoolmessenger.Dashboard.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.Ads.AdsDisplayOptions
import com.vs.schoolmessenger.CommonScreens.MenuDetails.ContactDetails
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.AttendanceMark
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReport
import com.vs.schoolmessenger.School.Assignment.Assignment
import com.vs.schoolmessenger.School.Communication.CommunicationSchool
import com.vs.schoolmessenger.School.DailyCollection.DailyCollection
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.ExamSchedule.Exam
import com.vs.schoolmessenger.School.Homework.HomeWork
import com.vs.schoolmessenger.School.ImportantInfo.ImportantInfo
import com.vs.schoolmessenger.School.InteractionWithStudent.InteractionWithStudent
import com.vs.schoolmessenger.School.LessonPlan.LessonPlan
import com.vs.schoolmessenger.School.MarkYourAttendance.MarkYourAttendance
import com.vs.schoolmessenger.School.MessageFromManagement.MessageFromManagement
import com.vs.schoolmessenger.School.NoticeBoard.CreateNoticeBoard
import com.vs.schoolmessenger.School.OnlineMeeting.OnlineMeeting
import com.vs.schoolmessenger.School.SchoolStrength.SchoolStrength
import com.vs.schoolmessenger.School.StaffWiseAttendanceReport.StaffWiseAttendanceReport
import com.vs.schoolmessenger.School.StudentReport.StudentReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolHomeFragmentBinding
import java.util.Locale
import javax.sql.DataSource


class SchoolHomeFragment : Fragment(), View.OnClickListener, MenuClickListener {

    private lateinit var binding: SchoolHomeFragmentBinding // Automatically generated binding class
    lateinit var isMenuAdapter: SchoolMenuAdapter
    private var isSearchVisible = false
    private var appViewModel: App? = null
    var userDetails: UserDetails? = null
    var staffDetails: StaffDetails? = null
    var isDashBoardData: List<DashboardData>? = null
    var isContactDetails: ContactDetails? = null
    var isMenuDetails: List<MenuDetail>? = null
    var isAdItem: List<AdItem>? = null
    var isAdsDisplayOptions: AdsDisplayOptions? = null
    var access_token = ""
    private lateinit var allMenuItems: List<MenuDetail>
    private val isMenuItems = mutableListOf<MenuDetail>()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SchoolHomeFragmentBinding.inflate(layoutInflater)
        binding.imgNotification.setOnClickListener(this)
        binding.imgSearchClick.setOnClickListener(this)
        binding.changeroll.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.changeroll.paintFlags = binding.changeroll.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        userDetails = SharedPreference.getUserDetails(requireActivity())
        staffDetails = SharedPreference.getStaffDetails(requireActivity())


        Log.d("school_logo", staffDetails!!.school_logo)

        if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
            access_token = staffDetails!!.access_token
            binding.lblSchoolName.text = staffDetails!!.school_name
            if (staffDetails!!.school_name_regional != ""){
                binding.lblSchoolRegionalName.visibility= View.GONE
                binding.lblSchoolRegionalName.text = staffDetails!!.school_name_regional
            }else{
                binding.lblSchoolRegionalName.visibility= View.GONE
            }
            binding.lblSchoolAddress.text = staffDetails!!.school_address
            binding.lblSchoolAddress.visibility = View.VISIBLE
            Glide.with(requireActivity())
                .load(userDetails!!.staff_details[0].school_logo)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Handler(Looper.getMainLooper()).post {
                            Glide.with(requireActivity())
                                .load(R.drawable.school_sample)
                                .into(binding.imgSchoolLogo)
                        }
                        return false
                    }


                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: com.bumptech.glide.load.DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("Glide", "Image load success")
                        return false
                    }
                })
                .into(binding.imgSchoolLogo)

        } else {
            access_token = userDetails!!.staff_details[0].access_token
            if (userDetails!!.staff_details.size > 1) {
                binding.lblSchoolName.text = userDetails!!.role_name
//                binding.lblSchoolAddress.visibility = View.GONE
            } else {
//                binding.lblSchoolAddress.visibility = View.VISIBLE
                binding.lblSchoolName.text = userDetails!!.staff_details[0].school_name
                if (staffDetails!!.school_name_regional != ""){
                    binding.lblSchoolRegionalName.visibility= View.GONE
                    binding.lblSchoolRegionalName.text = staffDetails!!.school_name_regional
                }else{
                    binding.lblSchoolRegionalName.visibility= View.GONE
                }
                binding.lblSchoolAddress.text = userDetails!!.staff_details[0].school_address
                Glide.with(requireActivity())
                    .load(userDetails!!.staff_details[0].school_logo)
                    .listener(object : RequestListener<Drawable> {

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Handler(Looper.getMainLooper()).post {
                                Glide.with(requireActivity())
                                    .load(R.drawable.school_sample)
                                    .into(binding.imgSchoolLogo)
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            dataSource: com.bumptech.glide.load.DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("Glide", "Image load success")
                            return false
                        }
                    })
                    .into(binding.imgSchoolLogo)
            }
        }

        if (userDetails!!.is_parent && userDetails!!.is_staff) {
            binding.changeroll.visibility = View.VISIBLE
        } else {
            if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                if (userDetails!!.staff_details.size > 1) {
                    binding.changeroll.visibility = View.VISIBLE
                } else {
                    binding.changeroll.visibility = View.GONE
                }
            } else {
                binding.changeroll.visibility = View.GONE
            }
        }

        binding.lblViewDetails.paintFlags =
            binding.lblViewDetails.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.lblGif.playAnimation()
        binding.lblGif.setAnimation(R.raw.mathematics)

        isDashBoardData()

        appViewModel!!.isDashBoardData?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    val isDashboardResponse = response.data
                    isDashBoardData = isDashboardResponse
                    isContactDetails = isDashBoardData!![0].contactDetails
                    isMenuDetails = isDashBoardData!![0].menuDetails
                    allMenuItems=isMenuDetails!!
                    isGetAds()
                }
            }
        }

        appViewModel!!.isGetAds?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    isAdItem = response.data
//
//                    isAdsDisplayOptions = isAdItem!![0].ads_display_options
//                    isLoadData()

                    if (status) {
                        // Filter out the first item (which contains ads_display_options)
                        val filteredAds = response.data.filter { it.id != null }
                        isAdsDisplayOptions = isAdItem!![0].ads_display_options
                        // Save the list of ads in a variable
                        val adList: List<AdItem> = filteredAds.map { ad ->
                            AdItem(
                                ad.id!!, ad.name ?: "", ad.content_url ?: "", ad.redirect_url ?: ""
                            )
                        }

                        // Now you can use `adList` anywhere in the activity/fragment
                        isAdItem = adList
                        isLoadData()
                    }
                }
            }
        }


        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                   filter(s.toString())
            }
        })


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (userDetails!!.is_parent && userDetails!!.is_staff) {
                    val intent = Intent(requireActivity(), PrioritySelection::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }else {
                    handleBackPress()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        return binding.root
    }

    private fun isLoadData() {

        val isAdapter = SchoolMenuAdapter(
            requireActivity(), this, isMenuDetails, isAdItem, Constant.isShimmerViewDisable
        )
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        // Adjust span count again for the updated adapter
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (isAdapter.getItemViewType(position)) {
                    2 -> 3 // TYPE_AD: Span across all 3 columns
                    else -> 1 // Default: 1 span per item
                }
            }
        }
        binding.recyclerViewMenus.layoutManager = gridLayoutManager
        binding.recyclerViewMenus.adapter = isAdapter
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
            R.id.imgNotification -> {
                val intent = Intent(requireActivity(), Notification::class.java)
                startActivity(intent)
            }

            R.id.changeroll -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            R.id.imgSearchClick -> {

                if (isSearchVisible) {
                    isSearchVisible = false
                    binding.rytSearch.visibility = View.GONE
                } else {
                    isSearchVisible = true
                    binding.rytSearch.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun handleBackPress() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.Go_Back))
            .setMessage(getString(R.string.Do_you_want_Exit))
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
               requireActivity().finishAffinity()
            }
            .setNegativeButton(getString(R.string.No), null)
            .show()
    }


    private fun isDashBoardData() {

        isMenuAdapter =
            SchoolMenuAdapter(requireActivity(), this, null, null, Constant.isShimmerViewShow)
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)

        // Adjust span count for special layout
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (isMenuAdapter.getItemViewType(position)) {
                    2 -> 3 // TYPE_AD: Span across all 3 columns
                    else -> 1 // Default: 1 span per item
                }
            }
        }

        binding.recyclerViewMenus.layoutManager = gridLayoutManager
        binding.recyclerViewMenus.adapter = isMenuAdapter

        appViewModel!!.isDashBoardData(
            access_token, Constant.staff_, requireActivity()

        )
    }

    private fun isGetAds() {
        activity?.let { safeActivity ->
            appViewModel?.isGetAds(access_token, "102", safeActivity)
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("Status", "onResume")
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
                Assignment::class.java
            }

            Constant.M_HOMEWORK -> {
                HomeWork::class.java
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
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
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
            Constant.M_EVENTS_HOLIDAYS -> CreateEvent::class.java
            Constant.M_SCHEDULE_EXAM_TEST -> Exam::class.java

            Constant.M_MESSAGES_FROM_MANAGEMENT -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
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
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    InteractionWithStudent::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        InteractionWithStudent::class.java
                    }
                }
            }

            Constant.M_ONLINE_MEETING -> OnlineMeeting::class.java
            Constant.M_DAILY_COLLECTION -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
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
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
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

                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
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
                    LessonPlan::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        //go to fee pending report
                        LessonPlan::class.java
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
                    LessonPlan::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        //go to ptm page
                        LessonPlan::class.java
                    }
                }
            }

            //
            Constant.M_ATTACHMENTS -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    //go to ptm page
                    LessonPlan::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        //go to ptm page
                        LessonPlan::class.java
                    }
                }
            }


            Constant.M_VERY_IMPORTANT_INFO -> ImportantInfo::class.java
            Constant.M_FEEDBACK -> ImportantInfo::class.java

            // Constant.sch_feedback_id -> ImportantInfo::class.java
            else -> null
        }

        activityClass?.let {
            startActivity(Intent(requireActivity(), it))
        }
    }

}