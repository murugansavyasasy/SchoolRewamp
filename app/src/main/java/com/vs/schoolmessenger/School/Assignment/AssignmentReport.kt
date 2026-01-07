package com.vs.schoolmessenger.School.Assignment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
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
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.FileItem
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
    private var userDetails: UserDetails? = null
    private var msg_id: Int = -1
    private var headerId: String? = null
    private var instituteId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        //in futher if you want the rlaSpinner to be visible in the toolbar in the postion make sure gone the imgSearchToolBar and visible the imgSearchToolBarforCreate
//        val params =
//            binding.toolbarLayout.lytTitleAndName.layoutParams as RelativeLayout.LayoutParams// Get current layout params (RelativeLayout.LayoutParams)
//        params.removeRule(RelativeLayout.START_OF)
//        params.addRule(RelativeLayout.START_OF, R.id.rlaSpinner)
//        binding.toolbarLayout.lytTitleAndName.layoutParams = params
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        userDetails = SharedPreference.getUserDetails(this)
        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)
        if (fromNotification) {
            Constant.isParentChoose = false
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            instituteId = intent.getStringExtra(Constant.institute_id)
            menu_name = intent.getStringExtra(Constant.menu_name)
            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )
            val matchedChild = userDetails?.staff_details?.find { it.school_id == instituteId }
            SharedPreference.putStaffDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }
        binding.toolbarLayout.layoutCreateSlot.visibility = View.GONE
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.rlaSpinner.visibility = View.GONE
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        isAcademicYear = isAcademicYearList
        isLoadAcademicYear(isAcademicYear)
        if (!isAcademicYear.isNullOrEmpty()) {
            isValidAcademicYear = isAcademicYear!!.any { it.current_academic_year == true }
            isAcademicYearId = isAcademicYear!![0].id
            isAcademicYearId = Constant.isCurrentAcademicYearId
            isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
        }
        if (fromNotification) {
            isGetAcademicYear()
        }
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.toolbarLayout.rytSearch.isVisible) {
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.toolbarLayout.txtSearch.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
            } else {
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.toolbarLayout.txtSearch.text.clear()
                binding.toolbarLayout.txtSearch.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.toolbarLayout.txtSearch, InputMethodManager.SHOW_IMPLICIT)
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
                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) {
                    isAssignmentReportData?.let { data ->
                        isAssignmentAdapter?.updateList(data)
                    } ?: run {
                        isAssignmentAdapter?.updateList(emptyList())
                    }
                } else {
                    isAssignmentAdapter?.filter?.filter(query)
                }
                binding.rcyAssignmentReport.post {
                    updateNoDataVisibility()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
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
        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            response?.data?.let { academicList ->
                val data = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYearList == data) return@observe
                isAcademicYearList = data
                isAcademicYear = data
                val activeYear = data.find { it.current_academic_year == true }
                Constant.isCurrentAcademicYearId = activeYear?.id!!
                isAcademicYearId = Constant.isCurrentAcademicYearId
                isLoadAcademicYear(isAcademicYear)
            }
        }


        appViewModel?.getassignmentlist?.observe(this) { response ->
            if (response != null) {
                if (response?.status == true && !response.data.isNullOrEmpty()) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    binding.toolbarLayout.rytSearch.visibility = View.GONE
// adapter.updateList(response.data)
                    binding.rcyAssignmentReport.visibility = View.VISIBLE
                    binding.lytNoDataFound.visibility = View.GONE
                    if (fromNotification) {
                        scrollToMessageId(headerId)
                    }
                } else {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.toolbarLayout.rytSearch.visibility = View.GONE
                    binding.rcyAssignmentReport.visibility = View.GONE
                    binding.lytNoDataFound.visibility = View.VISIBLE
                    binding.noDataFound.text = getString(R.string.no_data_found)
                }
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
                    val isAssignmentReport = response.data ?: emptyList() // Ensure non-null
                    isAssignmentReportData = isAssignmentReport
                    loadAssignmentReportData()
                    if (fromNotification) {
                        scrollToMessageId(headerId)
                    }
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
                } else {
                    isAssignmentReportData = emptyList()
                    loadAssignmentReportData()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
                }
            }
        }
    }

    private fun isGetAcademicYear() {
        appViewModel!!.isGetAcademicYear(isAccessToken!!, this)
    }


    private fun scrollToMessageId(headerId: String?) {
        if (msg_id == -1 || headerId.isNullOrEmpty()) return

        val pos = isAssignmentAdapter?.getPositionById(headerId)
            ?: isAssignmentReportData?.indexOfFirst { it.id == headerId } ?: -1

        if (pos == -1) {
            Log.d("ScrollDebug", "No item found with headerId: $headerId")
            return
        }

        Log.d("ScrollDebug", "Scrolling to index $pos")

        binding.rcyAssignmentReport.post {
            (binding.rcyAssignmentReport.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(pos, 0)

            val listener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        rv.removeOnScrollListener(this)
                        rv.post { highlightItemTemporarily(rv, pos) }
                    }
                }
            }

            binding.rcyAssignmentReport.addOnScrollListener(listener)
            binding.rcyAssignmentReport.postDelayed({
                highlightItemTemporarily(binding.rcyAssignmentReport, pos)
            }, 60)
        }
    }


    private fun highlightItemTemporarily(recyclerView: RecyclerView, position: Int) {
        // Try a few times if not yet bound.
        val maxRetries = 6
        val retryDelay = 80L

        fun tryHighlight(attempt: Int) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            if (viewHolder?.itemView != null) {
                val itemView = viewHolder.itemView
                val originalBackground = itemView.background

                itemView.setBackgroundColor(Color.parseColor("#FFE082"))

                Handler(Looper.getMainLooper()).postDelayed({
                    itemView.background = originalBackground
                }, 3000)
            } else if (attempt < maxRetries) {
                recyclerView.postDelayed({ tryHighlight(attempt + 1) }, retryDelay)
            } else {
                Log.d(
                    "ScrollDebug",
                    "Failed to highlight position $position after $maxRetries attempts"
                )
            }
        }

        tryHighlight(0)
    }


    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = NewAcademicYearAdapter(this, isAcademicYear)
        binding.toolbarLayout.isAcademicSpinner.adapter = adapter
        binding.toolbarLayout.isAcademicSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
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
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        binding.toolbarLayout.txtSearch.setText("")
        binding.lytNoDataFound.visibility = View.GONE
        binding.rcyAssignmentReport.visibility = View.VISIBLE
        val shimmerLoading = !Constant.isShimmerViewDisable
        isAssignmentAdapter = AssignmentAdapter(mutableListOf(), this, this, shimmerLoading)
        binding.rcyAssignmentReport.layoutManager = LinearLayoutManager(this)
        binding.rcyAssignmentReport.isNestedScrollingEnabled = false
        binding.rcyAssignmentReport.adapter = isAssignmentAdapter
        appViewModel?.isGetAssignmentReport(isAccessToken!!, isAcademicYearId, this)
    }

    private fun loadAssignmentReportData() {
        val query = binding.toolbarLayout.txtSearch.text.toString().trim() // Capture current query
        val hasData = !isAssignmentReportData.isNullOrEmpty()
        if (!hasData) {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.toolbarLayout.rytSearch.visibility = View.GONE
            binding.rcyAssignmentReport.visibility = View.GONE
            binding.lytNoDataFound.visibility = View.VISIBLE
            binding.noDataFound.text = getString(R.string.no_data_found) // Set message if needed
            isAssignmentAdapter = null
        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            binding.toolbarLayout.rytSearch.visibility =
                View.GONE // Hide search layout, but keep input visible if active
            binding.rcyAssignmentReport.visibility = View.VISIBLE
            binding.lytNoDataFound.visibility = View.GONE
            isAssignmentAdapter = AssignmentAdapter(
                isAssignmentReportData!!.toMutableList(), this, this, Constant.isShimmerViewDisable
            )
            binding.rcyAssignmentReport.layoutManager = LinearLayoutManager(this)
            binding.rcyAssignmentReport.isNestedScrollingEnabled = false
            binding.rcyAssignmentReport.adapter = isAssignmentAdapter
            if (query.isNotEmpty()) {
                isAssignmentAdapter?.filter?.filter(query)
            }
            binding.rcyAssignmentReport.post {
                updateNoDataVisibility()
            }
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

    private fun updateNoDataVisibility() {
        if (isAssignmentAdapter?.itemCount == 0) {
            binding.rcyAssignmentReport.visibility = View.GONE
            binding.lytNoDataFound.visibility = View.VISIBLE
            binding.noDataFound.text =
                if (binding.toolbarLayout.txtSearch.text.toString().trim().isNotEmpty()) {
                    getString(R.string.no_data_found)
                } else {
                    getString(R.string.no_data_found)
                }
        } else {
            binding.rcyAssignmentReport.visibility = View.VISIBLE
            binding.lytNoDataFound.visibility = View.GONE
        }
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
            Constant.isClickEdit = true
            val intent = Intent(this, AssignmentCreate::class.java)
            intent.putExtra(Constant.assignment_data, data)
            startActivity(intent)
            // isEditProcess(data)
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

    override fun onResume() {
        super.onResume()
        if (isAcademicYearId != -1) {
            fetchAssignmentReportData()
        }
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        binding.toolbarLayout.txtSearch.setText("")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (fromNotification) {
            val intent = Intent(this, SchoolDashboard::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}