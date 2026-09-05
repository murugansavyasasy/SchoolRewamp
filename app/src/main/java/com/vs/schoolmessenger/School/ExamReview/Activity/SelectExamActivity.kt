package com.vs.schoolmessenger.School.ExamReview.Activity

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamReview.Adapter.AnalysisSetAdapter
import com.vs.schoolmessenger.School.ExamReview.AnalysisSetResponseModel.AnalysisSet
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SelectExamReveiwBinding

class SelectExamActivity : BaseActivity<SelectExamReveiwBinding>(), View.OnClickListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var userDetails: UserDetails? = null

    private lateinit var examAdapter: AnalysisSetAdapter
    private var currentSets: List<AnalysisSet> = emptyList()

    override fun getViewBinding(): SelectExamReveiwBinding =
        SelectExamReveiwBinding.inflate(layoutInflater)

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnChangeStudent.setOnClickListener(this)
        binding.btnViewAnalysis.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text =  getString(R.string.class_set_analysis)
        binding.toolbarLayout.lblSchoolName.visibility= View.GONE


        bindSelectedStudent()
        setupExamList()
        observeAnalysisSets()
        fetchAnalysisSets()
    }

    private fun bindSelectedStudent() {
        val studentName = Constant.isSelectedStudent?.name?.takeIf { it.isNotBlank() } ?: "Student"
        val standardName = Constant.isSelectedStandardName
        val sectionName = Constant.isSelectedSections.firstOrNull()?.sectionName
        binding.lblStudentNameValue.text = studentName
        binding.lblStudentAvatarLetter.text =
            studentName.filter { it.isLetter() }.take(2).ifBlank { "--" }.uppercase()

        val classSection = if (!standardName.isNullOrBlank() && !sectionName.isNullOrBlank()) {
            "$standardName - $sectionName"
        } else {
            "-"
        }
        binding.lblStudentDetail.text = classSection
        binding.lblrollno.text ="Roll No : " + Constant.isSelectedStudent?.roll_no
        binding.lbladminno.text = "Admin No : " + Constant.isSelectedStudent?.admission_no
    }

    private fun setupExamList() {
        // No "select all" for a single-choice list
        binding.btnSelectAll.visibility = View.GONE

        examAdapter = AnalysisSetAdapter(
            itemList = emptyList()
        ) { selectedSet ->
            updateSelectionState(selectedSet)
        }

        binding.rcExamList.apply {
            layoutManager = LinearLayoutManager(this@SelectExamActivity)
            adapter = examAdapter
        }

        updateSelectionState(null)
    }

    private fun fetchAnalysisSets() {
        val classId = Constant.isSelectedStandardId
        val sectionId = Constant.isSelectedSections.firstOrNull()?.sectionId

        if (isAccessToken.isNullOrEmpty() || classId.isNullOrEmpty() || sectionId.isNullOrEmpty()) {
            showNoExamData("Missing class/section details")
            return
        }

        appViewModel!!.isExamtestAnalysisSets(isAccessToken!!, classId, sectionId, this)
    }

    private fun observeAnalysisSets() {
        appViewModel!!.isExamtestAnalysisSets?.observe(this) { response ->
            if (response != null && response.status && response.data.isNotEmpty()) {
                currentSets = response.data
                showExamList()
                examAdapter.updateList(currentSets)
                updateSelectionState(null)
            } else {
                showNoExamData(response?.message)
            }
        }
    }

    private fun showExamList() {
        binding.rcExamList.visibility = View.VISIBLE
        binding.lytNoExamData.visibility = View.GONE
    }

    private fun showNoExamData(message: String?) {
        binding.rcExamList.visibility = View.GONE
        binding.lytNoExamData.visibility = View.VISIBLE
        if (!message.isNullOrBlank()) {
            binding.lblNoExamDataSubtitle.text = message
        }
        currentSets = emptyList()
        examAdapter.updateList(emptyList())
        updateSelectionState(null)
    }

    private fun updateSelectionState(selected: AnalysisSet?) {
        binding.lblSelectionCount.text = selected?.setName?.let { "Selected: $it" }
            ?: "No test set selected"
        binding.btnViewAnalysis.isEnabled = selected != null
        binding.btnViewAnalysis.alpha = if (selected != null) 1f else 0.5f
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.toolbarLayout.imgBack.id, binding.btnBack.id -> onBackPressed()

            binding.btnChangeStudent.id -> onBackPressed()

            binding.btnViewAnalysis.id -> {
                val selectedSet = examAdapter.getSelectedItem() ?: return
                Constant.isSelectedAnalysisSetId = selectedSet.id
                startActivity(Intent(this, ExamAnalysisActivity::class.java))
            }
        }
    }
}