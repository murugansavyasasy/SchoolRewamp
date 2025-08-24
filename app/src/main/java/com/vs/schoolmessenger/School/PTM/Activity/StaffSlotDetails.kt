package com.vs.schoolmessenger.School.PTM.Activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.StaffSlotStatusAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.Slot
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PtmStaffSlotDetailsBinding

class StaffSlotDetails : BaseActivity<PtmStaffSlotDetailsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): PtmStaffSlotDetailsBinding {
        return PtmStaffSlotDetailsBinding.inflate(layoutInflater)
    }

    var isSlot: List<Slot>? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    lateinit var mAdapter: StaffSlotStatusAdapter

    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        isSlot = intent.getSerializableExtra("isSlot") as? ArrayList<Slot>
        val isSlotsDetails = intent.getSerializableExtra("isSlotDetails") as SlotDetail

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name


        binding.lblMeetingTitle.text = isSlotsDetails.event_name
        binding.lblMeetingMode.text = "Mode" + " - " + isSlotsDetails.event_mode
        binding.lblDate.text = "Date needed"
        binding.lblHostName.text = "Host by needed"
        binding.lblNotify.text = isSlotsDetails.meeting_duration.toString()
        binding.lblTime.text = "Time needed"

        isLoadDataAdapter(isSlot)
    }

    fun isLoadDataAdapter(slotDetail: List<Slot>?) {
        mAdapter = StaffSlotStatusAdapter(slotDetail, this, Constant.isShimmerViewDisable)
        binding.rcySlotList.layoutManager = LinearLayoutManager(this)
        binding.rcySlotList.adapter = mAdapter
    }

    override fun onClick(v: View?) {

    }
}