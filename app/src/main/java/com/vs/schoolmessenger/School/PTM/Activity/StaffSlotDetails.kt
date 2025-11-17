package com.vs.schoolmessenger.School.PTM.Activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
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
import java.util.Locale

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
//        setupToolbarBlueWhite()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        isSlot = intent.getSerializableExtra("isSlot") as? ArrayList<Slot>
        val isSlotsDetails = intent.getSerializableExtra("isSlotDetails") as SlotDetail

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.lblMeetingTitle.text = isSlotsDetails.event_name
        binding.lblHostName.text = isSlotsDetails.meeting_duration.toString() + " "+getString(R.string.minutes)
        binding.lblMeetingMode.text = getString(R.string.mode_2) + " - " + isSlotsDetails.event_mode
        binding.lblModeMeeting.text = isSlotsDetails.event_mode
        binding.lblDate.text = formatApiDateToDisplay(isSlotsDetails.date)

        if (isSlotsDetails.event_mode == "Online" || isSlotsDetails.event_mode == "Virtual") {
            binding.rytJoin.visibility = View.VISIBLE
        } else {
            binding.rytJoin.visibility = View.GONE
        }

        binding.rytJoin.setOnClickListener {
            val joinUrl = isSlotsDetails.join_url

            if (!joinUrl.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(joinUrl))
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.join_url_not_available), Toast.LENGTH_SHORT).show()
            }
        }



        binding.lblTime.text = isSlotsDetails.start_time + " - " + isSlotsDetails.end_time

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        appViewModel!!.isPtmSlotCancelClose?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                val message = response.message ?: getString(R.string.failed_to_cancel_slot)
                Constant.showTopAlertPopup1(message, this, true)
            } else {
                Constant.showTopAlertPopup1(getString(R.string.something_went_wrong_please_try_again_later), this, true)
            }
        }


        appViewModel!!.isPtmSlotCancelReOpen?.observe(this) { response ->
            Constant.hideLoading(this)

            if (response != null) {
                val message = response.message ?: getString(R.string.failed_to_reopen_slot)
                Constant.showTopAlertPopup1(message, this, true)
            } else {
                Constant.showTopAlertPopup1(getString(R.string.something_went_wrong_please_try_again_later), this, true)
            }
        }
        isLoadDataAdapter(isSlot)
        isLoadClasses(isSlotsDetails.std_sec_details)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatApiDateToDisplay(apiDate: String?): String {
        if (apiDate.isNullOrBlank()) return ""

        val raw = apiDate.trim()
        android.util.Log.d("StaffSlotDetails", "formatApiDateToDisplay input: $raw")
        try {
            if (raw.matches(Regex("^\\d{10}\$")) || raw.matches(Regex("^\\d{13}\$"))) {
                val millis = if (raw.length == 10) raw.toLong() * 1000L else raw.toLong()
                val out = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(java.util.Date(millis))
                android.util.Log.d("StaffSlotDetails", "parsed epoch -> $out")
                return out
            }
        } catch (_: Exception) { /* ignore */
        }

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "MM/dd/yyyy"
        )
        for (pattern in patterns) {
            try {
                val parser = java.text.SimpleDateFormat(pattern, Locale.getDefault())
                parser.isLenient = false
                val parsed = parser.parse(raw)
                if (parsed != null) {
                    val output = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val result = output.format(parsed)
                    android.util.Log.d(
                        "StaffSlotDetails",
                        "parsed with pattern [$pattern] -> $result"
                    )
                    return result
                }
            } catch (e: Exception) {
            }
        }
        try {
            val odt = java.time.OffsetDateTime.parse(raw)
            val zoned = odt.atZoneSameInstant(java.time.ZoneId.systemDefault())
            val fmt =
                java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
            val s = zoned.format(fmt)
            android.util.Log.d("StaffSlotDetails", "parsed with OffsetDateTime -> $s")
            return s
        } catch (_: Exception) { /* ignore */
        }

        android.util.Log.w("StaffSlotDetails", "Unable to parse date, returning raw -> $raw")
        return raw
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
                layout_reopen.visibility = View.VISIBLE
                layout_cancel.visibility = View.GONE
            }

            "Available" -> {
                layout_reopen.visibility = View.GONE
                layout_cancel.visibility = View.VISIBLE
            }

            "Upcoming" -> {
                layout_reopen.visibility = View.GONE
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
                appViewModel!!.isSlotCancelReOpen(isAccessToken!!, isReopen)
            } else {
                appViewModel!!.isSlotCancelClose(isAccessToken!!, mainObject)
            }

            alertDialog.dismiss()
            Constant.showLoading(this)
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }
}