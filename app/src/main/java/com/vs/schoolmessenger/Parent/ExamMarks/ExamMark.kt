
package com.vs.schoolmessenger.Dashboard.Parent

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel.ExamDataRewamp
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableAdapter
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetable
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableRubric
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamMarkBinding

class ExamMark : BaseActivity<ExamMarkBinding>(), View.OnClickListener, ExamMarkListener {
    override fun getViewBinding(): ExamMarkBinding {
        return ExamMarkBinding.inflate(layoutInflater)
    }

    private lateinit var examAdapter: ExamTimeTableAdapter
    private lateinit var exammarkadapter: ExamMarkAdapter

    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    var userDetails: UserDetails? = null


    var examTitle = ""

    private var currentTab = TabType.EXAM_TIMETABLE

    private enum class TabType {
        EXAM_MARKS, EXAM_TIMETABLE
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        userDetails = SharedPreference.getUserDetails(this)
        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)

        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)

            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )

            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }


        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token


        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtVideoMenu1.text.clear()
            }
        }


        binding.root.post {
            val finalName =
                Constant.isSelectedMenuName?.takeIf { it.isNotEmpty() } ?: menu_name ?: ""
            Log.d("NoticeBoard_HeaderFinal", "Setting headerview text: $finalName")
            binding.lblHeaderTitle.text = finalName
            binding.lblHeaderTitle.visibility = View.VISIBLE
        }


        binding.toolbarLayout.apply {
            imgBack.setOnClickListener(this@ExamMark)
            lblStudentName.text = isChildDetails!!.name
            lblStudentSection.text =
                "${isChildDetails.standard_name} - ${isChildDetails.section_name}"
        }
        binding.tabOneName.text = getString(R.string.exam_timetable)
        binding.tabTwoName.text = getString(R.string.exam_marks)

        binding.txtVideoMenu1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                when (currentTab) {
                    TabType.EXAM_MARKS -> {
                        if (::exammarkadapter.isInitialized) {
                            exammarkadapter.filter.filter(query)
                            Log.d("query", query)
                        }
                    }

                    TabType.EXAM_TIMETABLE -> {
                        if (::examAdapter.isInitialized) {
                            examAdapter.filter.filter(query)
                            Log.d("query", query)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        appViewModel?.getexams?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response != null) {
//                if (response == null || !response.status || response.data.isNullOrEmpty()) {
//
//                    val mobileNumber = SharedPreference.getMobileNumber(this)
//                    val jsonObject = JsonObject().apply {
//                        addProperty(APIKeyNames.mobile_number, mobileNumber)
//                        addProperty(APIKeyNames.activity, Constant.add_points_view_exam_schedule)
//                        addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
//                        addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
//                    }
//                    appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)
//                    Constant.hideLoading(this)
//                    showErrorUI(response?.message ?: getString(R.string.no_data_available))
//                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//                    return@observe
//                }
                val mobileNumber = SharedPreference.getMobileNumber(this)
                val jsonObject = JsonObject().apply {
                    addProperty(APIKeyNames.mobile_number, mobileNumber)
                    addProperty(APIKeyNames.activity, Constant.add_points_view_exam_schedule)
                    addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                    addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                }
                appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)

                if (response.status) {
                    Constant.hideLoading(this)
//                    val examTimetableResponse = ExamTimetableResponse(
//                        status = true,
//                        message = "AVAILABLE_EXAMS_RETRIEVED",
//                        data = listOf(
//
//                            // -------------------- Exam 1 --------------------
//                            ExamTimetable(
//                                examId = 1,
//                                examName = "Term 1 Finals",
//                                subjects = listOf(
//
//                                    // Subject 1
//                                    ExamTimetableSubject(
//                                        subjectId = 101,
//                                        subjectName = "Science",
//                                        totalMarks = 50,
//                                        activities = listOf(
//
//                                            // Activity with Activity-level schedule
//                                            ExamTimetableActivity(
//                                                activityId = 501,
//                                                activityName = "Lab Work",
//                                                schedulingDetails = ExamScheduleDetails(
//                                                    date = "2026-03-14",
//                                                    startTime = "09:00 AM",
//                                                    endTime = "12:00 PM",
//                                                    session = "Morning",
//                                                    venue = "Hall 10",
//                                                    syllabus = "Chapter 1-5"
//                                                ),
//                                                rubrics = emptyList()
//                                            ),
//
//                                            // Activity with Rubrics
//                                            ExamTimetableActivity(
//                                                activityId = 502,
//                                                activityName = "Theory Work",
//                                                schedulingDetails =null,
//                                                rubrics = listOf(
//
//                                                    ExamTimetableRubric(
//                                                        rubricId = 901,
//                                                        rubricName = "Botiba Setup",
//                                                        schedulingDetails = ExamScheduleDetails(
//                                                            date = "2026-03-14",
//                                                            startTime = "09:00 AM",
//                                                            endTime = "12:00 PM",
//                                                            session = "Morning",
//                                                            venue = "Hall 10",
//                                                            syllabus = "Chapter 1-5"
//                                                        )
//                                                    ),
//
//                                                    ExamTimetableRubric(
//                                                        rubricId = 902,
//                                                        rubricName = "Microscope Setup",
//                                                        schedulingDetails = ExamScheduleDetails(
//                                                            date = "2026-03-14",
//                                                            startTime = "09:00 AM",
//                                                            endTime = "12:00 PM",
//                                                            session = "Morning",
//                                                            venue = "Hall 10",
//                                                            syllabus = "Chapter 1-5"
//                                                        )
//                                                    ),
//
//                                                    ExamTimetableRubric(
//                                                        rubricId = 903,
//                                                        rubricName = "Chemical Analysis",
//                                                        schedulingDetails = ExamScheduleDetails(
//                                                            date = "2026-03-14",
//                                                            startTime = "09:00 AM",
//                                                            endTime = "12:00 PM",
//                                                            session = "Morning",
//                                                            venue = "Hall 10",
//                                                            syllabus = "Chapter 1-5"
//                                                        )
//                                                    )
//                                                )
//                                            )
//                                        )
//                                    ),
//
//                                    // Subject 2
//                                    ExamTimetableSubject(
//                                        subjectId = 102,
//                                        subjectName = "Mathematics",
//                                        totalMarks = 100,
//                                        activities = listOf(
//
//                                            ExamTimetableActivity(
//                                                activityId = 503,
//                                                activityName = "Objective Test",
//                                                schedulingDetails = null,
//                                                rubrics = listOf(
//                                                    ExamTimetableRubric(
//                                                        rubricId = 904,
//                                                        rubricName = "Section A",
//                                                        schedulingDetails = ExamScheduleDetails(
//                                                            date = "2026-03-15",
//                                                            startTime = "10:00 AM",
//                                                            endTime = "01:00 PM",
//                                                            session = "Morning",
//                                                            venue = "Hall 12",
//                                                            syllabus = "Unit 1-6"
//                                                        )
//                                                    )
//                                                )
//                                            ),
//
//                                            ExamTimetableActivity(
//                                                activityId = 504,
//                                                activityName = "Descriptive Test",
//                                                schedulingDetails = null,
//                                                rubrics = listOf(
//                                                    ExamTimetableRubric(
//                                                        rubricId = 905,
//                                                        rubricName = "Part A",
//                                                        schedulingDetails = ExamScheduleDetails(
//                                                            date = "2026-03-16",
//                                                            startTime = "09:00 AM",
//                                                            endTime = "12:00 PM",
//                                                            session = "Morning",
//                                                            venue = "Hall 8",
//                                                            syllabus = "Unit 1-3"
//                                                        )
//                                                    ),
//                                                    ExamTimetableRubric(
//                                                        rubricId = 906,
//                                                        rubricName = "Part B",
//                                                        schedulingDetails = ExamScheduleDetails(
//                                                            date = "2026-03-16",
//                                                            startTime = "01:00 PM",
//                                                            endTime = "03:00 PM",
//                                                            session = "Afternoon",
//                                                            venue = "Hall 8",
//                                                            syllabus = "Unit 4-6"
//                                                        )
//                                                    )
//                                                )
//                                            )
//                                        )
//                                    )
//                                )
//                            ),
//
//                            // -------------------- Exam 2 --------------------
//                            ExamTimetable(
//                                examId = 2,
//                                examName = "Mid Term Examination",
//                                subjects = listOf(
//
//                                    ExamTimetableSubject(
//                                        subjectId = 103,
//                                        subjectName = "English",
//                                        totalMarks = 75,
//                                        activities = listOf(
//
//                                            ExamTimetableActivity(
//                                                activityId = 505,
//                                                activityName = "Written Exam",
//                                                schedulingDetails = null,
//                                                rubrics = listOf(
//                                                    ExamTimetableRubric(
//                                                        rubricId = 907,
//                                                        rubricName = "Grammar",
//                                                        schedulingDetails = ExamScheduleDetails(
//                                                            date = "2026-04-01",
//                                                            startTime = "09:30 AM",
//                                                            endTime = "11:30 AM",
//                                                            session = "Morning",
//                                                            venue = "Hall 5",
//                                                            syllabus = "Lessons 1-10"
//                                                        )
//                                                    )
//                                                )
//                                            )
//                                        )
//                                    )
//                                )
//                            )
//                        )
//                    )

                    isLoadexams(response.data)
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                } else {
                    Constant.hideLoading(this)
                    showErrorUI(response.message ?: getString(R.string.no_data_available))
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                }
            }
        }

        appViewModel?.getexamslist?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                return@observe
            }
            if (response.status) {
                isLoadExamList(response.data)
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            } else {
                showErrorUI(response.message ?: getString(R.string.no_data_available))
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            }
        }

        appViewModel?.getProgressMarks?.observe(this) { response ->
            if (response != null) {

                if (response == null || !response.status || response.data.isNullOrEmpty()) {
                    Toast.makeText(this, response!!.message, Toast.LENGTH_SHORT).show()
                    return@observe

                }

                if (response.status) {
                    Constant.commonFileList.isEmpty()
                    Constant.commonFileList.clear()
                    Constant.commonFileList.add(
                        CommonFileData(
                            type = Constant.PDF,
                            path = response.data[0]
                        )
                    )
                    Constant.selectedFileIndex = 0
                    Log.d("File", Constant.commonFileList.toString())
                    Log.d("FileSize", Constant.commonFileList.size.toString())
                    val intent = Intent(this, FilesViewActivity::class.java)
                    intent.putExtra(Constant.subjectName, examTitle)
                    this.startActivity(intent)
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        fetchexamtimetable()

        binding.lnrTabOneName.setOnClickListener {
            if (currentTab == TabType.EXAM_TIMETABLE) return@setOnClickListener
            currentTab = TabType.EXAM_TIMETABLE
            binding.txtVideoMenu1.text.clear()
            binding.line1.setBackgroundResource(R.color.PrimaryColor)
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.PrimaryColor))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.line2.setBackgroundResource(R.color.white)
            binding.exammarkrecyclerview.visibility = View.GONE
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rytSearch1.visibility = View.GONE
            binding.rcExamTimeTable.visibility = View.VISIBLE
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.txtVideoMenu1.windowToken, 0)
            fetchexamtimetable()
        }


        binding.lnrTabTwoName.setOnClickListener {
            if (currentTab == TabType.EXAM_MARKS) return@setOnClickListener
            currentTab = TabType.EXAM_MARKS
            binding.txtVideoMenu1.text.clear()
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.PrimaryColor))
            binding.line2.setBackgroundResource(R.color.PrimaryColor)
            binding.line1.setBackgroundResource(R.color.white)
            binding.rcExamTimeTable.visibility = View.GONE
            binding.nomessage.visibility = View.GONE
            binding.rytSearch1.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.exammarkrecyclerview.visibility = View.VISIBLE
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.txtVideoMenu1.windowToken, 0)
            fetchexammark()
        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_notices_found)

            // Hide both RecyclerViews first
            binding.exammarkrecyclerview.visibility = View.GONE
            binding.rcExamTimeTable.visibility = View.GONE

        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE

            // Show only the current tab's RecyclerView
            when (currentTab) {
                TabType.EXAM_MARKS -> {
                    binding.exammarkrecyclerview.visibility = View.VISIBLE
                    binding.rcExamTimeTable.visibility = View.GONE
                }

                TabType.EXAM_TIMETABLE -> {
                    binding.rcExamTimeTable.visibility = View.VISIBLE
                    binding.exammarkrecyclerview.visibility = View.GONE
                }
            }
        }
    }

    override fun onExamSelected(examid: String, examName: String) {

        val jsonObject = JsonObject().apply {
            addProperty("report_id", 105) // or actual report ID
            addProperty("section_id", "91746")
            addProperty("standard_id", "8")
            addProperty("type", "term")
        }

        appViewModel?.getProgressMarks(isAccessToken ?: "",jsonObject , this)
        examTitle = examName
    }

    override fun onRubricClick(rubric: ExamTimetableRubric) {
    }


    private fun isLoadExamList(data: List<ExamDataRewamp>) {
        if (data.isNullOrEmpty()) {
            showErrorUI(getString(R.string.no_staff_data_available))
            return
        }
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.exammarkrecyclerview.layoutManager = GridLayoutManager(this, 2)
        exammarkadapter = ExamMarkAdapter(data, this, this, false)
        binding.exammarkrecyclerview.adapter = exammarkadapter
    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcExamTimeTable.visibility = View.GONE
    }

    private fun fetchexamtimetable() {
        Constant.showLoading(this)
        appViewModel?.getexams(
            isAccessToken ?: "", this
        )
    }

    private fun fetchexammark() {
        appViewModel?.getexamslist(
            isAccessToken ?: "", this
        )
    }


    private fun isLoadexams(data: List<ExamTimetable>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI(getString(R.string.no_exam_mark_data_available))
            return
        }
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE

        binding.rcExamTimeTable.apply {
            layoutManager = LinearLayoutManager(this@ExamMark)
            examAdapter = ExamTimeTableAdapter(data, this@ExamMark, this@ExamMark)
            binding.rcExamTimeTable.adapter = examAdapter
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}




//
//package com.vs.schoolmessenger.Dashboard.Parent
//
//import android.content.Intent
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.view.View
//import android.view.inputmethod.InputMethodManager
//import android.widget.Toast
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.gson.JsonObject
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
//import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
//import com.vs.schoolmessenger.CommonScreens.CommonFileData
//import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
//import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
//import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableAdapter
//import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Repository.APIKeyNames
//import com.vs.schoolmessenger.Repository.App
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.SharedPreference
//import com.vs.schoolmessenger.databinding.ExamMarkBinding
//
//class ExamMark : BaseActivity<ExamMarkBinding>(), View.OnClickListener, ExamMarkListener {
//    override fun getViewBinding(): ExamMarkBinding {
//        return ExamMarkBinding.inflate(layoutInflater)
//    }
//
//    private lateinit var examAdapter: ExamTimeTableAdapter
//    private lateinit var exammarkadapter: ExamMarkAdapter
//
//    private var isAccessToken: String? = null
//    private var appViewModel: App? = null
//
//    private var msg_id: Int = -1
//    private var headerId: String? = null
//    private var receiverId: String? = null
//    private var menu_name: String? = null
//    private var fromNotification: Boolean = false
//    var userDetails: UserDetails? = null
//
//
//    var examTitle = ""
//
//    private var currentTab = TabType.EXAM_TIMETABLE
//
//    private enum class TabType {
//        EXAM_MARKS, EXAM_TIMETABLE
//    }
//
//    override fun setupViews() {
//        super.setupViews()
//        isToolBarPrimaryParent(
//            mainViewId = R.id.main,
//            statusBarBgView = binding.statusBarBackground
//        )
//
//        appViewModel = ViewModelProvider(this).get(App::class.java)
//        appViewModel?.init()
//
//        userDetails = SharedPreference.getUserDetails(this)
//        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)
//
//        if (fromNotification) {
//            Constant.isParentChoose = true
//            msg_id = intent.getIntExtra(Constant.msg_id, -1)
//            headerId = intent.getStringExtra(Constant.header_id)
//            receiverId = intent.getStringExtra(Constant.receiverid)
//            menu_name = intent.getStringExtra(Constant.menu_name)
//
//            Log.d(
//                "NoticeBoard_EXTRAS",
//                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
//            )
//
//            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
//            SharedPreference.putChildDetails(this, matchedChild!!)
//            Constant.isSelectedMenuName = menu_name!!
//        }
//
//
//        val isChildDetails = SharedPreference.getChildDetails(this)
//        isAccessToken = isChildDetails?.access_token
//
//
//        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
//            if (binding.rytSearch1.visibility == View.VISIBLE) {
//                binding.rytSearch1.visibility = View.GONE
//            } else {
//                binding.rytSearch1.visibility = View.VISIBLE
//                binding.txtVideoMenu1.text.clear()
//            }
//        }
//
//
//        binding.root.post {
//            val finalName =
//                Constant.isSelectedMenuName?.takeIf { it.isNotEmpty() } ?: menu_name ?: ""
//            Log.d("NoticeBoard_HeaderFinal", "Setting headerview text: $finalName")
//            binding.lblHeaderTitle.text = finalName
//            binding.lblHeaderTitle.visibility = View.VISIBLE
//        }
//
//
//        binding.toolbarLayout.apply {
//            imgBack.setOnClickListener(this@ExamMark)
//            lblStudentName.text = isChildDetails!!.name
//            lblStudentSection.text =
//                "${isChildDetails.standard_name} - ${isChildDetails.section_name}"
//        }
//        binding.tabOneName.text = getString(R.string.exam_timetable)
//        binding.tabTwoName.text = getString(R.string.exam_marks)
//
//        binding.txtVideoMenu1.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                val query = s?.toString() ?: ""
//                when (currentTab) {
//                    TabType.EXAM_MARKS -> {
//                        if (::exammarkadapter.isInitialized) {
//                            exammarkadapter.filter.filter(query)
//                            Log.d("query", query)
//                        }
//                    }
//
//                    TabType.EXAM_TIMETABLE -> {
//                        if (::examAdapter.isInitialized) {
//                            examAdapter.filter.filter(query)
//                            Log.d("query", query)
//                        }
//                    }
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })
//
//
//        appViewModel?.getexams?.observe(this) { response ->
//            Log.d("response++", response.toString())
//            if (response != null) {
//                if (response == null || !response.status || response.data.isNullOrEmpty()) {
//
//                    val mobileNumber = SharedPreference.getMobileNumber(this)
//                    val jsonObject = JsonObject().apply {
//                        addProperty(APIKeyNames.mobile_number, mobileNumber)
//                        addProperty(APIKeyNames.activity, Constant.add_points_view_exam_schedule)
//                        addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
//                        addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
//                    }
//                    appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)
//                    Constant.hideLoading(this)
//                    showErrorUI(response?.message ?: getString(R.string.no_data_available))
//                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//                    return@observe
//                }
//                if (response.status) {
//                    Constant.hideLoading(this)
//                    isLoadexams(response.data)
//                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
//                } else {
//                    Constant.hideLoading(this)
//                    showErrorUI(response.message ?: getString(R.string.no_data_available))
//                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//
//                }
//            }
//        }
//
//        appViewModel?.getexamslist?.observe(this) { response ->
//            Log.d("response++", response.toString())
//            if (response == null) {
//                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
//                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//                return@observe
//            }
//            if (response.status) {
//                isLoadExamList(response.data)
//                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
//            } else {
//                showErrorUI(response.message ?: getString(R.string.no_data_available))
//                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//            }
//        }
//
//        appViewModel?.getProgressMarks?.observe(this) { response ->
//            if (response != null) {
//
//                if (response == null || !response.status || response.data.isNullOrEmpty()) {
//                    Toast.makeText(this, response!!.message, Toast.LENGTH_SHORT).show()
//                    return@observe
//
//                }
//
//                if (response.status) {
//                    Constant.commonFileList.isEmpty()
//                    Constant.commonFileList.clear()
//                    Constant.commonFileList.add(
//                        CommonFileData(
//                            type = Constant.PDF,
//                            path = response.data[0]
//                        )
//                    )
//                    Constant.selectedFileIndex = 0
//                    Log.d("File", Constant.commonFileList.toString())
//                    Log.d("FileSize", Constant.commonFileList.size.toString())
//                    val intent = Intent(this, FilesViewActivity::class.java)
//                    intent.putExtra(Constant.subjectName, examTitle)
//                    this.startActivity(intent)
//                } else {
//                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        fetchexamtimetable()
//
//        binding.lnrTabOneName.setOnClickListener {
//            if (currentTab == TabType.EXAM_TIMETABLE) return@setOnClickListener
//            currentTab = TabType.EXAM_TIMETABLE
//            binding.txtVideoMenu1.text.clear()
//            binding.line1.setBackgroundResource(R.color.PrimaryColor)
//            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.PrimaryColor))
//            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
//            binding.line2.setBackgroundResource(R.color.white)
//            binding.exammarkrecyclerview.visibility = View.GONE
//            binding.nomessage.visibility = View.GONE
//            binding.txtNoData.visibility = View.GONE
//            binding.rytSearch1.visibility = View.GONE
//            binding.rcExamTimeTable.visibility = View.VISIBLE
//            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(binding.txtVideoMenu1.windowToken, 0)
//            fetchexamtimetable()
//        }
//
//
//        binding.lnrTabTwoName.setOnClickListener {
//            if (currentTab == TabType.EXAM_MARKS) return@setOnClickListener
//            currentTab = TabType.EXAM_MARKS
//            binding.txtVideoMenu1.text.clear()
//            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
//            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.PrimaryColor))
//            binding.line2.setBackgroundResource(R.color.PrimaryColor)
//            binding.line1.setBackgroundResource(R.color.white)
//            binding.rcExamTimeTable.visibility = View.GONE
//            binding.nomessage.visibility = View.GONE
//            binding.rytSearch1.visibility = View.GONE
//            binding.txtNoData.visibility = View.GONE
//            binding.exammarkrecyclerview.visibility = View.VISIBLE
//            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(binding.txtVideoMenu1.windowToken, 0)
//            fetchexammark()
//        }
//    }
//
//    override fun onSearchResultEmpty(isEmpty: Boolean) {
//        if (isEmpty) {
//            binding.nomessage.visibility = View.VISIBLE
//            binding.txtNoData.visibility = View.VISIBLE
//            binding.txtNoData.text = getString(R.string.no_matching_notices_found)
//
//            // Hide both RecyclerViews first
//            binding.exammarkrecyclerview.visibility = View.GONE
//            binding.rcExamTimeTable.visibility = View.GONE
//
//        } else {
//            binding.nomessage.visibility = View.GONE
//            binding.txtNoData.visibility = View.GONE
//
//            // Show only the current tab's RecyclerView
//            when (currentTab) {
//                TabType.EXAM_MARKS -> {
//                    binding.exammarkrecyclerview.visibility = View.VISIBLE
//                    binding.rcExamTimeTable.visibility = View.GONE
//                }
//
//                TabType.EXAM_TIMETABLE -> {
//                    binding.rcExamTimeTable.visibility = View.VISIBLE
//                    binding.exammarkrecyclerview.visibility = View.GONE
//                }
//            }
//        }
//    }
//
//    override fun onExamSelected(examid: String, examName: String) {
//        appViewModel?.getProgressMarks(isAccessToken ?: "", examid, this)
//        examTitle = examName
//    }
//
//
//    private fun isLoadExamList(data: List<ExamData>) {
//        if (data.isNullOrEmpty()) {
//            showErrorUI(getString(R.string.no_staff_data_available))
//            return
//        }
//        binding.nomessage.visibility = View.GONE
//        binding.txtNoData.visibility = View.GONE
//        binding.exammarkrecyclerview.layoutManager = GridLayoutManager(this, 2)
//        exammarkadapter = ExamMarkAdapter(data, this, this, false)
//        binding.exammarkrecyclerview.adapter = exammarkadapter
//    }
//
//    private fun showErrorUI(message: String) {
//        binding.nomessage.visibility = View.VISIBLE
//        binding.txtNoData.text = message
//        binding.txtNoData.visibility = View.VISIBLE
//        binding.rcExamTimeTable.visibility = View.GONE
//    }
//
//    private fun fetchexamtimetable() {
//        Constant.showLoading(this)
//        appViewModel?.getexams(
//            isAccessToken ?: "", this
//        )
//    }
//
//    private fun fetchexammark() {
//        appViewModel?.getexamslist(
//            isAccessToken ?: "", this
//        )
//    }
//
//
//    private fun isLoadexams(data: List<ExamData>?) {
//        if (data.isNullOrEmpty()) {
//            showErrorUI(getString(R.string.no_exam_mark_data_available))
//            return
//        }
//        binding.nomessage.visibility = View.GONE
//        binding.txtNoData.visibility = View.GONE
//
//        binding.rcExamTimeTable.apply {
//            layoutManager = LinearLayoutManager(this@ExamMark)
//            examAdapter = ExamTimeTableAdapter(data, this@ExamMark, this@ExamMark)
//            binding.rcExamTimeTable.adapter = examAdapter
//        }
//    }
//
//
//    override fun onClick(p0: View?) {
//        when (p0?.id) {
//            R.id.imgBack -> {
//                onBackPressed()
//            }
//        }
//    }
//
//    override fun onBackPressed() {
//        super.onBackPressed()
//        val intent = Intent(this, ParentDashboard::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//        finish()
//    }
//}
//
