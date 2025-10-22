package com.vs.schoolmessenger.School.PTM.Activity

import android.content.Intent
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

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

        binding.layoutDatePicking.setOnClickListener(this)
        binding.imgDelete.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.layoutCreateSlot.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name

        loadData()

        appViewModel.isPtmSlotResponse?.observe(this) { response ->
            if (response != null && response.status) {
                isSlotCategory = response.data
                isLoadData(isSlotCategory)
            } else {
                binding.tvNoData.visibility = View.VISIBLE
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
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
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

        val todayList = ArrayList<SlotDetail>()
        val upcomingList = ArrayList<SlotDetail>()
        val completedList = ArrayList<SlotDetail>()

        for (category in isSlotCategory) {
            if (isAllSlot) {
                todayList.addAll(category.today.flatMap { it.details })
                upcomingList.addAll(category.upcoming.flatMap { it.details })
                completedList.addAll(category.completed.flatMap { it.details })
            } else {
                for (group in category.today) {
                    todayList.addAll(group.details.filter { toDashDate(it.date) == toDashDate(isSelectedDate) })
                }
                for (group in category.upcoming) {
                    upcomingList.addAll(group.details.filter { toDashDate(it.date) == toDashDate(isSelectedDate) })
                }
                for (group in category.completed) {
                    completedList.addAll(group.details.filter { toDashDate(it.date) == toDashDate(isSelectedDate) })
                }
            }
        }

        isLoadDataAdapter(todayList, binding.rcyToday)
        isLoadDataAdapter(upcomingList, binding.rcyUpcoming)
        isLoadDataAdapter(completedList, binding.rcyComplete)

        binding.lblToday.visibility = if (todayList.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyToday.visibility = binding.lblToday.visibility

        binding.lblUpComing.visibility = if (upcomingList.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyUpcoming.visibility = binding.lblUpComing.visibility

        binding.lblComplete.visibility = if (completedList.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyComplete.visibility = binding.lblComplete.visibility

        val hasData = todayList.isNotEmpty() || upcomingList.isNotEmpty() || completedList.isNotEmpty()
        binding.tvNoData.visibility = if (hasData) View.GONE else View.VISIBLE
        binding.imgNoData.visibility = if (hasData) View.GONE else View.VISIBLE



        binding.lblSlotCount.visibility = View.VISIBLE
        binding.lblSlotCount.text = if (todayList.isNotEmpty()) {
            "You have ${todayList.size} meeting's today"
        } else {
            "You have 0 meeting's today"
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
                "Cancel Slot",
                "Yes",
                "No",
                "",
                "Are you sure you want to cancel this slot?"
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
            Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadData() {
        binding.lblToday.visibility = View.GONE
        binding.lblUpComing.visibility = View.GONE
        binding.lblComplete.visibility = View.GONE
        binding.tvNoData.visibility = View.GONE

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
                Constant.showDatePickerNormal(this) { selectedDate ->
                    isSelectedDate = toDashDate(selectedDate)
                    binding.imgDelete.visibility = View.VISIBLE
                    binding.lblDatePicking.text = selectedDate
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
}
