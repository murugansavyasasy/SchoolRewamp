package com.vs.schoolmessenger.School.Assignment

import android.app.AlertDialog
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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.Parent.Assignment.AssignmentAdapter
import com.vs.schoolmessenger.Parent.Assignment.AssignmentClickListener
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentData
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.SubmittedAssignment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.School.Assignment.Model.AssignmentStudentListClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AssignmentReportBinding

class AssignmentReport : BaseActivity<AssignmentReportBinding>(),
    AssignmentStudentListClickListener, AssignmentClickListener {

    private lateinit var adapter: AssignmentStudentListAdapter


    override fun getViewBinding(): AssignmentReportBinding {
        return AssignmentReportBinding.inflate(layoutInflater)
    }


    var isAssignmentId = ""
    var isAssignmentPosition = 0
    var isTotalSelectedItem = 0
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null


    var isAssignmentType = ""
    var isSelectedDate = ""

    var isAcademicServerLoad = false
    private var isAssignmentReportData: List<AssignmentData>? = null
    var isAssignmentAdapter: AssignmentAdapter? = null
    var isValidAcademicYear = false
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    var isAcademicYear: List<AcademicYear>? = null
    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        val params = binding.toolbarLayout.lytTitleAndName.layoutParams as RelativeLayout.LayoutParams// Get current layout params (RelativeLayout.LayoutParams)
        params.removeRule(RelativeLayout.START_OF)// Remove the old rule
        params.addRule(RelativeLayout.START_OF, R.id.rlaSpinner)// Add the new rule -> align to start of rlaSpinner
        binding.toolbarLayout.lytTitleAndName.layoutParams = params// Re-apply params


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.layoutCreateSlot.visibility = View.GONE
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.rlaSpinner.visibility=View.VISIBLE
        binding.toolbarLayout.imgBack.setOnClickListener{onBackPressed()}
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)
        isValidAcademicYear =
            isAcademicYear?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYear!![0].id
        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year

        binding.toolbarLayout.imgSearchToolBarforCreate.setOnClickListener {
            if (binding.toolbarLayout.rytSearch.visibility == View.VISIBLE) {
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.toolbarLayout.txtSearch.text.clear()

            } else {
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.toolbarLayout.txtSearch.text.clear()
            }
        }

        binding.rcyAssignmentReport.layoutManager = LinearLayoutManager(this)
        adapter = AssignmentStudentListAdapter(
            itemList = emptyList(),
            listener = this,
            context = this,
            isLoading = false,
            noDataImage = binding.noDataImage,
            noDataText = binding.noDataFound
        )

        binding.rcyAssignmentReport.adapter = isAssignmentAdapter

        binding.toolbarLayout.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isAssignmentAdapter?.filter?.filter(s)
                binding.rcyAssignmentReport.post {
                    if (isAssignmentAdapter?.itemCount == 0) {
                        binding.rcyAssignmentReport.visibility = View.GONE
                        binding.lytNoDataFound.visibility = View.VISIBLE
                    } else {
                        binding.rcyAssignmentReport.visibility = View.VISIBLE
                        binding.lytNoDataFound.visibility = View.GONE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.toolbarLayout.layoutCreateSlot.setOnClickListener {
            val intent = Intent(this, AssignmentCreate::class.java)
            startActivity(intent)
        }


        binding.toolbarLayout.txtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
                binding.toolbarLayout.txtSearch.clearFocus()
                true
            } else false
        }

        appViewModel?.getassignmentlist?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
                binding.toolbarLayout.imgSearchToolBarforCreate.visibility=View.VISIBLE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
//                adapter.updateList(response.data)
                binding.rcyAssignmentReport.visibility = View.VISIBLE
                binding.lytNoDataFound.visibility = View.GONE
            } else {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
                binding.toolbarLayout.imgSearchToolBarforCreate.visibility=View.GONE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.rcyAssignmentReport.visibility = View.GONE
                binding.lytNoDataFound.visibility = View.VISIBLE
                binding.noDataFound.text = getString(R.string.no_data_found)
            }
        }

        appViewModel!!.isAssignmentDelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@AssignmentReport)
                    isAssignmentAdapter!!.removeItemAt(isAssignmentPosition)
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        appViewModel!!.isGetAssignmentReport?.observe(this) { response ->
            Constant.hideLoading(this@AssignmentReport)
            if (response != null) {
                if (response.status) {
                    binding.rcyAssignmentReport.visibility = View.VISIBLE
                    binding.lytNoDataFound.visibility = View.GONE
                    val isAssignmentReport = response.data
                    isAssignmentReportData = isAssignmentReport
                    loadAssignmentReportData()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
                } else {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
                    binding.toolbarLayout.imgSearchToolBarforCreate.visibility=View.GONE
                    binding.toolbarLayout.rytSearch.visibility = View.GONE
                    binding.rcyAssignmentReport.visibility = View.GONE
                    binding.lytNoDataFound.visibility = View.VISIBLE
                    binding.noDataFound.text = getString(R.string.no_data_found)
                }
            }
        }
    }


    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = NewAcademicYearAdapter(this, isAcademicYear)
        binding.toolbarLayout.isAcademicSpinner.adapter = adapter
        binding.toolbarLayout.isAcademicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                val selectedOption = isAcademicYear!![position]
                isAcademicYearId = selectedOption.id
                isCurrentAcademicYear = selectedOption.current_academic_year
                Log.d(
                    "DropdownMenu",
                    "Clicked Standard Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                )
                fetchAssignmentReportData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun fetchAssignmentReportData() {
        Constant.showLoading(this@AssignmentReport)
        binding.rcyAssignmentReport.visibility = View.VISIBLE
        isAssignmentAdapter =
            AssignmentAdapter(mutableListOf(), this, this, Constant.isShimmerViewDisable)
        binding.rcyAssignmentReport.layoutManager = LinearLayoutManager(this)
        binding.rcyAssignmentReport.isNestedScrollingEnabled = false
        binding.rcyAssignmentReport.adapter = isAssignmentAdapter
        appViewModel?.isGetAssignmentReport(
            isAccessToken!!, isAcademicYearId, this
        )
    }


    private fun loadAssignmentReportData() {
        if (isAssignmentReportData.isNullOrEmpty()){
            binding.toolbarLayout.imgSearchToolBarforCreate.visibility=View.GONE
            binding.toolbarLayout.rytSearch.visibility = View.GONE
        }
        else{
            binding.toolbarLayout.imgSearchToolBarforCreate.visibility=View.VISIBLE
            binding.toolbarLayout.rytSearch.visibility = View.GONE
            binding.rcyAssignmentReport.visibility = View.VISIBLE
            isAssignmentAdapter = AssignmentAdapter(
                isAssignmentReportData!!.toMutableList(), this, this, Constant.isShimmerViewDisable
            )
            binding.rcyAssignmentReport.layoutManager = LinearLayoutManager(this)
            binding.rcyAssignmentReport.isNestedScrollingEnabled = false
            binding.rcyAssignmentReport.adapter = isAssignmentAdapter
        }

    }

    override fun onSubmittedClick(data: AssignmentData) {
        val intent = Intent(this, AssignmentStudentList::class.java)
        intent.putExtra(Constant.assignment_id, data.id)
        intent.putExtra(Constant.type, Constant.SUBMITTED)
        startActivity(intent)
    }

    override fun onEditAndDeleteClick(
        data: AssignmentData, anchorView: View, adapterPosition: Int
    ) {
        isAssignmentId = data.id
        isAssignmentPosition = adapterPosition
        showEditDeletePopup(data, anchorView)
    }

    override fun onNotSubmittedClick(data: AssignmentData) {
        val intent = Intent(this, AssignmentStudentList::class.java)
        intent.putExtra(Constant.assignment_id, data.id)
        intent.putExtra(Constant.type, Constant.NOTSUBMITTED)
        startActivity(intent)
    }

    override fun onItemClick(
        data: AssignmentData,
        holder: AssignmentAdapter.DataViewHolder
    ) {
        TODO("Not yet implemented")
    }

    override fun onReadStatusClick(
        isData: ParentAssignmentData,
        isPosition: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onClickListener(
        data: SubmittedAssignment,
        anchorView: View,
        adapterPosition: Int
    ) {
        TODO("Not yet implemented")
    }


    fun showEditDeletePopup(data: AssignmentData, anchor: View) {
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
            Constant.isClickEdit=true
            val intent = Intent(this, AssignmentCreate::class.java)
            intent.putExtra(Constant.assignment_data, data)
            startActivity(intent)
            //   isEditProcess(data)
            popupWindow.dismiss()
        }

        layoutDelete.setOnClickListener {
            showSendConfirmationDialog(false)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, 0, 10)
    }

    fun showSendConfirmationDialog(isEventUpdate: Boolean) {
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
                jsonObject.addProperty(APIKeyNames.id, isAssignmentId)
                appViewModel?.isAssignmentDelete(isAccessToken!!, jsonObject, this)

        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

}