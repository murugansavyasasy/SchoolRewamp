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
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivitySubjectNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Adapter.MarksAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.*
import com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model.ColumnHeader
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.HorizontalScrollSync
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ReviewAndEditMarksBinding
import kotlin.collections.MutableList

class ReviewAndEditMarks : BaseActivity<ReviewAndEditMarksBinding>(), View.OnClickListener {

    override fun getViewBinding() = ReviewAndEditMarksBinding.inflate(layoutInflater)
    private var appViewModel: App? = null
    val SUBJECT_CELL_WIDTH = 200
    private val SUBJECT_CELL_GAP = 20
    private var isAccessToken: String? = null
    private var isFinalMapDetails: List<getActivitySubjectNameData>? = emptyList()

    private var isStaffDetails: StaffDetails? = null
    override fun setupViews() {
        super.setupViews()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)

        isFinalMapDetails =
            intent.getParcelableArrayListExtra(
                "FINAL_MAP_ACTIVITY"
            ) ?: emptyList()

        Log.d("isFinalMapDetails",isFinalMapDetails.toString())
        isAccessToken = isStaffDetails?.access_token
             isGetMarkDetails()

        appViewModel!!.isGetMarkDetails?.observe(this) { response ->
            val finalResponse = if (response == null || response.data.isEmpty()) {
                getMockMarkResponse()
            } else {
                response
            }
            setupMarksUI(finalResponse)
        }
    }

    private fun setupHeader(subjects: List<String?>) {

        val container = findViewById<LinearLayout>(R.id.headerSubjectContainer)
        val headerScroll = findViewById<HorizontalScrollView>(R.id.headerScroll)

        container.removeAllViews()

        subjects.forEachIndexed { index, subject ->

            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    SUBJECT_CELL_WIDTH, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = subject
                gravity = Gravity.CENTER
                textSize = 14f
                setPadding(10, 10, 10, 10)
                setTypeface(null, Typeface.BOLD)
            }

            binding.marksHeader.headerSubjectContainer.addView(tv)
            if (index != subjects.lastIndex) {
                val gap = View(this)
                gap.layoutParams = LinearLayout.LayoutParams(
                    SUBJECT_CELL_GAP, LinearLayout.LayoutParams.MATCH_PARENT
                )
                binding.marksHeader.headerSubjectContainer.addView(gap)
            }
        }

        HorizontalScrollSync.bind(headerScroll)
    }

    private fun getMockMarkResponse(): MarkResponse {

        return MarkResponse(
            status = true, message = "Mock mark details loaded", data = listOf(

                StudentMarkApi(
                    student_id = "9674704",
                    student_name = "CHANDHRU V",
                    roll_no = "",
                    admission_no = "SS-1",
                    marks = listOf(
                        SubjectMark(
                            subject_id = "112616", subject_name = "TAMIL", activities = listOf(
                                ActivityMark(
                                    id = "3061", name = "Marks", mark = "AB", max_mark = "100"
                                )
                            )
                        ), SubjectMark(
                            subject_id = "112625", subject_name = "SCIENCE", activities = listOf(
                                ActivityMark(
                                    id = "3062", name = "Paper 1", mark = "66", max_mark = "100"
                                ), ActivityMark(
                                    id = "3063", name = "Paper 2", mark = "110", max_mark = "100"
                                )
                            )
                        )
                    )
                ),

                StudentMarkApi(
                    student_id = "9674710",
                    student_name = "Murugan",
                    roll_no = "",
                    admission_no = "SS-7",
                    marks = listOf(
                        SubjectMark(
                            subject_id = "112616", subject_name = "TAMIL", activities = listOf(
                                ActivityMark(
                                    id = "3061", name = "Marks", mark = "", max_mark = "100"
                                )
                            )
                        ), SubjectMark(
                            subject_id = "112625", subject_name = "SCIENCE", activities = listOf(
                                ActivityMark(
                                    id = "3062", name = "Paper 1", mark = "77", max_mark = "100"
                                ), ActivityMark(
                                    id = "3063", name = "Paper 2", mark = "", max_mark = "100"
                                )
                            )
                        )
                    )
                ),

                StudentMarkApi(
                    student_id = "9674711",
                    student_name = "Bharath Student M",
                    roll_no = "",
                    admission_no = "SS-8",
                    marks = listOf(
                        SubjectMark(
                            subject_id = "112616", subject_name = "TAMIL", activities = listOf(
                                ActivityMark(
                                    id = "3061", name = "Marks", mark = "200", max_mark = "100"
                                )
                            )
                        ), SubjectMark(
                            subject_id = "112625", subject_name = "SCIENCE", activities = listOf(
                                ActivityMark(
                                    id = "3062", name = "Paper 1", mark = "", max_mark = "100"
                                ), ActivityMark(
                                    id = "3063", name = "Paper 2", mark = "AB", max_mark = "100"
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    private fun setupMarksUI(response: MarkResponse) {
        val columns = mutableListOf<MarkColumn>()

        val firstStudent = response.data.first()

        firstStudent.marks.forEach { subject ->
            subject.activities.forEach { activity ->
                columns.add(
                    MarkColumn(
                        subjectId = subject.subject_id,
                        subjectName = subject.subject_name,
                        activityId = activity.id,
                        activityName = activity.name,
                        maxMark = activity.max_mark.toIntOrNull() ?: 0
                    )
                )
            }
        }

        binding.marksHeader.headerSubjectContainer.removeAllViews()

        columns.forEach { col ->
            val headerLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    SUBJECT_CELL_WIDTH, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER
            }

            headerLayout.addView(TextView(this).apply {
                text = col.subjectName
                gravity = Gravity.CENTER
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
            })

            headerLayout.addView(TextView(this).apply {
                text = col.activityName
                gravity = Gravity.CENTER
                textSize = 13f
            })

            headerLayout.addView(TextView(this).apply {
                text = "(${col.maxMark})"
                gravity = Gravity.CENTER
                textSize = 12f
                setTextColor(Color.GRAY)
            })

            binding.marksHeader.headerSubjectContainer.addView(headerLayout)
        }
        val students = response.data.map { apiStudent ->

            val markTexts = MutableList(columns.size) { "" }
            val marks = MutableList<Int?>(columns.size) { null }

            apiStudent.marks.forEach { subject ->
                subject.activities.forEach { activity ->
                    val index = columns.indexOfFirst {
                        it.subjectId == subject.subject_id && it.activityId == activity.id
                    }

                    if (index != -1) {
                        val value = activity.mark.trim()
                        markTexts[index] = value
                        marks[index] = value.toIntOrNull()
                    }
                }
            }

            StudentMarkList(
                name = apiStudent.student_name,
                rollNo = apiStudent.admission_no,
                marks = marks,
                markTexts = markTexts
            )
        }.toMutableList()
        binding.rvMarks.adapter = MarksAdapter(
            students = students, subjectCount = columns.size, context = this
        )
        binding.rvMarks.layoutManager = LinearLayoutManager(this)
        binding.rvMarks.setHasFixedSize(true)
    }


    private fun isGetMarkDetails() {
        val json = JsonObject()
        json.addProperty("class_id", "32588")
        json.addProperty("section_id", "90831")
        json.addProperty("exam_id", "11027")
        val arr = JsonArray()
        val obj = JsonObject()
        obj.addProperty("subject_id", "112625")
        val act = JsonArray()
        act.add("3062")
        act.add("3063")
        obj.add("activities", act)
        arr.add(obj)
        json.add("selected_activities", arr)
        Log.d("REQ", json.toString())
        appViewModel!!.isMarkDetails(isAccessToken!!, json, this)
    }

    override fun onClick(v: View?) {}
}