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
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.Ads.AdsDisplayOptions
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.CommonScreens.MenuDetails.ContactDetails
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.AttendanceMark
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReport
import com.vs.schoolmessenger.School.Communication.CommunicationSchool
import com.vs.schoolmessenger.School.DailyCollection.DailyCollection
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.ExamSchedule.Exam
import com.vs.schoolmessenger.School.Homework.HomeWork
import com.vs.schoolmessenger.School.ImportantInfo.ImportantInfo
import com.vs.schoolmessenger.School.InteractionWithStudent.InteractionWithStudent
import com.vs.schoolmessenger.School.LessonPlan.LessonPlan
import com.vs.schoolmessenger.School.MessageFromManagement.MessageFromManagement
import com.vs.schoolmessenger.School.NoticeBoard.CreateNoticeBoard
import com.vs.schoolmessenger.School.OnlineMeeting.OnlineMeeting
import com.vs.schoolmessenger.School.SchoolStrength.SchoolStrength
import com.vs.schoolmessenger.School.StudentReport.StudentReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolHomeFragmentBinding


class SchoolHomeFragment : Fragment(), View.OnClickListener, MenuClickListener {

    private lateinit var binding: SchoolHomeFragmentBinding // Automatically generated binding class
    lateinit var isMenuAdapter: SchoolMenuAdapter

    //    private lateinit var items: ArrayList<GridItem>
//    private var isMenuItems: MutableList<GridItem> = mutableListOf()
    private var isSearchVisible = false
    private var appViewModel: App? = null


    private lateinit var aditems: List<AdItem>
    var userDetails: UserDetails? = null
    var isDashBoardData: List<DashboardData>? = null
    var isContactDetails: ContactDetails? = null
    var isMenuDetails: List<MenuDetail>? = null

    var isAdItem: List<AdItem>? = null
    var isAdsList: List<AdItem>? = null
    var isAdsDisplayOptions: AdsDisplayOptions? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SchoolHomeFragmentBinding.inflate(layoutInflater)
        binding.imgNotification.setOnClickListener(this)
        binding.imgSearchClick.setOnClickListener(this)

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel!!.init()

        userDetails = SharedPreference.getUserDetails(requireActivity())
        isDashBoardData()
        binding.lblSchoolAddress.text = userDetails!!.staff_details[0].school_address
        Glide.with(requireActivity()).load(userDetails!!.staff_details[0].school_logo)
            .into(binding.imgSchoolLogo)

        if (userDetails!!.staff_details.size > 1) {
            binding.lblSchoolName.text = userDetails!!.role_name
        } else {
            binding.lblSchoolName.text = userDetails!!.staff_details[0].school_name
        }

        binding.lblViewDetails.paintFlags =
            binding.lblViewDetails.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        binding.lblGif.playAnimation()
        binding.lblGif.setAnimation(R.raw.mathematics)


        appViewModel!!.isDashBoardData?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    val isDashboardResponse = response.data
                    isDashBoardData = isDashboardResponse
                    isContactDetails = isDashBoardData!![0].contactDetails
                    isMenuDetails = isDashBoardData!![0].menuDetails
                    isGetAds()
                }
            }
        }

        appViewModel!!.isGetAds?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
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
                                ad.id!!,
                                ad.name ?: "",
                                ad.content_url ?: "",
                                ad.redirect_url ?: ""
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
                //   filter(s.toString())
            }
        })
        return binding.root
    }

    private fun isLoadData() {
        val adapter =
            SchoolMenuAdapter(requireActivity(), this, null, null, Constant.isShimmerViewShow)
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

        Constant.executeAfterDelay {
            val isAdapter = SchoolMenuAdapter(
                requireActivity(), this, isMenuDetails, isAdItem, Constant.isShimmerViewDisable
            )
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
    }

//    @SuppressLint("NotifyDataSetChanged")
//    private fun filter(text: String) {
//        isMenuDetails.clear()
//        if (text.isEmpty()) {
//            isMenuDetails.addAll(items)  // If search is empty, show all items
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


    private fun isDashBoardData() {
//        val isToken = SharedPreference.getChildDetails(requireActivity())
        val isToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdGFmZl9pZCI6IjEwMDY5NTAzIiwic2Nob29sX2lkIjoiNTUxMiIsImlhdCI6MTc0MzQyMjM3N30.zk7XLtGdGh5slNHmeH0KCe7K1Mv6sQ0YFbuYSlP9GcM"
//        Log.d("isToken", isToken!!.access_token)
        appViewModel!!.isDashBoardData(
            isToken, "staff", requireActivity()
        )
    }

    private fun isGetAds() {
//        val isToken = SharedPreference.getChildDetails(requireActivity())
//        Log.d("isToken", isToken!!.access_token)
        val isToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdGFmZl9pZCI6IjEwMDY5NTAzIiwic2Nob29sX2lkIjoiNTUxMiIsImlhdCI6MTc0MzQyMjM3N30.zk7XLtGdGh5slNHmeH0KCe7K1Mv6sQ0YFbuYSlP9GcM"
        appViewModel!!.isGetMenuId(
            isToken, "101", requireActivity()
        )
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
            0 -> CommunicationSchool::class.java
            22 -> SchoolList::class.java
            9 -> HomeWork::class.java
            12 -> AttendanceMark::class.java
            6 -> AbsenteesReport::class.java
            7 -> SchoolStrength::class.java
            3 -> CreateNoticeBoard::class.java
            4 -> CreateEvent::class.java
            11 -> Exam::class.java
            13 -> MessageFromManagement::class.java
            16 -> InteractionWithStudent::class.java
            26 -> OnlineMeeting::class.java
            28 -> DailyCollection::class.java
            29 -> StudentReport::class.java
            30 -> LessonPlan::class.java
            21 -> ImportantInfo::class.java
//          14 -> ImportantInfo::class.java
            else -> null
        }

        activityClass?.let {
            startActivity(Intent(requireActivity(), it))
        }
    }
}