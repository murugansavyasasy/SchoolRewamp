package com.vs.schoolmessenger.School.AbsenteesMarking

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.SortType
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
        binding.cbSelect.visibility = View.VISIBLE
        binding.cbSelect.text = getString(R.string.Selectall)

        binding.toolbarLayout.imgSearch.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
        binding.toolbarLayout.imgBack.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)

        binding.toolbarLayout.imgSearch.setOnClickListener{
            if (binding.rlaSortSearch.visibility == View.VISIBLE) {
                binding.rlaSortSearch.visibility = View.GONE
            } else {
                binding.rlaSortSearch.visibility = View.VISIBLE
                binding.txtSearchMenu.text.clear()
            }
        }


        val filterCaterotyType = listOf(
            getString(R.string.nameasc),
            getString(R.string.namedsc),
            getString(R.string.admis_no_asc),
            getString(R.string.admis_no_dsc),
            getString(R.string.rollasc),
            getString(R.string.rolldsc)
        )


//        binding.txtSearchMenu.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                binding.cbSelect.visibility = View.GONE
//            }
//        }

        binding.cbSelect.setOnClickListener {
            if (binding.cbSelect.isChecked) {
                isSpecificStudent.clear()
                studentsList?.forEach {
                    isSpecificStudent.add(it)
                }
                mAdapter.setAllAbsent(true)
            } else {
                isSpecificStudent.clear()
                mAdapter.setAllAbsent(false)
            }
            isCountAttendance()
        }


        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = isStandardName + "-" + isSectionName
        binding.toolbarLayout.lblSchoolName.text =
            isStaffDetails!!.school_name

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
                    binding.cbSelect.visibility = View.VISIBLE
                    binding.rytSend.visibility = View.VISIBLE
                    studentsList = response.data
                    loadStudentAbsenteesList(studentsList!!)

                } else {
                    binding.lnrHeader.visibility = View.GONE
                    binding.recycleStudents.visibility = View.GONE
                    binding.rlaSortSearch.visibility = View.GONE
                    binding.cbSelect.visibility = View.GONE
                    binding.rytSend.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.recycleStudents.visibility = View.GONE
                binding.rlaSortSearch.visibility = View.GONE
                binding.cbSelect.visibility = View.GONE
                binding.rytSend.visibility = View.GONE
                ErrorMessage(getString(R.string.no_student_found))
            }
        }
        setupFilterCaterotyType(filterCaterotyType)

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
                binding.cbSelect.visibility =
                    if (!s.isNullOrEmpty()) View.GONE else View.VISIBLE

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
                    handleSpinnerSelection(position, adapter, filterCaterotyType)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        // Preselect first item manually
        adapter.selectedPosition = 0
        binding.isSpinnerSort.setSelection(0)
        adapter.notifyDataSetChanged()
        handleSpinnerSelection(0, adapter, filterCaterotyType)
    }

    private fun sortData(sortType: SortType) {
        val sortedList = when (sortType) {
            SortType.NO_ASC -> studentsList!!.sortedBy { it.admission_no }
            SortType.NO_DESC -> studentsList!!.sortedByDescending { it.admission_no }
            SortType.NAME_ASC -> studentsList!!.sortedBy { it.name }
            SortType.NAME_DESC -> studentsList!!.sortedByDescending { it.name }
            SortType.REG_ASC -> studentsList!!.sortedBy { it.roll_no }
            SortType.REG_DSC -> studentsList!!.sortedByDescending { it.roll_no }
        }

        mAdapter.updateData(sortedList)
    }

    private fun handleSpinnerSelection(
        position: Int,
        adapter: SpinnerLoadingAdapter,
        filterCaterotyType: List<String>
    ) {
        if (adapter.selectedPosition != position) {
            binding.txtSearchMenu.text.clear()
            adapter.selectedPosition = position
            adapter.notifyDataSetChanged()

            filterSelectedOption = filterCaterotyType[position]

            val sortType = when (filterSelectedOption) {
                getString(R.string.admis_no_asc) -> SortType.NO_ASC
                getString(R.string.admis_no_dsc) -> SortType.NO_DESC
                getString(R.string.nameasc) -> SortType.NAME_ASC
                getString(R.string.namedsc) -> SortType.NAME_DESC
                getString(R.string.rollasc) -> SortType.REG_ASC
                getString(R.string.rolldsc) -> SortType.REG_DSC
                else -> null
            }

            sortType?.let { sortData(it) }
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
            ErrorMessage(getString(R.string.no_student_found))
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

        mAdapter = AbsenteesMarkAdapter(null, this, Constant.isShimmerViewShow, this, this)
        binding.recycleStudents.layoutManager = LinearLayoutManager(this)
        binding.recycleStudents.adapter = mAdapter

        appViewModel!!.isGetStudentList(
            isAccessToken!!,
            isSectionId!!.toString(), isAcademicYearId!!, this
        )

    }

    fun loadStudentAbsenteesList(studentsList: List<NameAndIds>) {
        isCountAttendance()
        mAdapter =
            AbsenteesMarkAdapter(
                studentsList, this, Constant.isShimmerViewDisable, this, this
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

//    override fun onBackPressed() {
//        val searchText = binding.txtSearchMenu.text.toString().trim()
//        if (binding.txtSearchMenu.hasFocus()) {
//            binding.txtSearchMenu.clearFocus()
//
//            // Hide keyboard
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)
//
//            // Show checkbox only if search is empty
//            if (searchText.isEmpty()) {
//                binding.cbSelect.visibility = View.VISIBLE
//            }
//        } else {
//            super.onBackPressed()
//        }
//    }

    override fun onSelectionChanged(selectedIds: List<String>) {
        Log.d("ActivitySelectedIDs", selectedIds.toString())
        isSelectedIds = selectedIds
    }

    override fun onIdCheck(data: NameAndIds) {
        if (!isSpecificStudent.any { it.id == data.id }) {
            isSpecificStudent.add(data)
        }
        binding.cbSelect.isChecked = isSpecificStudent.size == studentsList?.size
        isCountAttendance()
    }
    fun isCountAttendance(){
        var presentCount= studentsList?.size?.minus(isSpecificStudent.size)
        binding.toolbarLayout.tvPresentCount.text=presentCount.toString()
        binding.toolbarLayout.tvAbsentCount.text=isSpecificStudent.size.toString()
    }

    override fun onIdUnchecked(data: NameAndIds) {
        isSpecificStudent.removeAll { it.id == data.id }
        binding.cbSelect.isChecked = false
        isCountAttendance()

    }
}