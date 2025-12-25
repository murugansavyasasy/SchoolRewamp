package com.vs.schoolmessenger.Dashboard.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.Ads.AdsDisplayOptions
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardCountData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.Parent.ChildMenuAdapter
import com.vs.schoolmessenger.Dashboard.Parent.ExamMark
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Dashboard.School.AutoScrollAdapterWithDots
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter
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
import com.vs.schoolmessenger.Parent.Timetable.TimeTable
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.FrequentParentlyUsedMenuItems
import com.vs.schoolmessenger.Utils.Constant.FrequentSchoollyUsedMenuItems
import com.vs.schoolmessenger.Utils.Constant.isParentAdItem
import com.vs.schoolmessenger.Utils.Constant.isParentContactDetails
import com.vs.schoolmessenger.Utils.Constant.isParentDashBoardData
import com.vs.schoolmessenger.Utils.Constant.isParentMenuCountDetails
import com.vs.schoolmessenger.Utils.Constant.isParentMenuDetails
import com.vs.schoolmessenger.Utils.Constant.isSchoolDashBoardData
import com.vs.schoolmessenger.Utils.Constant.isSchoolMenuCountDetails
import com.vs.schoolmessenger.Utils.Constant.isSchoolMenuDetails
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentHomeFragmentBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.view.isVisible

class ParentHomeFragment : Fragment(), View.OnClickListener, MenuClickListener {

    private lateinit var binding: ParentHomeFragmentBinding
    lateinit var isMenuAdapter: ChildMenuAdapter
    var childDetails: ChildDetails? = null
    var userDetails: UserDetails? = null
    private var appViewModel: App? = null
    var isDashBoardCountData: List<DashboardCountData>? = null
    var access_token = ""

    var isAdsDisplayOptions: AdsDisplayOptions? = null
    private lateinit var allMenuItems: List<MenuDetail>
    private val isMenuItems = mutableListOf<MenuDetail>()
    private val snapHelper = PagerSnapHelper()
    private lateinit var adapter: AutoScrollAdapterWithDots
    private lateinit var layoutManager: LinearLayoutManager

    private var currentPosition = 0
    private var mobile_number = ""
    private val REQUEST_CONTACT_PERMISSION = 1001

    private var originalMenuList = ArrayList<MenuDetail>()
    private var filteredMenuList = ArrayList<MenuDetail>()


    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = ParentHomeFragmentBinding.inflate(layoutInflater)
        binding.imgNotification.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)
        childDetails = SharedPreference.getChildDetails(requireActivity())
        userDetails = SharedPreference.getUserDetails(requireActivity())
        mobile_number = SharedPreference.getMobileNumber(requireActivity()).toString()
        access_token = childDetails!!.access_token
        binding.username.text = childDetails!!.name
        binding.lblSchoolName.text = childDetails!!.school_name
        if (childDetails!!.school_logo_url != "") {
            Glide.with(this)
                .load(childDetails!!.school_logo_url)
                .error(R.drawable.school_sample)
                .into(binding.profileImage)
        }
        Constant.checkBiometricSupport(requireActivity())
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        getGlobalVariables(access_token)
        appViewModel!!.isGlobalVariables?.observe(requireActivity()) { response ->
            if (response != null) {
                response.status
                response.message
                Constant.isGlobalVariableData = response.data[0]
                checkContactPermission()

            }
        }


        if (isParentDashBoardData == null || isParentDashBoardData!!.isEmpty()) {
            isDashBoardData()
        } else {
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

                    if (isDashboardResponse.isNotEmpty() && isDashboardResponse[0].is_birthday) {
                        showBirthdayPopup()
                    }

                    isParentContactDetails = isParentDashBoardData!![0].contactDetails
                    isParentMenuDetails = isParentDashBoardData!![0].menus
                    FrequentParentlyUsedMenuItems = isParentDashBoardData!![0].frequently_used
                    allMenuItems = isParentMenuDetails!!

                    originalMenuList.clear()
                    originalMenuList.addAll(isParentMenuDetails!!)

                    filteredMenuList.clear()
                    filteredMenuList.addAll(originalMenuList)

                    allMenuItems = filteredMenuList


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

        return binding.root
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

        isMenuAdapter.updateList(filteredMenuList)
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
        txtName.text = childDetails!!.name
        val currentDate = SimpleDateFormat("dd, MMM yyyy", Locale.getDefault()).format(Date())
        txtDate.text = currentDate
        Glide.with(this)
            .load(childDetails!!.profile)
            .error(R.drawable.default_profile)
            .into(imgProfile)
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
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.app_logo)
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

        if (!FrequentParentlyUsedMenuItems.isNullOrEmpty()) {
            binding.autoScrollRecyclerView.visibility = View.VISIBLE

            layoutManager =
                LinearLayoutManager(safeActivity, LinearLayoutManager.HORIZONTAL, false)
            binding.autoScrollRecyclerView.layoutManager = layoutManager

            adapter = AutoScrollAdapterWithDots(
                FrequentParentlyUsedMenuItems!!,
                isParentMenuCountDetails,
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

    private fun handleBackPress() {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.Go_Back))
            .setMessage(getString(R.string.Do_you_want_Exit))
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                requireActivity().finishAffinity()
            }.setNegativeButton(getString(R.string.No), null).show()
    }

    private fun isLoadData() {
        val safeActivity = activity ?: return

        isMenuAdapter = ChildMenuAdapter(
            safeActivity,
            this,
            filteredMenuList,
            isParentMenuCountDetails,
            Constant.isShimmerViewDisable
        )

        val gridLayoutManager = GridLayoutManager(safeActivity, 2)
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

            R.id.imgSearch -> {
                if (binding.rytsearch.isVisible) {
                    binding.rytsearch.visibility = View.GONE
                } else {
                    binding.rytsearch.visibility = View.VISIBLE
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
        Log.d("Loading", "Dashboard Data is Loading")
        isDashBoardData()
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
//            Constant.M_LEAVE_REQUEST -> Intent(requireActivity(), LeaveRequest::class.java)
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
//            Constant.M_PARENT_LEAVE_REQUEST -> Intent(requireActivity(), LeaveRequest::class.java)
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