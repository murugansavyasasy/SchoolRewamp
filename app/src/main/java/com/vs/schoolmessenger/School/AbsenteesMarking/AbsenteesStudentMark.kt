package com.vs.schoolmessenger.School.AbsenteesMarking

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SpecificStudentData.SpecificStudentSelectClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingAdapter.AbsenteesMarkAdapter
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.AbsenteesStudentMarkingBinding

class AbsenteesStudentMark : BaseActivity<AbsenteesStudentMarkingBinding>(),
    SpecificStudentSelectClickListener,
    AbsenteesSelectionListener,
    View.OnClickListener {

    private val selectedIds = mutableListOf<String>()
    lateinit var mAdapter: AbsenteesMarkAdapter
    private var appViewModel: App? = null
    val isSpecificStudent = mutableListOf<NameAndIds>()
    private var studentsList: List<NameAndIds>? = null
    private var isSelectedIds: List<String>? = null
    private lateinit var isStandardName: String
    private lateinit var isSectionName: String
    private var AllPresent: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var isAccessToken: String
    var isAcademicYearId = -1
    var isSectionId: String? = null
    private var filterSelectedOption: String? = null


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

        val filterCaterotyType = listOf(
            getString(R.string.nameasc),
            getString(R.string.namedsc),
            getString(R.string.admis_no_asc),
            getString(R.string.admis_no_dsc),
            getString(R.string.rollasc),
            getString(R.string.rolldsc)
        )

//        binding.toolbarLayout.cbSelect.setOnClickListener {
//            val isChecked = binding.toolbarLayout.cbSelect.isChecked
//            mAdapter.setAllAbsent(isChecked)
//        }

        binding.toolbarLayout.cbSelect.setOnClickListener {
            if (binding.toolbarLayout.cbSelect.isChecked) {
                isSpecificStudent.clear()
                studentsList?.forEach {
                    isSpecificStudent.add(it)
                }
                mAdapter.setAllAbsent(true)
            } else {
                isSpecificStudent.clear()
                mAdapter.setAllAbsent(false)
            }
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
        setupFilterCaterotyType(filterCaterotyType)

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextSSS",s.toString())
                filter(s.toString())

            }
        })
    }

    private fun setupFilterCaterotyType(filterCaterotyType: List<String>) {
        val adapter = SpinnerLoadingAdapter(this, filterCaterotyType)
        binding.isSpinnerSort.adapter = adapter

        binding.isSpinnerSort.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    handleSpinnerSelection(position, adapter,filterCaterotyType)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        // Preselect first item manually
        adapter.selectedPosition = 0
        binding.isSpinnerSort.setSelection(0)
        adapter.notifyDataSetChanged()
        handleSpinnerSelection(0, adapter,filterCaterotyType)
    }

    private fun handleSpinnerSelection(position: Int, adapter: SpinnerLoadingAdapter,filterCaterotyType: List<String>) {
        if (adapter.selectedPosition != position) {
            adapter.selectedPosition = position
            adapter.notifyDataSetChanged()

            filterSelectedOption = filterCaterotyType[position]

            when (filterSelectedOption) {
                getString(R.string.admis_no_asc) -> {
                    mAdapter.sortData(AbsenteesMarkAdapter.SortType.NO_ASC)
                }

                getString(R.string.admis_no_dsc) -> {
                    mAdapter.sortData(AbsenteesMarkAdapter.SortType.NO_DESC)
                }

                getString(R.string.nameasc) -> {
                    mAdapter.sortData(AbsenteesMarkAdapter.SortType.NAME_ASC)
                }

                getString(R.string.namedsc) -> {
                    mAdapter.sortData(AbsenteesMarkAdapter.SortType.NAME_DESC)
                }

                getString(R.string.rollasc) -> {
                    mAdapter.sortData(AbsenteesMarkAdapter.SortType.REG_ASC)
                }

                getString(R.string.rolldsc) -> {
                    mAdapter.sortData(AbsenteesMarkAdapter.SortType.REG_DSC)
                }
            }

        }
    }

    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            studentsList.orEmpty()
        } else {
            studentsList.orEmpty().filter { student ->
                val fieldsToSearch = listOf(
                    student.name?.lowercase().orEmpty(),
                    student.admission_no?.lowercase().orEmpty(),
                    student.roll_no?.lowercase().orEmpty()
                )

                searchWords.all { word ->
                    fieldsToSearch.any { field -> field.contains(word) }
                }
            }
        }

        if (filteredList.isNotEmpty()) {
            ShowData()
            mAdapter.updateData(filteredList)
        } else {
            binding.recycleStudents.visibility = View.GONE
            ErrorMessage(Constant.NO_DATA_FOUND)
        }
    }

    fun ShowData() {
        binding.recycleStudents.visibility = View.VISIBLE
        binding.lytNoDataFound.visibility = View.GONE
    }

    fun ErrorMessage(ErrorMessage: String) {
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = ErrorMessage
    }

    override fun onResume() {
        super.onResume()

        mAdapter = AbsenteesMarkAdapter(null, this, Constant.isShimmerViewShow, this,this)
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
                studentsList, this, Constant.isShimmerViewDisable, this,this
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

    override fun onIdCheck(data: NameAndIds) {
        if (!isSpecificStudent.any { it.id == data.id }) {
            isSpecificStudent.add(data)
        }
        binding.toolbarLayout.cbSelect.isChecked = isSpecificStudent.size == studentsList?.size
    }

    override fun onIdUnchecked(data: NameAndIds) {
        isSpecificStudent.removeAll { it.id == data.id }
        binding.toolbarLayout.cbSelect.isChecked = false
    }
}