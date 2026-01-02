package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.Interface.OnMarksChangedListener
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivitySubjectNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Adapter.MarksAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.*
import com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model.ParcelTableData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.HorizontalScrollSync
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ReviewAndEditMarksBinding

class ReviewAndEditMarks :
    BaseActivity<ReviewAndEditMarksBinding>(),
    View.OnClickListener, OnMarksChangedListener {

    override fun getViewBinding() =
        ReviewAndEditMarksBinding.inflate(layoutInflater)

    private var isFinalMapDetails: List<getActivitySubjectNameData>? = emptyList()
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null
    private val SUBJECT_CELL_WIDTH = 200
    private var markColumns: List<MarkColumn> = emptyList()
    private val SUBJECT_CELL_GAP = 40
    private val reviewFlagMap = mutableMapOf<String, String>()

    private var currentStudentsList: MutableList<StudentMarkList> = mutableListOf()
    private var lastIssueUpdateTime = 0L


    override fun setupViews() {
        super.setupViews()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        isFinalMapDetails =
            intent.getParcelableArrayListExtra(
                "FINAL_MAP_ACTIVITY"
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

            val finalResponse =
                if (Constant.isMarkUploadFromAi) {
                    mergeMarksWithExtractedTable(
                        baseResponse,
                        Constant.isExtractedDetails?.firstOrNull()
                    )
                } else {
                    baseResponse
                }
            reviewFlagMap.clear()

            Constant.isExtractedDetails
                ?.firstOrNull()
                ?.reviewFlags
                ?.forEach { flag ->

                    val key =
                        flag.studentId.toString().trim() + "_" +
                                flag.field.trim().lowercase()

                    reviewFlagMap[key] = flag.reason
                }
            Log.e(
                "REVIEW_MAP_DEBUG",
                "MAP SIZE = ${reviewFlagMap.size} | MAP = $reviewFlagMap"
            )

            markColumns = buildHeaderColumns(baseResponse)
            setupHeader(markColumns)
            setupMarksUI(finalResponse, baseResponse)

            if (binding.rvMarks.adapter == null) {
                binding.rvMarks.layoutManager =
                    LinearLayoutManager(this@ReviewAndEditMarks)

                binding.rvMarks.adapter = MarksAdapter(
                    currentStudentsList,
                    markColumns,
                    reviewFlagMap,
                    this@ReviewAndEditMarks,
                    this@ReviewAndEditMarks
                )

                (binding.rvMarks.itemAnimator as? SimpleItemAnimator)
                    ?.supportsChangeAnimations = false
            } else {
                binding.rvMarks.adapter?.notifyDataSetChanged()
            }

        }
        binding.lnrSaveAllMarks2.setOnClickListener {
            val maxIssues = getMaxMarkIssues(currentStudentsList, markColumns)
            if (maxIssues.isNotEmpty()) {
                val message = maxIssues.joinToString("\n") {
                    "• ${it.studentName} → ${it.subjectName} (${it.enteredMark}/${it.maxMark})"
                }

                Constant.errorAlert1(
                    this,
                    "Alert",
                    "⚠️ Max mark exceeded:\n$message\n\nPlease correct the marks."
                )
                return@setOnClickListener
            }
            val invalidIssues = getInvalidValueIssues(currentStudentsList, markColumns)
            if (invalidIssues.isNotEmpty()) {
                val message = invalidIssues.joinToString("\n") {
                    "• ${it.studentName} → ${it.subjectName} (${it.enteredValue})"
                }

                Constant.errorAlert1(
                    this,
                    "Alert",
                    "⚠️ Invalid mark values found:\n$message\n\nPlease correct them."
                )
                return@setOnClickListener
            }
            showSendConfirmationDialog()
        }
    }


    private fun mergeMarksWithExtractedTable(
        apiResponse: MarkResponse,
        tableData: ParcelTableData?
    ): MarkResponse {

        if (tableData == null || tableData.records.isEmpty()) {
            return apiResponse
        }

        val updatedStudents = apiResponse.data.map { student ->
            val matchedRow = tableData.records.firstOrNull {
                it["Student ID"]?.toString() == student.student_id
            } ?: return@map student

            val updatedSubjects = student.marks.map { subject ->
                val extractedValue = matchedRow.entries.firstOrNull { entry ->
                    normalize(entry.key) == normalize(subject.subject_name)
                }?.value?.toString()?.trim()

                val updatedActivities = subject.activities.map { activity ->
                    if (!extractedValue.isNullOrEmpty()) {
                        activity.copy(mark = extractedValue)
                    } else {
                        activity
                    }
                }

                subject.copy(activities = updatedActivities)
            }

            student.copy(marks = updatedSubjects)
        }

        return apiResponse.copy(data = updatedStudents)
    }

    private fun normalize(text: String): String {
        return text.trim().lowercase()
    }
    private fun buildHeaderColumns(response: MarkResponse): List<MarkColumn> {
        val columns = mutableListOf<MarkColumn>()
        val firstStudent = response.data.firstOrNull() ?: return columns
        firstStudent.marks.forEach { subject ->
            subject.activities.forEach { activity ->
                columns.add(
                    MarkColumn(
                        subjectId = subject.subject_id,
                        subjectName = subject.subject_name,
                        activityId = activity.id,
                        activityName = activity.name,
                        maxMark = activity.max_mark.toIntOrNull() ?: 100
                    )
                )
            }
        }
        return columns
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
                    SUBJECT_CELL_WIDTH,
                    LinearLayout.LayoutParams.WRAP_CONTENT
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
                text = "Max mark : " + col.maxMark
                gravity = Gravity.CENTER
                setTextColor(Color.GRAY)
            })

            container.addView(layout)

            if (index != columns.lastIndex) {
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        SUBJECT_CELL_GAP,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                })
            }
        }
        HorizontalScrollSync.bind(headerScroll)
    }

    private fun setupMarksUI(
        finalResponse: MarkResponse,
        baseResponse: MarkResponse
    ) {
        val columns = buildHeaderColumns(baseResponse)

        currentStudentsList =
            finalResponse.data.map { apiStudent ->

                val markTexts = MutableList(columns.size) { "" }
                val mockTexts = MutableList(columns.size) { "" }
                val marks = MutableList<Int?>(columns.size) { null }

                apiStudent.marks.forEach { subject ->
                    subject.activities.forEach { activity ->
                        val index = columns.indexOfFirst {
                            it.subjectName.equals(subject.subject_name, true)
                        }
                        if (index != -1) {
                            markTexts[index] = activity.mark
                            marks[index] = activity.mark.toIntOrNull()
                        }
                    }
                }

                val baseStudent =
                    baseResponse.data.firstOrNull {
                        it.student_id == apiStudent.student_id
                    }

                baseStudent?.marks?.forEach { subject ->
                    subject.activities.forEach { activity ->
                        val index = columns.indexOfFirst {
                            it.subjectName.equals(subject.subject_name, true)
                        }
                        if (index != -1) {
                            mockTexts[index] = activity.mark
                        }
                    }
                }

                StudentMarkList(
                    name = apiStudent.student_name,
                    student_id = apiStudent.student_id,
                    rollNo = apiStudent.admission_no,
                    marks = marks,
                    markTexts = markTexts,
                    mockMarkTexts = mockTexts
                )
            }.toMutableList()

        updateIssueLabel()
    }
    private fun isGetMarkDetails() {
        val json = JsonObject()
        json.addProperty("class_id", isFinalMapDetails!!.get(0).class_id)
        json.addProperty("section_id", isFinalMapDetails!!.get(0).section_id)
        json.addProperty("exam_id", Constant.isMarkUploadExamListDataDetails!!.id)
        val arr = JsonArray()
        val obj = JsonObject()
        obj.addProperty("subject_id", isFinalMapDetails!!.get(0).subject_id)
        val act = JsonArray()
        for (i in isFinalMapDetails!!.indices) {
            act.add(isFinalMapDetails!![0].paper[i].activity_id)
        }
        obj.add("activities", act)
        arr.add(obj)
        json.add("selected_activities", arr)
        appViewModel!!.isMarkDetails(isAccessToken!!, json, this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    override fun onMarksChanged() {
        val now = System.currentTimeMillis()
        if (now - lastIssueUpdateTime > 500) {
            lastIssueUpdateTime = now
            updateIssueLabel()
        }
    }

    private fun updateIssueLabel() {

        val issueSummary = calculateIssueSummary(currentStudentsList, markColumns)
        if (issueSummary.total != 0) {
            binding.lblIssueFound.visibility = View.VISIBLE
        } else {
            binding.lblIssueFound.visibility = View.GONE
        }

        binding.lblIssueFound.text =
            "⚠️ Found ${issueSummary.total} issue(s): " +
                    "AB → ${issueSummary.absentCount}, " +
                    "Max mark exceeded → ${issueSummary.maxMarkCount}, " +
                    "PLEASE MARK PROPERLY → ${issueSummary.systemMsgCount}"

    }

    private fun calculateIssueSummary(
        students: List<StudentMarkList>,
        columns: List<MarkColumn>
    ): IssueSummary {

        val summary = IssueSummary()
        students.forEach { student ->
            student.markTexts.forEachIndexed { index, rawText ->
                val value = rawText.toIntOrNull()
                val maxMark = columns.getOrNull(index)?.maxMark ?: return@forEachIndexed

                when {
                    rawText.equals("AB", true) -> {
                        summary.absentCount++
                        summary.total++
                    }

                    rawText.equals("PLEASE MARK PROPERLY", true) -> {
                        summary.systemMsgCount++
                        summary.total++
                    }

                    rawText.isNotEmpty() && value == null -> {
                        summary.invalidCount++
                        summary.total++
                    }

                    value != null && value > maxMark -> {
                        summary.maxMarkCount++
                        summary.total++
                    }
                }
            }
        }
        return summary
    }


    private fun isSaveTheMark(
        students: List<StudentMarkList>,
        columns: List<MarkColumn>
    ): JsonArray {

        val studentsArray = JsonArray()
        students.forEach { student ->
            val studentObj = JsonObject().apply {
                addProperty("student_id", student.student_id)
                addProperty("student_name", student.name)
                addProperty("roll_no", student.rollNo)
                addProperty("admission_no", "")
            }
            val marksArray = JsonArray()
            val subjectMap =
                LinkedHashMap<String, MutableList<Pair<Int, MarkColumn>>>()

            columns.forEachIndexed { index, column ->
                val list = subjectMap.getOrPut(column.subjectName) {
                    mutableListOf()
                }
                list.add(index to column)
            }

            subjectMap.forEach { (subjectName, columnList) ->

                val subjectObj = JsonObject().apply {
                    addProperty("subject_id", columnList.first().second.subjectId)
                    addProperty("subject_name", subjectName)
                }

                val activitiesArray = JsonArray()

                columnList.forEach { (index, column) ->

                    val rawText =
                        student.markTexts.getOrNull(index)?.trim().orEmpty()
                    val maxMark = column.maxMark
                    val activityObj = JsonObject().apply {
                        addProperty("id", column.activityId)
                        addProperty("name", column.activityName)
                        addProperty("mark", rawText)
                        addProperty("max_mark", maxMark.toString())
                    }

                    activitiesArray.add(activityObj)
                }
                subjectObj.add("activities", activitiesArray)
                marksArray.add(subjectObj)
            }
            studentObj.add("marks", marksArray)
            studentsArray.add(studentObj)
        }
        return studentsArray
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
        lblSelectTarget.text = "Are you want to save the marks"
        okButton.setOnClickListener {
            Constant.showLoading(this@ReviewAndEditMarks)
            val saveMarksJsonArray = isSaveTheMark(
                currentStudentsList,
                markColumns
            )
            Log.d("saveMarksJsonArray", saveMarksJsonArray.toString())
            appViewModel?.savemarks(
                isAccessToken!!,
                saveMarksJsonArray,
                this
            )
            alertDialog.dismiss()
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    private fun getMaxMarkIssues(
        students: List<StudentMarkList>,
        columns: List<MarkColumn>
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
        students: List<StudentMarkList>,
        columns: List<MarkColumn>
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
                            enteredValue = text
                        )
                    )
                }
            }
        }
        return issues
    }
}
