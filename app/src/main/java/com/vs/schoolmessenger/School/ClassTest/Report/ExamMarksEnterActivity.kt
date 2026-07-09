package com.vs.schoolmessenger.School.ClassTest.Report

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ClassTest.Class.Models.ClassTestItem
import com.vs.schoolmessenger.School.ClassTest.Class.Models.TestEntry
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ExamMarksEnterBinding

import android.app.AlertDialog
import android.content.Intent
import com.google.gson.JsonObject
import com.vs.schoolmessenger.School.ClassTest.UploadMarks.ClassUploadMarks

import com.vs.schoolmessenger.Utils.SharedPreference

class ExamMarksEnterActivity : BaseActivity<ExamMarksEnterBinding>() {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var adapter: ExamMarksEnterAdapter

    private var pendingDeleteItemPos: Int = -1
    private var pendingDeleteTestIndex: Int = -1

    override fun getViewBinding(): ExamMarksEnterBinding {
        return ExamMarksEnterBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = "View Test & Enter marks"

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token

        loadSubjectData()
        setupContinueButton()
        observeDeleteResponse()
        observeRefreshResponse()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun observeDeleteResponse() {
        appViewModel!!.isputClassTestDelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    showMessageDialog(response.message ?: "Deleted successfully") {
                        refreshExamData()
                    }
                } else {
                    showMessageDialog(
                        response.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
                pendingDeleteItemPos = -1
                pendingDeleteTestIndex = -1
            }
        }
    }

    private fun refreshExamData() {
        Constant.showLoading(this)
        appViewModel!!.isgetExamreportdetails(
            isToken = isAccessToken ?: "",
            class_test_id = Constant.isSelectedClassTestId,
            exam_date = "0",
            academic_year_id = Constant.isUploadMarksSelectedAcademicID,
            this
        )
    }

    private fun observeRefreshResponse() {
        appViewModel!!.isgetExamreportdetails?.observe(this) { response ->
            Constant.hideLoading(this)

            if (response == null || !response.status || response.data.isNullOrEmpty()) {
                return@observe
            }

            val matchedExam = response.data.firstOrNull {
                it.classTestId == Constant.isSelectedClassTestId
            } ?: return@observe

            val matchedSection = matchedExam.sections?.firstOrNull {
                it.sectionId == Constant.isSelectedSectionId
            } ?: return@observe

            Constant.isExamReportSubjects = matchedSection.subjects

            rebuildItemsFromSubjects(matchedSection.subjects)
        }
    }

    private fun rebuildItemsFromSubjects(subjects: List<com.vs.schoolmessenger.School.ClassTest.Report.Model.SubjectModeldata>) {
        if (subjects.isNullOrEmpty()) {
            showError("No subjects found for this section")
            return
        }

        val sectionLabel = "Section ${Constant.isSelectedSectionName}"

        val items = subjects.map { subject ->
            ClassTestItem(
                subjectId = subject.subjectId,
                subjectName = subject.subjectName,
                sectionLabel = sectionLabel,
                isExpanded = false,
                tests = subject.activities.map { activity ->
                    TestEntry(
                        examName = activity.activityName,
                        testDate = activity.examDate,
                        session = activity.session,
                        maxMarks = activity.maxMark,
                        minMarks = activity.minMark,
                        syllabus = activity.syllabus,
                        classTestSubjectId = activity.classTestSubjectId,
                        canDelete = activity.candelete
                    )
                }.toMutableList(),
                sectionId = Constant.isSelectedSectionId,
                isMerged = false,
                mergedSections = mutableListOf(),
                mergedSectionIds = mutableListOf()
            )
        }

        if (::adapter.isInitialized) {
            adapter.syncItems(items)
            binding.rcClassList.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        } else {
            buildAdapter(items)
        }
    }

    private fun onDeleteTestClicked(
        item: ClassTestItem,
        test: TestEntry,
        testIndex: Int,
        itemPos: Int
    ) {
        val activityId = test.classTestSubjectId

        if (activityId.isNullOrBlank()) {
            adapter.removeTestAt(itemPos, testIndex)
            return
        }

        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete this activity?")
            .setCancelable(true)
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                pendingDeleteItemPos = itemPos
                pendingDeleteTestIndex = testIndex

                val jsonObject = JsonObject().apply {
                    addProperty("class_test_subject_id", activityId)
                }

                appViewModel!!.isputClassTestDelete(
                    isToken = isAccessToken ?: "",
                    jsonObject = jsonObject,
                    this
                )
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showMessageDialog(message: String, onOkClick: (() -> Unit)? = null) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onOkClick?.invoke()
            }
            .show()
    }

    private fun loadSubjectData() {
        binding.examHeader.text = Constant.isExamName

        val subjects = Constant.isExamReportSubjects
        if (subjects.isNullOrEmpty()) {
            showError("No subjects found for this section")
            return
        }

        val sectionLabel = "Section ${Constant.isSelectedSectionName}"

        val items = subjects.map { subject ->
            ClassTestItem(
                subjectId = subject.subjectId,
                subjectName = subject.subjectName,
                sectionLabel = sectionLabel,
                isExpanded = false,
                tests = subject.activities.map { activity ->
                    TestEntry(
                        examName = activity.activityName,
                        testDate = activity.examDate,
                        session = activity.session,
                        maxMarks = activity.maxMark,
                        minMarks = activity.minMark,
                        syllabus = activity.syllabus,
                        classTestSubjectId = activity.classTestSubjectId,
                        canDelete = activity.candelete
                    )
                }.toMutableList(),
                sectionId = Constant.isSelectedSectionId,
                isMerged = false,
                mergedSections = mutableListOf(),
                mergedSectionIds = mutableListOf()
            )
        }

        buildAdapter(items)
    }

    private fun buildAdapter(items: List<ClassTestItem>) {
        items.firstOrNull()?.isExpanded = true

        adapter = ExamMarksEnterAdapter(items.toMutableList()) { item, test, testIndex, itemPos ->
            onDeleteTestClicked(item, test, testIndex, itemPos)
        }
        binding.rcClassList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ExamMarksEnterActivity.adapter
        }
        binding.rcClassList.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    private fun setupContinueButton() {
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnContinue.setOnClickListener {
            val classTestSubjectIds = getAllClassTestSubjectIds()
            val globalexamname = Constant.isExamName

            val intent = Intent(this, ClassUploadMarks::class.java).apply {
                putExtra(Constant.CLASS_TEST_ID, Constant.isSelectedClassTestId)
                putExtra(Constant.SECTION_ID, Constant.isSelectedSectionId)
                putExtra(Constant.CLASS_TEST_SUBJECT_ID, classTestSubjectIds)
                putExtra(Constant.isExamName, globalexamname)
            }
            startActivity(intent)
        }
    }

    private fun getAllClassTestSubjectIds(): String {
        val subjects = Constant.isExamReportSubjects ?: return ""

        return subjects
            .flatMap { it.activities }
            .mapNotNull { it.classTestSubjectId }
            .filter { it.isNotBlank() }
            .joinToString(",")
    }

    private fun showError(message: String) {
        binding.rcClassList.visibility = View.GONE
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = message
    }
}