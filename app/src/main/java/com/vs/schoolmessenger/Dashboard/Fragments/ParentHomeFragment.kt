package com.vs.schoolmessenger.Dashboard.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.Ads.AdsDisplayOptions
import com.vs.schoolmessenger.CommonScreens.MenuDetails.ContactDetails
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.Dashboard.Parent.ChildMenuAdapter
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.Parent.Assignment.Assignment
import com.vs.schoolmessenger.Parent.Attendance.AttendanceReport
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequest
import com.vs.schoolmessenger.Parent.Communication.CommunicationParent
import com.vs.schoolmessenger.Parent.EventsHolidays.Event
import com.vs.schoolmessenger.Parent.FeeDetails.FeeDetails
import com.vs.schoolmessenger.Parent.Homework.HomeWork
import com.vs.schoolmessenger.Parent.InteractionWithStaff.InteractionWithStaff
import com.vs.schoolmessenger.Parent.LSRW.LSRW
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoard
import com.vs.schoolmessenger.Parent.OnlineMeeting.OnlineMeeting
import com.vs.schoolmessenger.Parent.QuizExam.Quiz
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequest
import com.vs.schoolmessenger.Parent.Timetable.TimeTable
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamSchedule.Exam
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentHomeFragmentBinding

class ParentHomeFragment : Fragment(), View.OnClickListener, MenuClickListener {

    private lateinit var binding: ParentHomeFragmentBinding
    lateinit var isMenuAdapter: ChildMenuAdapter
    private lateinit var aditems: List<AdItem>
    private var isSearchVisible = false
    var childDetails: ChildDetails? = null
    var userDetails: UserDetails? = null
    private var appViewModel: App? = null
    var isDashBoardData: List<DashboardData>? = null
    var isContactDetails: ContactDetails? = null
    var isMenuDetails: List<MenuDetail>? = null
    var isAdItem: List<AdItem>? = null
    var isAdsDisplayOptions: AdsDisplayOptions? = null


    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = ParentHomeFragmentBinding.inflate(layoutInflater)
        binding.imgNotification.setOnClickListener(this)
        binding.imgSearchClick.setOnClickListener(this)
        binding.lblChangeRoll.setOnClickListener(this)
        childDetails = SharedPreference.getChildDetails(requireActivity())
        userDetails = SharedPreference.getUserDetails(requireActivity())

        binding.lblStudentName.text = getString(R.string.Hello) + childDetails!!.name
        binding.lblSchoolName.text = childDetails!!.school_name
        binding.lblSchoolAddress.text = childDetails!!.student_address
        binding.lblChangeRoll.paintFlags =
            binding.lblChangeRoll.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel!!.init()
        isDashBoardData()

        if (userDetails!!.is_parent && userDetails!!.is_staff) {
            binding.lblChangeRoll.visibility = View.VISIBLE
        } else {
            if (userDetails!!.child_details.size > 1) {
                binding.lblChangeRoll.visibility = View.VISIBLE
            } else {
                binding.lblChangeRoll.visibility = View.GONE
            }
        }


        binding.lblViewDetails.paintFlags =
            binding.lblViewDetails.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        binding.lblViewDetails.setOnClickListener {
            this.startActivity(
                Intent(
                    requireActivity(), AttendanceReport::class.java
                )
            )
        }

        binding.lblGif.playAnimation()
        binding.lblGif.setAnimation(R.raw.mathematics)

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // filter(s.toString())
            }
        })

        appViewModel!!.isDashBoardData?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    val isDashboardResponse = response.data
                    isDashBoardData = isDashboardResponse
                    isContactDetails = isDashBoardData!![0].contactDetails
                    isMenuDetails = isDashBoardData!![0].menuDetails
                    Log.d("isMenuDetails", isMenuDetails!!.size.toString())
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

                    if (status) {
                        val filteredAds = response.data.filter { it.id != null }
                        isAdsDisplayOptions = isAdItem!![0].ads_display_options
                        val adList: List<AdItem> = filteredAds.map { ad ->
                            AdItem(
                                ad.id!!, ad.name ?: "", ad.content_url ?: "", ad.redirect_url ?: ""
                            )
                        }
                        isAdItem = adList
                        isLoadData()
                    }
                }
            }
        }

        return binding.root
    }

    private fun isLoadData() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)

//        Constant.executeAfterDelay {
        val isAdapter = ChildMenuAdapter(
            requireActivity(), this, isMenuDetails, isAdItem, Constant.isShimmerViewDisable
        )
//            Log.d("aditems", aditems.size.toString())
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
//        }
    }

    private fun isDashBoardData() {

        val adapter =
            ChildMenuAdapter(requireActivity(), this, null, null, Constant.isShimmerViewShow)
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)

        // Adjust span count for special layout
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    2 -> 3 // TYPE_AD: Span across all 3 columns
                    else -> 1 // Default: 1 span per item
                }
            }
        }

        binding.recyclerViewMenus.layoutManager = gridLayoutManager
        binding.recyclerViewMenus.adapter = adapter



        Log.d("isToken", childDetails!!.access_token)
        appViewModel!!.isDashBoardData(
            childDetails!!.access_token, Constant.parent, requireActivity()
        )
    }


//    @SuppressLint("NotifyDataSetChanged")
//    private fun filter(text: String) {
//        isMenuItems.clear()
//        if (text.isEmpty()) {
//            isMenuItems.addAll(items)  // If search is empty, show all items
//        } else {
//            for (item in items) {
//                if (item.title.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
//                    isMenuItems.add(item)  // Add the matching GridItem to filteredList
//                }
//            }
//        }
//        isMenuAdapter.notifyDataSetChanged()
//    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgNotification -> {
                val intent = Intent(requireActivity(), Notification::class.java)
                startActivity(intent)
            }

            R.id.lblChangeRoll -> {
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

    private fun isGetAds() {
        activity?.let { safeActivity ->
            appViewModel?.isGetAds(childDetails!!.access_token, "102", safeActivity)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Status", "onPause")
    }

    override fun onClick(data: MenuDetail) {
        val intent = when (data.id) {

            Constant.M_COMMUNICATION -> Intent(requireActivity(), CommunicationParent::class.java)
            Constant.M_HOMEWORK -> Intent(requireActivity(), HomeWork::class.java)
            Constant.M_EXAM -> Intent(requireActivity(), Exam::class.java)
            Constant.M_NOTICEBOARD -> Intent(requireActivity(), NoticeBoard::class.java)
            Constant.M_EVENTS_HOLIDAYS -> Intent(requireActivity(), Event::class.java)
            Constant.M_ATTENDANCE_REPORT -> Intent(requireActivity(), AttendanceReport::class.java)
            Constant.M_LEAVE_REQUEST -> Intent(requireActivity(), LeaveRequest::class.java)
            Constant.M_FEE_DETAILS -> Intent(requireActivity(), FeeDetails::class.java)
            Constant.M_INTERACTION_WITH_STAFF -> Intent(
                requireActivity(),
                InteractionWithStaff::class.java
            )
//            15 -> Intent(requireActivity(), OnlineTextBook::class.java)
            Constant.M_ASSIGNMENT -> Intent(requireActivity(), Assignment::class.java)
//            19 -> Intent(requireActivity(), Attachments::class.java)
            Constant.M_ONLINE_MEETING -> Intent(requireActivity(), OnlineMeeting::class.java)
            Constant.M_QUIZ_EXAM -> Intent(requireActivity(), Quiz::class.java)
            Constant.M_LSRW -> Intent(requireActivity(), LSRW::class.java)
            Constant.M_CLASS_TIME_TABLE -> Intent(requireActivity(), TimeTable::class.java)
//            24 -> Intent(requireActivity(), UserProfile::class.java)
            Constant.M_CERTIFICATE_REQUEST -> Intent(
                requireActivity(),
                CertificateRequest::class.java
            )

            else -> null
        }
        intent?.let { requireActivity().startActivity(it) }
    }
}