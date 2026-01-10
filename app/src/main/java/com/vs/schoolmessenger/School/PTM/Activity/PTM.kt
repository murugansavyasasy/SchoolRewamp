package com.vs.schoolmessenger.School.PTM.Activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.BookedSlotAdapter
import com.vs.schoolmessenger.School.PTM.Adapter.UpComingSlotAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.BookedSlotData
import com.vs.schoolmessenger.School.PTM.DataClass.BookedSlotItem
import com.vs.schoolmessenger.School.PTM.DataClass.SlotCategory
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.School.PTM.InterFace.BookedSlotCancelReOpenClickListener
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.showSendConfirmationDialog
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PtmStaffBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PTM : BaseActivity<PtmStaffBinding>(), View.OnClickListener, StaffSlotClickListener,
    BookedSlotCancelReOpenClickListener {

    override fun getViewBinding(): PtmStaffBinding {
        return PtmStaffBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isAllSlot = true
    var isSlotCategory: List<SlotCategory>? = null
    var isBookedSlotData: List<BookedSlotData>? = null
    var isSelectedDate = ""
    private lateinit var appViewModel: App
    private var userDetails: UserDetails? = null
    private var msg_id: Int = -1
    private var headerId: String? = null
    private var instituteId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    private val todaySlots = ArrayList<SlotDetail>()
    private val upcomingSlots = ArrayList<SlotDetail>()
    private val completedSlots = ArrayList<SlotDetail>()
    private val isTodaySlots = ArrayList<BookedSlotItem>()
    private val isUpcomingSlots = ArrayList<BookedSlotItem>()
    private val isCompletedSlots = ArrayList<BookedSlotItem>()

    private var isBookedSlot = false

    override fun setupViews() {
        super.setupViews()
        isPTMToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)

        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)

        if (fromNotification) {
            Constant.isParentChoose = false
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            instituteId = intent.getStringExtra(Constant.institute_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)
            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )
            val matchedChild = userDetails?.staff_details?.find { it.school_id == instituteId }
            SharedPreference.putStaffDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }

        binding.lnrTabBookedSlots.isEnabled=false
        binding.lnrTabMeeting.isEnabled=false
        binding.layoutDatePicking.setOnClickListener(this)
        binding.imgDelete.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.layoutCreateSlot.setOnClickListener(this)
        binding.lnrTabMeeting.setOnClickListener(this)
        binding.lnrTabBookedSlots.setOnClickListener(this)


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        binding.lblMenuName.text = Constant.isSelectedMenuName

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name

        loadData()

        appViewModel.isPtmSlotResponse?.observe(this) { response ->
            //Only when response comes the tab will be enabled
            binding.lnrTabBookedSlots.isEnabled=true
            binding.lnrTabMeeting.isEnabled=false
            if (response != null && response.status) {
                isSlotCategory = response.data
                isLoadData(isSlotCategory)
                if (fromNotification) {
                    scrollToMessageId(headerId)
                }
            } else {
                binding.tvNoData.visibility = View.VISIBLE
                binding.imgNoData.visibility = View.VISIBLE
                binding.tvNoData.text =
                    response!!.message ?: getString(R.string.no_meeting_available)
                binding.rcyToday.adapter = null
                binding.rcyUpcoming.adapter = null
                binding.rcyComplete.adapter = null
            }
        }

        appViewModel.isBookedSlotsData?.observe(this) { response ->
            //Only when response comes the tab will be enabled
            binding.lnrTabBookedSlots.isEnabled=false
            binding.lnrTabMeeting.isEnabled=true
            if (response != null && response.status) {
                isBookedSlotData = response.data
                isLoadBookedData(isBookedSlotData)
            } else {
                binding.tvNoData.visibility = View.VISIBLE
                binding.imgNoData.visibility = View.VISIBLE
                binding.tvNoData.text =
                    response!!.message ?: getString(R.string.no_meeting_available)
                binding.rcyToday.adapter = null
                binding.rcyUpcoming.adapter = null
                binding.rcyComplete.adapter = null
            }
        }

        appViewModel.isPtmSlotCancelClose?.observe(this) { result ->
            Constant.hideLoading(this)
            if (result != null) {
                if (result.status) {
                    Toast.makeText(this, "${result.message}", Toast.LENGTH_SHORT).show()
                    if (!isBookedSlot) {
                        loadData()
                    } else {
                        isBookedSlotDetails()
                    }
                } else {
                    Toast.makeText(this, "${result.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.something_went_wrong_please_try_again_later),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isLoadBookedData(isBookedSlotData: List<BookedSlotData>?) {
        if (isBookedSlotData.isNullOrEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
            binding.imgNoData.visibility = View.VISIBLE
            binding.tvNoData.text = getString(R.string.no_meeting_available)
            return
        }
        isTodaySlots.clear()
        isUpcomingSlots.clear()
        isCompletedSlots.clear()

        val item = isBookedSlotData[0]

        if (isAllSlot) {
            isTodaySlots.addAll(item.today)
            isUpcomingSlots.addAll(item.upcoming)
            isCompletedSlots.addAll(item.completed)
        } else {
            isTodaySlots.addAll(
                item.today.filter { toDashDate(it.date ?: it.date) == toDashDate(isSelectedDate) }
            )
            isUpcomingSlots.addAll(
                item.upcoming.filter {
                    toDashDate(
                        it.date ?: it.date
                    ) == toDashDate(isSelectedDate)
                }
            )
            isCompletedSlots.addAll(
                item.completed.filter {
                    toDashDate(
                        it.date ?: it.date
                    ) == toDashDate(isSelectedDate)
                }
            )
        }
        isBookedSlotAdapter(isTodaySlots, binding.rcyToday)
        isBookedSlotAdapter(isUpcomingSlots, binding.rcyUpcoming)
        isBookedSlotAdapter(isCompletedSlots, binding.rcyComplete)
        binding.lblToday.visibility = if (isTodaySlots.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyToday.visibility = binding.lblToday.visibility

        binding.lblUpComing.visibility =
            if (isUpcomingSlots.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyUpcoming.visibility = binding.lblUpComing.visibility

        binding.lblComplete.visibility =
            if (isCompletedSlots.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyComplete.visibility = binding.lblComplete.visibility

        val hasData =
            isTodaySlots.isNotEmpty() || isUpcomingSlots.isNotEmpty() || isCompletedSlots.isNotEmpty()
        binding.tvNoData.visibility = if (hasData) View.GONE else View.VISIBLE
        binding.imgNoData.visibility = if (hasData) View.GONE else View.VISIBLE
        binding.lblSlotCount.visibility = View.VISIBLE
//        binding.lblSlotCount.text = if (todaySlots.isNotEmpty()) {
//            "${getString(R.string.You_have)} ${todaySlots.size} ${getString(R.string.meeting_s_today)}"
//        } else {
//            "No booked slots"
//        }
    }

    private fun scrollToMessageId(headerId: String?) {
        if (headerId.isNullOrEmpty()) return

        var found = false
        val todayIndex = todaySlots.indexOfFirst { detail ->
            detail.slots.any { slot -> slot.slot_id == headerId }
        }
        if (todayIndex != -1) {
            Log.d("ScrollDebug", "Found in today at index $todayIndex")
            binding.rcyToday.post {
                binding.rcyToday.smoothScrollToPosition(todayIndex)
                highlightItemTemporarily(binding.rcyToday, todayIndex)
            }
            found = true
        } else {
            val upcomingIndex = upcomingSlots.indexOfFirst { detail ->
                detail.slots.any { slot -> slot.slot_id == headerId }
            }
            if (upcomingIndex != -1) {
                Log.d("ScrollDebug", "Found in upcoming at index $upcomingIndex")
                binding.rcyUpcoming.post {
                    binding.rcyUpcoming.smoothScrollToPosition(upcomingIndex)
                    highlightItemTemporarily(binding.rcyUpcoming, upcomingIndex)
                }
                found = true
            } else {
                val completedIndex = completedSlots.indexOfFirst { detail ->
                    detail.slots.any { slot -> slot.slot_id == headerId }
                }
                if (completedIndex != -1) {
                    Log.d("ScrollDebug", "Found in completed at index $completedIndex")
                    binding.rcyComplete.post {
                        binding.rcyComplete.smoothScrollToPosition(completedIndex)
                        highlightItemTemporarily(binding.rcyComplete, completedIndex)
                    }
                    found = true
                }
            }
        }
        if (!found) {
            Log.d("ScrollDebug", "No item found with headerId: $headerId")
        }
    }

    private fun highlightItemTemporarily(recyclerView: RecyclerView, position: Int) {
        recyclerView.post {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.itemView?.let { itemView ->
                val originalBackground = itemView.background

                itemView.setBackgroundColor(Color.parseColor("#FFE082"))

                Handler(Looper.getMainLooper()).postDelayed({
                    itemView.background = originalBackground
                }, 3000)
            }
        }
    }

    private fun toDashDate(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val trimmed = input.trim()
        return try {
            val parsed = when {
                trimmed.contains("/") -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(
                    trimmed
                )

                trimmed.contains("-") -> SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(
                    trimmed
                )

                else -> null
            }
            if (parsed != null) SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(parsed) else trimmed
        } catch (e: Exception) {
            trimmed
        }
    }


    fun isLoadData(isSlotCategory: List<SlotCategory>?) {
        if (isSlotCategory.isNullOrEmpty()) return

        // Clear existing lists
        todaySlots.clear()
        upcomingSlots.clear()
        completedSlots.clear()

        for (category in isSlotCategory) {
            if (isAllSlot) {
                todaySlots.addAll(category.today.flatMap { it.details })
                upcomingSlots.addAll(category.upcoming.flatMap { it.details })
                completedSlots.addAll(category.completed.flatMap { it.details })
            } else {
                for (group in category.today) {
                    todaySlots.addAll(group.details.filter {
                        toDashDate(it.date) == toDashDate(
                            isSelectedDate
                        )
                    })
                }
                for (group in category.upcoming) {
                    upcomingSlots.addAll(group.details.filter {
                        toDashDate(it.date) == toDashDate(
                            isSelectedDate
                        )
                    })
                }
                for (group in category.completed) {
                    completedSlots.addAll(group.details.filter {
                        toDashDate(it.date) == toDashDate(
                            isSelectedDate
                        )
                    })
                }
            }
        }

        isLoadDataAdapter(todaySlots, binding.rcyToday)
        isLoadDataAdapter(upcomingSlots, binding.rcyUpcoming)
        isLoadDataAdapter(completedSlots, binding.rcyComplete)

        binding.lblToday.visibility = if (todaySlots.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyToday.visibility = binding.lblToday.visibility

        binding.lblUpComing.visibility = if (upcomingSlots.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyUpcoming.visibility = binding.lblUpComing.visibility

        binding.lblComplete.visibility =
            if (completedSlots.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyComplete.visibility = binding.lblComplete.visibility

        val hasData =
            todaySlots.isNotEmpty() || upcomingSlots.isNotEmpty() || completedSlots.isNotEmpty()
        binding.tvNoData.visibility = if (hasData) View.GONE else View.VISIBLE
        binding.imgNoData.visibility = if (hasData) View.GONE else View.VISIBLE

        binding.lblSlotCount.visibility = View.VISIBLE
        binding.lblSlotCount.text = if (todaySlots.isNotEmpty()) {
            "${getString(R.string.You_have)} ${todaySlots.size} ${getString(R.string.meeting_s_today)}"
        } else {
//            getString(R.string.you_have_0_meeting_s_today)
            getString(R.string.there_are_no_meetings_planned_for_you_today)
        }
    }

    private fun isLoadDataAdapter(list: ArrayList<SlotDetail>, recyclerView: RecyclerView) {
        val adapter = UpComingSlotAdapter(list, this, this, Constant.isShimmerViewDisable)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun isBookedSlotAdapter(list: ArrayList<BookedSlotItem>, recyclerView: RecyclerView) {
        val adapter = BookedSlotAdapter(list, this, this, Constant.isShimmerViewDisable)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }


    private fun showSlotOptionsPopup(data: SlotDetail, anchor: View) {
        val popupView = layoutInflater.inflate(R.layout.cancel_reopen_layout, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        val layoutReopen = popupView.findViewById<View>(R.id.layout_reopen)
        val layoutCancel = popupView.findViewById<View>(R.id.layout_cancel)

        layoutReopen.visibility = View.GONE
        layoutCancel.visibility = View.VISIBLE

        layoutCancel.setOnClickListener {
            showSendConfirmationDialog(
                this,
                getString(R.string.cancel_slot),
                getString(R.string.Yes),
                getString(R.string.No),
                "",
                getString(R.string.are_you_sure_you_want_to_cancel_this_slot)
            ) { confirmed ->
                if (confirmed) callCancelReopenApi(data, "Cancel")
            }
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor)
    }

    private fun callCancelReopenApi(data: SlotDetail, action: String) {
        try {
            val json = JsonObject()
            json.addProperty("slot_id", data.slots.firstOrNull()?.slot_id ?: "")
            json.addProperty("action", action)

            Constant.showLoading(this)
            appViewModel.isSlotCancelClose(isAccessToken!!, json,this)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.invalid_request), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadData() {
        binding.lblToday.visibility = View.GONE
        binding.lblUpComing.visibility = View.GONE
        binding.lblComplete.visibility = View.GONE
        binding.tvNoData.visibility = View.GONE
        binding.imgNoData.visibility = View.GONE

        // Clear lists before loading
        todaySlots.clear()
        upcomingSlots.clear()
        completedSlots.clear()

        binding.rcyToday.layoutManager = LinearLayoutManager(this)
        binding.rcyToday.adapter = UpComingSlotAdapter(null, this, this, Constant.isShimmerViewShow)

        binding.rcyUpcoming.layoutManager = LinearLayoutManager(this)
        binding.rcyUpcoming.adapter =
            UpComingSlotAdapter(null, this, this, Constant.isShimmerViewShow)

        binding.rcyComplete.layoutManager = LinearLayoutManager(this)
        binding.rcyComplete.adapter =
            UpComingSlotAdapter(null, this, this, Constant.isShimmerViewShow)

        appViewModel.isSlotForStaff(isAccessToken!!, "ALL",this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.layoutDatePicking -> {
                Constant.showDatePickerNormal(
                    this, preSelectedDate = isSelectedDate
                ) { selectedDate ->
                    isSelectedDate = toDashDate(selectedDate)
                    binding.imgDelete.visibility = View.VISIBLE
                    binding.lblDatePicking.text = Constant.convertDateTimeFormat(selectedDate)
                    isAllSlot = false
                    if (!isBookedSlot) {
                        isLoadData(isSlotCategory)
                    } else {
                        isLoadBookedData(isBookedSlotData)
                    }
                }
            }

            R.id.imgDelete -> {
                isSelectedDate = ""
                binding.lblDatePicking.text = "All"
                binding.imgDelete.visibility = View.GONE
                isAllSlot = true
                if (!isBookedSlot) {
                    isLoadData(isSlotCategory)
                } else {
                    isLoadBookedData(isBookedSlotData)
                }
            }

            R.id.imgBack -> onBackPressed()
            R.id.layoutCreateSlot -> {
                val intent = Intent(this, CreateSlots::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }

            R.id.lnrTabMeeting -> {
                isTabClick(binding.lnrTabMeeting)
            }

            R.id.lnrTabBookedSlots -> {
                isTabClick(binding.lnrTabBookedSlots)
            }
        }
    }

    override fun onClickListener(data: SlotDetail) {
        val intent = Intent(this, StaffSlotDetails::class.java)
        val slotList = ArrayList(data.slots)
        intent.putExtra("isSlot", slotList)
        intent.putExtra("isSlotDetails", data)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onSlotCancelReOpenClick(data: SlotDetail, anchor: View) {
        showSlotOptionsPopup(data, anchor)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, SchoolDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun isTabClick(isClickedTab: LinearLayout) {
        if (isClickedTab == binding.lnrTabMeeting) {
            binding.txtTabMeeting.setTextColor(ContextCompat.getColor(this, R.color.PrimaryColor))
            binding.viewTabMeeting.setBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.PrimaryColor
                )
            )

            binding.txtTabBookedSlots.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.viewTabBookedSlots.setBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.mild_grey6
                )
            )
            isBookedSlot = false
            loadData()
        } else if (isClickedTab == binding.lnrTabBookedSlots) {
            binding.txtTabBookedSlots.setTextColor(ContextCompat.getColor(this, R.color.PrimaryColor))
            binding.viewTabBookedSlots.setBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.PrimaryColor
                )
            )
            binding.txtTabMeeting.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.viewTabMeeting.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.mild_grey6
                )
            )
            isBookedSlot = true
            isBookedSlotDetails()
        }
    }

    private fun isBookedSlotDetails() {
        binding.lblToday.visibility = View.GONE
        binding.lblUpComing.visibility = View.GONE
        binding.lblComplete.visibility = View.GONE
        binding.tvNoData.visibility = View.GONE
        binding.imgNoData.visibility = View.GONE

        todaySlots.clear()
        upcomingSlots.clear()
        completedSlots.clear()

        binding.rcyToday.layoutManager = LinearLayoutManager(this)
        binding.rcyToday.adapter =
            BookedSlotAdapter(emptyList(), this, this, Constant.isShimmerViewShow)

        binding.rcyUpcoming.layoutManager = LinearLayoutManager(this)
        binding.rcyUpcoming.adapter =
            BookedSlotAdapter(emptyList(), this, this, Constant.isShimmerViewShow)

        binding.rcyComplete.layoutManager = LinearLayoutManager(this)
        binding.rcyComplete.adapter =
            BookedSlotAdapter(emptyList(), this, this, Constant.isShimmerViewShow)

        appViewModel.isBookedSlotsData(isAccessToken!!, "ALL",this)
    }

    override fun onBookedSlotCancelReOpenClickListener(
        data: BookedSlotItem,
        view: View,
        adapterPosition: Int
    ) {
        isCancelAndReOpen(data, view)
    }

    fun isCancelAndReOpen(data: BookedSlotItem, anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.cancel_reopen_layout, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        val layout_reopen = popupView.findViewById<LinearLayout>(R.id.layout_reopen)
        val layout_cancel = popupView.findViewById<LinearLayout>(R.id.layout_cancel)

        if (data.is_cancelled!!) {
            layout_reopen.visibility = View.VISIBLE
            layout_cancel.visibility = View.GONE
        } else {
            layout_reopen.visibility = View.GONE
            layout_cancel.visibility = View.VISIBLE
        }


        layout_reopen.setOnClickListener {
            showSendConfirmationDialogCancel(true, data)
            popupWindow.dismiss()
        }

        layout_cancel.setOnClickListener {
            showSendConfirmationDialogCancel(false, data)
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 0, 10)
    }


    fun showSendConfirmationDialogCancel(isSlotReOpen: Boolean, data: BookedSlotItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        if (isSlotReOpen) {
            alertMessage.text = getString(R.string.are_you_sure_want_to_reopen_this_slot)
        } else {
            alertMessage.text = getString(R.string.are_you_sure_want_to_cancel_this_slot)
        }

        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            val slotId = data.slot_id
            val slotArray = JsonArray().apply {
                add(slotId)
            }
            val isReopen = JsonObject()
            isReopen.addProperty("slot_id", slotId)

            val mainObject = JsonObject().apply {
                add("slot_ids", slotArray)
            }
            if (isSlotReOpen) {
                appViewModel.isSlotCancelReOpen(isAccessToken!!, isReopen,this)
            } else {
                appViewModel.isSlotCancelClose(isAccessToken!!, mainObject,this)
            }
            alertDialog.dismiss()
            Constant.showLoading(this)
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }
}