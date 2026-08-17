package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
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
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.unit.dp
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
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.FilterState
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
    private val TAG_SORT = "GENDER_SORT_DEBUG"
    private val savedFilters = mutableListOf<FilterState>()

    private var isStaffDetails: StaffDetails? = null
    private val ALL_TYPES = listOf(
        "Student Name",
        "Admission Number",
        "Roll Number",
        "Gender"
    )
    private val MAX_FILTER_COUNT = 4

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

        binding.lblDisclaimerForAI.visibility = if (Constant.isMarkUploadFromAi) View.VISIBLE else View.GONE

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

                val row = tableData.records.firstOrNull {
                    it[Constant.Student_ID]?.toString()?.trim() == student.student_id
                } ?: return@map student

                val updatedMarks = student.marks.map { subject ->

                    val updatedActivities = subject.activities.map { activity ->

                        val baseMark = activity.mark?.trim().orEmpty()
                        val selectedKey = normalize(activity.selected_name)

                        val extractedValue = row.entries.firstNotNullOfOrNull { entry ->
                            val header = normalize(entry.key)
                            if (header == selectedKey) {
                                entry.value?.toString()?.trim()
                            } else null
                        }

                        val finalMark = when {
                            isSystemMessage(extractedValue) -> extractedValue!!
                            isValidNumber(baseMark) && isValidNumber(extractedValue) -> if (baseMark != extractedValue) extractedValue!! else baseMark
                            !isValidNumber(baseMark) && isValidNumber(extractedValue) -> extractedValue!!
                            isValidNumber(baseMark) && !isValidNumber(extractedValue) -> baseMark
                            else -> baseMark
                        }

                        // Also merge rubric marks if present
                        val updatedRubrics = activity.rubrics?.map { rubric ->
                            val rubricKey = normalize(rubric.selected_name)
                            val rubricExtracted = row.entries.firstNotNullOfOrNull { entry ->
                                val header = normalize(entry.key)
                                if (header == rubricKey) {
                                    entry.value?.toString()?.trim()
                                } else null
                            }
                            val rubricFinal = when {
                                isSystemMessage(rubricExtracted) -> rubricExtracted!!
                                isValidNumber(rubric.mark) && isValidNumber(rubricExtracted) -> if (rubric.mark != rubricExtracted) rubricExtracted!! else rubric.mark
                                !isValidNumber(rubric.mark) && isValidNumber(rubricExtracted) -> rubricExtracted!!
                                isValidNumber(rubric.mark) && !isValidNumber(rubricExtracted) -> rubric.mark
                                else -> rubric.mark
                            }
                            rubric.copy(mark = rubricFinal)
                        }

                        activity.copy(mark = finalMark, rubrics = updatedRubrics)
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

                if (!activity.rubrics.isNullOrEmpty()) {
                    activity.rubrics.forEach { rubric ->
                        columns.add(
                            MarkColumn(
                                subjectId = subject.subject_id,
                                subjectName = subject.subject_name,
                                activityId = activity.id,
                                activityName = activity.name,
                                selected_name = rubric.selected_name, // Use rubric's selected_name
                                maxMark = rubric.max_mark.toIntOrNull() ?: 0,
                                isRubric = true,
                                rubricId = rubric.id,
                                rubricName = rubric.name?.trim(),
                                parentActivityName = activity.selected_name?.trim() // Activity's selected_name
                            )
                        )
                    }
                } else {
                    // No rubrics - single column for activity itself
                    columns.add(
                        MarkColumn(
                            subjectId = subject.subject_id,
                            subjectName = subject.subject_name,
                            activityId = activity.id,
                            activityName = activity.name,
                            selected_name = activity.selected_name,
                            maxMark = activity.max_mark.toIntOrNull() ?: 0,
                            isRubric = false
                        )
                    )
                }
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
            val marks = MutableList<Double?>(columns.size) { null }
            val isEditList = MutableList(columns.size) { true }

            apiStudent.marks.orEmpty().forEach { subject ->
                subject.activities.orEmpty().forEach { activity ->

                    if (!activity.rubrics.isNullOrEmpty()) {
                        activity.rubrics.forEach { rubric ->
                            val index = columns.indexOfFirst {
                                it.subjectId == subject.subject_id &&
                                        it.activityId == activity.id &&
                                        it.isRubric &&
                                        it.rubricId == rubric.id  // CRITICAL: Match by rubric ID
                            }

                            if (index != -1) {
                                markTexts[index] = rubric.mark ?: ""
                                marks[index] = rubric.mark?.toDoubleOrNull()
                                isEditList[index] = rubric.is_edit
                            }
                        }
                    } else {
                        val index = columns.indexOfFirst {
                            it.subjectId == subject.subject_id &&
                                    !it.isRubric &&
                                    it.activityId == activity.id  // Match by activity ID
                        }
                        if (index != -1) {
                            markTexts[index] = activity.mark ?: ""
                            marks[index] = activity.mark?.toDoubleOrNull()
                            isEditList[index] = activity.is_edit
                        }
                    }
                }
            }

            val baseStudent = baseStudents.firstOrNull {
                it.student_id == apiStudent.student_id
            }

            baseStudent?.marks.orEmpty().forEach { subject ->
                subject.activities.orEmpty().forEach { activity ->

                    if (!activity.rubrics.isNullOrEmpty()) {
                        activity.rubrics.forEach { rubric ->
                            val index = columns.indexOfFirst {
                                it.subjectId == subject.subject_id &&
                                        it.activityId == activity.id &&
                                        it.isRubric &&
                                        it.rubricId == rubric.id
                            }
                            if (index != -1) {
                                mockTexts[index] = rubric.mark ?: ""
                            }
                        }
                    } else {
                        val index = columns.indexOfFirst {
                            it.subjectId == subject.subject_id &&
                                    !it.isRubric &&
                                    it.activityId == activity.id
                        }
                        if (index != -1) {
                            mockTexts[index] = activity.mark ?: ""
                        }
                    }
                }
            }

            StudentMarkList(
                name = apiStudent.student_name,
                student_id = apiStudent.student_id,
                gender = apiStudent.gender,
                rollNo = apiStudent.roll_no,
                admission_no = apiStudent.admission_no,
                marks = marks,
                markTexts = markTexts,
                mockMarkTexts = mockTexts,
                isEditList = isEditList
            )
        }.toMutableList()
    }

    private fun setupHeader(columns: List<MarkColumn>) {
        val container = findViewById<LinearLayout>(R.id.headerSubjectContainer)
        val headerScroll = findViewById<HorizontalScrollView>(R.id.headerScroll)
        container.removeAllViews()

        fun dpToPx(dp: Int): Int = (dp * Resources.getSystem().displayMetrics.density).toInt()

        val px2 = dpToPx(2)
        val px3 = dpToPx(3)
        val px8 = dpToPx(8)
        val px12 = dpToPx(12)

        val subjectRowHeight = dpToPx(32)
        val activityRowHeight = dpToPx(24)
        val rubricRowHeight = dpToPx(48)
        val totalHeaderHeight = subjectRowHeight + activityRowHeight + rubricRowHeight


        val dividerColor = Color.parseColor("#BBBBBB")

        val subjectGroups = columns.groupBy { it.subjectId }

        subjectGroups.forEach { (subjectId, subjectColumns) ->

            val subjectContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    totalHeaderHeight
                )
            }

            subjectContainer.addView(TextView(this).apply {
                text = subjectColumns.firstOrNull()?.subjectName ?: ""
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                textSize = 14f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    subjectRowHeight
                )
            })

            val activitiesRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    activityRowHeight
                )
            }

            val activityGroups = subjectColumns.groupBy { it.activityId }

            activityGroups.forEach { (activityId, activityCols) ->
                val activityName = activityCols.firstOrNull()?.parentActivityName?.trim()
                    ?: activityCols.firstOrNull()?.activityName?.trim()
                    ?: ""

                val rubricCount = activityCols.count { it.isRubric }
                val activityWidth = if (rubricCount > 0) {
                    (rubricCount * SUBJECT_CELL_WIDTH) + ((rubricCount - 1) * px8)
                } else {
                    SUBJECT_CELL_WIDTH
                }

                activitiesRow.addView(TextView(this).apply {
                    text = activityName
                    gravity = Gravity.CENTER
                    textSize = 11f
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        activityWidth,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                })

                if (activityId != activityGroups.keys.lastOrNull()) {
                    activitiesRow.addView(View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(px2, LinearLayout.LayoutParams.MATCH_PARENT)
                        setBackgroundColor(dividerColor)
                    })
                    activitiesRow.addView(View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(px8, LinearLayout.LayoutParams.MATCH_PARENT)
                    })
                }
            }

            subjectContainer.addView(activitiesRow)

            val rubricsRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    rubricRowHeight
                )
            }

            activityGroups.forEach { (activityId, activityCols) ->
                val rubricCols = activityCols.filter { it.isRubric }

                if (rubricCols.isNotEmpty()) {
                    rubricCols.forEachIndexed { index, col ->
                        val rubricLayout = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            gravity = Gravity.CENTER
                            layoutParams = LinearLayout.LayoutParams(
                                SUBJECT_CELL_WIDTH,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                        }

                        rubricLayout.addView(TextView(this).apply {
                            text = col.selected_name?.trim() ?: col.rubricName?.trim() ?: ""
                            gravity = Gravity.CENTER
                            textSize = 11f
                            maxLines = 2
                            setTextColor(Color.BLACK)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.2f
                            )
                        })

                        rubricLayout.addView(TextView(this).apply {
                            text = "Max: ${col.maxMark}"
                            gravity = Gravity.CENTER
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                            setTextColor(Color.GRAY)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.8f
                            )
                        })

                        rubricsRow.addView(rubricLayout)


                        if (index != rubricCols.lastIndex) {
                            rubricsRow.addView(View(this).apply {
                                layoutParams = LinearLayout.LayoutParams(px2, LinearLayout.LayoutParams.MATCH_PARENT)
                                setBackgroundColor(dividerColor)
                            })
                            rubricsRow.addView(View(this).apply {
                                layoutParams = LinearLayout.LayoutParams(px8, LinearLayout.LayoutParams.MATCH_PARENT)
                            })
                        }
                    }
                } else {
                    activityCols.forEach { col ->
                        val layout = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            gravity = Gravity.CENTER
                            layoutParams = LinearLayout.LayoutParams(
                                SUBJECT_CELL_WIDTH,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                        }

                        layout.addView(TextView(this).apply {
                            text = col.selected_name?.trim() ?: col.activityName
                            gravity = Gravity.CENTER
                            textSize = 11f
                            maxLines = 2
                            setTextColor(Color.BLACK)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.2f
                            )
                        })

                        layout.addView(TextView(this).apply {
                            text = "Max: ${col.maxMark}"
                            gravity = Gravity.CENTER
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                            setTextColor(Color.GRAY)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.8f
                            )
                        })

                        rubricsRow.addView(layout)
                    }
                }

                if (activityId != activityGroups.keys.lastOrNull()) {
                    rubricsRow.addView(View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(px2, LinearLayout.LayoutParams.MATCH_PARENT)
                        setBackgroundColor(dividerColor)
                    })
                    rubricsRow.addView(View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(px8, LinearLayout.LayoutParams.MATCH_PARENT)
                    })
                }
            }

            subjectContainer.addView(rubricsRow)
            container.addView(subjectContainer)

            if (subjectId != subjectGroups.keys.lastOrNull()) {
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(px3, LinearLayout.LayoutParams.MATCH_PARENT)
                    setBackgroundColor(Color.parseColor("#999999"))
                })
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(px12, LinearLayout.LayoutParams.MATCH_PARENT)
                })
            }
        }

        HorizontalScrollSync.bind(headerScroll)
    }

    private fun isGetMarkDetails() {
        Constant.showLoading(this)

        Log.d("FINAL_LIST_INPUT", "size=${isFinalMapDetails?.size ?: 0}")
        isFinalMapDetails?.forEach { sub ->
            Log.d("FINAL_LIST_INPUT",
                "subject=${sub.subject} | papers=${sub.paper.map { "${it.name}(rubrics=${it.rubrics.size})" }}")
        }

        val json = JsonObject().apply {
            addProperty(Constant.class_id, isFinalMapDetails!![0].class_id)
            addProperty(Constant.section_id, isFinalMapDetails!![0].section_id)
            addProperty(Constant.exam_id, Constant.isMarkUploadExamListDataDetails!!.id)
            addProperty(APIKeyNames.academic_year_id, Constant.isUploadMarksSelectedAcademicID)
        }

        val selectedActivitiesArray = JsonArray()

        for (subject in isFinalMapDetails!!) {

            val subjectObj = JsonObject().apply {
                addProperty(Constant.subject_id, subject.subject_id)
            }

            val activitiesArray = JsonArray()

            for (paper in subject.paper) {
                val hasRubrics = paper.rubrics.isNotEmpty()

                val shouldInclude = when {
                    Constant.isMarkUploadFromAi -> {
                        if (hasRubrics) {
                            paper.rubrics.any { !it.selectedRubricesValue.isNullOrEmpty() }
                        } else {
                            !paper.selectedValue.isNullOrEmpty()
                        }
                    }
                    else -> {
                        if (hasRubrics) {
                            paper.rubrics.any { it.isSelected }
                        } else {
                            paper.selectedActivityID == paper.activity_id
                        }
                    }
                }

                Log.d("JSON_BUILDER",
                    "subject=${subject.subject} paper=${paper.name} " +
                            "isAi=${Constant.isMarkUploadFromAi} hasRubrics=$hasRubrics " +
                            "shouldInclude=$shouldInclude")

                if (!shouldInclude) continue

                val activityId = (paper.activity_id?.toString()
                    ?: paper.selectedActivityID?.toString()).orEmpty()

                val selectedName = when {
                    Constant.isMarkUploadFromAi && hasRubrics -> paper.name
                    Constant.isMarkUploadFromAi -> paper.selectedValue
                    else -> paper.name
                }

                Log.d("JSON_BUILDER", "  → activityId=$activityId selectedName=$selectedName")

                if (activityId.isEmpty() || selectedName.isNullOrEmpty()) continue

                val activityObj = JsonObject().apply {
                    addProperty(APIKeyNames.id, activityId)
                    addProperty(APIKeyNames.selected_name, selectedName)

                    if (hasRubrics) {
                        val rubricsArray = JsonArray()

                        val selectedRubrics = if (Constant.isMarkUploadFromAi) {
                            paper.rubrics.filter { !it.selectedRubricesValue.isNullOrEmpty() }
                        } else {
                            paper.rubrics.filter { it.isSelected }
                        }

                        selectedRubrics.forEach { rubric ->
                            val rubricSelectedName = if (Constant.isMarkUploadFromAi) {
                                rubric.selectedRubricesValue?.trim()
                            } else {
                                rubric.rubric_name?.trim()
                            }

                            if (!rubricSelectedName.isNullOrEmpty()) {
                                val rubricObj = JsonObject().apply {
                                    addProperty("id", rubric.rubric_id?.toString())
                                    addProperty("selected_name", rubricSelectedName)
                                }
                                rubricsArray.add(rubricObj)
                                Log.d("JSON_BUILDER",
                                    "    → rubric=${rubric.rubric_name} name=$rubricSelectedName")
                            }
                        }

                        if (rubricsArray.size() > 0) {
                            add("rubrics", rubricsArray)
                        }
                    }
                }

                activitiesArray.add(activityObj)
            }

            if (activitiesArray.size() > 0) {
                subjectObj.add(Constant.activities, activitiesArray)
                selectedActivitiesArray.add(subjectObj)
                Log.d("JSON_BUILDER",
                    "  → subject ${subject.subject} ADDED with ${activitiesArray.size()} activities")
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

        dialog.setCanceledOnTouchOutside(false)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val container = dialog.findViewById<LinearLayout>(R.id.lytFilterContainer)
        val btnApply = dialog.findViewById<Button>(R.id.btnApply)
        val btnClear = dialog.findViewById<Button>(R.id.btnClear)
        val imgClose = dialog.findViewById<ImageView>(R.id.imgCloseFilter)

        container.removeAllViews()

        val onSelectionChanged: () -> Unit = {
            updateApplyButtonState(container, btnApply)
        }

        if (savedFilters.isEmpty()) {
            addFilterRow(container, onSelectionChanged = onSelectionChanged)
        } else {
            savedFilters.forEach {
                addFilterRow(container, it, onSelectionChanged)
            }
        }

        updateApplyButtonState(container, btnApply)

        imgClose?.setOnClickListener {
            dialog.dismiss()
        }

        btnApply.setOnClickListener {

            savedFilters.clear()

            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i)
                val spnType = row.findViewById<Spinner>(R.id.spnType)
                val spnValue = row.findViewById<Spinner>(R.id.spnValue)

                val type = spnType.selectedItem?.toString() ?: "Select Type"

                if (type != "Select Type") {
                    val value = spnValue.selectedItem?.toString() ?: "Ascending"
                    savedFilters.add(FilterState(type, value))
                }
            }

            if (savedFilters.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please select at least one filter type",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            applySortUsingSavedFilters()

            dialog.dismiss()
        }

        btnClear.setOnClickListener {

            savedFilters.clear()

            container.removeAllViews()
            addFilterRow(container, onSelectionChanged = onSelectionChanged)
            updateApplyButtonState(container, btnApply)

            currentStudentsList.clear()
            currentStudentsList.addAll(originalStudentsList)
            binding.rvMarks.adapter?.notifyDataSetChanged()

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateApplyButtonState(container: LinearLayout, btnApply: Button) {
        var hasValidType = false

        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            val spnType = row.findViewById<Spinner>(R.id.spnType)
            val type = spnType.selectedItem?.toString()
            if (!type.isNullOrBlank() && type != "Select Type") {
                hasValidType = true
                break
            }
        }

        btnApply.isEnabled = hasValidType
        btnApply.alpha = if (hasValidType) 1f else 0.5f
    }

    private fun applySortUsingSavedFilters() {

        val list = originalStudentsList.toMutableList()

        Log.d(TAG_SORT, "----- APPLY SORT START -----")

        savedFilters.forEachIndexed { index, filter ->
            Log.d(
                TAG_SORT,
                "Filter[$index] type=${filter.type}, value=${filter.value}"
            )
        }

        val orderedFilters = mutableListOf<FilterState>()

        savedFilters.firstOrNull { it.type == "Gender" }?.let {
            orderedFilters.add(it)
            Log.d(TAG_SORT, "Gender filter moved to FIRST priority")
        }

        orderedFilters.addAll(savedFilters.filter { it.type != "Gender" })

        orderedFilters.forEachIndexed { index, filter ->
            Log.d(
                TAG_SORT,
                "Priority[$index] => ${filter.type} (${filter.value})"
            )
        }

        list.sortWith(Comparator { a, b ->

            for (filter in orderedFilters) {

                val result = when (filter.type) {

                    "Gender" -> {
                        val asc = filter.value == "Ascending"

                        val wA = genderWeight(a.gender, asc)
                        val wB = genderWeight(b.gender, asc)

                        Log.d(
                            TAG_SORT,
                            "COMPARE Gender | ${a.name}:${a.gender}($wA) vs ${b.name}:${b.gender}($wB)"
                        )

                        wA.compareTo(wB)
                    }

                    "Student Name" -> {
                        val res = a.name.lowercase()
                            .compareTo(b.name.lowercase())

                        Log.d(
                            TAG_SORT,
                            "COMPARE Name | ${a.name} vs ${b.name} = $res"
                        )

                        if (filter.value == "Ascending") res else -res
                    }

                    "Roll Number" -> {
                        val r1 = a.rollNo ?: ""
                        val r2 = b.rollNo ?: ""
                        val res = r1.compareTo(r2)

                        Log.d(
                            TAG_SORT,
                            "COMPARE Roll | $r1 vs $r2 = $res"
                        )

                        if (filter.value == "Ascending") res else -res
                    }

                    "Admission Number" -> {
                        val a1 = a.admission_no ?: ""
                        val a2 = b.admission_no ?: ""
                        val res = a1.compareTo(a2)

                        Log.d(
                            TAG_SORT,
                            "COMPARE Admission | $a1 vs $a2 = $res"
                        )

                        if (filter.value == "Ascending") res else -res
                    }

                    else -> 0
                }

                if (result != 0) return@Comparator result
            }

            0
        })

        Log.d(TAG_SORT, "----- SORT RESULT ORDER -----")
        list.forEachIndexed { index, s ->
            Log.d(
                TAG_SORT,
                "$index -> ${s.name} | gender=${s.gender}"
            )
        }

        currentStudentsList.clear()
        currentStudentsList.addAll(list)
        binding.rvMarks.adapter?.notifyDataSetChanged()

        Log.d(TAG_SORT, "----- APPLY SORT END -----")
    }

    private fun genderWeight(gender: String?, asc: Boolean): Int {
        val g = gender?.lowercase()?.trim()

        return if (asc) {
            when (g) {
                "female", "f" -> 0
                "male", "m" -> 1
                else -> 2
            }
        } else {
            when (g) {
                "male", "m" -> 0
                "female", "f" -> 1
                else -> 2
            }
        }
    }

    private fun addFilterRow(
        container: LinearLayout,
        state: FilterState? = null,
        onSelectionChanged: (() -> Unit)? = null
    ) {
        val row = layoutInflater.inflate(R.layout.item_filter_row, container, false)

        val spnType = row.findViewById<Spinner>(R.id.spnType)
        val spnValue = row.findViewById<Spinner>(R.id.spnValue)
        val imgAdd = row.findViewById<ImageView>(R.id.imgAddFilter)
        val lytValueSpinner = row.findViewById<RelativeLayout>(R.id.lytValueSpinner)

        val usedTypes = getSelectedTypes(container)

        val typeList = mutableListOf("Select Type")
        typeList.addAll(ALL_TYPES.filter { it !in usedTypes })

        spnType.adapter = spinnerAdapter(typeList)

        fun loadValueSpinner(restore: String? = null) {
            val values = listOf("Ascending", "Descending")
            spnValue.adapter = spinnerAdapter(values)

            restore?.let {
                val idx = values.indexOf(it)
                if (idx >= 0) spnValue.setSelection(idx)
            }
        }

        state?.let {
            val index = typeList.indexOf(it.type)
            if (index >= 0) {
                spnType.setSelection(index)
                loadValueSpinner(it.value)
            }
        }

        spnType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedType = parent.getItemAtPosition(position).toString()

                if (selectedType != "Select Type") {
                    lytValueSpinner.visibility = View.VISIBLE
                    loadValueSpinner()
                } else {
                    lytValueSpinner.visibility = View.GONE
                }

                onSelectionChanged?.invoke()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        imgAdd.setOnClickListener {

            val type = spnType.selectedItem?.toString() ?: "Select Type"
            val value = spnValue.selectedItem?.toString() ?: ""

            if (type == "Select Type" || value.isBlank()) {
                Toast.makeText(
                    this,
                    "Please select type and order",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (container.childCount >= MAX_FILTER_COUNT) {
                Toast.makeText(
                    this,
                    "Maximum $MAX_FILTER_COUNT filters allowed",
                    Toast.LENGTH_SHORT
                ).show()
                imgAdd.visibility = View.GONE
                return@setOnClickListener
            }

            imgAdd.visibility = View.GONE
            addFilterRow(container, onSelectionChanged = onSelectionChanged)
            onSelectionChanged?.invoke()
        }

        if (container.childCount > 0) {
            container.getChildAt(container.childCount - 1)
                .findViewById<ImageView>(R.id.imgAddFilter)
                .visibility = View.GONE
        }

        container.addView(row)
    }

    private fun getSelectedTypes(container: LinearLayout): Set<String> {
        val selected = mutableSetOf<String>()

        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            val spnType = row.findViewById<Spinner>(R.id.spnType)
            val type = spnType.selectedItem?.toString()
            if (!type.isNullOrBlank() && type != "Select Type") {
                selected.add(type)
            }
        }
        return selected
    }

    private fun spinnerAdapter(list: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            list
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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

                // Skip review-flag warning for AB / NA
                if (trimmed.isNotEmpty() && trimmed == oldValue &&
                    reviewFlagMap.containsKey(reviewKey) &&
                    !isSpecialText(trimmed)
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

                // Skip invalid-value issue for AB / NA
                if (trimmed.isNotEmpty() && value == null && !isSpecialText(trimmed)) {
                    summary.total++
                    summary.invalidCount++
                    summary.details.add(
                        IssueDetail(subject, selected, trimmed)
                    )
                    return@forEachIndexed
                }

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

    private fun isSpecialText(value: String): Boolean {
        return value.equals("AB", true) || value.equals("NA", true)
    }

    private fun updateIssueLabel() {

        val issueSummary = calculateIssueSummary(currentStudentsList, markColumns)

        if (issueSummary.total == 0) {
            binding.lblIssueFound.visibility = View.GONE
            return
        }

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
            }

            val marksArray = JsonArray()

            val subjectGroups = columns.groupBy { it.subjectId }

            subjectGroups.forEach { (subjectId, subjectColumns) ->

                val subjectObj = JsonObject().apply {
                    addProperty(Constant.subject_id, subjectId)
                }

                val activitiesArray = JsonArray()

                val activityGroups = subjectColumns.groupBy { it.activityId }

                activityGroups.forEach { (activityId, activityColumns) ->

                    val hasRubrics = activityColumns.any { it.isRubric }

                    if (hasRubrics) {
                        val rubricsArray = JsonArray()

                        activityColumns.filter { it.isRubric }.forEach { col ->
                            val index = columns.indexOf(col)
                            val rawText = student.markTexts.getOrNull(index)?.trim().orEmpty()

                            val rubricObj = JsonObject().apply {
                                addProperty("id", col.rubricId)
                                addProperty("mark", rawText)
                                addProperty("max_mark", col.maxMark.toString())
                            }
                            rubricsArray.add(rubricObj)
                        }

                        val activityObj = JsonObject().apply {
                            addProperty(Constant.id, activityId)
                            addProperty(Constant.mark, "")
                            addProperty(Constant.max_mark, activityColumns.firstOrNull()?.maxMark?.toString() ?: "--")
                            add("rubrics", rubricsArray)
                        }
                        activitiesArray.add(activityObj)

                    } else {
                        activityColumns.forEach { col ->
                            val index = columns.indexOf(col)
                            val rawText = student.markTexts.getOrNull(index)?.trim().orEmpty()

                            val activityObj = JsonObject().apply {
                                addProperty(Constant.id, activityId)
                                addProperty(Constant.mark, rawText)
                                addProperty(Constant.max_mark, col.maxMark.toString())
                                add("rubrics", JsonArray())
                            }
                            activitiesArray.add(activityObj)
                        }
                    }
                }

                subjectObj.add(Constant.activities, activitiesArray)
                marksArray.add(subjectObj)
            }

            studentObj.add(Constant.marks, marksArray)
            uploadDetailsArray.add(studentObj)
        }

        return JsonObject().apply {
            addProperty(Constant.exam_id, Constant.isMarkUploadExamListDataDetails!!.id)
            addProperty(Constant.section_id, isFinalMapDetails!![0].section_id)
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

    private fun getInvalidValueIssues(
        students: List<StudentMarkList>,
        columns: List<MarkColumn>
    ): List<InvalidMarkIssue> {

        val issues = mutableListOf<InvalidMarkIssue>()

        students.forEach { student ->

            student.markTexts.forEachIndexed { index, rawText ->

                val text = rawText.trim()

                if (text.isEmpty()) return@forEachIndexed
                if (text.equals("AB", true) || text.equals("NA", true)) return@forEachIndexed

                if (text.toDoubleOrNull() == null) {

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

    private fun getMaxMarkIssues(
        students: List<StudentMarkList>,
        columns: List<MarkColumn>
    ): List<MaxMarkIssue> {

        val issues = mutableListOf<MaxMarkIssue>()

        students.forEach { student ->
            student.markTexts.forEachIndexed { index, rawText ->

                val value = rawText.toDoubleOrNull() ?: return@forEachIndexed
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
}