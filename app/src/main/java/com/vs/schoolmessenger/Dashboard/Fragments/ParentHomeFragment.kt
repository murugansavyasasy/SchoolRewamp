package com.vs.schoolmessenger.Dashboard.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.Ads.AdsDisplayOptions
import com.vs.schoolmessenger.CommonScreens.MenuDetails.ContactDetails
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardCountData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuCountDetail
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.Parent.ChildMenuAdapter
import com.vs.schoolmessenger.Dashboard.Parent.ExamMark
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Dashboard.School.AutoScrollAdapterWithDots
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.Parent.Assignment.Assignment
import com.vs.schoolmessenger.Parent.Attachment.Attachment
import com.vs.schoolmessenger.Parent.Attendance.Attendance
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequest
import com.vs.schoolmessenger.Parent.Communication.CommunicationParent
import com.vs.schoolmessenger.Parent.Coupon.CouponView.CouponDashboardActivity
import com.vs.schoolmessenger.Parent.EBooks.Ebooks
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Event
import com.vs.schoolmessenger.Parent.FeeDetails.FeeDetails
import com.vs.schoolmessenger.Parent.Homework.HomeWork
import com.vs.schoolmessenger.Parent.InteractionWithStaff.InteractionWithStaff
import com.vs.schoolmessenger.Parent.LSRW.LSRW
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoard
import com.vs.schoolmessenger.Parent.PTM.PTM
import com.vs.schoolmessenger.Parent.QuizExam.Quiz
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequest
import com.vs.schoolmessenger.Parent.Timetable.TimeTable
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.FrequentParentlyUsedMenuItems
import com.vs.schoolmessenger.Utils.Constant.isParentAdItem
import com.vs.schoolmessenger.Utils.Constant.isParentContactDetails
import com.vs.schoolmessenger.Utils.Constant.isParentDashBoardData
import com.vs.schoolmessenger.Utils.Constant.isParentMenuCountDetails
import com.vs.schoolmessenger.Utils.Constant.isParentMenuDetails
import com.vs.schoolmessenger.Utils.ScrollItem
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentHomeFragmentBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ParentHomeFragment : Fragment(), View.OnClickListener, MenuClickListener {

    private lateinit var binding: ParentHomeFragmentBinding
    lateinit var isMenuAdapter: ChildMenuAdapter
    private lateinit var aditems: List<AdItem>
    private var isSearchVisible = false
    var childDetails: ChildDetails? = null
    var userDetails: UserDetails? = null
    private var appViewModel: App? = null
    var isDashBoardCountData: List<DashboardCountData>? = null
    private lateinit var items: List<ScrollItem>
    var isContactDetails: ContactDetails? = null
    var access_token = ""

    var isAdsDisplayOptions: AdsDisplayOptions? = null
    private lateinit var allMenuItems: List<MenuDetail>
    private val isMenuItems = mutableListOf<MenuDetail>()
    private val snapHelper = PagerSnapHelper()
    private lateinit var adapter: AutoScrollAdapterWithDots
    private lateinit var layoutManager: LinearLayoutManager

    private var currentPosition = 0
    private var mobile_number = ""


    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = ParentHomeFragmentBinding.inflate(layoutInflater)
        binding.imgNotification.setOnClickListener(this)
        childDetails = SharedPreference.getChildDetails(requireActivity())
        userDetails = SharedPreference.getUserDetails(requireActivity())
        mobile_number = SharedPreference.getMobileNumber(requireActivity()).toString()
        access_token = childDetails!!.access_token
        binding.username.text = childDetails!!.name
        binding.lblSchoolName.text = childDetails!!.school_name
        if(childDetails!!.school_logo_url != "") {
            Glide.with(this)
                .load(childDetails!!.school_logo_url)
                .error(R.drawable.school_sample)
                .into(binding.profileImage)
        }
        Constant.checkBiometricSupport(requireActivity())
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        if (isParentDashBoardData == null || isParentDashBoardData!!.isEmpty()) {
            isDashBoardData()
        }
        else{
            isLoadData()
            setupRecyclerView()
            appViewModel!!.isDashBoardCountData(
                access_token, Constant.parent, requireActivity()
            )
        }
        binding.imgBurgerMenu.setOnClickListener(this)
        binding.imgBurgerMenu.setOnClickListener {
            (activity as? ParentDashboard)?.openDrawer()
        }

        appViewModel!!.isDashBoardData?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    val isDashboardResponse = response.data
                    isParentDashBoardData = isDashboardResponse

                    isParentContactDetails = isParentDashBoardData!![0].contactDetails
                    isParentMenuDetails = isParentDashBoardData!![0].menus
                    FrequentParentlyUsedMenuItems = isParentDashBoardData!![0].frequently_used
                    allMenuItems = isParentMenuDetails!!

                    //We are saving the menu name in list to use anywhere
                    Constant.setMenuNames(allMenuItems)
                    isLoadData()
                    setupRecyclerView()

                    appViewModel!!.isDashBoardCountData(
                        access_token, Constant.parent, requireActivity()
                    )

                    Log.d("isMenuDetails", isParentMenuDetails!!.size.toString())

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
                    isParentMenuCountDetails = isDashBoardCountData!![0].menu_details
                   // isGetAds()
                    isLoadData()
                    setupRecyclerView()

                }
            }
        }

        appViewModel!!.isGetAds?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    isParentAdItem = response.data
                    val filteredAds = response.data.filter { it.id != null }
                    isAdsDisplayOptions = isParentAdItem!![0].ads_display_options
                    val adList: List<AdItem> = filteredAds.map { ad ->
                        AdItem(
                            ad.id!!, ad.name ?: "", ad.content_url ?: "", ad.redirect_url ?: ""
                        )
                    }
                    isParentAdItem = adList
                }
                isLoadData()
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (userDetails!!.is_parent && userDetails!!.is_staff ||  userDetails!!.staff_details.size > 1) {
                    val intent = Intent(requireActivity(), PrioritySelection::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }
                else {
                    handleBackPress()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return binding.root
    }

    private fun setupRecyclerView() {

        if (!FrequentParentlyUsedMenuItems.isNullOrEmpty()) {
            binding.autoScrollRecyclerView.visibility = View.VISIBLE

            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            binding.autoScrollRecyclerView.layoutManager = layoutManager

            adapter = AutoScrollAdapterWithDots(FrequentParentlyUsedMenuItems!!,isParentMenuCountDetails, this)
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
    private fun handleBackPress() {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.Go_Back))
            .setMessage(getString(R.string.Do_you_want_Exit))
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                requireActivity().finishAffinity()
            }.setNegativeButton(getString(R.string.No), null).show()
    }


    private fun isLoadData() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        isMenuAdapter = ChildMenuAdapter(
            requireActivity(),
            this,
            isParentMenuDetails,
            isParentMenuCountDetails,
            Constant.isShimmerViewDisable
        )
        binding.gridRecyclerView.layoutManager = gridLayoutManager
        binding.gridRecyclerView.adapter = isMenuAdapter
    }

    private fun isDashBoardData() {

        val adapter =
            ChildMenuAdapter(requireActivity(), this, null, null, Constant.isShimmerViewShow)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)

        binding.gridRecyclerView.layoutManager = gridLayoutManager
        binding.gridRecyclerView.adapter = adapter

        Log.d("isToken", childDetails!!.access_token)
        appViewModel!!.isDashBoardData(
            childDetails!!.access_token, Constant.parent, mobile_number, requireActivity()
        )
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
                Constant.isParentChoose = true
                val intent = Intent(requireActivity(), Notification::class.java)
                startActivity(intent)
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
        Log.d("Loading", "Dashboard Data is Loading")
        // isDashBoardData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Status", "onPause")
    }

    override fun onClick(data: MenuDetail) {
        val intent = when (data.id) {

            Constant.M_COMMUNICATION -> Intent(requireActivity(), CommunicationParent::class.java)
            Constant.M_HOMEWORK -> Intent(requireActivity(), HomeWork::class.java)
            Constant.M_NOTICEBOARD -> Intent(requireActivity(), NoticeBoard::class.java)
            Constant.M_PARENT_CLASS_EVENTS -> Intent(requireActivity(), Event::class.java)
            Constant.M_ATTENDANCE_REPORT -> Intent(requireActivity(), Attendance::class.java)
            Constant.M_LEAVE_REQUEST -> Intent(requireActivity(), LeaveRequest::class.java)
            Constant.M_FEE_DETAILS -> Intent(requireActivity(), FeeDetails::class.java)
            Constant.M_ATTACHMENTS -> Intent(requireActivity(), Attachment::class.java)
            Constant.M_INTERACTION_WITH_STAFF -> Intent(
                requireActivity(),
                InteractionWithStaff::class.java
            )
            Constant.M_ASSIGNMENT -> Intent(requireActivity(), Assignment::class.java)
            Constant.M_QUIZ_EXAM -> Intent(requireActivity(), Quiz::class.java)
            Constant.M_LSRW -> Intent(requireActivity(), LSRW::class.java)
            Constant.M_CLASS_TIME_TABLE -> Intent(requireActivity(), TimeTable::class.java)
            Constant.M_PARENT_LEAVE_REQUEST -> Intent(requireActivity(), LeaveRequest::class.java)
            Constant.M_CERTIFICATE_REQUEST -> Intent(
                requireActivity(),
                CertificateRequest::class.java
            )
            Constant.M_COUPON_PACKET -> Intent(
                requireActivity(),
                CouponDashboardActivity::class.java
            )
            Constant.M_EXAM -> Intent(
                requireActivity(),
                ExamMark::class.java
            )

            Constant.M_PTM -> Intent(
                requireActivity(),
                PTM::class.java
            )
            Constant.M_ONLINE_TEXT_BOOK -> Intent(
                requireActivity(),
                Ebooks::class.java
            )

            else -> null
        }
        intent?.let { requireActivity().startActivity(it) }
    }
}