package com.vs.schoolmessenger.School.QuizExam.QuizExamReport

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.AddQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.PickQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.Model.PickFromQuestionBank.GetPickFromQBankData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.QuestionSource
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
    var isFirstClick=true
    private lateinit var savedQuizQuestionReportList: List<GetQuizQuestionReportData>
    private lateinit var pickQBankList: List<GetPickFromQBankData>
    private lateinit var editableQuizQuestionReportList: MutableList<GetQuizQuestionReportData>

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: AddQuestionAdapter
    private lateinit var adapter2: PickQuestionAdapter



    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblParentToolBar.setOnClickListener{
            val FinalList=adapter.getUpdatedList()
            Log.d("FinalList",FinalList.toString())
        }
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        isQuestionLimit = intent.getIntExtra("limitQuestion", -1)
        Log.d("isQuestionLimit",isQuestionLimit.toString())

        isQuizID = intent.getStringExtra("quiz_Id").toString()
        isSubjectID = intent.getStringExtra("subjectID").toString()
        isQuizTitle = intent.getStringExtra("quiz_Title").toString()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblImportQuestion.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text=isQuizTitle

        appViewModel?.isGetQuizQuestionReport?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    savedQuizQuestionReportList = response.data

                    // Mark all as API type
                    editableQuizQuestionReportList = savedQuizQuestionReportList.map { it.copy(sourceType = QuestionSource.API)
                    }.toMutableList()

                    isLoadQuizQuestionReport()
                }
                else {
                    Constant.showErrorAlert(
                        this,
                        getString(R.string.alert),
                        response.message
                    )

                    savedQuizQuestionReportList = response.data
                    editableQuizQuestionReportList = savedQuizQuestionReportList.map {it.copy(sourceType = QuestionSource.API)
                    }.toMutableList()
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

        appViewModel?.isGetPickFromQBank?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this)
                    pickQBankList = response.data.map { it.copy(checked = false) }
                    showResumeListDialog(this, pickQBankList)
                }
                else {
                    Constant.hideLoading(this)
                    Constant.showErrorAlert(
                        this,
                        getString(R.string.alert),
                        response.message
                    )
                }
            } else {
                Constant.hideLoading(this)
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
        binding.rcAddQuestion.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcAddQuestion.isNestedScrollingEnabled = false
        binding.rcAddQuestion.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.rcAddQuestion.adapter = adapter

        adapter.onQBankItemRemoved = { removedId ->
            adapter2.uncheckItemById(removedId)
        }

        // Add an empty item only if list is empty or has 0/1 item
        if (editableQuizQuestionReportList.size<=0) {
            adapter.addItem()
        }
        binding.lblAddQuestion.setOnClickListener {
            if (adapter.showValidationErrors(binding.rcAddQuestion)) {

                if (isQuestionLimit<adapter.getUpdatedList().size){
                    adapter.addItem()
                    isQuestionLimit+=1
                }
                else{
                    Constant.showErrorAlert(
                        this,
                        getString(R.string.alert),
                        getString(R.string.question_limit_reached)
                    )
                }
            }
        }

    }

    private fun isFetchQuizQuestionReport() {
        adapter = AddQuestionAdapter(null, this,true)
//        binding.rcAddQuestion.layoutManager = LinearLayoutManager(this)
        binding.rcAddQuestion.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcAddQuestion.isNestedScrollingEnabled = false
        binding.rcAddQuestion.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.rcAddQuestion.adapter = adapter

        appViewModel?.isGetQuizQuestionReport(isAccessToken ?: "", isQuizID)
    }

    private fun isFetchFromQuestionBank(){
        appViewModel?.isGetPickFromQBank(isAccessToken ?: "", isSubjectID)
    }

    fun showResumeListDialog(
        activity: Activity,
        pickFomQbank: List<GetPickFromQBankData>
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val dialogView = LayoutInflater.from(activity).inflate(R.layout.pick_question_from_qbank, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Check again before showing
        if (!activity.isFinishing && !activity.isDestroyed) {
            alertDialog.show()
        }

        val lblClose = dialogView.findViewById<TextView>(R.id.lblClose)
        val lblImportQuestion = dialogView.findViewById<TextView>(R.id.lblImportQuestion)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcPickFromQBank)
        val cbSelect = dialogView.findViewById<CheckBox>(R.id.cbSelect)

        recyclerView.layoutManager = LinearLayoutManager(activity)
//        adapter2 = PickQuestionAdapter(pickFomQbank.toMutableList(), activity, false){ isChecked ->
////            if (!isChecked && cbSelect.isChecked) {
////                cbSelect.isChecked = false
////            }
////            if (adapter2.getSelectedQuestions().size == pickFomQbank.size) {
////                cbSelect.isChecked = true
////            }
//        }

//        adapter2 = PickQuestionAdapter(pickFomQbank.toMutableList(), activity, false) {
//            // Called whenever an item’s checkbox changes
//            val total = pickFomQbank.size
//            val selected = adapter2.getSelectedQuestions().size
//
//            cbSelect.setOnCheckedChangeListener(null) // avoid recursion
//            cbSelect.isChecked = (selected == total && total > 0)
//            cbSelect.setOnCheckedChangeListener { _, isChecked ->
//                adapter2.selectAll(isChecked)
//            }
//        }

        adapter2 = PickQuestionAdapter(pickFomQbank.toMutableList(), activity, false) {
            val total = pickFomQbank.size
            val selected = adapter2.getSelectedQuestions().size

            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = (selected == total && total > 0)
            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                adapter2.selectAll(isChecked)
            }
        }


        recyclerView.adapter = adapter2

//        lblImportQuestion.setOnClickListener {
//            val selectedQuestions = adapter2.getSelectedQuestions()
//            // Convert to AddQuestionAdapter model
//            val quizQuestions = selectedQuestions.map { it.toQuizQuestionReportData() }
//            adapter.updateItems(quizQuestions)
//            alertDialog.dismiss()
//        }

        lblImportQuestion.setOnClickListener {

            if (isQuestionLimit<adapter.getUpdatedList().size) {
                val selectedQuestions = adapter2.getSelectedQuestions()
                val quizQuestions = selectedQuestions.map {
                    it.toQuizQuestionReportData().copy(sourceType = QuestionSource.QBANK)
                }
                adapter.updateItems(quizQuestions)
                alertDialog.dismiss()
            }
            else{
                Constant.showErrorAlert(
                    this,
                    getString(R.string.alert),
                    getString(R.string.question_limit_reached)
                )

            }
        }


        lblClose.setOnClickListener {
            alertDialog.dismiss()
        }

        cbSelect.setOnCheckedChangeListener { _, isChecked ->
            adapter2.selectAll(isChecked)
        }

    }

    fun GetPickFromQBankData.toQuizQuestionReportData(): GetQuizQuestionReportData {
        return GetQuizQuestionReportData(
            id = this.id,
            quiz_id = "",
            question = this.question,
            chapter = this.chapter,
            answer = this.answer,
            a_option = this.a_option,
            b_option = this.b_option,
            c_option = this.c_option,
            d_option = this.d_option,
            mark = this.mark,
            option_a_counts = 0,
            option_b_counts = 0,
            option_c_counts = 0,
            option_d_counts = 0,
            correct_answer_counts = 0,
            incorrect_answer_counts = 0,
            correct_answer = this.answer
        )
    }



    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.lblImportQuestion->{
                if(isFirstClick){
                    Constant.showLoading(this)
                    isFetchFromQuestionBank()
                    isFirstClick=false
                }
                else{
                    showResumeListDialog(this, pickQBankList)
                }

            }


        }
    }
}