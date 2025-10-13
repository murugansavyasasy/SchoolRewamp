package com.vs.schoolmessenger.Parent.PTM

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
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
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PtmBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PTM : BaseActivity<PtmBinding>(), View.OnClickListener,OnCancelClickListener {

    override fun getViewBinding(): PtmBinding {
        return PtmBinding.inflate(layoutInflater)
    }
    private var lastCancelledPosition: Int = -1
    var isSelectedDate = ""
    private val selectedSlotIds = mutableListOf<String>()
    lateinit var isMeetingHistoryAdapter: MeetingHistoryAdapter
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    var isClassTeacherId = ""
    var isSubjectId = ""

    private var progressDialog: AlertDialog? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.rytsearch.visibility = View.GONE
        binding.txtSearchMeeting.setText("")
        binding.lblScheduleMeeting.setOnClickListener(this)
        binding.lblBookSlots.setOnClickListener(this)
        binding.lblYourMeeting.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token
        binding.toolbarLayout.lblStudentName1.text = childDetails?.name
        binding.toolbarLayout.lblStudentSection.text =
            childDetails?.standard_name + " - " + childDetails?.section_name
        isSelectedDate = Constant.getCurrentDate()
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
            if (binding.rytsearch.visibility == View.VISIBLE) {
                binding.rytsearch.visibility = View.GONE
                binding.txtSearchMeeting.setText("")
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMeeting.windowToken, 0)
            } else {
                binding.rytsearch.visibility = View.VISIBLE
                binding.txtSearchMeeting.setText("")
                binding.txtSearchMeeting.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.txtSearchMeeting, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        appViewModel?.isStudentSlotResponse?.observe(this) { response ->
            if (response!!.status) {
                if (response.data.isNotEmpty()) {
                    binding.rytNoDataFound.visibility = View.GONE
                    binding.recyclerViewSlots.visibility = View.VISIBLE
                    isLoadData(response.data)
                } else {
                    binding.rytNoDataFound.visibility = View.VISIBLE
                    binding.recyclerViewSlots.visibility = View.GONE
                }
            } else {
                binding.rytNoDataFound.visibility = View.VISIBLE
                binding.recyclerViewSlots.visibility = View.GONE
            }
        }

        appViewModel?.isSlotCancelByStudent?.observe(this) { response ->
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

//        appViewModel?.isSlotBookingForStudent?.observe(this) { response ->
//            binding.rcyMeetingHistory.postDelayed({
//                Constant.hideLoading(this)
//                if (response?.status == true) {
//                    Constant.showTopAlertPopup(response.message, this)
//                } else {
//                    Constant.showTopAlertPopup("Booking failed!", this)
//                }
//            }, 2000)
//        }

        appViewModel?.isSlotBookingForStudent?.observe(this) { response ->
            binding.rcyMeetingHistory.postDelayed({
                Constant.hideLoading(this)

                if (response?.status == true) {
                    AlertDialog.Builder(this)
                        .setMessage(response.message ?: "Slot booked successfully!")
                        .setCancelable(false)
                        .setPositiveButton("OK") { dlg, _ ->
                            dlg.dismiss()
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


        appViewModel?.isSubjectResponse?.observe(this) { response ->
            if (response?.status!!) {
                isLoadSubjectList(response.data)
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
            val calendar = Calendar.getInstance()
            val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
            val todayMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
            val todayPos = dates.indexOfFirst { it.first == todayMonth && it.second == todayDay }
            if (todayPos != -1) {
                adapter.setDefaultSelected(todayPos)
                binding.recyclerViewDates.scrollToPosition(todayPos)
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
                    binding.rcyMeetingHistory.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                }
            } else {
                binding.lytList.visibility = View.VISIBLE
                binding.rcyMeetingHistory.visibility = View.GONE
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            }

    }
    }

    fun isLoadData(data: List<MeetingData>) {
        val adapter = ParentMeetingAdapter(data) { meeting, slot ->
            val meetingKey = "${meeting.staff_id}_${meeting.start_time}_${meeting.event_name}"
            // Remove old selected slot for this meeting if exists
            selectedSlotIds.removeAll { existingId ->
                // Find the slot with same meetingKey
                selectedSlotIds.removeAll { existingId ->
                    data.any { meetingItem ->
                        val key = "${meetingItem.staff_id}_${meetingItem.start_time}_${meetingItem.event_name}"
                        key == meetingKey && meetingItem.slots.any { it.id == existingId }
                    }
                }
            }
            selectedSlotIds.add(slot.id)
            binding.lblBookSlots.visibility = if (selectedSlotIds.isNotEmpty()) View.VISIBLE else View.GONE
            println("Selected Slot IDs: $selectedSlotIds")
            if (selectedSlotIds.isNotEmpty()) {
                binding.lblBookSlots.visibility = View.VISIBLE
            } else {
                binding.lblBookSlots.visibility = View.GONE
            }
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
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()

                val selectedSubject = mutableList[position]
                if (position == 0) {
                    isSubjectId = "0"
                    isClassTeacherId = "0"
                } else {
                    isSubjectId = selectedSubject.id
                    isClassTeacherId = "0"
                }

                isScheduleCallList()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    fun isLoadMeetingData(data: List<MeetingDataWrapper>) {
        if (data.isNullOrEmpty()){
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        }
        else{

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
            isClassTeacherId
        )
    }

    fun isMeetingHistoryList() {
        appViewModel!!.isSlotHistoryStudent(isAccessToken!!)
    }

    fun isGetSubjectList() {
        appViewModel!!.isSubjectListWithClassTeacher(isAccessToken!!)
    }

    fun isDateWiseSlotCount() {
        appViewModel!!.isSlotCountByDate(isAccessToken!!)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.lblScheduleMeeting -> {
                isChangeBackGroundTab(binding.lblScheduleMeeting)
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE

            }

            R.id.lblYourMeeting -> {
                isChangeBackGroundTab(binding.lblYourMeeting)
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            }

            R.id.lblBookSlots -> {
                showSendConfirmationDialog("Are you sure want to book this slots?")
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
            appViewModel!!.isSlotBookingStudent(isAccessToken!!, jsonObject)
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    fun isChangeBackGroundTab(isSelectedTab: TextView) {
        binding.lblScheduleMeeting.background = null
        binding.lblYourMeeting.background = null
        isSelectedTab.background = this.getDrawable(R.drawable.white_radious)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.txtSearchMeeting, InputMethodManager.SHOW_IMPLICIT)

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
        }
    }

    fun generateDates(daysCount: Int): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        repeat(daysCount) {
            val month = monthFormat.format(calendar.time)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            list.add(month to day)
            calendar.add(Calendar.DAY_OF_MONTH, 1) // move forward by 1 day
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
        appViewModel!!.isSlotCancelByStudent(isAccessToken!!, jsonObject)
    }
}