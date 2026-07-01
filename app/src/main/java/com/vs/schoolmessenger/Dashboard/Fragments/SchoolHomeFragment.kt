package com.vs.schoolmessenger.Dashboard.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.Ads.AdsDisplayOptions
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardCountData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.AutoScrollAdapterWithDots
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.Parent.BusTracking.BusList
import com.vs.schoolmessenger.Parent.Coupon.CouponView.CouponDashboardActivity
import com.vs.schoolmessenger.Parent.EBooks.Ebooks
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.AttendanceMark
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReport
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.ApproveStaffLeaveRequest
import com.vs.schoolmessenger.School.Assignment.AssignmentCreate
import com.vs.schoolmessenger.School.Attachment.Attachment
import com.vs.schoolmessenger.School.AttendanceReportFromStaff.AttendanceReportFromStaff
import com.vs.schoolmessenger.School.ClassTest.Standard.StandardActivity
import com.vs.schoolmessenger.School.Communication.CommunicationSchool
import com.vs.schoolmessenger.School.DailyCollection.DailyCollection
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.ExamMarkUpload.ClassList.ClassList
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReport
import com.vs.schoolmessenger.School.Homework.HomeWorkCreate
import com.vs.schoolmessenger.School.Hostel.HostelList
import com.vs.schoolmessenger.School.ImportantInfo.ImportantInfo
import com.vs.schoolmessenger.School.InteractionWithStudent.InteractionWithStudent
import com.vs.schoolmessenger.School.LSRW.LsrwMain
import com.vs.schoolmessenger.School.LeaveRequests.LeaveRequests
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary.LessonPlan
import com.vs.schoolmessenger.School.MarkYourAttendance.MarkYourAttendance
import com.vs.schoolmessenger.School.MessageFromManagement.MessageFromManagement
import com.vs.schoolmessenger.School.NoticeBoard.CreateNoticeBoard
import com.vs.schoolmessenger.School.NoticeBoard.NoticeBoardReport
import com.vs.schoolmessenger.School.PTM.Activity.PTM
import com.vs.schoolmessenger.School.QuizExam.ExamQuiz
import com.vs.schoolmessenger.School.SchoolNeeds.SchoolNeeds
import com.vs.schoolmessenger.School.SchoolStrength.SchoolStrength
import com.vs.schoolmessenger.School.StaffLeaveRequest.StaffLeaveRequest
import com.vs.schoolmessenger.School.StudentReport.StudentReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.FrequentSchoollyUsedMenuItems
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.Constant.isSchoolAdItem
import com.vs.schoolmessenger.Utils.Constant.isSchoolContactDetails
import com.vs.schoolmessenger.Utils.Constant.isSchoolDashBoardData
import com.vs.schoolmessenger.Utils.Constant.isSchoolMenuCountDetails
import com.vs.schoolmessenger.Utils.Constant.isSchoolMenuDetails
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolHomeFragmentBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.jvm.java

class SchoolHomeFragment : Fragment(), View.OnClickListener, MenuClickListener {

    private lateinit var binding: SchoolHomeFragmentBinding
    private var isMenuAdapter: SchoolMenuAdapter? = null
    private var appViewModel: App? = null
    var userDetails: UserDetails? = null
    var staffDetails: StaffDetails? = null
    var isDashBoardCountData: List<DashboardCountData>? = null
    var isAdsDisplayOptions: AdsDisplayOptions? = null
    var access_token = ""
    private lateinit var allMenuItems: List<MenuDetail>
    private lateinit var adapter: AutoScrollAdapterWithDots
    private lateinit var layoutManager: LinearLayoutManager
    private val snapHelper = PagerSnapHelper()
    private var currentPosition = 0
    private var mobile_number = ""
    private val REQUEST_CONTACT_PERMISSION = 1001

    private var originalMenuList = ArrayList<MenuDetail>()
    private var filteredMenuList = ArrayList<MenuDetail>()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SchoolHomeFragmentBinding.inflate(inflater)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        Constant.checkBiometricSupport(requireActivity())
        binding.imgSearch.setOnClickListener(this)
        mobile_number = SharedPreference.getMobileNumber(requireActivity()).toString()
        userDetails = SharedPreference.getUserDetails(requireActivity())
        staffDetails = SharedPreference.getStaffDetails(requireActivity())

        if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
            access_token = staffDetails!!.access_token
            binding.lblSchoolName.text = staffDetails!!.school_name
            binding.username.text = userDetails!!.staff_details[0].name
            binding.lblRole.text = userDetails!!.staff_details[0].role
            binding.profileImage.visibility = View.VISIBLE

            if (staffDetails!!.school_logo != "") {
                Glide.with(this)
                    .load(staffDetails!!.school_logo)
                    .error(R.drawable.school_sample)
                    .into(binding.profileImage)
            }
        } else {
            access_token = userDetails!!.staff_details[0].access_token
            if (userDetails!!.staff_details.size > 1) {
                binding.username.text = userDetails!!.staff_details[0].name
                binding.lblRole.text = userDetails!!.staff_details[0].role
                binding.lblSchoolName.visibility = View.GONE
                binding.profileImage.visibility = View.GONE
            } else {
                binding.lblSchoolName.visibility = View.VISIBLE
                binding.profileImage.visibility = View.VISIBLE
                binding.lblSchoolName.text = userDetails!!.staff_details[0].school_name
                binding.username.text = userDetails!!.staff_details[0].name
                binding.lblRole.text = userDetails!!.staff_details[0].role

                if (userDetails!!.staff_details[0].school_logo != "") {
                    Glide.with(this)
                        .load(userDetails!!.staff_details[0].school_logo)
                        .error(R.drawable.school_sample)
                        .into(binding.profileImage)
                }
            }
        }
        getGlobalVariables(access_token)
        appViewModel!!.isGlobalVariables?.observe(requireActivity()) { response ->
            if (response != null) {
                response.status
                response.message
                Constant.isGlobalVariableData = response.data[0]
                SharedPreference.putGlobalvariables(requireActivity(), response.data[0])
                checkContactPermission()
            }
        }

        appViewModel!!.isGetAcademicList?.observe(requireActivity()) { response ->
            response?.data?.let { academicList ->
                val data = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYearList == data) return@observe
                isAcademicYearList = data
                val activeYear = data.find { it.current_academic_year == true }
                Constant.isCurrentAcademicYearId = activeYear?.id!!
            }
        }

        binding.imgBurgerMenu.setOnClickListener(this)
        binding.imgNotification.setOnClickListener(this)

        binding.profileImage.setOnClickListener {
            val imageUrl = if (userDetails!!.staff_role == Constant.isStaffRole) {
                staffDetails?.school_logo
            } else {
                userDetails!!.staff_details[0].school_logo
            }
            if (imageUrl.isNullOrEmpty()) return@setOnClickListener
            Constant.showImagePreview(requireContext(),imageUrl)
        }

        binding.imgBurgerMenu.setOnClickListener {
            (activity as? SchoolDashboard)?.openDrawer()
        }

        appViewModel!!.isDashBoardData?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    binding.gridRecyclerView.visibility = View.VISIBLE
                    binding.rytNORecordFound.visibility = View.GONE
                    val isDashboardResponse = response.data
                    isSchoolDashBoardData = isDashboardResponse

                    if (isDashboardResponse.isNotEmpty() && isDashboardResponse[0].is_birthday) {
                        showBirthdayPopup()
                    }
                    Log.d("DashboardDataMenus", "DashboardData")
                    isSchoolContactDetails = isSchoolDashBoardData!![0].contactDetails
                    val safeActivity = activity ?: return@observe

                    appViewModel!!.isDashBoardCountData(
                        access_token, Constant.staff_, safeActivity
                    )
                    isSchoolMenuDetails = isSchoolDashBoardData!![0].menus
                    FrequentSchoollyUsedMenuItems = isSchoolDashBoardData!![0].frequently_used
                    originalMenuList.clear()
                    originalMenuList.addAll(isSchoolMenuDetails!!)
                    //Hardcode from here
                    //Added the hardcode data
//                    originalMenuList.add(
//                        MenuDetail(
//                            id = 789,
//                            name = "Class create",
//                            description = "Creating Standard and Section"
//                        )
//                    )

//                                        originalMenuList.add(
//                        MenuDetail(
//                            id = 205,
//                            name = "Bus Live Tracking",
//                            description = "Monitor and manage student data"
//                        )
//                    )

                    //Hardcode till here
                    filteredMenuList.clear()
                    filteredMenuList.addAll(originalMenuList)

                    allMenuItems = filteredMenuList
                    Constant.setMenuNames(allMenuItems)
                    isLoadData()
                    setupRecyclerView()
                } else {
                    binding.gridRecyclerView.visibility = View.GONE
                    binding.rytNORecordFound.visibility = View.VISIBLE
                }
            }
        }

        appViewModel!!.isDashBoardCountData?.observe(requireActivity()) { response ->
            if (response != null) {
                val safeActivity = activity ?: return@observe
                Constant.hideLoadingEnable(safeActivity)
                val status = response.status
                response.message
                if (status) {
                    val isDashboardResponse = response.data
                    isDashBoardCountData = isDashboardResponse
                    isSchoolMenuCountDetails = isDashBoardCountData!![0].menu_details
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
                if (userDetails!!.is_parent && userDetails!!.is_staff || userDetails!!.staff_details.size > 1) {
                    val intent = Intent(requireActivity(), PrioritySelection::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                } else {
                    handleBackPress()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDashboardMenu(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        isGetAcademicYear()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        Constant.showLoadingDisableScreen(requireActivity())
    }

    private fun isGetAcademicYear() {
        appViewModel!!.isGetAcademicYear(
            access_token!!, requireActivity()
        )
    }

    private fun filterDashboardMenu(query: String) {
        filteredMenuList.clear()

        if (query.isEmpty()) {
            filteredMenuList.addAll(originalMenuList)
        } else {
            val searchText = query.lowercase()

            originalMenuList.forEach { menu ->
                if (
                    menu.name.lowercase().contains(searchText) ||
                    menu.description.lowercase().contains(searchText)
                ) {
                    filteredMenuList.add(menu)
                }
            }
        }

        if (filteredMenuList.isEmpty()) {
            binding.gridRecyclerView.visibility = View.GONE
            binding.rytNORecordFound.visibility = View.VISIBLE
        } else {
            binding.gridRecyclerView.visibility = View.VISIBLE
            binding.rytNORecordFound.visibility = View.GONE
        }

        isMenuAdapter!!.updateList(filteredMenuList)
    }


    private fun showBirthdayPopup() {
        val inflater = LayoutInflater.from(requireActivity())
        val view = inflater.inflate(R.layout.birthday_popup, null)

        val rootView = requireActivity().findViewById<ViewGroup>(android.R.id.content)
        val dimView = View(requireActivity()).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        rootView.addView(dimView)
        rootView.addView(view, layoutParams)

        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val imgProfile: ImageView = view.findViewById(R.id.imgProfile)
        val lottieView: LottieAnimationView = view.findViewById(R.id.lottieBirthday)

        lottieView.playAnimation()

        if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
            txtName.text = staffDetails!!.name
            Glide.with(this)
                .load(staffDetails!!.staff_profile)
                .error(R.drawable.default_profile)
                .into(imgProfile)
        } else {
            txtName.text = userDetails!!.staff_details[0].name
            Glide.with(this)
                .load(userDetails!!.staff_details[0].staff_profile)
                .error(R.drawable.default_profile)
                .into(imgProfile)
        }

        val currentDate = SimpleDateFormat("dd, MMM yyyy", Locale.getDefault()).format(Date())
        txtDate.text = currentDate

        dimView.setOnClickListener {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }
        view.alpha = 0f
        view.animate().alpha(1f).setDuration(300).start()
    }


    private fun getGlobalVariables(token: String) {
        val jsonObject = JsonObject()
        val jsonArray = JsonArray()
        jsonObject.add("key_names", jsonArray)
        appViewModel!!.isGetGlobalVariables(jsonObject, token, requireActivity())
    }

    private fun checkContactPermission() {
        val safeActivity = activity ?: return

        if (ContextCompat.checkSelfPermission(safeActivity, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                safeActivity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CONTACT_PERMISSION
            )
        } else {
            if (!Constant.isGlobalVariableData!!.v_card_numbers.equals("")) {

                val contacts = mutableListOf<Pair<String, String>>()

                val numbers = Constant.isGlobalVariableData!!.v_card_numbers.split(",")
                for (item in numbers) {
                    contacts.add(
                        Pair(
                            Constant.isGlobalVariableData!!.contact_display_name,
                            item.trim()
                        )
                    )
                }
                val missingContacts = contacts.filterNot { contactExists(it.second) }
                if (missingContacts.isNotEmpty()) {
                    saveContactsPopup(missingContacts)
                }

            }
        }
    }

    private fun contactExists(phoneNumber: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        val projection = arrayOf(ContactsContract.PhoneLookup._ID)
        var exists = false
        val resolver = requireActivity().contentResolver
        val cursor = resolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                exists = true
            }
        }
        return exists
    }

    private fun saveContactsPopup(missingContacts: List<Pair<String, String>>) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.save_contact_popup, null)

        val alertTitle: TextView = view.findViewById(R.id.alertTitle)
        val alertMessage: TextView = view.findViewById(R.id.alertMessage)
        alertTitle.text = Constant.isGlobalVariableData!!.contact_alert_title
        alertMessage.text = Constant.isGlobalVariableData!!.contact_alert_content

        val btnSave: TextView = view.findViewById(R.id.lblSave)
        val btnNo: TextView = view.findViewById(R.id.lblNo)

        val rootView = requireActivity().findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true // prevent clicks on background
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, requireActivity().resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        btnSave.setOnClickListener {
            closePopup()
            saveContacts(missingContacts)
        }
        btnNo.setOnClickListener {
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true
    }

    private fun saveContacts(missingContacts: List<Pair<String, String>>) {
        val newContacts = Array(missingContacts.size) { "" }
        // Loop through and check which contacts are missing
        for (i in missingContacts.indices) {
            val contact = missingContacts[i]
            if (!contactExists(contact.second)) {
                Log.d("Index", "Current index = $i")
                newContacts[i] = contact.second
            }
        }

        // Convert image to byte array (for contact photo)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.school_splash_logo)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        val data = ArrayList<ContentValues>()

        // Add contact photo
        val rowPhoto = ContentValues().apply {
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArray)
        }
        data.add(rowPhoto)

        // Add all phone numbers
        for (i in newContacts.indices) {
            val number = newContacts[i]
            if (number.isNotEmpty()) {
                val rowNumber = ContentValues().apply {
                    put(
                        ContactsContract.RawContacts.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    put(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
                }
                data.add(rowNumber)
            }
        }

        // Prepare Intent to insert contact (user will confirm)
        val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
        intent.putExtra(
            ContactsContract.Intents.Insert.NAME,
            Constant.isGlobalVariableData!!.contact_display_name
        ) // set contact name
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data)

        startActivityForResult(intent, 100)
    }

    private fun setupRecyclerView() {
        val safeActivity = activity ?: return

        if (!FrequentSchoollyUsedMenuItems.isNullOrEmpty()) {
            binding.autoScrollRecyclerView.visibility = View.VISIBLE

            layoutManager =
                LinearLayoutManager(safeActivity, LinearLayoutManager.HORIZONTAL, false)
            binding.autoScrollRecyclerView.layoutManager = layoutManager

            adapter = AutoScrollAdapterWithDots(
                FrequentSchoollyUsedMenuItems!!,
                isSchoolMenuCountDetails,
                this
            )
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

    private fun isLoadData() {
        val safeActivity = activity ?: return

        isMenuAdapter = SchoolMenuAdapter(
            safeActivity,
            this,
            filteredMenuList,
            isSchoolMenuCountDetails,
            Constant.isShimmerViewDisable
        )

        val gridLayoutManager = GridLayoutManager(safeActivity, 2)
        binding.gridRecyclerView.layoutManager = gridLayoutManager
        binding.gridRecyclerView.adapter = isMenuAdapter
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgNotification -> {
                val intent = Intent(requireActivity(), Notification::class.java)
                startActivity(intent)
            }

            R.id.imgSearch -> {
                if (binding.rytsearch.isVisible) {
                    hideKeyboard(binding.edtSearch)
                    binding.rytsearch.visibility = View.GONE
                    binding.edtSearch.text.clear()
                } else {
                    binding.rytsearch.visibility = View.VISIBLE
                    binding.edtSearch.requestFocus()
                }
            }

        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext()
            .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
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
        isDashBoardData()
        setupRecyclerView()
        Log.d("Loading", "Dashboard Data is Refreshed")
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    override fun onClick(data: MenuDetail) {

        val activityClass = when (data.id) {
            Constant.M_COMMUNICATION -> CommunicationSchool::class.java
            Constant.M_ASSIGNMENT -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    AssignmentCreate::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        AssignmentCreate::class.java
                    }
                }
            }

            Constant.M_HOMEWORK -> {

                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    HomeWorkCreate::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        HomeWorkCreate::class.java
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

            Constant.M_NOTICEBOARD -> {
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    NoticeBoardReport::class.java
                } else {
                    CreateNoticeBoard::class.java
                }
            }

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
                MessageFromManagement::class.java
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
                    AttendanceReportFromStaff::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        AttendanceReportFromStaff::class.java
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
                if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                    LeaveRequests::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        LeaveRequests::class.java
                    }
                }
            }

            Constant.M_VERY_IMPORTANT_INFO -> ImportantInfo::class.java
            Constant.M_ONLINE_TEXT_BOOK -> Ebooks::class.java

            Constant.M_FEEDBACK -> ImportantInfo::class.java
            Constant.M_SCHOOL_NEEDS -> SchoolNeeds::class.java

            Constant.M_LSRW -> {
                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    LsrwMain::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        LsrwMain::class.java
                    }
                }
            }

            Constant.M_UPLOAD_MARKS -> {
                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    ClassList::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        ClassList::class.java
                    }
                }
            }

            Constant.M_COUPON_PACKET -> {
                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    CouponDashboardActivity::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        CouponDashboardActivity::class.java
                    }
                }
            }

            Constant.M_STAFF_LEAVE_REQUEST -> {
                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    StaffLeaveRequest::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        StaffLeaveRequest::class.java
                    }
                }
            }

            Constant.M_APPROVE_STAFF_LEAVE_REQUEST -> {

                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    ApproveStaffLeaveRequest::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        ApproveStaffLeaveRequest::class.java
                    }
                }
            }

            Constant.M_HOSTEL-> {

                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    HostelList::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        HostelList::class.java
                    }
                }
            }

            Constant.M_LIVE_BUS_TRACKING-> {

                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    BusList::class.java
                } else {
                    if (userDetails!!.staff_details.size > 1) {
                        SchoolList::class.java
                    } else {
                        BusList::class.java
                    }
                }
            }

            Constant.M_STUDENTDATE-> {
                StandardActivity::class.java
            }

            else -> null
        }
        activityClass?.let {
            startActivity(Intent(requireActivity(), it))
        }
    }
}