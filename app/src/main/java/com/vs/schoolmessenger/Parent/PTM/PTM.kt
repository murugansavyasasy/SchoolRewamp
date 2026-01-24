package com.vs.schoolmessenger.Parent.PTM

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.PTM.Adapter.MeetingHistoryAdapter
import com.vs.schoolmessenger.Parent.PTM.Adapter.MeetingListItem
import com.vs.schoolmessenger.Parent.PTM.Adapter.ParentMeetingAdapter
import com.vs.schoolmessenger.Parent.PTM.Adapter.PtmParentCalender
import com.vs.schoolmessenger.Parent.PTM.Adapter.SubjectListWithClassTeacherAdapter
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingData
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingDataWrapper
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingItem
import com.vs.schoolmessenger.Parent.PTM.DataClass.SubjectData
import com.vs.schoolmessenger.Parent.PTM.Listener.OnCancelClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PtmBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PTM : BaseActivity<PtmBinding>(), View.OnClickListener, OnCancelClickListener {

    override fun getViewBinding(): PtmBinding {
        return PtmBinding.inflate(layoutInflater)
    }

    private var lastCancelledPosition: Int = -1
    var isSelectedDate = ""
    private val selectedSlotIds = mutableListOf<String>()
    lateinit var isMeetingHistoryAdapter: MeetingHistoryAdapter
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    var isClassTeacherId = "0"
    var isSubjectId = "0"
    var isManagement = false
    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    var userDetails: UserDetails? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        userDetails = SharedPreference.getUserDetails(this)
        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)

        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)

            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )

            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)
//            Constant.isParentMenuName = menu_name!!
            Constant.isSelectedMenuName = menu_name!!
        }



        binding.rytsearch.visibility = View.GONE
        binding.txtSearchMeeting.setText("")
        binding.lblScheduleMeeting.setOnClickListener(this)
        binding.lblBookSlots.setOnClickListener(this)
        binding.lblYourMeeting.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token
        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblStudentSection.text =
            childDetails?.standard_name + " - " + childDetails?.section_name
        isSelectedDate = Constant.getCurrentDate()

        binding.toolbarLayout.lblScheduleMeeting.setOnClickListener {
            isChangeBackGroundTab(binding.lblScheduleMeeting)
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        }
        binding.toolbarLayout.lblYourMeeting.setOnClickListener {
            isChangeBackGroundTab(binding.lblYourMeeting)
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        }


        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }
        isDateWiseSlotCount()
        isGetSubjectList()
        binding.txtSearchMeeting.addTextChangedListener { editable ->
            val query = editable.toString()
            if (::isMeetingHistoryAdapter.isInitialized) {
                isMeetingHistoryAdapter.filter.filter(query)
            }
        }

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytsearch.isVisible) {
                binding.rytsearch.visibility = View.GONE
                binding.txtSearchMeeting.setText("")
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMeeting.windowToken, 0)
            } else {
                binding.rytsearch.visibility = View.VISIBLE
                binding.txtSearchMeeting.setText("")
                binding.txtSearchMeeting.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.txtSearchMeeting, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        appViewModel?.isStudentSlotResponse?.observe(this) { response ->
            if (response != null) {

                if (response!!.status) {
                    if (response.data.isNotEmpty()) {
                        binding.rytNoDataFound.visibility = View.GONE
                        binding.recyclerViewSlots.visibility = View.VISIBLE
                        isLoadData(response.data)

                        val mobileNumber = SharedPreference.getMobileNumber(this)
                        val jsonObject = JsonObject().apply {
                            addProperty(APIKeyNames.mobile_number, mobileNumber)
                            addProperty(APIKeyNames.activity, Constant.add_points_view_ptm)
                            addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                            addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                        }
                        appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)


                    } else {
                        binding.rytNoDataFound.visibility = View.VISIBLE
                        binding.NoData.text = "No meeting found"
                        binding.recyclerViewSlots.visibility = View.GONE
                    }
                } else {
                    binding.NoData.text = response.message ?: "No meeting found"
                    binding.rytNoDataFound.visibility = View.VISIBLE
                    binding.recyclerViewSlots.visibility = View.GONE
                }
            }
        }

        appViewModel?.isSlotCancelByStudent?.observe(this) { response ->
            if (response != null) {
                if (response?.status == true) {
                    AlertDialog.Builder(this)
                        .setTitle("Success")
                        .setMessage(response.message)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            if (::isMeetingHistoryAdapter.isInitialized && lastCancelledPosition >= 0) {
                                isMeetingHistoryAdapter.removeItem(lastCancelledPosition)
                                lastCancelledPosition = -1
                                isMeetingHistoryList()
                                isScheduleCallList()

                                if (isMeetingHistoryAdapter.itemCount == 0) {
                                    binding.rytNoDataFound.visibility = View.VISIBLE
                                    binding.NoData.text = response.message ?: "No meeting found"
                                    binding.rcyMeetingHistory.visibility = View.GONE
                                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                                } else {
                                    binding.rytNoDataFound.visibility = View.GONE
                                    binding.rcyMeetingHistory.visibility = View.VISIBLE
                                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                                }
                            }
                        }
                        .show()
                }
            }
        }

        appViewModel?.isSlotBookingForStudent?.observe(this) { response ->
            if (response != null) {
                binding.rcyMeetingHistory.postDelayed({
                    Constant.hideLoading(this)
                    if (response?.status == true) {
                        AlertDialog.Builder(this)
                            .setMessage(response.message ?: "Slot booked successfully!")
                            .setCancelable(false)
                            .setPositiveButton("OK") { dlg, _ ->
                                dlg.dismiss()
                                isDateWiseSlotCount()
                                selectedSlotIds.clear()
                                binding.lblBookSlots.visibility = View.GONE
                                isScheduleCallList()
                            }
                            .show()
                    } else {
                        AlertDialog.Builder(this)
                            .setMessage(response?.message ?: "Booking failed!")
                            .setCancelable(false)
                            .setPositiveButton("OK") { dlg, _ -> dlg.dismiss() }
                            .show()
                    }
                }, 2000)
            }
        }


        appViewModel?.isSubjectResponse?.observe(this) { response ->
            if (response != null) {
                if (response?.status!!) {
                    isLoadSubjectList(response.data)
                }
            }
        }


        appViewModel?.isSlotCountResponse?.observe(this) { response ->
            val dates = generateDates(60)
            val adapter = PtmParentCalender(dates, response!!.data) { selectedDate ->
                isSelectedDate = selectedDate
                selectedSlotIds.clear()
                binding.lblBookSlots.visibility = View.GONE
                isScheduleCallList()
            }
            binding.recyclerViewDates.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerViewDates.adapter = adapter
//            val calendar = Calendar.getInstance()
//            val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
//            val todayMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
//            val todayPos = dates.indexOfFirst { it.first == todayMonth && it.second == todayDay }
//            if (todayPos != -1) {
//                adapter.setDefaultSelected(todayPos)
//                binding.recyclerViewDates.scrollToPosition(todayPos)
//            }

            //  Try to find previously selected date
            val selectedPos = isSelectedDate?.let { savedDate ->
                dates.indexOfFirst { (month, day, year) ->
                    formatDate(month, day, year) == savedDate
                }
            } ?: -1

            when {
                //  If old selected date exists → reselect it
                selectedPos != -1 -> {
                    adapter.setDefaultSelected(selectedPos)
                    binding.recyclerViewDates.scrollToPosition(selectedPos)
                }

                // Else → fallback to today
                else -> {
                    val calendar = Calendar.getInstance()
                    val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
                    val todayMonth =
                        SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)

                    val todayPos = dates.indexOfFirst {
                        it.first == todayMonth && it.second == todayDay
                    }

                    if (todayPos != -1) {
                        adapter.setDefaultSelected(todayPos)
                        binding.recyclerViewDates.scrollToPosition(todayPos)
                    }
                }
            }
        }


        appViewModel?.isSlotDetailsHistory?.observe(this) { response ->
            if (response != null && response.status) {
                if (response.data.isNotEmpty()) {
                    binding.lytList.visibility = View.GONE
                    binding.rcyMeetingHistory.visibility = View.VISIBLE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    isLoadMeetingData(response.data)
                } else {
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.text = "No meeting found"
                    binding.rcyMeetingHistory.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                }
            } else {
                binding.txtNoData.text = response?.message ?: "No meeting found"
                binding.lytList.visibility = View.VISIBLE
                binding.rcyMeetingHistory.visibility = View.GONE
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            }
        }
    }

    fun formatDate(month: String, day: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthToIndex(month))
        calendar.set(Calendar.DAY_OF_MONTH, day)

        return SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            .format(calendar.time)
    }

    fun monthToIndex(month: String): Int {
        return when (month.lowercase(Locale.ENGLISH)) {
            "jan", "january" -> 0
            "feb", "february" -> 1
            "mar", "march" -> 2
            "apr", "april" -> 3
            "may" -> 4
            "jun", "june" -> 5
            "jul", "july" -> 6
            "aug", "august" -> 7
            "sep", "sept", "september" -> 8
            "oct", "october" -> 9
            "nov", "november" -> 10
            "dec", "december" -> 11
            else -> 0
        }
    }


    fun isLoadData(data: List<MeetingData>) {
        val adapter = ParentMeetingAdapter(data, this) { meeting, slot, isSelected ->
            slot?.let {
                if (isSelected) {
                    if (!selectedSlotIds.contains(it.id)) {
                        selectedSlotIds.add(it.id)
                    } else {

                    }
                } else {
                    selectedSlotIds.remove(it.id)
                }
            }
            binding.lblBookSlots.visibility =
                if (selectedSlotIds.isNotEmpty()) View.VISIBLE else View.GONE

            println("Selected Slot IDs: $selectedSlotIds")
        }

        binding.recyclerViewSlots.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
        binding.recyclerViewSlots.adapter = adapter
        binding.recyclerViewSlots.setHasFixedSize(true)
    }

    fun isLoadSubjectList(data: List<SubjectData>) {
        val mutableList = mutableListOf<SubjectData>()
        mutableList.add(SubjectData(id = "0", name = "All Subjects"))
        mutableList.addAll(data)
        val adapter = SubjectListWithClassTeacherAdapter(this, mutableList)
        binding.spinnerType.adapter = adapter

        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()

                val selectedSubject = mutableList[position]
                val isSelectedType = parent.getItemAtPosition(position).toString()

                when (isSelectedType) {
                    "All Subject" -> {
                        isSubjectId = "0"
                        isClassTeacherId = "0"
                        isManagement = false
                    }

                    "Management" -> {
                        isSubjectId = "0"
                        isClassTeacherId = "0"
                        isManagement = true
                    }

                    "Class Teacher" -> {
                        isSubjectId = "0"
                        isClassTeacherId = selectedSubject.id
                        isManagement = false
                    }

                    else -> {
                        isSubjectId = selectedSubject.id
                        isClassTeacherId = "0"
                        isManagement = false
                    }
                }
                isScheduleCallList()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    fun isLoadMeetingData(data: List<MeetingDataWrapper>) {
        if (data.isNullOrEmpty()) {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        } else {

            binding.rcyMeetingHistory.layoutManager = LinearLayoutManager(this)
            val meetingItems = mutableListOf<MeetingListItem>()
            val todayMeetings = data.firstOrNull()?.today ?: emptyList()
            val upcomingMeetings = data.firstOrNull()?.upcoming ?: emptyList()
            val completedMeetings = data.firstOrNull()?.completed ?: emptyList()

            if (todayMeetings.isNotEmpty()) {
                meetingItems.add(MeetingListItem.Header("Today Meetings"))
                todayMeetings.forEach { meetingItems.add(MeetingListItem.Item(it)) }
            }
            if (upcomingMeetings.isNotEmpty()) {
                meetingItems.add(MeetingListItem.Header("Upcoming Meetings"))
                upcomingMeetings.forEach { meetingItems.add(MeetingListItem.Item(it)) }
            }
            if (completedMeetings.isNotEmpty()) {
                meetingItems.add(MeetingListItem.Header("Completed Meetings"))
                completedMeetings.forEach { meetingItems.add(MeetingListItem.Item(it)) }
            }

            val adapter = MeetingHistoryAdapter(meetingItems, this) { isEmpty ->
                binding.lytList.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.rcyMeetingHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }

            isMeetingHistoryAdapter = adapter
            binding.rcyMeetingHistory.adapter = adapter
        }
    }

    fun isScheduleCallList() {
        appViewModel!!.isSlotAvailableForStudent(
            isAccessToken!!,
            isSelectedDate,
            isSubjectId,
            isClassTeacherId,
            isManagement,
            this
        )
    }

    fun isMeetingHistoryList() {
        appViewModel!!.isSlotHistoryStudent(isAccessToken!!, this)
    }

    fun isGetSubjectList() {
        appViewModel!!.isSubjectListWithClassTeacher(isAccessToken!!, this)
    }

    fun isDateWiseSlotCount() {
        appViewModel!!.isSlotCountByDate(isAccessToken!!, this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
//            R.id.lblScheduleMeeting -> {
//                isChangeBackGroundTab(binding.lblScheduleMeeting)
//                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//
//            }
//
//            R.id.lblYourMeeting -> {
//                isChangeBackGroundTab(binding.lblYourMeeting)
//                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
//            }

            R.id.lblBookSlots -> {
                showSendConfirmationDialog(getString(R.string.are_you_sure_want_to_book_this_slots))
            }
        }
    }

    fun showSendConfirmationDialog(isMessage: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)

        lblSelectTarget.visibility = View.GONE
        alertMessage.text = isMessage

        okButton.setOnClickListener {
            alertDialog.dismiss()
            Constant.showLoading(this)
            val jsonObject = JsonObject()
            val jsonArray = JsonArray()
            for (i in selectedSlotIds.indices) {
                jsonArray.add(selectedSlotIds[i])
            }
            jsonObject.add("slot_ids", jsonArray)
            Log.d("isSelectedId", jsonObject.toString())
            appViewModel!!.isSlotBookingStudent(isAccessToken!!, jsonObject, this)
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    fun isChangeBackGroundTab(isSelectedTab: TextView) {
        Constant.hideKeyboardIfOpen(this)
        binding.lblScheduleMeeting.background = null
        binding.lblYourMeeting.background = null
        isSelectedTab.background = this.getDrawable(R.drawable.white_radious)
//        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(binding.txtSearchMeeting, InputMethodManager.SHOW_IMPLICIT)

        if (isSelectedTab == binding.lblYourMeeting) {
            binding.rytScheduleMeeting.visibility = View.GONE
            binding.rytYourMeeting.visibility = View.VISIBLE
            binding.toolbarLayout.imgBack.visibility = View.VISIBLE
            binding.rytsearch.visibility = View.GONE
            binding.txtSearchMeeting.setText("")

            isMeetingHistoryList()
        } else {
            binding.rytYourMeeting.visibility = View.GONE
            binding.rytScheduleMeeting.visibility = View.VISIBLE
            binding.toolbarLayout.imgBack.visibility = View.VISIBLE
            binding.rytsearch.visibility = View.GONE
            binding.txtSearchMeeting.setText("")
            isDateWiseSlotCount()
        }
    }

    fun generateDates(daysCount: Int): List<Triple<String, Int, Int>> { // month, day, year
        val list = mutableListOf<Triple<String, Int, Int>>()
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        repeat(daysCount) {
            val month = monthFormat.format(calendar.time)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val year = calendar.get(Calendar.YEAR)
            list.add(Triple(month, day, year))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return list
    }

    override fun onCancelClick(meeting: MeetingItem, position: Int, reason: String) {
        lastCancelledPosition = position
        val jsonObject = JsonObject().apply {
            addProperty("slot_id", meeting.id)
            addProperty("cancelled_reason", reason)
        }
        Log.d("CancelSlotRequest", jsonObject.toString())
        appViewModel!!.isSlotCancelByStudent(isAccessToken!!, jsonObject, this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}