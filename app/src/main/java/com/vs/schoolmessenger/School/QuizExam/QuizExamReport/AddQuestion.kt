package com.vs.schoolmessenger.School.QuizExam.QuizExamReport

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.AddQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AddQuestionBinding


class AddQuestion : BaseActivity<AddQuestionBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): AddQuestionBinding {
        return AddQuestionBinding.inflate(layoutInflater)
    }
    var isQuizID=""
    var isQuestionLimit=-1
    var isQuizTitle=""
    var isSubjectID=""
    private lateinit var savedQuizQuestionReportList: List<GetQuizQuestionReportData>
    private lateinit var editableQuizQuestionReportList: MutableList<GetQuizQuestionReportData>

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: AddQuestionAdapter



    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        isQuestionLimit = intent.getIntExtra("limitQuestion", -1)
        isQuizID = intent.getStringExtra("quiz_Id").toString()
        isSubjectID = intent.getStringExtra("subjectID").toString()
        isQuizTitle = intent.getStringExtra("quiz_Title").toString()
        binding.toolbarLayout.lblParentToolBar.text=isQuizTitle

        appViewModel?.isGetQuizQuestionReport?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    savedQuizQuestionReportList=response.data
                    editableQuizQuestionReportList = savedQuizQuestionReportList.map { it.copy() }.toMutableList()
                    isLoadQuizQuestionReport()
                }
                else {
                    Constant.showErrorAlert(
                        this,
                        getString(R.string.alert),
                        response.message
                    )
                    savedQuizQuestionReportList=response.data
                    editableQuizQuestionReportList = savedQuizQuestionReportList.map { it.copy() }.toMutableList()
                    isLoadQuizQuestionReport()
                }
            } else {
                Constant.showErrorAlert(
                    this,
                    getString(R.string.fail),
                    getString(R.string.Something_went_wrong_Please_try_again)
                )
            }
        }

        isFetchQuizQuestionReport()

    }

    private fun isLoadQuizQuestionReport() {
        adapter = AddQuestionAdapter(editableQuizQuestionReportList.toMutableList(), this,false)
        binding.rcAddQuestion.layoutManager = LinearLayoutManager(this)
        binding.rcAddQuestion.isNestedScrollingEnabled = false
        binding.rcAddQuestion.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.rcAddQuestion.adapter = adapter

        // Add an empty item only if list is empty or has 0/1 item
        if (editableQuizQuestionReportList.size<=0) {
            adapter.addItem()
        }
        binding.lblAddQuestion.setOnClickListener {
            if (adapter.showValidationErrors(binding.rcAddQuestion)) {
                adapter.addItem()
            }
        }

    }

    private fun isFetchQuizQuestionReport() {
        adapter = AddQuestionAdapter(null, this,true)
        binding.rcAddQuestion.layoutManager = LinearLayoutManager(this)
        binding.rcAddQuestion.adapter = adapter

        appViewModel?.isGetQuizQuestionReport(isAccessToken ?: "", isQuizID)
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }
}