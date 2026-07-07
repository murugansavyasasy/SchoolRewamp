package com.vs.schoolmessenger.School.ClassTest.UploadMarks

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
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ClassTest.UploadMarks.Adapter.ClassUploadMarksAdapter
import com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model.ActivityDataMarkClassEntry
import com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model.ClassEntryMarkResponse
import com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model.StudentMarkEntryUploadData
import com.vs.schoolmessenger.School.ExamMarkUpload.Interface.OnMarksChangedListener
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.FilterState
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.InvalidMarkIssue
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.IssueDetail
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.IssueSummary
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.MarkColumn
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.MaxMarkIssue
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.StudentMarkList
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.HorizontalScrollSync
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ClassUploadReviewBinding
import java.lang.ref.WeakReference

class ClassUploadMarks : BaseActivity<ClassUploadReviewBinding>(), View.OnClickListener,
    OnMarksChangedListener {

    override fun getViewBinding() = ClassUploadReviewBinding.inflate(layoutInflater)

    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null

    private val TAG_SORT = "GENDER_SORT_DEBUG"
    private val savedFilters = mutableListOf<FilterState>()
    private val ALL_TYPES = listOf(
        "Student Name",
        "Admission Number",
        "Roll Number",
        "Gender"
    )
    private val MAX_FILTER_COUNT = 4

    private val SUBJECT_CELL_WIDTH = 200
    private val SUBJECT_CELL_GAP = 40

    private var markColumns: List<MarkColumn> = emptyList()
    private val reviewFlagMap = mutableMapOf<String, String>() // kept for adapter signature; stays empty now

    private var currentStudentsList: MutableList<StudentMarkList> = mutableListOf()
    private var originalStudentsList: MutableList<StudentMarkList> = mutableListOf()
    private var lastIssueUpdateTime = 0L

    private var classTestId: String = ""
    private var sectionId: String = ""

    private var activeMarkEditText: WeakReference<EditText>? = null
    private lateinit var markSuggestionBar: LinearLayout

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    override fun setupViews() {
        super.setupViews()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isToolBarPrimarySchool(mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground)

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

        classTestId = intent.getStringExtra(Constant.CLASS_TEST_ID).orEmpty()
        sectionId = intent.getStringExtra(Constant.SECTION_ID).orEmpty()

        binding.lblDisclaimerForAI.visibility = View.GONE

        setupMarkSuggestionBar()

        fetchExamDetailsMark()

        appViewModel!!.isexamDetailsMark?.observe(this) { response ->
            Constant.hideLoading(this@ClassUploadMarks)
            val baseResponse: ClassEntryMarkResponse = response ?: return@observe

            baseResponse.data.firstOrNull()?.let { testData ->
                if (testData.classTestId.isNotBlank()) classTestId = testData.classTestId
                if (testData.sectionId.isNotBlank()) sectionId = testData.sectionId
            }

            markColumns = buildHeaderColumns(baseResponse)
            setupHeader(markColumns)
            setupMarksUI(baseResponse)

            if (binding.rvMarks.adapter == null) {
                binding.rvMarks.layoutManager = LinearLayoutManager(this)
                originalStudentsList = currentStudentsList.toMutableList()
                binding.rvMarks.adapter = ClassUploadMarksAdapter(
                    currentStudentsList, markColumns, reviewFlagMap, this, this
                ) { focusedEditText ->
                    onMarkFieldFocusChanged(focusedEditText)
                }
                (binding.rvMarks.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            } else {
                binding.rvMarks.adapter?.notifyDataSetChanged()
                updateEmptyState()
            }
        }

        appViewModel!!.isexamdetailsmarkpost?.observe(this) { response ->
            Constant.hideLoading(this@ClassUploadMarks)
            if (response != null) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        binding.lnrSaveAllMarks2.setOnClickListener {
            val maxIssues = getMaxMarkIssues(currentStudentsList, markColumns)
            if (maxIssues.isNotEmpty()) {
                val message = maxIssues.joinToString("\n") {
                    "• ${it.studentName} → ${it.subjectName} → ${it.activityName} → ${it.selected_name} (${it.enteredMark}/${it.maxMark})"
                }
                Constant.errorAlert1(
                    this, getString(R.string.alert),
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
                    this, getString(R.string.alert),
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

    private fun setupMarkSuggestionBar() {
        val btnAB = TextView(this).apply {
            text = "AB"
            setTextColor(ContextCompat.getColor(this@ClassUploadMarks, R.color.PrimaryColor))
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(32.dp, 12.dp, 32.dp, 12.dp)
            background = ContextCompat.getDrawable(
                this@ClassUploadMarks, R.drawable.bg_ab_suggestion_chip
            )
            setOnClickListener {
                activeMarkEditText?.get()?.let { et ->
                    et.setText("AB")
                    et.setSelection(et.text.length)
                }
            }
        }

        markSuggestionBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(16.dp, 8.dp, 16.dp, 8.dp)
            visibility = View.GONE
            addView(btnAB)
        }

        val rootContent = findViewById<FrameLayout>(android.R.id.content)
        val barParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            leftMargin = 16.dp
            bottomMargin = 4.dp
        }
        rootContent.addView(markSuggestionBar, barParams)

        ViewCompat.setOnApplyWindowInsetsListener(rootContent) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            if (imeVisible && activeMarkEditText?.get() != null) {
                markSuggestionBar.visibility = View.VISIBLE
                markSuggestionBar.translationY = -imeInsets.bottom.toFloat()
            } else {
                markSuggestionBar.visibility = View.GONE
            }
            insets
        }
    }

    private fun onMarkFieldFocusChanged(focusedEditText: EditText?) {
        activeMarkEditText = if (focusedEditText != null) WeakReference(focusedEditText) else null

        val insets = ViewCompat.getRootWindowInsets(binding.rvMarks)
        val imeVisible = insets?.isVisible(WindowInsetsCompat.Type.ime()) == true

        if (imeVisible && focusedEditText != null) {
            markSuggestionBar.visibility = View.VISIBLE
            val imeBottom = insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
            markSuggestionBar.translationY = -imeBottom.toFloat()
        } else {
            markSuggestionBar.visibility = View.GONE
        }
    }


    private fun fetchExamDetailsMark() {
        Constant.showLoading(this)
        appViewModel!!.isexamDetailsMark(isAccessToken!!, classTestId, sectionId, this)
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
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s.toString().trim()

                    if (query.isEmpty()) {
                        currentStudentsList.clear()
                        currentStudentsList.addAll(originalStudentsList)
                    } else {
                        val filteredList = originalStudentsList.filter { student ->
                            student.name.contains(query, true) ||
                                    student.rollNo.contains(query, true) ||
                                    student.admission_no.contains(query, true)
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

    private fun flattenSubjectsAndActivities(
        response: ClassEntryMarkResponse
    ): List<Pair<MarkColumn, ActivityDataMarkClassEntry>> {

        val testData = response.data.firstOrNull() ?: return emptyList()
        val result = mutableListOf<Pair<MarkColumn, ActivityDataMarkClassEntry>>()

        testData.subjects.forEach { subject ->
            subject.activities.forEach { activity ->
                val column = MarkColumn(
                    subjectId = subject.subjectId,
                    subjectName = subject.subjectName,
                    activityId = activity.classTestSubjectId,
                    activityName = activity.activityName,
                    selected_name = activity.activityName,
                    maxMark = activity.maxMark.toIntOrNull() ?: 100
                )
                result.add(column to activity)
            }
        }
        return result
    }

    private fun buildHeaderColumns(response: ClassEntryMarkResponse): List<MarkColumn> {
        return flattenSubjectsAndActivities(response).map { it.first }
    }

    private fun setupMarksUI(response: ClassEntryMarkResponse) {
        val flattened = flattenSubjectsAndActivities(response)
        val columns = flattened.map { it.first }
        val activities = flattened.map { it.second }

        val studentOrder = LinkedHashMap<String, StudentMarkEntryUploadData>()
        activities.forEach { activity ->
            activity.students.forEach { s ->
                studentOrder.putIfAbsent(s.studentId, s)
            }
        }

        currentStudentsList = studentOrder.values.map { baseStudent ->
            val markTexts = MutableList(columns.size) { "" }
            val mockTexts = MutableList(columns.size) { "" }
            val marks = MutableList<Double?>(columns.size) { null }
            val isEditList = MutableList(columns.size) { true }

            activities.forEachIndexed { index, activity ->
                val studentEntry = activity.students.firstOrNull { it.studentId == baseStudent.studentId }
                if (studentEntry != null) {
                    markTexts[index] = studentEntry.mark
                    marks[index] = studentEntry.mark.toDoubleOrNull()
                }
            }

            StudentMarkList(
                name = baseStudent.studentName,
                student_id = baseStudent.studentId,
                gender = "",
                rollNo = baseStudent.rollNo,
                admission_no = baseStudent.admissionNo,
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

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.imgFilterStudentList -> showFilterDialog()
            R.id.lytSearch -> toggleSearch(true)
        }
    }

    private fun showFilterDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_filter_student)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val container = dialog.findViewById<LinearLayout>(R.id.lytFilterContainer)
        val btnApply = dialog.findViewById<Button>(R.id.btnApply)
        val btnClear = dialog.findViewById<Button>(R.id.btnClear)

        container.removeAllViews()

        if (savedFilters.isEmpty()) {
            addFilterRow(container)
        } else {
            savedFilters.forEach {
                addFilterRow(container, it)
            }
        }

        btnApply.setOnClickListener {
            savedFilters.clear()

            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i)
                val type = row.findViewById<Spinner>(R.id.spnType).selectedItem.toString()
                val value = row.findViewById<Spinner>(R.id.spnValue).selectedItem.toString()

                if (type != "Select Type") {
                    savedFilters.add(FilterState(type, value))
                }
            }

            applySortUsingSavedFilters()
            dialog.dismiss()
        }

        btnClear.setOnClickListener {
            savedFilters.clear()

            container.removeAllViews()
            addFilterRow(container)

            currentStudentsList.clear()
            currentStudentsList.addAll(originalStudentsList)
            binding.rvMarks.adapter?.notifyDataSetChanged()

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applySortUsingSavedFilters() {
        val list = originalStudentsList.toMutableList()

        Log.d(TAG_SORT, "----- APPLY SORT START -----")

        savedFilters.forEachIndexed { index, filter ->
            Log.d(TAG_SORT, "Filter[$index] type=${filter.type}, value=${filter.value}")
        }

        val orderedFilters = mutableListOf<FilterState>()

        savedFilters.firstOrNull { it.type == "Gender" }?.let {
            orderedFilters.add(it)
            Log.d(TAG_SORT, "Gender filter moved to FIRST priority")
        }

        orderedFilters.addAll(savedFilters.filter { it.type != "Gender" })

        orderedFilters.forEachIndexed { index, filter ->
            Log.d(TAG_SORT, "Priority[$index] => ${filter.type} (${filter.value})")
        }

        list.sortWith(Comparator { a, b ->
            for (filter in orderedFilters) {
                val result = when (filter.type) {
                    "Gender" -> {
                        val asc = filter.value == "Ascending"
                        val wA = genderWeight(a.gender, asc)
                        val wB = genderWeight(b.gender, asc)
                        Log.d(TAG_SORT, "COMPARE Gender | ${a.name}:${a.gender}($wA) vs ${b.name}:${b.gender}($wB)")
                        wA.compareTo(wB)
                    }

                    "Student Name" -> {
                        val res = a.name.lowercase().compareTo(b.name.lowercase())
                        Log.d(TAG_SORT, "COMPARE Name | ${a.name} vs ${b.name} = $res")
                        if (filter.value == "Ascending") res else -res
                    }

                    "Roll Number" -> {
                        val r1 = a.rollNo ?: ""
                        val r2 = b.rollNo ?: ""
                        val res = r1.compareTo(r2)
                        Log.d(TAG_SORT, "COMPARE Roll | $r1 vs $r2 = $res")
                        if (filter.value == "Ascending") res else -res
                    }

                    "Admission Number" -> {
                        val a1 = a.admission_no ?: ""
                        val a2 = b.admission_no ?: ""
                        val res = a1.compareTo(a2)
                        Log.d(TAG_SORT, "COMPARE Admission | $a1 vs $a2 = $res")
                        if (filter.value == "Ascending") res else -res
                    }

                    else -> 0
                }

                if (result != 0) return@Comparator result
            }
            0
        })

        list.forEachIndexed { index, s ->
            Log.d(TAG_SORT, "$index -> ${s.name} | gender=${s.gender}")
        }

        currentStudentsList.clear()
        currentStudentsList.addAll(list)
        binding.rvMarks.adapter?.notifyDataSetChanged()

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
        state: FilterState? = null
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
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        imgAdd.setOnClickListener {
            val type = spnType.selectedItem?.toString() ?: "Select Type"
            val value = spnValue.selectedItem?.toString() ?: ""

            if (type == "Select Type" || value.isBlank()) {
                Toast.makeText(this, "Please select type and order", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (container.childCount >= MAX_FILTER_COUNT) {
                Toast.makeText(this, "Maximum $MAX_FILTER_COUNT filters allowed", Toast.LENGTH_SHORT).show()
                imgAdd.visibility = View.GONE
                return@setOnClickListener
            }

            imgAdd.visibility = View.GONE
            addFilterRow(container)
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

                if (trimmed.isNotEmpty() && trimmed == oldValue &&
                    reviewFlagMap.containsKey(reviewKey)
                ) {
                    summary.total++
                    summary.systemMsgCount++
                    summary.details.add(IssueDetail(subject, selected, reviewFlagMap[reviewKey]!!))
                    return@forEachIndexed
                }

                if (trimmed.isNotEmpty() && value == null && !trimmed.equals("AB", true)) {
                    summary.total++
                    summary.invalidCount++
                    summary.details.add(IssueDetail(subject, selected, trimmed))
                    return@forEachIndexed
                }

                if (value != null && value > column.maxMark) {
                    summary.total++
                    summary.maxMarkCount++
                    summary.details.add(
                        IssueDetail(subject, selected, "Max mark exceeded (${value}/${column.maxMark})")
                    )
                }
            }
        }
        return summary
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
            append(reasonCountMap.entries.joinToString(", ") { (reason, count) -> "$count $reason" })
        }

        binding.lblIssueFound.visibility = View.VISIBLE
        binding.lblIssueFound.text = message
    }

    private fun buildSaveMarksJson(
        students: List<StudentMarkList>,
        columns: List<MarkColumn>
    ): JsonObject {

        val subjectMap = LinkedHashMap<String, MutableList<Pair<Int, MarkColumn>>>()
        columns.forEachIndexed { index, column ->
            subjectMap.getOrPut(column.subjectId) { mutableListOf() }.add(index to column)
        }

        val subjectsArray = JsonArray()

        subjectMap.forEach { (subjectId, columnList) ->
            val subjectObj = JsonObject().apply {
                addProperty("subject_id", subjectId)
            }

            val activitiesArray = JsonArray()

            columnList.forEach { (index, column) ->
                val activityObj = JsonObject().apply {
                    addProperty("class_test_subject_id", column.activityId)
                }

                val studentsArray = JsonArray()
                students.forEach { student ->
                    val rawMark = student.markTexts.getOrNull(index)?.trim().orEmpty()
                    studentsArray.add(JsonObject().apply {
                        addProperty("student_id", student.student_id)
                        addProperty("attendance", "P")
                        addProperty("mark", rawMark)
                        addProperty("remarks", "")
                    })
                }

                activityObj.add("students", studentsArray)
                activitiesArray.add(activityObj)
            }

            subjectObj.add("activities", activitiesArray)
            subjectsArray.add(subjectObj)
        }

        return JsonObject().apply {
            addProperty("class_test_id", classTestId)
            addProperty("section_id", sectionId)
            add("subjects", subjectsArray)
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
            Constant.showLoading(this@ClassUploadMarks)
            val saveMarksJsonObject = buildSaveMarksJson(currentStudentsList, markColumns)
            Log.d("saveMarksJsonArray", saveMarksJsonObject.toString())

            appViewModel?.isexamdetailsmarkpost(isAccessToken!!, saveMarksJsonObject, this)
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
                if (text.equals("AB", true)) return@forEachIndexed

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