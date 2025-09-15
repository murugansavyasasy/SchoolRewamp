package com.vs.schoolmessenger.Parent.PTM

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.PTM.Adapter.MeetingHistoryAdapter
import com.vs.schoolmessenger.Parent.PTM.Adapter.MeetingListItem
import com.vs.schoolmessenger.Parent.PTM.Adapter.ParentMeetingAdapter
import com.vs.schoolmessenger.Parent.PTM.Adapter.PtmParentCalender
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingData
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingDataWrapper
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingItem
import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotData
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

    private val selectedSlots = mutableListOf<SlotData>()
    private var isParentMeetingAdapter: ParentMeetingAdapter? = null
    lateinit var isMeetingHistoryAdapter: MeetingHistoryAdapter
    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()

        binding.imgSearch.visibility = View.GONE
        binding.rytsearch.visibility = View.GONE
        binding.txtSearchMeeting.setText("")
        binding.lblScheduleMeeting.setOnClickListener(this)
        binding.lblYourMeeting.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        val dates = generateDates(60)
        isSelectedDate = Constant.getCurrentDate()

        val adapter = PtmParentCalender(dates) { selectedDate ->
            isSelectedDate = selectedDate
            isScheduleCallList()
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
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


        binding.txtSearchMeeting.addTextChangedListener { editable ->
            val query = editable.toString()
            if (::isMeetingHistoryAdapter.isInitialized) {
                isMeetingHistoryAdapter.filter.filter(query)
            }
        }



        binding.imgSearch.setOnClickListener {
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
                        }
                    }
                    .show()
            }
        }


        appViewModel?.isSlotDetailsHistory?.observe(this) { response ->
            if (response!!.status) {
                if (response.data.isNotEmpty()) {
                    binding.rytNoDataFound.visibility = View.GONE
                    binding.recyclerViewSlots.visibility = View.VISIBLE
                    isLoadMeetingData(response.data)
                } else {
                    binding.rytNoDataFound.visibility = View.VISIBLE
                    binding.recyclerViewSlots.visibility = View.GONE
                }
            } else {
                binding.rytNoDataFound.visibility = View.VISIBLE
                binding.recyclerViewSlots.visibility = View.GONE
            }
        }
        isScheduleCallList()
    }

    fun isLoadData(data: List<MeetingData>) {
        isParentMeetingAdapter = ParentMeetingAdapter(data) { meeting, slot ->
            Toast.makeText(
                this,
                "Selected ${slot.slot_from} - ${slot.slot_to} for ${meeting.staff_name}",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.recyclerViewSlots.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
        binding.recyclerViewSlots.adapter = isParentMeetingAdapter
        binding.recyclerViewSlots.setHasFixedSize(true)
    }

    fun isLoadMeetingData(data: List<MeetingDataWrapper>) {
        binding.rcyMeetingHistory.layoutManager = LinearLayoutManager(this)

        val meetingItems = mutableListOf<MeetingListItem>()

        val todayMeetings = data.firstOrNull()?.today ?: emptyList()
        val upcomingMeetings = data.firstOrNull()?.upcoming ?: emptyList()
        val completedMeetings = data.firstOrNull()?.completed ?: emptyList()

        if (todayMeetings.isNotEmpty()) {
            meetingItems.add(MeetingListItem.Header("Today"))
            todayMeetings.forEach { meetingItems.add(MeetingListItem.Item(it)) }
        }

        if (upcomingMeetings.isNotEmpty()) {
            meetingItems.add(MeetingListItem.Header("Upcoming"))
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



    fun isScheduleCallList() {
        appViewModel!!.isSlotAvailableForStudent(isAccessToken!!, isSelectedDate, "0", "0")
    }

    fun isMeetingHistoryList() {
        appViewModel!!.isSlotHistoryStudent(isAccessToken!!)
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.lblScheduleMeeting -> {
                isChangeBackGroundTab(binding.lblScheduleMeeting)
            }

            R.id.lblYourMeeting -> {
                isChangeBackGroundTab(binding.lblYourMeeting)
            }
        }
    }

    fun isChangeBackGroundTab(isSelectedTab: TextView) {
        binding.lblScheduleMeeting.background = null
        binding.lblYourMeeting.background = null
        isSelectedTab.background = this.getDrawable(R.drawable.white_radious)

        if (isSelectedTab == binding.lblYourMeeting) {
            binding.rytScheduleMeeting.visibility = View.GONE
            binding.rytYourMeeting.visibility = View.VISIBLE
            binding.imgSearch.visibility = View.VISIBLE
            binding.rytsearch.visibility = View.GONE
            binding.txtSearchMeeting.setText("")

            isMeetingHistoryList()
        } else {
            binding.rytYourMeeting.visibility = View.GONE
            binding.rytScheduleMeeting.visibility = View.VISIBLE
            binding.imgSearch.visibility = View.GONE
            binding.rytsearch.visibility = View.GONE
            binding.txtSearchMeeting.setText("")
        }
    }


    fun generateDates(daysCount: Int): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val calendar = Calendar.getInstance() // Start from today
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault()) // e.g., Sep

        repeat(daysCount) {
            val month = monthFormat.format(calendar.time)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            list.add(month to day)
            calendar.add(Calendar.DAY_OF_MONTH, 1) // move forward by 1 day
        }

        return list
    }

    override fun onCancelClick(
        meeting: MeetingItem,
        position: Int
    ) {
        lastCancelledPosition = position
        val jsonObject= JsonObject()
        jsonObject.addProperty("slot_id",meeting.id)
        jsonObject.addProperty("cancelled_reason","")
        Log.d("jsonObject",jsonObject.toString())
        appViewModel!!.isSlotCancelByStudent(isAccessToken!!,jsonObject)
    }
}