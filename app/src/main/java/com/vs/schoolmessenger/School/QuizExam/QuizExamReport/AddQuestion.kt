package com.vs.schoolmessenger.School.QuizExam.QuizExamReport

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
import com.google.gson.Gson
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.AddQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.PickQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.QuizQuestionRequest
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.QuizRequestBody
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.UpdateQBankItem
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
    var isQuizTitle=""
    var isSubjectID=""
    var isUpdatedQBankQuestions=-1
    var isSavedQuestionLimit=-1
    var isSubmittedCount=-1
    var isOkFlag=false
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
        Constant.isQuestionLimit = intent.getIntExtra("limitQuestion", -1)
        isSavedQuestionLimit= intent.getIntExtra("limitQuestion", -1)
        Log.d("isQuestionLimit",Constant.isQuestionLimit.toString())

        isSubmittedCount = intent.getIntExtra("submittedCount", -1)
        isQuizID = intent.getStringExtra("quiz_Id").toString()
        isSubjectID = intent.getStringExtra("subjectID").toString()
        isQuizTitle = intent.getStringExtra("quiz_Title").toString()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblImportQuestion.setOnClickListener(this)
        binding.lblSendQuiz.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text=isQuizTitle
        if (isSubmittedCount>0){
            isOkFlag=true
        }else{
            isOkFlag=false
        }

        appViewModel?.isGetQuizQuestionReport?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    savedQuizQuestionReportList = response.data

                    // Mark all as API type
                    editableQuizQuestionReportList = savedQuizQuestionReportList.map { it.copy(sourceType = QuestionSource.API)
                    }.toMutableList()
                    Constant.isQuestionLimit-=savedQuizQuestionReportList.size
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
                    Constant.isQuestionLimit-=savedQuizQuestionReportList.size
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

            appViewModel!!.isAddQuestion?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@AddQuestion)
                    Constant.showDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    Constant.hideLoading(this@AddQuestion)
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
            else {
                Constant.hideLoading(this@AddQuestion)
                Constant.showDataValidation(
                    resources.getString(R.string.fail),getString(R.string.Something_went_wrong_Please_try_again), this
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
        Log.d("QuestionLimitInAdapter", Constant.isQuestionLimit.toString())

        adapter = AddQuestionAdapter(editableQuizQuestionReportList.toMutableList(), this, false)
        binding.rcAddQuestion.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcAddQuestion.isNestedScrollingEnabled = false
        binding.rcAddQuestion.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.rcAddQuestion.adapter = adapter

        adapter.onQBankItemRemoved = { removedId ->
            adapter2.uncheckItemById(removedId)
        }

        // Add an empty item only if list is empty
        if (editableQuizQuestionReportList.size <= 0) {
            adapter.addItem()
        }

        binding.lblAddQuestion.setOnClickListener {
            if (adapter.showValidationErrors(binding.rcAddQuestion)) {
                Log.d("QuestionLimit", Constant.isQuestionLimit.toString())
                Log.d("FinalListSize", adapter.getUpdatedList().size.toString())
                if (Constant.isQuestionLimit > 0) {
                    adapter.addItem()
                }
                else {
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

        if (!activity.isFinishing && !activity.isDestroyed) {
            alertDialog.show()
        }

        val lblClose = dialogView.findViewById<TextView>(R.id.lblClose)
        val lblImportQuestion = dialogView.findViewById<TextView>(R.id.lblImportQuestion)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcPickFromQBank)
        val cbSelect = dialogView.findViewById<CheckBox>(R.id.cbSelect)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter2 = PickQuestionAdapter(pickFomQbank.toMutableList(), activity, false)

        cbSelect.isChecked = false // default

        adapter2.onSelectionChanged = { allSelected ->
            // Update cbSelect programmatically, but avoid infinite loop
            Log.d("allSelectedBefore",allSelected.toString())
            if (cbSelect.isChecked != allSelected) {
                Log.d("allSelectedAfter",allSelected.toString()+"AAAAAAAAAAAAAAAAAAAAAA")

                cbSelect.setOnCheckedChangeListener(null)
                cbSelect.isChecked = allSelected
                cbSelect.setOnCheckedChangeListener { _, isChecked ->
                    adapter2.selectAll(isChecked)
                }
            }
            Log.d("allSelectedFinal",allSelected.toString())


        }
        adapter2.notifySelectionChanged()


        cbSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val allQuestions = adapter2.getAllNotImported()
                val totalToSelect = allQuestions.size

                if (totalToSelect <= Constant.isQuestionLimit) {
                    // Within limit → mark all as imported
//                    Constant.isQuestionLimit -= totalToSelect
                    adapter2.markAsImported(allQuestions)
                    cbSelect.isChecked = true

                } else {

                    // Over limit → error
                    Constant.showErrorAlert(
                        this,
                        getString(R.string.alert),
                        getString(R.string.question_limit_reached)
                    )
                    cbSelect.isChecked = false
                }

                adapter2.notifySelectionChanged()
            } else {
                adapter2.clearSelections()
                adapter2.notifySelectionChanged()
            }
        }

        recyclerView.adapter = adapter2

        lblImportQuestion.setOnClickListener {
            val selectedQuestions = adapter2.getSelected()
            val selectedIds = selectedQuestions.map { it.id }.toSet()

            val currentAdded = adapter.getUpdatedList()
            val alreadyAddedIds = currentAdded.map { it.id }.toSet()

            // QBANK items currently present in AddQuestionAdapter
            val qbankAddedIds = currentAdded
                .filter { it.sourceType == QuestionSource.QBANK && it.id.isNotEmpty() }
                .map { it.id }

            // Items user just selected in dialog that are genuinely new to AddQuestionAdapter
            val newSelectedQuestions = selectedQuestions.filter { it.id !in alreadyAddedIds }

            // QBANK ids that were previously in AddQuestionAdapter but user deselected in the dialog now
            val deselectedIds = qbankAddedIds.filter { it !in selectedIds }

            // Effective available slots: current limit + slots freed by deselection
            val effectiveAvailableSlots = Constant.isQuestionLimit + deselectedIds.size

            Log.d("ImportDebug", "selected=${selectedIds.size}, newSelected=${newSelectedQuestions.size}, alreadyAdded=${alreadyAddedIds.size}, deselected=${deselectedIds.size}, effectiveSlots=$effectiveAvailableSlots")

            if (effectiveAvailableSlots <= 0) {
                Constant.showErrorAlert(
                    this,
                    getString(R.string.alert),
                    getString(R.string.question_limit_reached)
                )
                return@setOnClickListener
            }

            if (newSelectedQuestions.size <= effectiveAvailableSlots) {
                // convert to AddQuestion model
                val quizQuestions = newSelectedQuestions.map {
                    it.toQuizQuestionReportData().copy(sourceType = QuestionSource.QBANK)
                }

                // Remove only the deselected QBANK items (permanently)
                if (deselectedIds.isNotEmpty()) {
                    adapter.removeItemsByIds(deselectedIds)
                }

                // Add newly selected QBANK items
                if (quizQuestions.isNotEmpty()) {
                    adapter.addItems(quizQuestions)
                }

                //Update remaining limit: available - used
                val newRemaining = (effectiveAvailableSlots - newSelectedQuestions.size).coerceAtLeast(0)
                Constant.isQuestionLimit = newRemaining

                // Sync PickQuestionAdapter: mark newly imported as imported (sets checked = true in pick adapter)
                adapter2.markAsImported(newSelectedQuestions)

                //ensure the deselected items in pick adapter are unchecked (defensive)
                deselectedIds.forEach { id -> adapter2.uncheckItemById(id) }

                alertDialog.dismiss()
            } else {
                // not enough slots; do nothing (no removals), just show error
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
            correct_answer = this.answer,
            iframe="",
            file_size="",
            thumbnail="",
            sourceType = QuestionSource.QBANK,
            file_path = emptyList()
        )
    }

    fun isAddQuestionSubmit() {
        val allQuestions = adapter.getUpdatedList()

        val apiUserQuestions = allQuestions
            .filter { it.sourceType == QuestionSource.API || it.sourceType == QuestionSource.USER ||it.sourceType == QuestionSource.QBANK}
            .map {
                QuizQuestionRequest(
                    ques_no = it.id,
                    chapter = it.chapter,
                    question = it.question,
                    a_option = it.a_option,
                    b_option = it.b_option,
                    c_option = it.c_option,
                    d_option = it.d_option,
                    answer = it.answer,
                    mark = it.mark,
                    iframe = it.iframe?:"",
                    file_size ="4"?:"",
                    thumbnail = it.thumbnail?:"",
                    file_path = listOf(
                        FilePath(
                            url = "https://schoolchimes-communication.s3.ap-south-1.amazonaws.com/communication/7044/2025-08-22/IMG_1755839782816.jpg",
                            type = "IMAGE"
                        ),
                        FilePath(
                            url = "https://schoolchimes-communication.s3.ap-south-1.amazonaws.com/communication/7044/2025-08-22/IMG_1755839782401.jpg",
                            type = "IMAGE"
                        ),
                        FilePath(
                            url = "https://schoolchimes-communication.s3.ap-south-1.amazonaws.com/uploads/Documents/file-sample_150kB.pdf",
                            type = "PDF"
                        ),
                        FilePath(
                            url = "https://schoolchimes-communication.s3.ap-south-1.amazonaws.com/uploads/Documents/file-sample_100kB.docx",
                            type = "WORD"
                        )
                    )
                )
            }

        val updateQBankList: List<UpdateQBankItem> = allQuestions
            .filter { it.sourceType == QuestionSource.QBANK }
            .mapNotNull { updatedItem ->
                val originalItem = pickQBankList.find { it.id == updatedItem.id }

                if (originalItem != null) {
                    if (
                        updatedItem.question != originalItem.question ||
                        updatedItem.chapter != originalItem.chapter ||
                        updatedItem.a_option != originalItem.a_option ||
                        updatedItem.b_option != originalItem.b_option ||
                        updatedItem.c_option != originalItem.c_option ||
                        updatedItem.d_option != originalItem.d_option ||
                        updatedItem.answer != originalItem.answer ||
                        updatedItem.mark != originalItem.mark
                    ) {
                        UpdateQBankItem(
                            ques_no = updatedItem.id,
                            chapter = updatedItem.chapter,
                            question = updatedItem.question,
                            a_option = updatedItem.a_option,
                            b_option = updatedItem.b_option,
                            c_option = updatedItem.c_option,
                            d_option = updatedItem.d_option,
                            answer = updatedItem.answer,
                            mark = updatedItem.mark
                        )
                    } else null
                } else null
            }

        isUpdatedQBankQuestions=updateQBankList.size
        val totalMaxMark = apiUserQuestions.sumOf { it.mark }

        val body = QuizRequestBody(
            quiz_id = isQuizID,
            questions = apiUserQuestions,
            max_mark = totalMaxMark,
            ok_flag = isOkFlag,
            update_question_bank = updateQBankList
        )

        val jsonObject = Gson().toJsonTree(body).asJsonObject
        Log.d("FinalJSON", jsonObject.toString())

        Log.d("isUpdatedQBankQuestion",isUpdatedQBankQuestions.toString())
        Constant.hideLoading(this)

        if (isUpdatedQBankQuestions>0){

            val textQuestion = if (isUpdatedQBankQuestions == 1) {
                getString(R.string.question_)
            } else {
                getString(R.string.questions)
            }

            val isMessage = "${getString(R.string.You_have_modified)} $isUpdatedQBankQuestions $textQuestion ${getString(R.string.from_the_Question_Bank_Do_you_want_to_update_the_Question_Bank)}"

            Constant.showSendConfirmationDialog(
                this,
                getString(R.string.confirmation),
                getString(R.string.Update),
                getString(R.string.Cancel),
                "",
                isMessage
            ) { confirmed ->
                if (confirmed) {
                    Constant.showLoading(this)
                    appViewModel?.isQuizAddQuestion(isAccessToken!!, jsonObject)
                }
            }

        }
        else{
            Constant.showLoading(this)
            appViewModel?.isQuizAddQuestion(isAccessToken!!, jsonObject)
        }
    }


    fun isCallAddQuestion() {

        if (isSavedQuestionLimit==adapter.getUpdatedList().size){
            isAddQuestionSubmit()
        }
        else{
            val currentCount = adapter.getUpdatedList().size
            val isBalance = isSavedQuestionLimit -adapter.getUpdatedList().size
            val textQuestion = if (isBalance == 1) {
                getString(R.string.question_)  // e.g. "question"
            } else {
                getString(R.string.questions) // e.g. "questions"
            }

            val isMessage = "${getString(R.string.almost_there_You_ve_entered)} $currentCount $textQuestion. " +
                    "${getString(R.string.just)} $isBalance ${getString(R.string.more_to_complete_the_quiz_but_don_t_worry_you_can_add_them_later)}"

            Constant.showSendConfirmationDialog(
                this,
                getString(R.string.confirmation),
                getString(R.string.send),
                getString(R.string.Cancel),
                "",
                isMessage
            ) { confirmed ->
                if (confirmed) {
                    Constant.showLoading(this)
                    isAddQuestionSubmit()
                }
            }
        }
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
            R.id.lblSendQuiz->{

               if (adapter.getUpdatedList().size<=isSavedQuestionLimit) {
                   if (isSubmittedCount <= 0) {
                       isCallAddQuestion()
                   } else {
                       val studentText = if (isSubmittedCount == 1) {
                           getString(R.string.student_)
                       } else {
                           getString(R.string.students)
                       }

                       val isMessage =
                           getString(R.string.this_question_has_already_been_submitted_by) +
                                   " ${isSubmittedCount} $studentText " +
                                   getString(R.string.do_you_want_to_update_it)

                       Constant.showSendConfirmationDialog(
                           this,
                           getString(R.string.confirmation),
                           getString(R.string.permission_ok),
                           getString(R.string.Cancel),
                           "",
                           isMessage
                       ) { confirmed ->
                           if (confirmed) {
                               isCallAddQuestion()
                           }
                       }
                   }
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
}

