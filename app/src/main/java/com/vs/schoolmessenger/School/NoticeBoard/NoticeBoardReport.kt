package com.vs.schoolmessenger.School.NoticeBoard

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.NoticeboardReportBinding

class NoticeBoardReport : BaseActivity<NoticeboardReportBinding>(), NoticeBoardClickListener,
    View.OnClickListener {

    override fun getViewBinding(): NoticeboardReportBinding {
        return NoticeboardReportBinding.inflate(layoutInflater)
    }


    private var mAdapter: ImagePickingAdapter? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var noticeboardadapter: SchoolNoticeBoardAdapter
    private var userDetails: UserDetails? = null
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    var isTotalSelectedItem = 0
    var isNoticeBoardId = ""
    var isNoticeBoardPosition = 0
    private var noticeList: List<NoticeStaffData> = emptyList()
    private var completeNoticeList: List<NoticeStaffData> = emptyList()
    private var isUpdatingSearchText = false


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        //Important Note:see actually what ever token we pass,From backend we recieve all the data from all school we are suppose to filter them using the school id this scenrio is for multiple school
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        userDetails = SharedPreference.getUserDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)



        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
//        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch323.visibility == View.VISIBLE) {
                binding.rytSearch323.visibility = View.GONE
                binding.edtSearch.text.clear()

            } else {
                binding.rytSearch323.visibility = View.VISIBLE
                binding.edtSearch.text.clear()
            }
        }

        binding.toolbarLayout.layoutCreateSlot.visibility = View.GONE
        binding.toolbarLayout.layoutCreateSlot.setOnClickListener {
            val intent = Intent(this, CreateNoticeBoard::class.java)
            startActivity(intent)
        }

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        noticeboardadapter = SchoolNoticeBoardAdapter(
            emptyList(), this, this, false,
            binding.nomessage,
            binding.txtNoData
        )
        binding.rcyNoticeBoard.adapter = noticeboardadapter
        binding.rcyNoticeBoard.layoutManager = LinearLayoutManager(this)
        binding.rcyNoticeBoard.adapter = noticeboardadapter

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::noticeboardadapter.isInitialized) {
                    noticeboardadapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        if (userDetails?.staff_role.equals(Constant.isStaffRole)) {
            binding.rytSpinner.visibility = View.GONE
            isAccessToken = isStaffDetails!!.access_token
            isGetNoticeBoardList()
            binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        } else {
            if (userDetails?.staff_details?.size!! > 1) {
                binding.rytSpinner.visibility = View.VISIBLE
                //Important Note:see actually what ever token we pass,From backend we recieve all the data from all school we are suppose to filter them using the school id this scenrio is for multiple school
                userDetails?.let { setupSchoolSpinner(it.staff_details) }
            } else {
                isAccessToken = userDetails!!.staff_details.get(0).access_token
                binding.rytSpinner.visibility = View.GONE
                isGetNoticeBoardList()
                binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
                binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
            }
        }


        appViewModel!!.isnoticeboarddelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@NoticeBoardReport)
                    noticeboardadapter!!.removeItemAt(isNoticeBoardPosition)
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }



        appViewModel?.isNoticeBoardStaffReport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response?.status == true && !response.data.isNullOrEmpty()) {
                    binding.rcyNoticeBoard.visibility = View.VISIBLE
                    binding.nomessage.visibility = View.GONE
                    binding.txtNoData.visibility = View.GONE
                    isloadhomeworkData(response.data)
                    completeNoticeList = response.data
                    binding.edtSearch.text.clear()

                } else {
                    isloadhomeworkData(emptyList())
                    binding.rcyNoticeBoard.visibility = View.GONE
                    binding.nomessage.visibility = View.VISIBLE
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.rytSearch323.visibility = View.GONE
                    binding.edtSearch.text.clear()
                }
            }
        }



        appViewModel?.isNoticeBoardReport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response?.status == true && !response.data.isNullOrEmpty()) {
                    binding.rcyNoticeBoard.visibility = View.VISIBLE
                    binding.nomessage.visibility = View.GONE
                    binding.txtNoData.visibility = View.GONE
                    isloadhomeworkData(response.data)
                    completeNoticeList = response.data
                    binding.edtSearch.text.clear()

                } else {
                    isloadhomeworkData(emptyList())
                    binding.rcyNoticeBoard.visibility = View.GONE
                    binding.nomessage.visibility = View.VISIBLE
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.rytSearch323.visibility = View.GONE
                    binding.edtSearch.text.clear()
                }
            }
        }

        val channel = NotificationChannel(
            "reminder_channel", "Reminders", NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

    }


    private fun setupSchoolSpinner(staffList: List<StaffDetails>) {

        // Prepare list for spinner ("All" + school names)
        val schoolNames = mutableListOf<String>()
        schoolNames.add("All Schools")
        schoolNames.addAll(staffList.map { it.school_name })

        // Use your custom spinner adapter instead of ArrayAdapter
        val adapter = SpinnerLoadingAdapter(this, schoolNames)
        binding.schoollistfilter.adapter = adapter
        binding.schoollistfilter.setSelection(0)

        binding.schoollistfilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                private var lastSelectedPosition: Int = -1

                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    // Update UI highlight in custom adapter
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    if (position != lastSelectedPosition) {
                        lastSelectedPosition = position

                        if (position == 0) {
                            // "All" option selected
                            isloadhomeworkData(completeNoticeList)
                            binding.toolbarLayout.lblSchoolName.visibility = View.GONE
                            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
                        } else {
                            // Specific school selected
                            val selectedStaff = staffList[position - 1]
                            isAccessToken = selectedStaff.access_token
                            isStaffDetails = selectedStaff
                            val selectedSchoolId = selectedStaff.school_id

                            // Filter list by school ID
                            val filteredList =
                                completeNoticeList.filter { it.school_id == selectedSchoolId }

                            Log.d(
                                "SpinnerSelection",
                                "Selected school: ${selectedStaff.school_name}, " +
                                        "Selected school id: ${selectedStaff.school_id}, " +
                                        "Data: $filteredList, Token: $isAccessToken"
                            )

                            isloadhomeworkData(filteredList)
                            binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
                            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        //Important Note:see actually what ever token we pass,From backend we recieve all the data from all school we are suppose to filter them using the school id this scenrio is for multiple school
        // Initial load (default "All")
        if (staffList.isNotEmpty()) {
            isAccessToken = staffList[0].access_token
            isStaffDetails = staffList[0]
            Log.d("DefaultSelection", "Default token: $isAccessToken")
            isGetNoticeBoardList()
        }
    }


    private fun isloadhomeworkData(newData: List<NoticeStaffData>?) {

        if (newData != null && newData.isNotEmpty()) {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            binding.rytSearch323.visibility = View.GONE
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)


            noticeList = newData
            noticeboardadapter.isLoading = false
            noticeboardadapter.updateList(newData, true)

            isUpdatingSearchText = true
            binding.edtSearch.setText("")
            isUpdatingSearchText = false

            Log.d(
                "SearchDebug",
                "Data loaded successfully, adapter item count: ${noticeboardadapter.itemCount}"
            )
        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.rytSearch323.visibility = View.GONE

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)

            noticeList = emptyList()
            noticeboardadapter.isLoading = false
            noticeboardadapter.updateList(emptyList(), true)

            isUpdatingSearchText = true
            binding.edtSearch.setText("")
            isUpdatingSearchText = false

            Log.d("SearchDebug", "Empty data loaded")
        }
    }


    private fun isGetNoticeBoardList() {
        Constant.showLoading(this)

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcyNoticeBoard.visibility = View.VISIBLE

        binding.rcyNoticeBoard.layoutManager = GridLayoutManager(this, 2)
        binding.rcyNoticeBoard.isNestedScrollingEnabled = false
        noticeboardadapter.isLoading = true
        noticeboardadapter.notifyDataSetChanged()

        if (userDetails!!.staff_role == Constant.isStaffRole) {
            appViewModel!!.isNoticeBoardReport(isAccessToken!!, this)
        } else {
            appViewModel!!.isNoticeBoardStaffReport(isAccessToken!!, this)
        }

    }


    fun showEditDeletePopup(data: NoticeStaffData, anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_delete, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        val layoutEdit = popupView.findViewById<LinearLayout>(R.id.layout_edit)
        val layoutDelete = popupView.findViewById<LinearLayout>(R.id.layout_delete)

        layoutEdit.visibility = if (data.can_edit) View.VISIBLE else View.GONE
        layoutDelete.visibility = if (data.can_delete) View.VISIBLE else View.GONE

        layoutEdit.setOnClickListener {
            Constant.isClickEdit = true
            val intent = Intent(this, CreateNoticeBoard::class.java)
            intent.putExtra(Constant.notice_data, data)
            startActivity(intent)
            popupWindow.dismiss()
        }

        layoutDelete.setOnClickListener {
            showSendConfirmationDialog(false)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, 0, 10)
    }


    override fun onClickListener(
        data: NoticeStaffData,
        anchorView: View,
        adapterPosition: Int
    ) {
        isNoticeBoardId = data.id
        isNoticeBoardPosition = adapterPosition
        showEditDeletePopup(data, anchorView)
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        Log.d("SearchResult", "Search result empty? $isEmpty for query '${binding.edtSearch.text}'")
        if (isEmpty) {
            Log.d("NOData", "NoData")
            binding.rcyNoticeBoard.visibility = View.GONE
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
        } else {
            Log.d("NOData", "Data")
            binding.rcyNoticeBoard.visibility = View.VISIBLE
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
        }
    }

    fun showSendConfirmationDialog(isNoticeBoardUpdate: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        alertMessage.text = getString(R.string.are_you_sure_want_to_update_this_noticeboard)


        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            val jsonObject = JsonObject()
            jsonObject.addProperty(APIKeyNames.id, isNoticeBoardId)
            appViewModel?.isnoticeboarddelete(
                isAccessToken!!,
                jsonObject,
                this
            )
        }

        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    override fun onClick(v: View?) {

    }
}