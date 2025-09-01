package com.vs.schoolmessenger.School.PTM.Activity

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.UpComingSlotAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.SlotCategory
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.School.PTM.DataClass.SlotGroup
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PtmStaffBinding

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
        binding.lblCreateSlot.setOnClickListener(this)

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
        isSlotDetail.clear()
        if (isSlotCategory.isNullOrEmpty()) return

        for (category in isSlotCategory) {
            val allGroups = mutableListOf<SlotGroup>()
            allGroups.addAll(category.today)
            allGroups.addAll(category.upcoming)
            allGroups.addAll(category.completed)

            for (group in allGroups) {
                if (isAllSlot) {
                    isSlotDetail.addAll(group.details)
                } else {
                    for (detail in group.details) {
                        if (detail.date == isSelectedDate) {
                            isSlotDetail.add(detail)
                        }
                    }
                }
            }
        }

        Log.d("PTM", "Loaded slot details: ${isSlotDetail.size}")
        isLoadDataAdapter(isSlotDetail)
    }

    fun isLoadDataAdapter(isSlotDetail: ArrayList<SlotDetail>?) {
        mAdapter = UpComingSlotAdapter(isSlotDetail, this, this, Constant.isShimmerViewDisable)
        binding.rcySlots.layoutManager = LinearLayoutManager(this)
        binding.rcySlots.adapter = mAdapter
    }

    fun loadData() {
        mAdapter = UpComingSlotAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcySlots.layoutManager = LinearLayoutManager(this)
        binding.rcySlots.adapter = mAdapter
        isAccessToken="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdGFmZl9pZCI6IjEwMDc3NjQ4Iiwic2Nob29sX2lkIjoiNzA0NCIsImlhdCI6MTc1NjcwNTIxM30.EkV33rNEvCE51bw7wpM1JZK41rq9ySydWFmGrxPmTiU"
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

            R.id.lblCreateSlot -> {
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
