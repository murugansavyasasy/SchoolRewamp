package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var currentStudentsList: MutableList<StudentMarkList> = mutableListOf()

    override fun setupViews() {
        super.setupViews()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token
        isFinalMapDetails =
            intent.getParcelableArrayListExtra<getActivitySubjectNameData>(
                "FINAL_MAP_ACTIVITY"
            ) ?: emptyList()
        Log.d("isFinalMapDetails", isFinalMapDetails.toString())

        isGetMarkDetails()

        appViewModel!!.isGetMarkDetails?.observe(this) { response ->

            val baseResponse = response

            val finalResponse =
                if (Constant.isMarkUploadFromAi) {
                    mergeMarksWithExtractedTable(
                        baseResponse!!,
                        Constant.isExtractedDetails?.firstOrNull()
                    )
                } else {
                    baseResponse
                }
            setupHeader(buildHeaderColumns(baseResponse!!))
            // UI
            setupMarksUI(finalResponse!!, baseResponse)
        }

        binding.lnrSaveAllMarks2.setOnClickListener {
            val saveMarksJsonArray = isSaveTheMark(
                currentStudentsList,
                markColumns
            )
        }
    }

    private fun mergeMarksWithExtractedTable(
        apiResponse: MarkResponse,
        tableData: ParcelTableData?
    ): MarkResponse {

        if (tableData == null || tableData.records.isEmpty()) return apiResponse

        val updatedStudents = apiResponse.data.map { student ->

            val matchedRow = tableData.records.firstOrNull {
                it["Student ID"]?.toString() == student.student_id
            } ?: return@map student

            val newMarks = matchedRow
                .filterKeys {
                    it != "S.No" &&
                            it != "Reg No" &&
                            it != "Student ID" &&
                            it != "Student Name"
                }
                .map { (subjectName, value) ->
                    SubjectMark(
                        subject_id = "",
                        subject_name = subjectName,
                        activities = listOf(
                            ActivityMark(
                                id = "TABLE",
                                name = "Marks",
                                mark = value.toString(),
                                max_mark = "100"
                            )
                        )
                    )
                }

            student.copy(marks = newMarks)
        }

        return apiResponse.copy(data = updatedStudents)
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
                text = "(${col.maxMark})"
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

        val students: MutableList<StudentMarkList> =
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

                // previous marks
                val baseStudent = baseResponse.data.firstOrNull {
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
                    rollNo = apiStudent.admission_no,
                    marks = marks,
                    markTexts = markTexts,
                    mockMarkTexts = mockTexts
                )
            }.toMutableList()
        currentStudentsList = students
        binding.rvMarks.layoutManager = LinearLayoutManager(this)
        binding.rvMarks.adapter =
            MarksAdapter(students, columns.size, this, this)
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

    override fun onClick(v: View?) {}

    override fun onMarksChanged() {
        updateIssueLabel()
    }

    private fun updateIssueLabel() {

        val issueSummary = calculateIssueSummary(currentStudentsList)
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
        maxMark: Int = 100
    ): IssueSummary {

        val summary = IssueSummary()

        students.forEach { student ->
            student.markTexts.forEach { rawText ->

                val value = rawText.toIntOrNull()

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
           //     addProperty("student_id", student.)
                addProperty("student_name", student.name)
                addProperty("roll_no", student.rollNo)
                addProperty("admission_no", "")
            }

            // Group columns by subject
            val subjectMap = columns.withIndex().groupBy {
                it.value.subjectName
            }

            val marksArray = JsonArray()

            subjectMap.forEach { (subjectName, columnList) ->

                val subjectObj = JsonObject().apply {
                    addProperty("subject_id", columnList.first().value.subjectId)
                    addProperty("subject_name", subjectName)
                }

                val activitiesArray = JsonArray()

                columnList.forEach { (index, column) ->

                    val rawText = student.markTexts[index].trim()
                    val value = rawText.toIntOrNull()
                    val maxMark = column.maxMark

                    val (confidence, reason) = when {
                        rawText.equals("AB", true) ->
                            false to "Student is absent"

                        rawText.equals("PLEASE MARK PROPERLY", true) ->
                            false to "PLEASE MARK PROPERLY"

                        rawText.isNotEmpty() && value == null ->
                            false to "Invalid mark entry"

                        value != null && value > maxMark ->
                            false to "Mark exceeds maximum ($maxMark)"

                        else ->
                            true to ""
                    }

                    val activityObj = JsonObject().apply {
                        addProperty("id", column.activityId)
                        addProperty("name", column.activityName)
                        addProperty("mark", rawText)
                        addProperty("change_mark", "")
                        addProperty("max_mark", maxMark.toString())
                        addProperty("cnfidenceLvl", confidence)
                        addProperty("reason", reason)
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

}
