package com.vs.schoolmessenger.Parent.ParentClassTestExamAnalysis.ParentExamselection

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Parent.ParentClassTestExamAnalysis.ParentExamAnalysis.ParentExamAnalysisActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamReview.AnalysisSetResponseModel.AnalysisSet
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentSelectExamReviewBinding

class ParentSelectExamActivity : BaseActivity<ParentSelectExamReviewBinding>(), View.OnClickListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var userDetails: UserDetails? = null

    private lateinit var examAdapter: ParentAnalysisSetAdapter
    private var currentSets: List<AnalysisSet> = emptyList()

    override fun getViewBinding(): ParentSelectExamReviewBinding =
        ParentSelectExamReviewBinding.inflate(layoutInflater)

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnChangeStudent.setOnClickListener(this)
        binding.btnViewAnalysis.setOnClickListener(this)
        binding.toolbarLayout.lblStudentName.text = "Class Set Analysis"
        binding.toolbarLayout.lblStudentSection.visibility=View.GONE


//        bindSelectedStudent()
        setupExamList()
        observeAnalysisSets()
        fetchAnalysisSets()
    }

//    private fun bindSelectedStudent() {
//        val student = Constant.isSelectedStudent
//        val studentName = student?.name?.takeIf { it.isNotBlank() } ?: "-"
//        val standardName = Constant.isSelectedStandardName
//        val sectionName = Constant.isSelectedSections.firstOrNull()?.sectionName
//
//        binding.lblStudentNameValue.text = studentName
//        binding.lblStudentAvatarLetter.text =
//            studentName.filter { it.isLetter() }.take(2).ifBlank { "--" }.uppercase()
//
//        val classSection = if (!standardName.isNullOrBlank() && !sectionName.isNullOrBlank()) {
//            "Class $standardName - Section $sectionName"
//        } else {
//            "-"
//        }
//        binding.lblStudentDetail.text = classSection
//    }

    private fun setupExamList() {
        // No "select all" for a single-choice list
        binding.btnSelectAll.visibility = View.GONE

        examAdapter = ParentAnalysisSetAdapter(
            itemList = emptyList()
        ) { selectedSet ->
            updateSelectionState(selectedSet)
        }

        binding.rcExamList.apply {
            layoutManager = LinearLayoutManager(this@ParentSelectExamActivity)
            adapter = examAdapter
        }

        updateSelectionState(null)
    }

    private fun fetchAnalysisSets() {
        val token = isAccessToken
        if (token.isNullOrEmpty()) {
            showNoExamData("Missing student access details")
            return
        }
        appViewModel!!.isExamtestAnalysisSets(token, "", "", this)
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
        binding.lytSelectExamsHeader.visibility = View.VISIBLE
    }

    private fun showNoExamData(message: String?) {
        binding.rcExamList.visibility = View.GONE
        binding.lytNoExamData.visibility = View.VISIBLE
        binding.lytSelectExamsHeader.visibility = View.GONE
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
                startActivity(Intent(this, ParentExamAnalysisActivity::class.java))
            }
        }
    }
}