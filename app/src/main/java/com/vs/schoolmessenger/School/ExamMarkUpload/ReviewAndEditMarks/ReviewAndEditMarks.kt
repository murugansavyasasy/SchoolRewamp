package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.Interface.OnMarksChangedListener
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivitySubjectNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Adapter.MarksAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.InvalidMarkIssue
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.IssueDetail
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.IssueSummary
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.MarkColumn
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.MarkResponse
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.MaxMarkIssue
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.StudentMarkApi
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.StudentMarkList
import com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model.ParcelTableData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.HorizontalScrollSync
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ReviewAndEditMarksBinding

class ReviewAndEditMarks : BaseActivity<ReviewAndEditMarksBinding>(), View.OnClickListener,
    OnMarksChangedListener {

    override fun getViewBinding() = ReviewAndEditMarksBinding.inflate(layoutInflater)
    private var isFinalMapDetails: List<getActivitySubjectNameData>? = emptyList()
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null
    private var selectedSortId: Int = -1
    private var selectedMaleSubSortId: Int = -1
    private var selectedFemaleSubSortId: Int = -1
    private var searchText: String = ""

    private var isAccessToken: String? = null
    private val SUBJECT_CELL_WIDTH = 200
    private var markColumns: List<MarkColumn> = emptyList()
    private val SUBJECT_CELL_GAP = 40
    private val reviewFlagMap = mutableMapOf<String, String>()
    private var currentStudentsList: MutableList<StudentMarkList> = mutableListOf()
    private var lastIssueUpdateTime = 0L
    var isExamSectionId = ""
    private var originalStudentsList: MutableList<StudentMarkList> = mutableListOf()

    override fun setupViews() {
        super.setupViews()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)
        binding.lytSearch.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        binding.imgFilterStudentList.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        isFinalMapDetails = intent.getParcelableArrayListExtra(
            Constant.FINAL_MAP_ACTIVITY
        ) ?: emptyList()
        Log.d("isFinalMapDetails", isFinalMapDetails.toString())

        isGetMarkDetails()

        appViewModel!!.savemarks?.observe(this) { response ->
            Constant.hideLoading(this@ReviewAndEditMarks)
            if (response != null) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isGetMarkDetails?.observe(this) { response ->

            val baseResponse = response ?: return@observe
            isExamSectionId = baseResponse.data[0].exam_section_id
            val finalResponse = if (Constant.isMarkUploadFromAi) {
                mergeMarksWithExtractedTable(
                    baseResponse, Constant.isExtractedDetails?.firstOrNull()
                )
            } else baseResponse

            reviewFlagMap.clear()

            Constant.isExtractedDetails?.firstOrNull()?.reviewFlags?.forEach { flag ->
                val key = flag.studentId.toString().trim() + "_" + flag.field.trim().lowercase()
                reviewFlagMap[key] = flag.reason
            }

            markColumns = buildHeaderColumns(baseResponse)
            setupHeader(markColumns)
            setupMarksUI(finalResponse, baseResponse)

            if (binding.rvMarks.adapter == null) {
                Constant.hideLoading(this)
                binding.rvMarks.layoutManager = LinearLayoutManager(this)
                originalStudentsList = currentStudentsList.toMutableList()
                binding.rvMarks.adapter = MarksAdapter(
                    currentStudentsList, markColumns, reviewFlagMap, this, this
                )

                (binding.rvMarks.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations =
                    false
            } else {
                binding.rvMarks.adapter?.notifyDataSetChanged()
                updateEmptyState()
            }
        }

        binding.lnrSaveAllMarks2.setOnClickListener {

            val maxIssues = getMaxMarkIssues(currentStudentsList, markColumns)
            if (maxIssues.isNotEmpty()) {

                val message = maxIssues.joinToString("\n") {
                    "• ${it.studentName} → ${it.subjectName} → ${it.activityName} → ${it.selected_name} (${it.enteredMark}/${it.maxMark})"
                }


                Constant.errorAlert1(
                    this,
                    getString(R.string.alert),
                    getString(R.string.max_mark_exceeded_please_correct_the_marks, message)
                )
                return@setOnClickListener
            }

            val invalidIssues = getInvalidValueIssues(currentStudentsList, markColumns)
            if (invalidIssues.isNotEmpty()) {

                val message = invalidIssues.joinToString("\n") {
                    "• ${it.studentName} → ${it.subjectName} → ${it.selectedname} (${it.enteredValue})"
                }

                Constant.errorAlert1(
                    this,
                    getString(R.string.alert),
                    getString(R.string.invalid_mark_values_found_please_correct_them, message)
                )
                return@setOnClickListener
            }

            showSendConfirmationDialog()
        }

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            binding.lytSearch.visibility = View.VISIBLE
        }
        setupSearch()
    }

    private fun toggleSearch(show: Boolean) {
        binding.lytSearch.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.edtSearch.requestFocus()
        } else {
            binding.edtSearch.setText("")
        }
    }

    private fun updateEmptyState() {
        if (currentStudentsList.isEmpty()) {
            binding.lytEmptyState.visibility = View.VISIBLE
            binding.rvMarks.visibility = View.GONE
        } else {
            binding.lytEmptyState.visibility = View.GONE
            binding.rvMarks.visibility = View.VISIBLE
        }
    }

    private fun setupSearch() {

        binding.run {
            edtSearch.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    val query = s.toString().trim()

                    if (query.isEmpty()) {
                        // Reset list
                        currentStudentsList.clear()
                        currentStudentsList.addAll(originalStudentsList)
                    } else {
                        val filteredList = originalStudentsList.filter { student ->
                            student.name.contains(query, true) || student.rollNo.contains(
                                query,
                                true
                            ) || student.admission_no.contains(query, true)
                        }

                        currentStudentsList.clear()
                        currentStudentsList.addAll(filteredList)
                    }

                    rvMarks.adapter?.notifyDataSetChanged()
                    updateEmptyState()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        binding.edtSearch.setOnEditorActionListener { _, actionId, _ ->
            actionId == EditorInfo.IME_ACTION_DONE
        }
    }


    private fun normalize(value: String?): String {
        return value?.trim()?.lowercase()?.replace("[^a-z0-9]".toRegex(), "") ?: ""
    }

    private fun mergeMarksWithExtractedTable(
        apiResponse: MarkResponse, tableData: ParcelTableData?
    ): MarkResponse {

        // If no Excel / extracted data → return API data
        if (tableData == null || tableData.records.isEmpty()) {
            return apiResponse
        }

        fun normalize(value: String?): String {
            return value?.trim()?.lowercase()?.replace("[^a-z0-9]".toRegex(), "") ?: ""
        }

        fun isSystemMessage(value: String?): Boolean {
            return value?.trim()?.equals("PLEASE MARK PROPERLY", true) == true
        }

        fun isValidNumber(value: String?): Boolean {
            if (value.isNullOrBlank()) return false
            if (value.equals("AB", true)) return true
            return value.toIntOrNull() != null
        }

        val updatedSections = apiResponse.data.map { section ->

            val updatedStudents = section.upload_details.map { student ->

                // Find Excel row by Student ID
                val row = tableData.records.firstOrNull {
                    it[Constant.Student_ID]?.toString()?.trim() == student.student_id
                } ?: return@map student

                val updatedMarks = student.marks.map { subject ->

                    val updatedActivities = subject.activities.map { activity ->

                        val baseMark = activity.mark?.trim().orEmpty()
                        val selectedKey = normalize(activity.selected_name)

                        // Find matching Excel column ONLY by selected_name
                        val extractedValue = row.entries.firstNotNullOfOrNull { entry ->
                            val header = normalize(entry.key)
                            if (header == selectedKey) {
                                entry.value?.toString()?.trim()
                            } else null
                        }

                        // =========================
                        // FINAL DECISION RULE
                        // =========================
                        val finalMark = when {

                            // Excel says: PLEASE MARK PROPERLY
                            isSystemMessage(extractedValue) -> extractedValue!!

                            // Both API & Excel have valid numbers
                            isValidNumber(baseMark) && isValidNumber(extractedValue) -> if (baseMark != extractedValue) extractedValue!! else baseMark

                            // Only Excel has value
                            !isValidNumber(baseMark) && isValidNumber(extractedValue) -> extractedValue!!

                            // Only API has value
                            isValidNumber(baseMark) && !isValidNumber(extractedValue) -> baseMark

                            // Nothing valid
                            else -> baseMark
                        }

                        activity.copy(mark = finalMark)
                    }

                    subject.copy(activities = updatedActivities)
                }

                student.copy(marks = updatedMarks)
            }

            section.copy(upload_details = updatedStudents)
        }

        return apiResponse.copy(data = updatedSections)
    }

    private fun MarkResponse.getAllStudents(): List<StudentMarkApi> {
        return data.flatMap { it.upload_details }
    }

    private fun buildHeaderColumns(response: MarkResponse): List<MarkColumn> {
        val columns = mutableListOf<MarkColumn>()

        val students = response.getAllStudents()
        val firstStudent = students.firstOrNull() ?: return columns

        firstStudent.marks.orEmpty().forEach { subject ->
            subject.activities.orEmpty().forEach { activity ->
                columns.add(
                    MarkColumn(
                        subjectId = subject.subject_id,
                        subjectName = subject.subject_name,
                        activityId = activity.id,
                        activityName = activity.name,
                        selected_name = activity.selected_name,
                        maxMark = activity.max_mark.toIntOrNull() ?: 100
                    )
                )
            }
        }
        return columns
    }

    private fun setupMarksUI(
        finalResponse: MarkResponse, baseResponse: MarkResponse
    ) {

        val columns = buildHeaderColumns(baseResponse)

        val finalStudents = finalResponse.getAllStudents()
        val baseStudents = baseResponse.getAllStudents()

        currentStudentsList = finalStudents.map { apiStudent ->

            val markTexts = MutableList(columns.size) { "" }
            val mockTexts = MutableList(columns.size) { "" }
            val marks = MutableList<Int?>(columns.size) { null }

            apiStudent.marks.orEmpty().forEach { subject ->
                subject.activities.orEmpty().forEach { activity ->
                    val index = columns.indexOfFirst {
                        it.subjectId == subject.subject_id && normalize(it.selected_name) == normalize(
                            activity.selected_name
                        )
                    }


                    if (index != -1) {
                        markTexts[index] = activity.mark
                        marks[index] = activity.mark.toIntOrNull()
                    }
                }
            }

            val baseStudent = baseStudents.firstOrNull {
                it.student_id == apiStudent.student_id
            }

            baseStudent?.marks.orEmpty().forEach { subject ->
                subject.activities.orEmpty().forEach { activity ->
                    val index = columns.indexOfFirst {
                        it.subjectId == subject.subject_id && normalize(it.selected_name) == normalize(
                            activity.selected_name
                        )
                    }

                    if (index != -1) {
                        mockTexts[index] = activity.mark
                    }
                }
            }

            StudentMarkList(
                name = apiStudent.student_name,
                student_id = apiStudent.student_id,
                gender = "Male",
                rollNo = apiStudent.roll_no,
                admission_no = apiStudent.admission_no,
                marks = marks,
                markTexts = markTexts,
                mockMarkTexts = mockTexts
            )
        }.toMutableList()
    }

    private fun setupHeader(columns: List<MarkColumn>) {
        val container = findViewById<LinearLayout>(R.id.headerSubjectContainer)
        val headerScroll = findViewById<HorizontalScrollView>(R.id.headerScroll)
        container.removeAllViews()
        columns.forEachIndexed { index, col ->
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    SUBJECT_CELL_WIDTH, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            layout.addView(TextView(this).apply {
                text = col.subjectName
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            })

            layout.addView(TextView(this).apply {
                text = col.activityName
                gravity = Gravity.CENTER
            })

            layout.addView(TextView(this).apply {
                text = context.getString(R.string.max_mark) + col.maxMark
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(Color.GRAY)
            })

            container.addView(layout)

            if (index != columns.lastIndex) {
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        SUBJECT_CELL_GAP, LinearLayout.LayoutParams.MATCH_PARENT
                    )
                })
            }
        }
        HorizontalScrollSync.bind(headerScroll)
    }

    private fun isGetMarkDetails() {
        Constant.showLoading(this)
        val json = JsonObject().apply {
            addProperty(Constant.class_id, isFinalMapDetails!![0].class_id)
            addProperty(Constant.section_id, isFinalMapDetails!![0].section_id)
            addProperty(Constant.exam_id, Constant.isMarkUploadExamListDataDetails!!.id)
        }

        val selectedActivitiesArray = JsonArray()

        for (subject in isFinalMapDetails!!) {

            val subjectObj = JsonObject().apply {
                addProperty(Constant.subject_id, subject.subject_id)
            }

            val activitiesArray = JsonArray()

            for (paper in subject.paper) {

                val activityId = paper.activity_id ?: paper.selectedActivityID
                val selectedName =
                    if (Constant.isMarkUploadFromAi) paper.selectedValue else paper.name

                if (!activityId.isNullOrEmpty() && !selectedName.isNullOrEmpty()) {

                    val activityObj = JsonObject().apply {
                        addProperty(APIKeyNames.id, activityId.toInt())
                        addProperty(APIKeyNames.selected_name, selectedName)
                    }

                    activitiesArray.add(activityObj)
                }
            }

            if (activitiesArray.size() > 0) {
                subjectObj.add(Constant.activities, activitiesArray)
                selectedActivitiesArray.add(subjectObj)
            }
        }
        json.add(Constant.selected_activities, selectedActivitiesArray)

        Log.d("FINAL_JSON", json.toString())

        appViewModel!!.isMarkDetails(isAccessToken!!, json, this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.imgFilterStudentList -> {
                showFilterDialog()
            }

            R.id.lytSearch -> {
                toggleSearch(true)
            }
        }
    }

    private fun showFilterDialog() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_filter_student)
        dialog.setCancelable(true)

        // 🔹 Transparent window (important)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 🔹 Dim background
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setDimAmount(0.5f)

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val sideMarginDp = 24
        val sideMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sideMarginDp.toFloat(), displayMetrics
        ).toInt()

        dialog.window?.setLayout(
            screenWidth - (sideMarginPx * 2), ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // ================= VIEW REFERENCES =================

        val rgSort = dialog.findViewById<RadioGroup>(R.id.rgSort)

        val rbMale = dialog.findViewById<RadioButton>(R.id.rbMale)
        val rbFemale = dialog.findViewById<RadioButton>(R.id.rbFemale)

        val rgMaleSubSort = dialog.findViewById<RadioGroup>(R.id.rgMaleSubSort)
        val rgFemaleSubSort = dialog.findViewById<RadioGroup>(R.id.rgFemaleSubSort)

        val etSearch = dialog.findViewById<EditText>(R.id.etSearch)

        val btnApply = dialog.findViewById<Button>(R.id.btnApply)
        val btnClear = dialog.findViewById<Button>(R.id.btnClear)

        // ================= RESTORE STATE =================

        if (selectedSortId != -1) rgSort.check(selectedSortId)
        if (selectedMaleSubSortId != -1) rgMaleSubSort.check(selectedMaleSubSortId)
        if (selectedFemaleSubSortId != -1) rgFemaleSubSort.check(selectedFemaleSubSortId)

        etSearch.setText(searchText)

        rgMaleSubSort.visibility = if (rbMale.isChecked) View.VISIBLE else View.GONE
        rgFemaleSubSort.visibility = if (rbFemale.isChecked) View.VISIBLE else View.GONE

        // ================= SHOW / HIDE SUB SORT =================

        rbMale.setOnCheckedChangeListener { _, checked ->
            rgMaleSubSort.visibility = if (checked) View.VISIBLE else View.GONE
            if (checked) rgFemaleSubSort.visibility = View.GONE
        }

        rbFemale.setOnCheckedChangeListener { _, checked ->
            rgFemaleSubSort.visibility = if (checked) View.VISIBLE else View.GONE
            if (checked) rgMaleSubSort.visibility = View.GONE
        }

        // ================= APPLY =================

        btnApply.setOnClickListener {

            selectedSortId = rgSort.checkedRadioButtonId
            selectedMaleSubSortId = rgMaleSubSort.checkedRadioButtonId
            selectedFemaleSubSortId = rgFemaleSubSort.checkedRadioButtonId
            searchText = etSearch.text.toString().trim()

            var list = originalStudentsList.toList()

            // 🔍 Search
            if (searchText.isNotEmpty()) {
                list = list.filter {
                    it.name.contains(searchText, true) || it.rollNo.contains(
                        searchText,
                        true
                    ) || it.admission_no.contains(searchText, true)
                }
            }

            // 👤 Gender
            if (rbMale.isChecked) {
                list = list.filter { it.gender.equals("Male", true) }
            } else if (rbFemale.isChecked) {
                list = list.filter { it.gender.equals("Female", true) }
            }

            // 🔃 Sort
            list = when {

                selectedSortId == R.id.rbNameAZ -> list.sortedBy { it.name.lowercase() }

                selectedSortId == R.id.rbNameZA -> list.sortedByDescending { it.name.lowercase() }

                selectedSortId == R.id.rbNumberAsc -> list.sortedBy {
                    it.admission_no.toIntOrNull() ?: Int.MAX_VALUE
                }

                selectedSortId == R.id.rbNumberDesc -> list.sortedByDescending {
                    it.admission_no.toIntOrNull() ?: Int.MIN_VALUE
                }

                selectedSortId == R.id.rbRoleNumberAsc -> list.sortedBy {
                    it.rollNo.toIntOrNull() ?: Int.MAX_VALUE
                }

                selectedSortId == R.id.rbRoleNumberDesc -> list.sortedByDescending {
                    it.rollNo.toIntOrNull() ?: Int.MIN_VALUE
                }

                rbMale.isChecked && selectedMaleSubSortId != -1 -> applySubSort(
                    list,
                    selectedMaleSubSortId
                )

                rbFemale.isChecked && selectedFemaleSubSortId != -1 -> applySubSort(
                    list,
                    selectedFemaleSubSortId
                )

                else -> list
            }

            currentStudentsList.clear()
            currentStudentsList.addAll(list)
            binding.rvMarks.adapter?.notifyDataSetChanged()
            updateEmptyState()
            dialog.dismiss()
        }

        // ================= CLEAR =================

        btnClear.setOnClickListener {

            selectedSortId = -1
            selectedMaleSubSortId = -1
            selectedFemaleSubSortId = -1
            searchText = ""

            rgSort.clearCheck()
            rgMaleSubSort.clearCheck()
            rgFemaleSubSort.clearCheck()

            rgMaleSubSort.visibility = View.GONE
            rgFemaleSubSort.visibility = View.GONE

            etSearch.setText("")

            currentStudentsList.clear()
            currentStudentsList.addAll(originalStudentsList)
            binding.rvMarks.adapter?.notifyDataSetChanged()
            updateEmptyState()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun applySubSort(
        list: List<StudentMarkList>, checkedId: Int
    ): List<StudentMarkList> {

        return when (checkedId) {

            R.id.rbMaleAZ, R.id.rbFemaleAZ -> list.sortedBy { it.name.lowercase() }

            R.id.rbMaleZA, R.id.rbFemaleZA -> list.sortedByDescending { it.name.lowercase() }

            R.id.rbMaleAdmAsc, R.id.rbFemaleAdmAsc -> list.sortedBy {
                it.admission_no.toIntOrNull() ?: Int.MAX_VALUE
            }

            R.id.rbMaleAdmDesc, R.id.rbFemaleAdmDesc -> list.sortedByDescending {
                it.admission_no.toIntOrNull() ?: Int.MIN_VALUE
            }

            R.id.rbMaleRollAsc, R.id.rbFemaleRollAsc -> list.sortedBy {
                it.rollNo.toIntOrNull() ?: Int.MAX_VALUE
            }

            R.id.rbMaleRollDesc, R.id.rbFemaleRollDesc -> list.sortedByDescending {
                it.rollNo.toIntOrNull() ?: Int.MIN_VALUE
            }

            else -> list
        }
    }


    override fun onMarksChanged() {
        val now = System.currentTimeMillis()
        if (now - lastIssueUpdateTime > 500) {
            lastIssueUpdateTime = now
            updateIssueLabel()
        }
    }


    private fun calculateIssueSummary(
        students: List<StudentMarkList>, columns: List<MarkColumn>
    ): IssueSummary {

        val summary = IssueSummary()

        students.forEach { student ->
            student.markTexts.forEachIndexed { index, rawText ->

                val column = columns.getOrNull(index) ?: return@forEachIndexed
                val trimmed = rawText.trim()
                val value = trimmed.toIntOrNull()

                val subject = column.subjectName
                val selected = column.selected_name ?: column.activityName

                val oldValue = student.mockMarkTexts.getOrNull(index)?.trim().orEmpty()

                val reviewKey = "${student.student_id}_${
                    column.selected_name?.lowercase()?.replace("[^a-z0-9]".toRegex(), "")
                }"

                /* 🔴 1. REVIEW FLAG (ONLY IF NOT EDITED) */
                if (trimmed.isNotEmpty() && trimmed == oldValue &&          // ⭐ KEY FIX
                    reviewFlagMap.containsKey(reviewKey)
                ) {
                    summary.total++
                    summary.systemMsgCount++
                    summary.details.add(
                        IssueDetail(
                            subject, selected, reviewFlagMap[reviewKey]!!
                        )
                    )
                    return@forEachIndexed
                }

                /* 🔴 2. EXCEL / SYSTEM MESSAGE */
                if (trimmed.isNotEmpty() && value == null && !trimmed.equals("AB", true)) {
                    summary.total++
                    summary.invalidCount++
                    summary.details.add(
                        IssueDetail(subject, selected, trimmed)
                    )
                    return@forEachIndexed
                }

                /* 🔴 3. MAX MARK */
                if (value != null && value > column.maxMark) {
                    summary.total++
                    summary.maxMarkCount++
                    summary.details.add(
                        IssueDetail(
                            subject, selected, "Max mark exceeded (${value}/${column.maxMark})"
                        )
                    )
                }
            }
        }
        return summary
    }

    private fun updateIssueLabel() {

        val issueSummary = calculateIssueSummary(currentStudentsList, markColumns)

        // 🔹 No issues → hide label
        if (issueSummary.total == 0) {
            binding.lblIssueFound.visibility = View.GONE
            return
        }

        // 🔹 Group adapter error messages
        val reasonCountMap = linkedMapOf<String, Int>()

        issueSummary.details.forEach { issue ->
            val reason = issue.reason.trim().lowercase()
            reasonCountMap[reason] = (reasonCountMap[reason] ?: 0) + 1
        }
        val message = buildString {
            append("⚠️ ")
            append(issueSummary.total)
            append(" issue")
            if (issueSummary.total > 1) append("s")
            append(" found : ")

            append(
                reasonCountMap.entries.joinToString(", ") { (reason, count) ->
                    "$count $reason"
                })
        }

        binding.lblIssueFound.visibility = View.VISIBLE
        binding.lblIssueFound.text = message
    }

    private fun isSaveTheMark(
        students: List<StudentMarkList>, columns: List<MarkColumn>
    ): JsonObject {

        val uploadDetailsArray = JsonArray()

        students.forEach { student ->

            val studentObj = JsonObject().apply {
                addProperty(Constant.student_id, student.student_id)
                addProperty(Constant.student_name, student.name)
                addProperty(Constant.roll_no, student.rollNo)
                addProperty(Constant.admission_no, "")
            }

            val marksArray = JsonArray()
            val subjectMap = LinkedHashMap<String, MutableList<Pair<Int, MarkColumn>>>()

            columns.forEachIndexed { index, column ->
                subjectMap.getOrPut(column.subjectName) {
                    mutableListOf()
                }.add(index to column)
            }

            subjectMap.forEach { (subjectName, columnList) ->

                val subjectObj = JsonObject().apply {
                    addProperty(Constant.subject_id, columnList.first().second.subjectId)
                    addProperty(Constant.subject_name, subjectName)
                }

                val activitiesArray = JsonArray()

                columnList.forEach { (index, column) ->

                    val rawText = student.markTexts.getOrNull(index)?.trim().orEmpty()

                    val activityObj = JsonObject().apply {
                        addProperty(Constant.id, column.activityId)
                        addProperty(Constant.name__, column.activityName)
                        addProperty(Constant.mark, rawText)
                        addProperty(Constant.max_mark, column.maxMark.toString())
                    }

                    activitiesArray.add(activityObj)
                }

                subjectObj.add(Constant.activities, activitiesArray)
                marksArray.add(subjectObj)
            }

            studentObj.add(Constant.marks, marksArray)
            uploadDetailsArray.add(studentObj)
        }
        return JsonObject().apply {
            addProperty(
                APIKeyNames.exam_section_id, isExamSectionId
            )
            add(APIKeyNames.upload_details, uploadDetailsArray)
        }
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
        alertMessage.text = ""
        alertMessage.visibility = View.VISIBLE
        lblSelectTarget.text = getString(R.string.are_you_want_to_save_the_marks)
        okButton.setOnClickListener {
            Constant.showLoading(this@ReviewAndEditMarks)
            val saveMarksJsonObject = isSaveTheMark(
                currentStudentsList, markColumns
            )
            Log.d("saveMarksJsonArray", saveMarksJsonObject.toString())
            appViewModel?.savemarks(
                isAccessToken!!, saveMarksJsonObject, this
            )
            alertDialog.dismiss()
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    private fun getMaxMarkIssues(
        students: List<StudentMarkList>, columns: List<MarkColumn>
    ): List<MaxMarkIssue> {
        val issues = mutableListOf<MaxMarkIssue>()
        students.forEach { student ->
            student.markTexts.forEachIndexed { index, rawText ->
                val value = rawText.toIntOrNull() ?: return@forEachIndexed
                val column = columns.getOrNull(index) ?: return@forEachIndexed
                if (value > column.maxMark) {
                    issues.add(
                        MaxMarkIssue(
                            studentName = student.name,
                            subjectName = column.subjectName,
                            activityName = column.activityName,
                            selected_name = column.selected_name,
                            enteredMark = rawText,
                            maxMark = column.maxMark
                        )
                    )
                }
            }
        }
        return issues
    }

    private fun getInvalidValueIssues(
        students: List<StudentMarkList>, columns: List<MarkColumn>
    ): List<InvalidMarkIssue> {

        val issues = mutableListOf<InvalidMarkIssue>()

        students.forEach { student ->
            student.markTexts.forEachIndexed { index, rawText ->

                val text = rawText.trim()
                if (text.isEmpty()) return@forEachIndexed
                if (text.equals("AB", true)) return@forEachIndexed
                if (text.toIntOrNull() == null) {
                    val column = columns.getOrNull(index) ?: return@forEachIndexed
                    issues.add(
                        InvalidMarkIssue(
                            studentName = student.name,
                            subjectName = column.subjectName,
                            selectedname = column.selected_name,
                            enteredValue = text
                        )
                    )
                }
            }
        }
        return issues
    }
}