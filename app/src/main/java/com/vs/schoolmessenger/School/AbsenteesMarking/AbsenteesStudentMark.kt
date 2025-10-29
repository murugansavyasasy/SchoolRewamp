package com.vs.schoolmessenger.School.AbsenteesMarking

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesFinalListAdapter.AbsenteesFinalListAdapter
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingAdapter.AbsenteesMarkAdapter
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails.GetAttendanceStudentList
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails.GetAttendanceStudentListData
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.AttachmentMediaAdapter
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.PickQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.Model.PickFromQuestionBank.GetPickFromQBankData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.QuestionSource
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.AbsenteesStudentMarkingBinding
import me.relex.circleindicator.CircleIndicator2

class AbsenteesStudentMark : BaseActivity<AbsenteesStudentMarkingBinding>(),
    AbsenteesSelectionListener,
    View.OnClickListener {

    lateinit var mAdapter: AbsenteesMarkAdapter
    private var appViewModel: App? = null
//    val isSpecificStudent = mutableListOf<GetAttendanceStudentListData>()
    private var studentsList: List<GetAttendanceStudentListData>? = null
    private var FilterAttendanceList: List<GetAttendanceStudentListData>? = null
    private var originalAttendanceList: List<GetAttendanceStudentListData>? = null
    //    private var isSelectedIds: List<GetAttendanceStudentListData>? = null
    private lateinit var isStandardName: String
    private lateinit var isSectionName: String
    private var AllPresent: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var isAccessToken: String
    var isAcademicYearId = -1
    var isSectionId: String? = null
    var isFullDay: String? = null
    var isHalfDay: String? = null
    var isCurrentAttendanceType: String? = null
    private var filterSelectedOption: String? = null


    override fun getViewBinding(): AbsenteesStudentMarkingBinding {
        return AbsenteesStudentMarkingBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
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

        isHalfDay=Constant.isMarkAttendanceDataSending?.session_type
        isFullDay=Constant.isMarkAttendanceDataSending?.attendance_type

        if (isFullDay==Constant.halfDay){
            isCurrentAttendanceType=isHalfDay
        }
        else{
            isCurrentAttendanceType=isFullDay
        }

        binding.toolbarLayout.imgSearch.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )
        binding.toolbarLayout.imgBack.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )

        binding.toolbarLayout.imgSearch.setOnClickListener {
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



        binding.cbSelect.setOnClickListener {
            mAdapter.setAllAbsent(binding.cbSelect.isChecked,isCurrentAttendanceType.toString())
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
                    Constant.hideLoading(this@AbsenteesStudentMark)
                    Constant.showDataValidation(getString(R.string.fail), response.message, this)
                }
            }
            else {
                Constant.hideLoading(this@AbsenteesStudentMark)
                Constant.showDataValidation(getString(R.string.fail),getString(R.string.something_went_wrong_please_try_again_later), this)
            }
        }


        appViewModel!!.getAttendanceStudentList!!.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rytSearchBar.visibility=View.VISIBLE
                    binding.imgSearch.isEnabled = true
                    binding.lnrHeader.visibility = View.VISIBLE
                    binding.recycleStudents.visibility = View.VISIBLE
                    binding.cbSelect.visibility = View.VISIBLE
                    binding.rytSend.visibility = View.VISIBLE
                    studentsList = response.data
                    FilterAttendanceList=response.data
                    originalAttendanceList=response.data
                    loadStudentAbsenteesList(studentsList!!)

                } else {
                    binding.rytSearchBar.visibility=View.GONE
                    binding.toolbarLayout.imgSearch.visibility=View.GONE
                    binding.lnrHeader.visibility = View.GONE
                    binding.imgSearch.isEnabled = false
                    binding.recycleStudents.visibility = View.GONE
                    binding.rlaSortSearch.visibility = View.GONE
                    binding.cbSelect.visibility = View.GONE
                    binding.rytSend.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.rytSearchBar.visibility=View.GONE
                binding.toolbarLayout.imgSearch.visibility=View.GONE
                binding.imgSearch.isEnabled = false
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
            FilterAttendanceList.orEmpty()
        } else {
            FilterAttendanceList.orEmpty().filter { student ->
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

        mAdapter = AbsenteesMarkAdapter(null,"", this, Constant.isShimmerViewShow, this)
        binding.recycleStudents.layoutManager = LinearLayoutManager(this)
        binding.recycleStudents.adapter = mAdapter

        appViewModel!!.getAttendanceStudentList(
            isAccessToken!!,Constant.isMarkAttendanceDataSending?.class_id.toString(),
            isSectionId!!.toString(),Constant.isMarkAttendanceDataSending?.attendance_date!!
        )

    }

    fun loadStudentAbsenteesList(studentsList: List<GetAttendanceStudentListData>) {
        if(studentsList.isNotEmpty()){
            ShowData()
            binding.rytSearchBar.visibility=View.VISIBLE
            isCountAttendance()
            binding.toolbarLayout.imgSearch.visibility=View.VISIBLE
            mAdapter =
                AbsenteesMarkAdapter(
                    studentsList.toMutableList(),isCurrentAttendanceType!!, this, Constant.isShimmerViewDisable, this
                )
            binding.recycleStudents.adapter = mAdapter
        }
        else{
            binding.toolbarLayout.imgSearch.visibility=View.GONE
            binding.recycleStudents.visibility = View.GONE
            binding.rytSearchBar.visibility=View.GONE
            ErrorMessage(getString(R.string.no_student_found))
        }

    }

    fun showResumeListDialog(
        activity: Activity,
        selectedFinalList: List<GetAttendanceStudentListData>,
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val dialogView = LayoutInflater.from(activity).inflate(R.layout.absentees_final_list, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!activity.isFinishing && !activity.isDestroyed) {
            alertDialog.show()
        }

        val lblClose = dialogView.findViewById<TextView>(R.id.lblClose)
        val lblMarkAsAbsent = dialogView.findViewById<TextView>(R.id.lblMarkAsAbsent)
        val rcFinalList = dialogView.findViewById<RecyclerView>(R.id.rcFinalList)
        val noDataFound = dialogView.findViewById<TextView>(R.id.noDataFound)
        val lblAbsenteesListCount = dialogView.findViewById<TextView>(R.id.lblAbsenteesListCount)
        val lytNoDataFound = dialogView.findViewById<LinearLayout>(R.id.lytNoDataFound)
        val lnrTabThreeName = dialogView.findViewById<LinearLayout>(R.id.lnrTabThreeName)
        val lnrTabTwoName = dialogView.findViewById<LinearLayout>(R.id.lnrTabTwoName)
        val lnrTabOneName = dialogView.findViewById<LinearLayout>(R.id.lnrTabOneName)
        val tabThreeName = dialogView.findViewById<TextView>(R.id.tabThreeName)
        val tabTwoName = dialogView.findViewById<TextView>(R.id.tabTwoName)
        val tabOneName = dialogView.findViewById<TextView>(R.id.tabOneName)
        val line3 = dialogView.findViewById<View>(R.id.line3)
        val line2 = dialogView.findViewById<View>(R.id.line2)
        val line1 = dialogView.findViewById<View>(R.id.line1)


        // Lists that persist user changes
        val removedStudents = mutableListOf<GetAttendanceStudentListData>()
        val absenteesList = filterByType(selectedFinalList, "A").toMutableList()
        val lateComerList = filterByType(selectedFinalList, "P~").toMutableList()
        val odList = filterByType(selectedFinalList, "OD").toMutableList()

        var activeTab = 1 // 1 = Absent, 2 = Late, 3 = OD

        fun updateTabUI(tab: Int) {
            val blue = ContextCompat.getColor(activity, R.color.iconBlue)
            val black = ContextCompat.getColor(activity, R.color.black)
            val gray = R.color.athens_gray

            lnrTabOneName.isEnabled = tab != 1
            lnrTabTwoName.isEnabled = tab != 2
            lnrTabThreeName.isEnabled = tab != 3

            tabOneName.setTextColor(if (tab == 1) blue else black)
            tabTwoName.setTextColor(if (tab == 2) blue else black)
            tabThreeName.setTextColor(if (tab == 3) blue else black)

            line1.setBackgroundResource(if (tab == 1) R.color.iconBlue else gray)
            line2.setBackgroundResource(if (tab == 2) R.color.iconBlue else gray)
            line3.setBackgroundResource(if (tab == 3) R.color.iconBlue else gray)
        }
        fun updateAdapter(list: MutableList<GetAttendanceStudentListData>, label: String) {
            val allEmpty = absenteesList.isEmpty() && lateComerList.isEmpty() && odList.isEmpty()

            if (allEmpty) {
                // All tabs are empty
                rcFinalList.visibility = View.GONE
                lytNoDataFound.visibility = View.VISIBLE
                noDataFound.text = getString(R.string.All_students_are_marked_as_present)
                tabOneName.text = "Absent (${absenteesList.size})"
                tabTwoName.text = "Late (${lateComerList.size})"
                tabThreeName.text = "OD (${odList.size})"
                return
            }


            if (list.isEmpty()) {
                rcFinalList.visibility = View.GONE
                lytNoDataFound.visibility = View.VISIBLE
                tabOneName.text = "Absent (${absenteesList.size})"
                tabTwoName.text = "Late (${lateComerList.size})"
                tabThreeName.text = "OD (${odList.size})"
                noDataFound.text = "No $label students found!"
                return
            }

            rcFinalList.visibility = View.VISIBLE
            lytNoDataFound.visibility = View.GONE
            tabOneName.text = "Absent (${absenteesList.size})"
            tabTwoName.text = "Late (${lateComerList.size})"
            tabThreeName.text = "OD (${odList.size})"

            rcFinalList.layoutManager = LinearLayoutManager(activity)
            rcFinalList.adapter = AbsenteesFinalListAdapter(
                list,
                context = activity,
                isLoading = false,
                onRemove = { data ->
                    removedStudents.add(data)
                    when (activeTab) {
                        1 -> tabOneName.text = "Absent (${absenteesList.size})"
                        2 -> tabTwoName.text = "Late (${lateComerList.size})"
                        3 -> tabThreeName.text = "OD (${odList.size})"
                    }

                    // Handle "no data" case
                    if (absenteesList.isEmpty() && activeTab == 1 ||
                        lateComerList.isEmpty() && activeTab == 2 ||
                        odList.isEmpty() && activeTab == 3
                    ) {
                        rcFinalList.visibility = View.GONE
                        lytNoDataFound.visibility = View.VISIBLE
                        noDataFound.text = "No ${when (activeTab) {
                            1 -> "Absent"
                            2 -> "Late"
                            else -> "OD"
                        }} students found!"
                    }

                    // Also re-check after removal if all are now empty
                    if (absenteesList.isEmpty() && lateComerList.isEmpty() && odList.isEmpty()) {
                        rcFinalList.visibility = View.GONE
                        lytNoDataFound.visibility = View.VISIBLE
                        noDataFound.text = getString(R.string.All_students_are_marked_as_present)
                        tabOneName.text = "Absent (${absenteesList.size})"
                        tabTwoName.text = "Late (${lateComerList.size})"
                        tabThreeName.text = "OD (${odList.size})"
                    }

                },
                onListCountChange = { count ->
                    when (activeTab) {
                        1 -> tabOneName.text = "Absent ($count)"
                        2 -> tabTwoName.text = "Late ($count)"
                        3 -> tabThreeName.text = "OD ($count)"
                    }
                }
            )
        }

        // Initial
        updateTabUI(1)
        updateAdapter(absenteesList, "Absent")

        // Tab click listeners
        lnrTabOneName.setOnClickListener {
            activeTab = 1
            updateTabUI(1)
            updateAdapter(absenteesList, "Absent")
        }

        lnrTabTwoName.setOnClickListener {
            activeTab = 2
            updateTabUI(2)
            updateAdapter(lateComerList, "Late")
        }

        lnrTabThreeName.setOnClickListener {
            activeTab = 3
            updateTabUI(3)
            updateAdapter(odList, "OD")
        }




        lblMarkAsAbsent.setOnClickListener {
            alertDialog.dismiss()
            mAdapter.unselectStudents(removedStudents)
            isMarkAttendance()
        }


        lblClose.setOnClickListener {
            alertDialog.dismiss()
            mAdapter.unselectStudents(removedStudents)

        }

    }


    fun filterByType(
        list: List<GetAttendanceStudentListData>,
        type: String // "A", "OD", or "P~"
    ): List<GetAttendanceStudentListData> {
        return list.filter { student ->
            val parts = student.att_status.split("/")
            val status = when (isCurrentAttendanceType) {
                "SH" -> parts.getOrNull(1)?.trim()
                "FH", "F" -> parts.getOrNull(0)?.trim()
                else -> parts.getOrNull(0)?.trim()
            }
            status.equals(type, ignoreCase = true)
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
                //  Check if all are "PRESENT" based on isCurrentAttendanceType

                val allPresent = studentsList?.all { student ->
                    val parts = student.att_status.split("/")
                    val currentStatus = when (isCurrentAttendanceType) {
                        "SH" -> parts.getOrNull(1) ?: "P"  // second half
                        else -> parts.getOrNull(0) ?: "P"  // first half or full day
                    }
                    currentStatus.equals("P", ignoreCase = true)
//                            currentStatus.equals("P~", ignoreCase = true) // allow Latecomer as present
                } == true



                if (!studentsList.isNullOrEmpty()&& !allPresent){
                    Log.d("FinalList",studentsList.toString())
                    showResumeListDialog(this, studentsList!!)
                }
                else {
                    Constant.showSendConfirmationDialog(
                        this,
                        getString(R.string.confirmation),
                        getString(R.string.permission_ok),
                        getString(R.string.Cancel),
                        "",
                        getString(R.string.are_you_sure_want_to_submit_the_attendance)
                    ) { confirmed ->
                        if (confirmed) {
                            isMarkAttendance()
                        }
                    }
                }
            }
        }
    }


    private fun isMarkAttendance() {
        val hasAbsent = studentsList!!.any { student ->
            val attStatus = student.att_status ?: ""
            when (isCurrentAttendanceType) {
                "F" -> attStatus.contains("A", ignoreCase = true)
                "FH" -> attStatus.split("/").firstOrNull()?.equals("A", true) == true
                "SH" -> attStatus.split("/").getOrNull(1)?.equals("A", true) == true
                else -> false
            }
        }

        AllPresent= if (hasAbsent) Constant.fullDay else Constant.allPresent

        Log.d("isSelectedIds",studentsList.toString())
        Log.d("AllPresent",AllPresent.toString())
        if (Constant.isMarkAttendanceDataSending?.class_id != "" && Constant.isMarkAttendanceDataSending?.section_id != ""
            && Constant.isMarkAttendanceDataSending?.attendance_date != null
        ) {
            if (Constant.isMarkAttendanceDataSending?.attendance_type == Constant.fullDay && Constant.isMarkAttendanceDataSending?.session_type == "") {
                isUpdateMarkAtttendance()

            } else if (Constant.isMarkAttendanceDataSending?.attendance_type == Constant.halfDay && Constant.isMarkAttendanceDataSending?.session_type!!.isNotEmpty()) {
                isUpdateMarkAtttendance()
            }
        }
        else{
            Constant.errorAlert(this,getString(R.string.Oops),getString(R.string.something_went_wrong_please_try_again_later))
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
                studentsList?.forEach { student ->

                    val parts = student.att_status.split("/")
                    val status = when (Constant.isMarkAttendanceDataSending?.attendance_type) {
                        "SH" -> parts.getOrNull(1) ?: "" // second half
                        else -> parts.getOrNull(0) ?: "" // first half or full day
                    }

                    val splType = when (status.uppercase()) {
                        "P" -> "PRESENT"
                        "A" -> "ABSENT"
                        "OD" -> "OD"
                        "P~" -> "LATECOMER"
                        else -> "PRESENT"
                    }

                    add(JsonObject().apply {
                        addProperty(APIKeyNames.id, student.id.toString())
                        addProperty(APIKeyNames.spl_attendance_type, splType)
                    })
                }
            }

            add("student_details", studentArray)

            Log.d("AbsenteesStudentID", studentArray.toString())
            Log.d("AttendanceList", studentsList.toString())
        }
        Constant.showLoading(this)
        appViewModel?.isUpdateSendAbsenteeSMS(isAccessToken!!, jsonObject, this)

    }


    override fun onSelectionChanged(selectedIds: List<GetAttendanceStudentListData>) {
        selectedIds.forEach { updated ->
            FilterAttendanceList = FilterAttendanceList?.map { original ->
                if (original.id == updated.id) updated else original
            }
        }

        studentsList = FilterAttendanceList


        isCountAttendance()
        Log.d("ActivitySelectedIDs", studentsList.toString())

        binding.cbSelect.isChecked = studentsList?.all { student ->
            val parts = student.att_status.split("/")
            when (isCurrentAttendanceType) {
                "SH" -> parts.getOrNull(1)?.equals("A", ignoreCase = true) == true
                "FH", "F" -> parts.getOrNull(0)?.equals("A", ignoreCase = true) == true
                else -> false
            }
        } == true

    }



    fun isCountAttendance() {
        val currentList = studentsList ?: return

        var presentCount = 0
        var absentCount = 0
        var odCount = 0
        var lateComerCount = 0

        for (student in currentList) {
            val parts = student.att_status?.split("/") ?: listOf("P", "P")
            val firstHalf = parts.getOrNull(0) ?: "P"
            val secondHalf = parts.getOrNull(1) ?: "P"

            // Decide which half to use based on current attendance type
            val currentStatus = when (isCurrentAttendanceType) {
                "F" -> firstHalf             // Full day → take first half’s status
                "FH" -> firstHalf            // First half → take first half
                "SH" -> secondHalf           // Second half → take second half
                else -> firstHalf
            }

            // Count based on that selected status
            when (currentStatus) {
                "P" -> presentCount++
                "A" -> absentCount++
                "OD" -> odCount++
                "P~" -> presentCount++
            }
        }

        binding.toolbarLayout.tvPresentCount.text =
            if (presentCount > 0) String.format(Constant.time02d, presentCount) else Constant.zero
        binding.toolbarLayout.tvAbsentCount.text =
            if (absentCount > 0) String.format(Constant.time02d, absentCount) else Constant.zero
        binding.toolbarLayout.tvodCount.text =
            if (odCount > 0) String.format(Constant.time02d, odCount) else Constant.zero

//    binding.toolbarLayout.tvLateCount.text =
//        if (lateComerCount > 0) String.format(Constant.time02d, lateComerCount) else Constant.zero
    }





}