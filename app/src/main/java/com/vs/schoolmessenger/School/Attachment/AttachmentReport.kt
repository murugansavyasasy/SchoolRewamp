package com.vs.schoolmessenger.School.Attachment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentDataReport
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.AttachmentReportBinding

class AttachmentReport : BaseActivity<AttachmentReportBinding>(), View.OnClickListener,
    OnAttachmentReportClickListener {

    override fun getViewBinding(): AttachmentReportBinding {
        return AttachmentReportBinding.inflate(layoutInflater)
    }

    private var cameraPermissionDeniedCount = 0
    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        private const val MAX_FILES = 10

        private const val CAMERA_IMAGE_REQUEST = 1004
    }

    var mAttachmentReportAdapter: AttachmentReportAdapter? = null

    private var isUserDetails: UserDetails? = null
    var isMultipleSchool = false
    private var appViewModel: App? = null

    var isAccessToken = ""
    var isAttachmentId = ""
    var isAttachmentPosition = 0
    private var isStaffDetails: StaffDetails? = null
    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null
    private var completeAttachmentList: List<AttachmentDataReport> = emptyList()
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isTotalSelectedItem = 0
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null

    override fun setupViews() {
        //Important Note:see actually what ever token we pass,From backend we recieve all the data from all school we are suppose to filter them using the school id this scenrio is for multiple school
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName

//        binding.lnrTabOneName.setOnClickListener(this)
//        binding.lnrTabTwoName.setOnClickListener(this)

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener{
            if (binding.search.isVisible) {
                binding.search.visibility = View.GONE
                binding.edtSearch.text.clear()
            } else {
                binding.search.visibility = View.VISIBLE
                binding.edtSearch.text.clear()

            }
        }



        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isUserDetails = SharedPreference.getUserDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)

        if (isUserDetails?.staff_role.equals(Constant.isStaffRole)){
            binding.rytSpinner.visibility=View.GONE
            isAccessToken = isStaffDetails!!.access_token
            isGetAttachmentReport()
            binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        }
        else{
            if (isUserDetails?.staff_details?.size!! > 1) {
                binding.rytSpinner.visibility = View.VISIBLE
                //Important Note:see actually what ever token we pass,From backend we recieve all the data from all school we are suppose to filter them using the school id this scenrio is for multiple school
                isUserDetails?.let { setupSchoolSpinner(it.staff_details) }
            }
            else{
                isAccessToken = isUserDetails!!.staff_details.get(0).access_token
                binding.rytSpinner.visibility=View.GONE
                isGetAttachmentReport()
                binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
                binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
            }
        }


        binding.toolbarLayout.layoutCreateSlot.visibility = View.GONE
        binding.toolbarLayout.layoutCreateSlot.setOnClickListener {
            val intent = Intent(this, Attachment::class.java)
            startActivity(intent)
        }

        appViewModel!!.isDeleteAttachment?.observe(this) { response ->
            if (response != null) {
                Constant.hideLoading(this@AttachmentReport)
                if (response.status){
                    mAttachmentReportAdapter!!.removeItemAt(isAttachmentPosition)
                    Constant.showDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                }else{
                    mAttachmentReportAdapter!!.removeItemAt(isAttachmentPosition)
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }


        appViewModel!!.isAttachmentReportResponse?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcyAttachment.visibility= View.VISIBLE
                    binding.txtNoData.visibility= View.GONE
                    binding.nomessage.visibility= View.GONE
                    completeAttachmentList=response.data
                    isLoadAttachmentReportList(response.data)
                }else{
                    binding.rcyAttachment.visibility= View.GONE
                    binding.txtNoData.visibility= View.VISIBLE
                    binding.txtNoData.text=response.message
                    binding.nomessage.visibility= View.VISIBLE
                    binding.search.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                }
            }else{
                binding.rcyAttachment.visibility= View.GONE
                binding.txtNoData.visibility= View.VISIBLE
                binding.txtNoData.text=response?.message?:getString(R.string.no_list_found)
                binding.nomessage.visibility= View.VISIBLE
                binding.search.visibility = View.GONE
                binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE

            }
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mAttachmentReportAdapter?.filter?.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun isLoadAttachmentReportList(isHomeAttachmentReport: List<AttachmentDataReport>) {

        if (isHomeAttachmentReport.isNullOrEmpty()){
            binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
            binding.search.visibility = View.GONE
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
        }
        else{
            binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
            binding.search.visibility = View.GONE

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)


            mAttachmentReportAdapter = AttachmentReportAdapter(
                isHomeAttachmentReport,
                this,
                this,
                Constant.isShimmerViewDisable,
                binding.nomessage,
                binding.txtNoData
            )


            binding.rcyAttachment.layoutManager = LinearLayoutManager(this)
            binding.rcyAttachment.isNestedScrollingEnabled = false
            binding.rcyAttachment.adapter = mAttachmentReportAdapter
        }
    }


    fun isGetAttachmentReport() {
        mAttachmentReportAdapter =
            AttachmentReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyAttachment.layoutManager = LinearLayoutManager(this)
        binding.rcyAttachment.isNestedScrollingEnabled = false
        binding.rcyAttachment.adapter = mAttachmentReportAdapter
        appViewModel!!.getAttachmentListReport(
            isAccessToken, this
        )
    }

//    private fun setupSchoolSpinner(staffList: List<StaffDetails>) {
//        val schoolNames = staffList.map { it.school_name }
//        Log.d("schoolNames", schoolNames.size.toString())
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, schoolNames)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.schoollistfilter.adapter = adapter
//
//        binding.schoollistfilter.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    parent: AdapterView<*>, view: View?, position: Int, id: Long
//                ) {
//                    val selectedStaff = staffList[position]
//                    isAccessToken = selectedStaff.access_token
//                    isStaffDetails = selectedStaff
//                    SharedPreference.putStaffDetails(this@AttachmentReport,isStaffDetails!!)
//                    Log.d(
//                        "SpinnerSelection",
//                        "Selected school: ${selectedStaff.school_name}, Token: $isAccessToken"
//                    )
//                    isGetAttachmentReport()
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>) {}
//            }
//
//        if (staffList.isNotEmpty()) {
//            isAccessToken = staffList[0].access_token
//            isStaffDetails = staffList[0]
//            Log.d("DefaultSelection", "Default token: $isAccessToken")
//        }
//    }

    private fun setupSchoolSpinner(staffList: List<StaffDetails>) {

        // Prepare spinner list (Add "All" + school names)
        val schoolNames = mutableListOf<String>()
        schoolNames.add("All")
        schoolNames.addAll(staffList.map { it.school_name })

        // Use your custom spinner adapter
        val adapter = SpinnerLoadingAdapter(this, schoolNames)
        binding.schoollistfilter.adapter = adapter
        binding.schoollistfilter.setSelection(0)

        binding.schoollistfilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                private var lastSelectedPosition: Int = -1

                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    // Highlight currently selected item in custom adapter
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    if (position != lastSelectedPosition) {
                        lastSelectedPosition = position

                        if (position == 0) {
                            // "All" schools selected
                            isLoadAttachmentReportList(completeAttachmentList)
                            binding.toolbarLayout.lblSchoolName.visibility = View.GONE
                            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
                        } else {
                            // Specific school selected
                            val selectedStaff = staffList[position - 1]
                            isAccessToken = selectedStaff.access_token
                            isStaffDetails = selectedStaff
                            val selectedSchoolId = selectedStaff.school_id

                            // Filter attachment list by selected school
                            val filteredList = completeAttachmentList.filter { it.school_id == selectedSchoolId }

                            Log.d(
                                "SpinnerSelection",
                                "Selected school: ${selectedStaff.school_name}, " +
                                        "Selected school id: ${selectedStaff.school_id}, " +
                                        "Data: $filteredList, Token: $isAccessToken"
                            )

                            isLoadAttachmentReportList(filteredList)
                            binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
                            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }


        //Important Note:see actually what ever token we pass,From backend we recieve all the data from all school we are suppose to filter them using the school id this scenrio is for multiple school
        // Initial load (All schools)
        if (staffList.isNotEmpty()) {
            isAccessToken = staffList[0].access_token
            isStaffDetails = staffList[0]
            Log.d("DefaultSelection", "Default token: $isAccessToken")
            isGetAttachmentReport()
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }


    override fun onItemClick(
        isAttachmentData: List<AttachmentDataReport>,
        view: View,
        isPosition: Int
    ) {
        isAttachmentId = isAttachmentData[isPosition].id
        isAttachmentPosition = isPosition
        showEditDeletePopup(isAttachmentData, view)
    }


    @SuppressLint("SuspiciousIndentation")
    fun showEditDeletePopup(data: List<AttachmentDataReport>, anchor: View) {
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

        layoutEdit.setOnClickListener {
            Constant.isClickEdit = true
            val intent = Intent(this, Attachment::class.java)
            val json = Gson().toJson(data)
            intent.putExtra(Constant.attachment_data, json)
            intent.putExtra("isPosition", isAttachmentPosition)
            startActivity(intent)
            popupWindow.dismiss()
        }

        layoutDelete.setOnClickListener {
            showSendConfirmationDialog()
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, 0, 10)
    }


    fun showSendConfirmationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        alertMessage.text = getString(R.string.are_you_sure_want_to_delete)
        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            val jsonObject = JsonObject()
            jsonObject.addProperty(APIKeyNames.id, isAttachmentId)
            appViewModel?.isAttachmentDelete(isAccessToken!!, jsonObject, this)
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    override fun onReadStatusClick(
        isData: List<AttachmentDataReport>,
        isPosition: Int
    ) {

    }
}