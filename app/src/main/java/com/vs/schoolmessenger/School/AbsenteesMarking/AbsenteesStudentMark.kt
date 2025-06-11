package com.vs.schoolmessenger.School.AbsenteesMarking

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingAdapter.AbsenteesMarkAdapter
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AbsenteesStudentMarkingBinding

class AbsenteesStudentMark : BaseActivity<AbsenteesStudentMarkingBinding>(),
    AbsenteesSelectionListener,
    View.OnClickListener {

    private val selectedIds = mutableListOf<String>()
    lateinit var mAdapter: AbsenteesMarkAdapter
    private var appViewModel: App? = null
    private var studentsList: List<NameAndIds>? = null
    private var isSelectedIds: List<String>? = null
    private lateinit var isStandardName: String
    private lateinit var isSectionName: String
    private var AllPresent: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var isAccessToken: String
    var isAcademicYearId = -1
    var isSectionId: String? = null


    override fun getViewBinding(): AbsenteesStudentMarkingBinding {
        return AbsenteesStudentMarkingBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rytSend.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isStandardName = Constant.isMarkAttendanceDataSending?.class_name.toString()
        isSectionName = Constant.isMarkAttendanceDataSending?.section_name.toString()
        isSectionId = Constant.isMarkAttendanceDataSending?.section_id
        isAcademicYearId = Constant.isMarkAttendanceDataSending?.academic_year_id!!
        binding.toolbarLayout.cbSelect.visibility = View.VISIBLE
        binding.toolbarLayout.cbSelect.text = getString(R.string.Selectall)
        binding.toolbarLayout.cbSelect.setOnClickListener {
            val isChecked = binding.toolbarLayout.cbSelect.isChecked
            mAdapter.setAllAbsent(isChecked)
        }
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.MarkAttendance)
        binding.toolbarLayout.lblSchoolName.text =
            isStaffDetails!!.school_name + " | " + isStandardName + "-" + isSectionName


        appViewModel!!.isSendAbsenteeSMS?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@AbsenteesStudentMark)
                    Log.d("isSendAbsenteeSMS", response.message)
                    Constant.showDataValidation(getString(R.string.success), response.message, this)
                } else {
                    Constant.showDataValidation(getString(R.string.fail), response.message, this)
                }
            }
        }


        appViewModel!!.isStudentList!!.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.lnrHeader.visibility = View.VISIBLE
                    binding.recycleStudents.visibility = View.VISIBLE
                    studentsList = response.data
                    loadStudentAbsenteesList(studentsList!!)

                } else {
                    binding.lnrHeader.visibility = View.GONE
                    binding.recycleStudents.visibility = View.GONE
                    ErrorMessage(response.message)

                }
            }
        }
    }

    fun ErrorMessage(ErrorMessage: String) {
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = ErrorMessage
    }

    override fun onResume() {
        super.onResume()

        mAdapter = AbsenteesMarkAdapter(null, this, Constant.isShimmerViewShow, this)
        binding.recycleStudents.layoutManager = LinearLayoutManager(this)
        binding.recycleStudents.adapter = mAdapter

        appViewModel!!.isGetStudentList(
            isAccessToken!!,
            isSectionId!!.toString(), isAcademicYearId!!, this
        )

    }

    fun loadStudentAbsenteesList(studentsList: List<NameAndIds>) {
        mAdapter =
            AbsenteesMarkAdapter(
                studentsList, this, Constant.isShimmerViewDisable, this
            )
        binding.recycleStudents.adapter = mAdapter
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rytSend -> {
                isMarkAttendance()
            }
        }

    }


    private fun isMarkAttendance() {
        AllPresent = if (isSelectedIds.isNullOrEmpty()) "T" else "F"
        if (Constant.isMarkAttendanceDataSending?.class_id != "" && Constant.isMarkAttendanceDataSending?.section_id != ""
            && Constant.isMarkAttendanceDataSending?.attendance_date != null
        ) {

            if (Constant.isMarkAttendanceDataSending?.attendance_type == "F" && Constant.isMarkAttendanceDataSending?.session_type == "") {
                isUpdateMarkAtttendance()

            } else if (Constant.isMarkAttendanceDataSending?.attendance_type == "H" && Constant.isMarkAttendanceDataSending?.session_type!!.isNotEmpty()) {
                isUpdateMarkAtttendance()
            }
        }
    }

    private fun isUpdateMarkAtttendance() {


        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.class_id, Constant.isMarkAttendanceDataSending?.class_id)
            addProperty(APIKeyNames.section_id, Constant.isMarkAttendanceDataSending?.section_id)
            addProperty(APIKeyNames.all_present, AllPresent)
            addProperty(
                APIKeyNames.attendance_type,
                Constant.isMarkAttendanceDataSending?.attendance_type
            )
            addProperty(
                APIKeyNames.session_type,
                Constant.isMarkAttendanceDataSending?.session_type
            )
            addProperty(
                APIKeyNames.attendance_date,
                Constant.isMarkAttendanceDataSending?.attendance_date
            )
            val studentArray = JsonArray().apply {
                isSelectedIds?.forEach { id ->
                    add(JsonObject().apply {
                        addProperty("ID", id)
                    })
                }
            }
            add(APIKeyNames.student_id, studentArray)
            Log.d("AbsenteesStudentID", studentArray.toString())
        }
        appViewModel?.isUpdateSendAbsenteeSMS(isAccessToken!!, jsonObject, this)

    }


    override fun onSelectionChanged(selectedIds: List<String>) {
        Log.d("ActivitySelectedIDs", selectedIds.toString())
        isSelectedIds = selectedIds
    }
}