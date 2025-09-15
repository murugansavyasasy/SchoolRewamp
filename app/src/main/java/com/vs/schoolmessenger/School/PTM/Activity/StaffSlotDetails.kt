package com.vs.schoolmessenger.School.PTM.Activity

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.ClassesLoadAdapter
import com.vs.schoolmessenger.School.PTM.Adapter.StaffSlotStatusAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.ClassSection
import com.vs.schoolmessenger.School.PTM.DataClass.Slot
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotCancelReOpenClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PtmStaffSlotDetailsBinding

class StaffSlotDetails : BaseActivity<PtmStaffSlotDetailsBinding>(),
    View.OnClickListener, StaffSlotCancelReOpenClickListener {

    override fun getViewBinding(): PtmStaffSlotDetailsBinding {
        return PtmStaffSlotDetailsBinding.inflate(layoutInflater)
    }

    var isSlot: List<Slot>? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    lateinit var mAdapter: StaffSlotStatusAdapter
    lateinit var isAdapter: ClassesLoadAdapter

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
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()


        binding.lblMeetingTitle.text = isSlotsDetails.event_name
        binding.lblMeetingMode.text = "Mode" + " - " + isSlotsDetails.event_mode
        binding.lblDate.text = isSlotsDetails.date
        binding.lblTime.text = isSlotsDetails.start_time + " - " + isSlotsDetails.end_time

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        appViewModel!!.isPtmSlotCancelReOpen?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isPtmSlotCancelClose?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        isLoadDataAdapter(isSlot)
        isLoadClasses(isSlotsDetails.std_sec_details)
    }


    fun isLoadClasses(slotDetail: List<ClassSection>) {
        isAdapter = ClassesLoadAdapter(slotDetail, this, Constant.isShimmerViewDisable)

        binding.rcyClasses.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.rcyClasses.adapter = isAdapter
    }



    fun isLoadDataAdapter(slotDetail: List<Slot>?) {
        mAdapter = StaffSlotStatusAdapter(slotDetail, this, this, Constant.isShimmerViewDisable)
        binding.rcySlotList.layoutManager = LinearLayoutManager(this)
        binding.rcySlotList.adapter = mAdapter
    }

    override fun onClick(v: View?) {
    }


    override fun onStaffSlotCancelReOpenClickListener(
        data: Slot,
        view: View,
        adapterPosition: Int
    ) {
        isCancelAndReOpen(data, view)
    }

    fun isCancelAndReOpen(data: Slot, anchor: View) {
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

        when (data.status) {
            "Cancelled" -> {
                layout_reopen.visibility = View.GONE
                layout_cancel.visibility = View.VISIBLE
            }
            "Available" -> {
                layout_reopen.visibility = View.VISIBLE
                layout_cancel.visibility = View.VISIBLE
            }
        }

        layout_reopen.setOnClickListener {
            showSendConfirmationDialog(true, data)
            popupWindow.dismiss()
        }

        layout_cancel.setOnClickListener {
            showSendConfirmationDialog(false, data)
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 0, 10)
    }




    fun showSendConfirmationDialog(isSlotReOpen: Boolean, data: Slot) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        if (isSlotReOpen) {
            alertMessage.text = "Are you sure want to reopen this slot?"
        } else {
            alertMessage.text = "Are you sure want to cancel this slot?"
        }

        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            val jsonObject = JsonObject()
            jsonObject.addProperty("slot_id", data.slot_id)
            alertDialog.dismiss()
            if (isSlotReOpen) {
                appViewModel!!.isSlotCancelReOpen(isAccessToken!!, jsonObject)
            } else {
                appViewModel!!.isSlotCancelClose(isAccessToken!!, jsonObject)
            }
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }


}