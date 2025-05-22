package com.vs.schoolmessenger.School.AbsenteesMarking

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AbsenteesStudentMarkingBinding

class AbsenteesStudentMark : BaseActivity<AbsenteesStudentMarkingBinding>(), AbsenteesClickListener,
    AbsenteesSelectionListener,
    View.OnClickListener {

    private val selectedIds = mutableListOf<String>()
    lateinit var mAdapter: AbsenteesMarkAdapter
    private var appViewModel: App? = null
    private lateinit var studentsList: List<NameAndIds>
    private lateinit var isSelectedIds: List<String>
    private lateinit var isStandardName: String
    private lateinit var isSectionName: String
    private var AllPresent: String? = null

    private lateinit var isAccessToken: String
    var isAcademicYearId: Int? = null
    var isSectionId: Int? = null


    override fun getViewBinding(): AbsenteesStudentMarkingBinding {
        return AbsenteesStudentMarkingBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.rytSend.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStandardName = intent.getStringExtra(Constant.isStandardName) ?: ""
        isSectionName = intent.getStringExtra(Constant.isSectionName) ?: ""
        binding.lnrSelectAll.setOnClickListener(this)

        Log.d("isGetStudentListisStandardName", isStandardName.toString())
        Log.d("isGetStudentListisSectionName", isSectionName.toString())






        isAccessToken = intent.getStringExtra(Constant.isAccessToken) ?: ""
        isAcademicYearId = intent.getIntExtra(Constant.isAcademicYearId, 0)
        isSectionId = intent.getIntExtra(Constant.isSectionId, 0)

        Log.d("isGetStudentListSectionID", isSectionId.toString())
        binding.lblClassAndSection.text = isStandardName + "-" + isSectionName

        appViewModel!!.isSendAbsenteeSMS?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.hideLoading(this@AbsenteesStudentMark)
                Log.d("isSendAbsenteeSMS", response.message)
//                val dialogRootView = view as ViewGroup
//                showTopAlertPopup(response.message, dialogRootView, -1, response.status, "isUpdate")
            }
        }


        appViewModel!!.isGetStudentList(
            isAccessToken!!,
            isSectionId!!.toString(), isAcademicYearId!!, this
        )


        appViewModel!!.isStudentList!!.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    studentsList = response.data
//                    isStudentData = isStudentList
//                    isStudentData()
                } else {
//                    binding.lblNoRecordsFound.visibility = View.VISIBLE
//                    binding.rcySpecificStudent.visibility = View.GONE
//                    binding.lblNoRecordsFound.text = response.message
                }
            } else {
//                binding.lblNoRecordsFound.visibility = View.VISIBLE
            }
        }


    }

    override fun onResume() {
        super.onResume()

        mAdapter = AbsenteesMarkAdapter(null, this, this, Constant.isShimmerViewShow, this)
        binding.recycleStudents.layoutManager = LinearLayoutManager(this)
        binding.recycleStudents.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter =
                AbsenteesMarkAdapter(
                    studentsList, this, this, Constant.isShimmerViewDisable, this
                )
            // Set GridLayoutManager (2 columns in this case)
            binding.recycleStudents.adapter = mAdapter
        }
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

            R.id.lnrSelectAll -> {
                binding.chSelectAll.isChecked = !binding.chSelectAll.isChecked
                val isChecked = binding.chSelectAll.isChecked
                mAdapter.setAllAbsent(isChecked)

            }

        }

    }



    private fun isMarkAttendance() {
        AllPresent = if (selectedIds.isEmpty()) "T" else "F"
        Constant.isAllPresent = AllPresent!!
        val MarkAttendanceData = Constant.isMarkAttendanceDataSending

        Log.d("MARK_ATTENDANCE", "AllPresent = $AllPresent")
        Log.d("MARK_ATTENDANCE", "MarkAttendanceData = $MarkAttendanceData")

        if (MarkAttendanceData?.class_id != "" &&
            MarkAttendanceData?.section_id != "" &&
            MarkAttendanceData?.attendance_date != null) {

            Log.d("MARK_ATTENDANCE", "Basic data valid, checking attendance conditions...")

            if (MarkAttendanceData.attendance_type == "F" &&
                MarkAttendanceData.session_type == "" &&
                selectedIds.isEmpty()) {

                Log.d("MARK_ATTENDANCE", "Calling isUpdateMarkAtttendance() for Full day all present")
                isUpdateMarkAtttendance()

            } else if (MarkAttendanceData.attendance_type == "H" &&
                MarkAttendanceData.session_type!!.isNotEmpty() &&
                selectedIds.isNotEmpty()) {

                Log.d("MARK_ATTENDANCE", "Calling isUpdateMarkAtttendance() for Half day with absentees")
                isUpdateMarkAtttendance()
            } else {
                Log.d("MARK_ATTENDANCE", "No condition matched")
            }

        } else {
            Log.d("MARK_ATTENDANCE", "Invalid or missing class_id/section_id/attendance_date")
        }
    }

//    private fun isMarkAttendance() {
//        // Assign values to the data holder
//        AllPresent = if (selectedIds.isEmpty()) "T" else "F"
//        Constant.isAllPresent = AllPresent!!
//        var MarkAttendanceData = Constant.isMarkAttendanceDataSending
//
//
//        // Decision logic to call update only when necessary
//        if (MarkAttendanceData?.class_id != "" && MarkAttendanceData?.section_id != "" && MarkAttendanceData?.attendance_date != null) {
//            if (MarkAttendanceData?.attendance_type == "F" && MarkAttendanceData?.session_type == "" && selectedIds.isEmpty()) {
//                isUpdateMarkAtttendance()
//            } else if (MarkAttendanceData?.attendance_type == "H" && MarkAttendanceData?.session_type!!.isNotEmpty() && selectedIds.isNotEmpty()) {
//                isUpdateMarkAtttendance()
//            }
//        }
//    }


    private fun isUpdateMarkAtttendance() {
        var MarkAttendanceData = Constant.isMarkAttendanceDataSending


        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.class_id, MarkAttendanceData?.class_id)
            addProperty(APIKeyNames.section_id, MarkAttendanceData?.section_id)
            addProperty(APIKeyNames.all_present, AllPresent)
            addProperty(APIKeyNames.attendance_type, MarkAttendanceData?.attendance_type)
            addProperty(APIKeyNames.session_type, MarkAttendanceData?.session_type)
            addProperty(APIKeyNames.attendance_date, MarkAttendanceData?.attendance_date)
            val studentArray = JsonArray().apply {
                isSelectedIds.forEach { id ->
                    add(JsonObject().apply {
                        addProperty("ID", id)
                    })
                }
            }
            add(APIKeyNames.student_id, studentArray)
            Log.d("AbsenteesStudentID",studentArray.toString())
        }
        appViewModel?.isUpdateSendAbsenteeSMS(isAccessToken!!, jsonObject, this)

    }

    override fun onItemClick(data: NameAndIds) {
        Log.d("SelectedData", data.name)
    }

    override fun onSelectionChanged(selectedIds: List<String>) {
           Log.d("ActivitySelectedIDs", selectedIds.toString())
        isSelectedIds = selectedIds
    }
}