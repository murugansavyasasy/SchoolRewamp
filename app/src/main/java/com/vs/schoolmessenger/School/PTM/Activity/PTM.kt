package com.vs.schoolmessenger.School.PTM.Activity

import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.UpComingSlotAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.SlotCategory
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.showSendConfirmationDialog
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PtmStaffBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PTM : BaseActivity<PtmStaffBinding>(),
    View.OnClickListener, StaffSlotClickListener {

    override fun getViewBinding(): PtmStaffBinding {
        return PtmStaffBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isAllSlot = true
    var isSlotCategory: List<SlotCategory>? = null
    var isSelectedDate = ""
    private lateinit var appViewModel: App

    private var userDetails: UserDetails? = null

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var instituteId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false

    // Add these class-level lists to store the flat SlotDetail lists for each section
    private val todaySlots = ArrayList<SlotDetail>()
    private val upcomingSlots = ArrayList<SlotDetail>()
    private val completedSlots = ArrayList<SlotDetail>()

    override fun setupViews() {
        super.setupViews()
        isPTMToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
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
            SharedPreference.putStaffDetails(this,matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }

        binding.layoutDatePicking.setOnClickListener(this)
        binding.imgDelete.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.layoutCreateSlot.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        binding.lblMenuName.text = Constant.isSelectedMenuName

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name

        loadData()

        appViewModel.isPtmSlotResponse?.observe(this) { response ->
            if (response != null && response.status) {
                isSlotCategory = response.data
                isLoadData(isSlotCategory)
                if (fromNotification) {
                    scrollToMessageId(headerId)
                }
            } else {
                binding.tvNoData.visibility = View.VISIBLE
                binding.tvNoData.text=response!!.message?: getString(R.string.no_meeting_available)
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
                    loadData() // refresh slots
                } else {
                    Toast.makeText(this, "${result.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.something_went_wrong_please_try_again_later), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scrollToMessageId(headerId: String?) {
        if (headerId.isNullOrEmpty()) return

        var found = false

        // Check todaySlots
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
            // Check upcomingSlots
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
                // Check completedSlots
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
                trimmed.contains("/") -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(trimmed)
                trimmed.contains("-") -> SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(trimmed)
                else -> null
            }
            if (parsed != null) SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(parsed) else trimmed
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
                    todaySlots.addAll(group.details.filter { toDashDate(it.date) == toDashDate(isSelectedDate) })
                }
                for (group in category.upcoming) {
                    upcomingSlots.addAll(group.details.filter { toDashDate(it.date) == toDashDate(isSelectedDate) })
                }
                for (group in category.completed) {
                    completedSlots.addAll(group.details.filter { toDashDate(it.date) == toDashDate(isSelectedDate) })
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

        binding.lblComplete.visibility = if (completedSlots.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyComplete.visibility = binding.lblComplete.visibility

        val hasData = todaySlots.isNotEmpty() || upcomingSlots.isNotEmpty() || completedSlots.isNotEmpty()
        binding.tvNoData.visibility = if (hasData) View.GONE else View.VISIBLE
        binding.imgNoData.visibility = if (hasData) View.GONE else View.VISIBLE

        binding.lblSlotCount.visibility = View.VISIBLE
        binding.lblSlotCount.text = if (todaySlots.isNotEmpty()) {
            "${getString(R.string.You_have)} ${todaySlots.size} ${getString(R.string.meeting_s_today)}"
        } else {
            getString(R.string.you_have_0_meeting_s_today)
        }
    }

    private fun isLoadDataAdapter(list: ArrayList<SlotDetail>, recyclerView: RecyclerView) {
        val adapter = UpComingSlotAdapter(list, this, this, Constant.isShimmerViewDisable)
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
            appViewModel.isSlotCancelClose(isAccessToken!!, json)
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

        // Clear lists before loading
        todaySlots.clear()
        upcomingSlots.clear()
        completedSlots.clear()

        binding.rcyToday.layoutManager = LinearLayoutManager(this)
        binding.rcyToday.adapter = UpComingSlotAdapter(null, this, this, Constant.isShimmerViewShow)

        binding.rcyUpcoming.layoutManager = LinearLayoutManager(this)
        binding.rcyUpcoming.adapter = UpComingSlotAdapter(null, this, this, Constant.isShimmerViewShow)

        binding.rcyComplete.layoutManager = LinearLayoutManager(this)
        binding.rcyComplete.adapter = UpComingSlotAdapter(null, this, this, Constant.isShimmerViewShow)

        appViewModel.isSlotForStaff(isAccessToken!!, "ALL")
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.layoutDatePicking -> {
                Constant.showDatePickerNormal(
                    this,
                    preSelectedDate = isSelectedDate // <-- pass previous date
                ) { selectedDate ->
                    isSelectedDate = toDashDate(selectedDate)
                    binding.imgDelete.visibility = View.VISIBLE
                    binding.lblDatePicking.text = Constant.convertDateTimeFormat(selectedDate)
                    isAllSlot = false
                    isLoadData(isSlotCategory)
                }
            }
            R.id.imgDelete -> {
                isSelectedDate = ""
                binding.lblDatePicking.text = "All"
                binding.imgDelete.visibility = View.GONE
                isAllSlot = true
                isLoadData(isSlotCategory)
            }
            R.id.imgBack -> onBackPressed()
            R.id.layoutCreateSlot -> {
                val intent = Intent(this, CreateSlots::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
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
}