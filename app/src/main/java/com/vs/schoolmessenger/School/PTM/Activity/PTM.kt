package com.vs.schoolmessenger.School.PTM.Activity

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.UpComingSlotAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.SlotCategory
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.School.PTM.DataClass.TimeSlot
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotClickListener
import com.vs.schoolmessenger.Utils.Constant
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
    var isSlotDetail: ArrayList<SlotDetail> = ArrayList()
    var isSelectedDate = ""

    lateinit var mAdapter: UpComingSlotAdapter
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

        binding.lblDatePicking.setOnClickListener(this)
        binding.imgDelete.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.layoutCreateSlot.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name

        loadData()

        appViewModel!!.isPtmSlotResponse?.observe(this) { response ->
            if (response != null && response.status) {
                isSlotCategory = response.data
                isLoadData(isSlotCategory)
            }
        }
    }

    fun isLoadData(isSlotCategory: List<SlotCategory>?) {
        if (isSlotCategory.isNullOrEmpty()) return

        // Clear old data
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
                    todayList.addAll(group.details.filter { it.date == isSelectedDate })
                }
                for (group in category.upcoming) {
                    upcomingList.addAll(group.details.filter { it.date == isSelectedDate })
                }
                for (group in category.completed) {
                    completedList.addAll(group.details.filter { it.date == isSelectedDate })
                }
            }
        }

        Log.d(
            "PTM",
            "Today: ${todayList.size}, Upcoming: ${upcomingList.size}, Complete: ${completedList.size}"
        )

        // Load into adapters
        isLoadDataAdapter(todayList, binding.rcyToday)
        isLoadDataAdapter(upcomingList, binding.rcyUpcoming)
        isLoadDataAdapter(completedList, binding.rcyComplete)
    }

    fun isLoadDataAdapter(list: ArrayList<SlotDetail>, recyclerView: RecyclerView) {
        val adapter = UpComingSlotAdapter(list, this, this, Constant.isShimmerViewDisable)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    fun loadData() {
        // Shimmer loading state
        val shimmerAdapter = UpComingSlotAdapter(null, this, this, Constant.isShimmerViewShow)

        binding.rcyToday.layoutManager = LinearLayoutManager(this)
        binding.rcyToday.adapter = shimmerAdapter

        binding.rcyUpcoming.layoutManager = LinearLayoutManager(this)
        binding.rcyUpcoming.adapter = shimmerAdapter

        binding.rcyComplete.layoutManager = LinearLayoutManager(this)
        binding.rcyComplete.adapter = shimmerAdapter

        isAccessToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdGFmZl9pZCI6IjEwMDc3NjQ4Iiwic2Nob29sX2lkIjoiNzA0NCIsImlhdCI6MTc1NjcwNTIxM30.EkV33rNEvCE51bw7wpM1JZK41rq9ySydWFmGrxPmTiU"
        appViewModel!!.isSlotForStaff(isAccessToken!!, "ALL")
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lblDatePicking -> {
                Constant.showDatePickerNormal(this) { selectedDate ->
                    Log.d("PTM", "Selected Date: $selectedDate")
                    binding.imgDelete.visibility = View.VISIBLE
                    binding.lblDatePicking.text = selectedDate
                    isSelectedDate = selectedDate
                    isAllSlot = false
                    isLoadData(isSlotCategory)
                }
            }

            R.id.imgDelete -> {
                binding.lblDatePicking.text = "All Slots"
                binding.imgDelete.visibility = View.GONE
                isAllSlot = true
                isLoadData(isSlotCategory)
            }

            R.id.imgBack -> {
                onBackPressed()
            }

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

}
