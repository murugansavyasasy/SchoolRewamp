package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks

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
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Adapter.MarksAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.*
import com.vs.schoolmessenger.Utils.HorizontalScrollSync
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ReviewAndEditMarksBinding
import kotlin.collections.MutableList

class ReviewAndEditMarks :
    BaseActivity<ReviewAndEditMarksBinding>(), View.OnClickListener {

    override fun getViewBinding() =
        ReviewAndEditMarksBinding.inflate(layoutInflater)

    private var appViewModel: App? = null
     val SUBJECT_CELL_WIDTH = 200
    private  val SUBJECT_CELL_GAP = 20
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    override fun setupViews() {
        super.setupViews()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

                isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token


        val subjects = listOf(
            "Tamil", "English", "Maths", "Science", "Social",
            "Computer", "GK", "Physics", "Chemistry", "Biology"
        ) // change size anytime

        setupHeader(subjects)


        val students: MutableList<StudentMarkList> = MutableList(30) { index ->

            val markTexts: MutableList<String> =
                MutableList(subjects.size) { "" }

            val marks: MutableList<Int?> =
                MutableList(subjects.size) { null }

            when (index) {

                0 -> {
                    markTexts[0] = "101"
                    markTexts[1] = "AB"
                    markTexts[3] = "66"
                }
                1 -> {
                    markTexts[0] = ""
                    markTexts[1] = ""
                    markTexts[2] = "190"
                    markTexts[3] = "AB"
                }

                2 -> {
                    markTexts[0] = ""
                    markTexts[1] = "AB"
                    markTexts[2] = "77"
                    markTexts[3] = "77"
                }

                3 -> {
                    markTexts[0] = "103"
                    markTexts[1] = "70"
                    markTexts[2] = ""
                    markTexts[3] = "110"
                }

                4 -> {
                    markTexts[0] = "200"
                    markTexts[1] = "AB"
                    markTexts[2] = "AB"
                    markTexts[3] = "AB"
                }

                5 -> {
                    markTexts[0] = "101"
                    markTexts[1] = "AB"
                    markTexts[3] = "66"
                }
                6 -> {
                    markTexts[0] = ""
                    markTexts[1] = ""
                    markTexts[2] = "190"
                    markTexts[3] = "AB"
                }

                7 -> {
                    markTexts[0] = ""
                    markTexts[1] = "AB"
                    markTexts[2] = "77"
                    markTexts[3] = "77"
                }

                8 -> {
                    markTexts[0] = "103"
                    markTexts[1] = "70"
                    markTexts[2] = ""
                    markTexts[3] = "110"
                }

                9 -> {
                    markTexts[0] = "200"
                    markTexts[1] = "AB"
                    markTexts[2] = "AB"
                    markTexts[3] = "AB"
                }


                10 -> {
                    markTexts[0] = "101"
                    markTexts[1] = "AB"
                    markTexts[3] = "66"
                }
                11 -> {
                    markTexts[0] = ""
                    markTexts[1] = ""
                    markTexts[2] = "190"
                    markTexts[3] = "AB"
                }

                22 -> {
                    markTexts[0] = ""
                    markTexts[1] = "AB"
                    markTexts[2] = "77"
                    markTexts[3] = "77"
                }

                13 -> {
                    markTexts[0] = "103"
                    markTexts[1] = "70"
                    markTexts[2] = ""
                    markTexts[3] = "110"
                }

                14 -> {
                    markTexts[0] = "200"
                    markTexts[1] = "AB"
                    markTexts[2] = "AB"
                    markTexts[3] = "AB"
                }
            }

            for (i in markTexts.indices) {
                marks[i] = markTexts[i].toIntOrNull()
            }

            StudentMarkList(
                name = "Student ${index + 1}",
                rollNo = "R%03d".format(index + 1),
                marks = marks,
                markTexts = markTexts
            )
        }




        binding.rvMarks.layoutManager = LinearLayoutManager(this)
        binding.rvMarks.adapter = MarksAdapter(students, subjects.size,this)

//        binding.rcExamList.layoutManager = LinearLayoutManager(this)
//
//        // Divider
//        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
//        divider.setDrawable(resources.getDrawable(R.drawable.divider_light, null))
//        binding.rcExamList.addItemDecoration(divider)
//
//        binding.headerLayout.headerScroll.setOnScrollChangeListener { _, x, _, _, _ ->
//            if (!Constant.isSyncing) {
//                Constant.isSyncing = true
//                Constant.scrollX = x
//                binding.rcExamList.adapter?.notifyDataSetChanged()
//                Constant.isSyncing = false
//            }
//        }

//        callApi()
//
//        appViewModel!!.isGetMarkDetails?.observe(this) { response ->
//            if (response != null && response.status) {
//                val rows = mapApiToUi(response)
//                binding.rcExamList.adapter = MarkEntryAdapter(rows)
//            }
//        }
    }

    private fun setupHeader(subjects: List<String>) {

        val container = findViewById<LinearLayout>(R.id.headerSubjectContainer)
        val headerScroll = findViewById<HorizontalScrollView>(R.id.headerScroll)

        container.removeAllViews()

        subjects.forEachIndexed { index, subject ->

            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    SUBJECT_CELL_WIDTH,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = subject
                gravity = Gravity.CENTER
                textSize = 14f
                setPadding(10, 10, 10, 10)
                setTypeface(null, Typeface.BOLD)
            }

            binding.marksHeader.headerSubjectContainer.addView(tv)

            // ✅ SAME GAP
            if (index != subjects.lastIndex) {
                val gap = View(this)
                gap.layoutParams = LinearLayout.LayoutParams(
                    SUBJECT_CELL_GAP,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                binding.marksHeader.headerSubjectContainer.addView(gap)
            }
        }


        HorizontalScrollSync.bind(headerScroll)
    }



//    private fun mapApiToUi(response: MarkResponse): MutableList<StudentMarkRow> {
//
//        val rows = mutableListOf<StudentMarkRow>()
//        var roll = 1
//
//        response.data.forEach { student ->
//
//            val map = hashMapOf<String, ActivityMark>()
//            student.marks.forEach { sub ->
//                sub.activities.forEach { map[it.id] = it }
//            }
//
//            val activities = mutableListOf<ActivityMark>()
//            MASTER_ACTIVITY_IDS.forEach { id ->
//                activities.add(map[id] ?: ActivityMark(id, "", "100"))
//            }
//
//            rows.add(
//                StudentMarkRow(
//                    rollNo = roll++,
//                    studentId = student.student_id,
//                    studentName = student.student_name,
//                    activities = activities
//                )
//            )
//        }
//        return rows
//    }

    private fun callApi() {

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
        appViewModel!!.isMarkDetails(isAccessToken!!, json,this)
    }

    override fun onClick(v: View?) {}
}







//package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks
//
//import android.util.Log
//import android.view.View
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.DividerItemDecoration
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.gson.JsonArray
//import com.google.gson.JsonObject
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Repository.App
//import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Adapter.MarkEntryAdapter
//import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.ActivityMark
//import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.MarkResponse
//import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.StudentMarkRow
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.SharedPreference
//import com.vs.schoolmessenger.databinding.ReviewAndEditMarksBinding
//
//class ReviewAndEditMarks :
//    BaseActivity<ReviewAndEditMarksBinding>(), View.OnClickListener {
//
//    override fun getViewBinding() =
//        ReviewAndEditMarksBinding.inflate(layoutInflater)
//
//    private var appViewModel: App? = null
//    private var isAccessToken: String? = null
//    private var isStaffDetails: StaffDetails? = null
//
//    /** ✅ FIXED 10 SUBJECT / ACTIVITY IDS */
//    private val MASTER_ACTIVITY_IDS = listOf(
//        "3061","3062","3063","3064","3065",
//        "3066","3067","3068","3069","3070"
//    )
//
//    override fun setupViews() {
//        super.setupViews()
//
//        // Toolbar
//        isToolBarPrimarySchool(
//            mainViewId = R.id.main,
//            statusBarBgView = binding.statusBarBackground
//        )
//        binding.toolbarLayout.imgBack.setOnClickListener(this)
//
//        isStaffDetails = SharedPreference.getStaffDetails(this)
//        isAccessToken = isStaffDetails?.access_token
//
//        binding.toolbarLayout.lblParentToolBar.text =
//            Constant.isSelectedMenuName
//        binding.toolbarLayout.lblSchoolName.text =
//            isStaffDetails?.school_name ?: ""
//
//        // ViewModel
//        appViewModel = ViewModelProvider(this)[App::class.java]
//        appViewModel!!.init()
//
//        // RecyclerView
//        binding.rcExamList.layoutManager = LinearLayoutManager(this)
//
//        // Light divider
//        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
//        divider.setDrawable(resources.getDrawable(R.drawable.divider_light, null))
//        binding.rcExamList.addItemDecoration(divider)
//
//        binding.headerLayout.headerScroll.setOnScrollChangeListener { _, scrollX, _, _, _ ->
//            Constant.scrollX = scrollX
//        }
//
//        // API call
//        isGetMarkDetails()
//
//        // Observe response
//        appViewModel!!.isGetMarkDetails?.observe(this) { response ->
//            if (response != null && response.status && response.data.isNotEmpty()) {
//
//                val studentRows = mapApiToUi(response)
//                binding.rcExamList.adapter = MarkEntryAdapter(studentRows)
//            }
//        }
//    }
//
//    /** ✅ API → UI mapping */
//    private fun mapApiToUi(response: MarkResponse): MutableList<StudentMarkRow> {
//
//        val rows = mutableListOf<StudentMarkRow>()
//        var rollNo = 1
//
//        response.data.forEach { studentApi ->
//
//            val activityMap = HashMap<String, ActivityMark>()
//            studentApi.marks.forEach { subject ->
//                subject.activities.forEach { activity ->
//                    activityMap[activity.id] = activity
//                }
//            }
//
//            val finalActivities = mutableListOf<ActivityMark>()
//            MASTER_ACTIVITY_IDS.forEach { activityId ->
//                finalActivities.add(
//                    activityMap[activityId]
//                        ?: ActivityMark(activityId, "", "100")
//                )
//            }
//
//            rows.add(
//                StudentMarkRow(
//                    rollNo = rollNo++,
//                    studentId = studentApi.student_id,
//                    studentName = studentApi.student_name,
//                    activities = finalActivities
//                )
//            )
//        }
//        return rows
//    }
//
//    /** ✅ Request JSON */
//    private fun isGetMarkDetails() {
//
//        val mainJson = JsonObject()
//        mainJson.addProperty("class_id", "32588")
//        mainJson.addProperty("section_id", "90831")
//        mainJson.addProperty("exam_id", "11027")
//
//        val selectedActivities = JsonArray()
//        val subjectObj = JsonObject()
//        subjectObj.addProperty("subject_id", "112625")
//
//        val actArray = JsonArray()
//        actArray.add("3062")
//        actArray.add("3063")
//
//        subjectObj.add("activities", actArray)
//        selectedActivities.add(subjectObj)
//
//        mainJson.add("selected_activities", selectedActivities)
//
//        Log.d("MARK_REQUEST", mainJson.toString())
//        appViewModel!!.isMarkDetails(isAccessToken!!, mainJson)
//    }
//
//    override fun onClick(v: View?) {
//        if (v?.id == R.id.imgBack) onBackPressed()
//    }
//}
