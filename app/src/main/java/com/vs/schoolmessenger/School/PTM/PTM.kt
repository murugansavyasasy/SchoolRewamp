package com.vs.schoolmessenger.School.PTM

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDate
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PtmStaffBinding

class PTM : BaseActivity<PtmStaffBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): PtmStaffBinding {
        return PtmStaffBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isAllSlot = true
    var isSlotDate: List<SlotDate>? = null
    var isSlotDetail: ArrayList<SlotDetail> = ArrayList()
    var isSelectedDate = ""

    lateinit var mAdapter: UpComingSlotAdapter
    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()
        binding.lblDatePicking.setOnClickListener(this)
        binding.imgDelete.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name
        loadData()

        appViewModel!!.isPtmSlotResponse?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    isSlotDate = response.data
                    isLoadData(isSlotDate)
                }
            }
        }
    }

    fun isLoadData(isSlotDate: List<SlotDate>?) {
        isSlotDetail.clear()
        Log.d("isSlotDate", isSlotDate!!.size.toString())
        if (isAllSlot) {
            for (i in isSlotDate.indices) {
                isSlotDetail.addAll(isSlotDate[i].details)
            }
        } else {
            for (i in isSlotDate.indices) {
                if (isSlotDate[i].date == isSelectedDate) {
                    isSlotDetail.addAll(isSlotDate[i].details)
                }
            }
        }
        isLoadDataAdapter(isSlotDetail)
    }

    fun isLoadDataAdapter(isSlotDetail: ArrayList<SlotDetail>?) {
        mAdapter = UpComingSlotAdapter(isSlotDetail, this, Constant.isShimmerViewDisable)
        binding.rcySlots.layoutManager = LinearLayoutManager(this)
        binding.rcySlots.adapter = mAdapter
    }

    fun loadData() {
        mAdapter = UpComingSlotAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcySlots.layoutManager = LinearLayoutManager(this)
        binding.rcySlots.adapter = mAdapter
        appViewModel!!.isSlotForStaff(
            isAccessToken!!, "ALL"
        )
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lblDatePicking -> {
                Constant.showDatePickerNormal(this) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.imgDelete.visibility = View.VISIBLE
                    binding.lblDatePicking.text = selectedDate
                    isSelectedDate = selectedDate
                    isAllSlot = false
                    isLoadData(isSlotDate)
                }
            }

            R.id.imgDelete -> {
                binding.lblDatePicking.text = "All Slots"
                binding.imgDelete.visibility = View.GONE
                isAllSlot = true
                isLoadData(isSlotDate)
            }

            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}