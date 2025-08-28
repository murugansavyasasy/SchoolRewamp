package com.vs.schoolmessenger.School.QuizExam.QuizExamReport

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
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
        Constant.isQuestionLimit = intent.getIntExtra("limitQuestion", -1)
        Log.d("isQuestionLimit",Constant.isQuestionLimit.toString())

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
                    Constant.isQuestionLimit -= 1
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

        // Check again before showing
        if (!activity.isFinishing && !activity.isDestroyed) {
            alertDialog.show()
        }

        val lblClose = dialogView.findViewById<TextView>(R.id.lblClose)
        val lblImportQuestion = dialogView.findViewById<TextView>(R.id.lblImportQuestion)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcPickFromQBank)
        val cbSelect = dialogView.findViewById<CheckBox>(R.id.cbSelect)

        recyclerView.layoutManager = LinearLayoutManager(activity)
//        adapter2 = PickQuestionAdapter(pickFomQbank.toMutableList(), activity, false) {
//
//
////old working
//            cbSelect.setOnCheckedChangeListener { _, isChecked ->
//                if (isChecked) {
//                    val allQuestions = adapter2.getAllNotImported()
//                    val totalToSelect = allQuestions.size
//
//                    if (totalToSelect <= Constant.isQuestionLimit) {
//                        //  Within limit → mark as imported
//                        Constant.isQuestionLimit -= totalToSelect
//                        adapter2.markAsImported(allQuestions)
//                        cbSelect.isChecked = true
//                    } else {
//                        // Over the limit → show message and reset checkbox
//                        Constant.showErrorAlert(
//                            this,
//                            getString(R.string.alert),
//                            getString(R.string.question_limit_reached)
//                        )
//                        cbSelect.isChecked = false
//                    }
//                } else {
//                    //  Uncheck → clear only selections (not imported ones)
//                    adapter2.clearSelections()
//                }
//            }
//
//
//
//        }

        adapter2 = PickQuestionAdapter(pickFomQbank.toMutableList(), activity, false)
        recyclerView.adapter = adapter2

        // Listener first
        val selectAllListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                adapter2.getAllNotImported().forEach { adapter2.tempSelect(it.id) }
            } else {
                adapter2.getAllNotImported().forEach { adapter2.tempUnselect(it.id) }
            }
        }

// Initialize Select All without triggering listener
        cbSelect.setOnCheckedChangeListener(null)
        cbSelect.isChecked = adapter2.getUpdatedList().all { it.checked || adapter2.tempSelection[it.id] == true }

// Sync Select All when individual selections change
        adapter2.onSelectionChanged = { allSelected ->
            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = allSelected
            cbSelect.setOnCheckedChangeListener(selectAllListener)
        }

// Attach listener
        cbSelect.setOnCheckedChangeListener(selectAllListener)


//
//        cbSelect.setOnCheckedChangeListener(null)
//
//// Initialize checkbox based on current selections
//        cbSelect.isChecked = adapter2.getUpdatedList().all { it.checked || adapter2.tempSelection[it.id] == true }
//
//// Listen for individual selection changes
//        adapter2.onSelectionChanged = { allSelected ->
//            cbSelect.isChecked = allSelected
//        }
//
//        cbSelect.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                // Temporarily select all not-imported items
//                adapter2.getAllNotImported().forEach { adapter2.tempSelect(it.id) }
//            } else {
//                adapter2.clearSelections()
//            }
//        }



//old working
//        lblImportQuestion.setOnClickListener {
//            val selectedQuestions = adapter2.getSelected()
//
//            //  get already added IDs from AddQuestionAdapter
//            val alreadyAddedIds = adapter.getUpdatedList().map { it.id }
//
//            // filter out questions already in AddQuestionAdapter
//            val newSelectedQuestions = selectedQuestions.filter { it.id !in alreadyAddedIds }
//
//            Log.d("selectedQuestions", selectedQuestions.toString())
//            Log.d("selectedQuestionsSize", selectedQuestions.size.toString())
//            Log.d("newSelectedQuestions", newSelectedQuestions.toString())
//            Log.d("newSelectedQuestionsSize", newSelectedQuestions.size.toString())
//
//            if (Constant.isQuestionLimit > 0) {
//                if (newSelectedQuestions.size <= Constant.isQuestionLimit) {
//                    val quizQuestions = newSelectedQuestions.map {
//                        it.toQuizQuestionReportData().copy(sourceType = QuestionSource.QBANK)
//                    }
//
//                    adapter.updateItems(quizQuestions)
//
//                    // reduce limit only by the actually new ones
//                    Constant.isQuestionLimit -= newSelectedQuestions.size
//
//                    adapter2.markAsImported(newSelectedQuestions)
//                    alertDialog.dismiss()
//                } else {
//                    Constant.showErrorAlert(
//                        this,
//                        getString(R.string.alert),
//                        getString(R.string.question_limit_reached)
//                    )
//                }
//            } else {
//                Constant.showErrorAlert(
//                    this,
//                    getString(R.string.alert),
//                    getString(R.string.question_limit_reached)
//                )
//            }
//        }

        //new working select all issue
//        lblImportQuestion.setOnClickListener {
//            val selectedQuestions = adapter2.getSelected()                       // PickQuestionAdapter -> GetPickFromQBankData
//            val selectedIds = selectedQuestions.map { it.id }.toSet()
//
//            val currentAdded = adapter.getUpdatedList()                         // AddQuestionAdapter -> GetQuizQuestionReportData
//            val alreadyAddedIds = currentAdded.map { it.id }.toSet()
//
//            // QBANK items currently present in AddQuestionAdapter
//            val qbankAddedIds = currentAdded
//                .filter { it.sourceType == QuestionSource.QBANK && it.id.isNotEmpty() }
//                .map { it.id }
//
//            // Items user just selected in dialog that are genuinely new to AddQuestionAdapter
//            val newSelectedQuestions = selectedQuestions.filter { it.id !in alreadyAddedIds }
//
//            // QBANK ids that were previously in AddQuestionAdapter but user deselected in the dialog now
//            val deselectedIds = qbankAddedIds.filter { it !in selectedIds }
//
//            // Effective available slots: current limit + slots freed by deselection
//            val effectiveAvailableSlots = Constant.isQuestionLimit + deselectedIds.size
//
//            Log.d("ImportDebug", "selected=${selectedIds.size}, newSelected=${newSelectedQuestions.size}, alreadyAdded=${alreadyAddedIds.size}, deselected=${deselectedIds.size}, effectiveSlots=$effectiveAvailableSlots")
//
//            if (effectiveAvailableSlots <= 0) {
//                Constant.showErrorAlert(
//                    this,
//                    getString(R.string.alert),
//                    getString(R.string.question_limit_reached)
//                )
//                return@setOnClickListener
//            }
//
//            if (newSelectedQuestions.size <= effectiveAvailableSlots) {
//                // convert to AddQuestion model
//                val quizQuestions = newSelectedQuestions.map {
//                    it.toQuizQuestionReportData().copy(sourceType = QuestionSource.QBANK)
//                }
//
//                // 1) Remove only the deselected QBANK items (permanently)
//                if (deselectedIds.isNotEmpty()) {
//                    adapter.removeItemsByIds(deselectedIds)
//                }
//
//                // 2) Add newly selected QBANK items
//                if (quizQuestions.isNotEmpty()) {
//                    adapter.addItems(quizQuestions)
//                }
//
//                // 3) Update remaining limit: available - used
//                val newRemaining = (effectiveAvailableSlots - newSelectedQuestions.size).coerceAtLeast(0)
//                Constant.isQuestionLimit = newRemaining
//
//                // 4) Sync PickQuestionAdapter:
//                //    - mark newly imported as imported (sets checked = true in pick adapter)
//                adapter2.markAsImported(newSelectedQuestions)
//
//                //    - ensure the deselected items in pick adapter are unchecked (defensive)
//                deselectedIds.forEach { id -> adapter2.uncheckItemById(id) }
//
//                alertDialog.dismiss()
//            } else {
//                // not enough slots; do nothing (no removals), just show error
//                Constant.showErrorAlert(
//                    this,
//                    getString(R.string.alert),
//                    getString(R.string.question_limit_reached)
//                )
//            }
//        }

//        lblImportQuestion.setOnClickListener {
//            val selectedQuestions = adapter2.getSelected()
//            val selectedIds = selectedQuestions.map { it.id }.toSet()
//
//            val currentAdded = adapter.getUpdatedList()
//            val alreadyAddedIds = currentAdded.map { it.id }.toSet()
//
//            val qbankAddedIds = currentAdded
//                .filter { it.sourceType == QuestionSource.QBANK && it.id.isNotEmpty() }
//                .map { it.id }
//
//            val newSelectedQuestions = selectedQuestions.filter { it.id !in alreadyAddedIds }
//            val deselectedIds = qbankAddedIds.filter { it !in selectedIds }
//
//            val effectiveAvailableSlots = Constant.isQuestionLimit + deselectedIds.size
//
//            if (effectiveAvailableSlots <= 0) {
//                Constant.showErrorAlert(this, getString(R.string.alert), getString(R.string.question_limit_reached))
//                return@setOnClickListener
//            }
//
//            if (newSelectedQuestions.size <= effectiveAvailableSlots) {
//                val quizQuestions = newSelectedQuestions.map {
//                    it.toQuizQuestionReportData().copy(sourceType = QuestionSource.QBANK)
//                }
//
//                // Permanently remove deselected items
//                if (deselectedIds.isNotEmpty()) adapter.removeItemsByIds(deselectedIds)
//
//                // Add newly selected
//                if (quizQuestions.isNotEmpty()) adapter.addItems(quizQuestions)
//
//                // Update remaining limit
//                Constant.isQuestionLimit = (effectiveAvailableSlots - newSelectedQuestions.size).coerceAtLeast(0)
//
//                // Sync PickQuestionAdapter
//                adapter2.markAsImported(newSelectedQuestions)
//                deselectedIds.forEach { id -> adapter2.uncheckItemById(id) }
//
//                alertDialog.dismiss()
//            } else {
//                Constant.showErrorAlert(this, getString(R.string.alert), getString(R.string.question_limit_reached))
//            }
//        }
        lblImportQuestion.setOnClickListener {
            val selectedQuestions = adapter2.getSelected()
            val selectedIds = selectedQuestions.map { it.id }.toSet()

            val currentAdded = adapter.getUpdatedList()
            val alreadyAddedIds = currentAdded.map { it.id }.toSet()

            val qbankAddedIds = currentAdded
                .filter { it.sourceType == QuestionSource.QBANK && it.id.isNotEmpty() }
                .map { it.id }

            // 1️⃣ Remove deselected items first (always)
            val deselectedIds = qbankAddedIds.filter { it !in selectedIds }
            Log.d("DeselectedIds",deselectedIds.toString())
            Log.d("DeselectedIdsSize",deselectedIds.size.toString())

            if (deselectedIds.isNotEmpty()) {
                adapter.removeItemsByIds(deselectedIds)
                deselectedIds.forEach { id -> adapter2.uncheckItemById(id) }
                Constant.isQuestionLimit += deselectedIds.size
            }

            // 2️⃣ Newly selected questions
            val newSelectedQuestions = selectedQuestions.filter { it.id !in alreadyAddedIds }
            if (newSelectedQuestions.isEmpty()) {
                alertDialog.dismiss()
                return@setOnClickListener
            }

            // 3️⃣ Limit check
            if (newSelectedQuestions.size <= Constant.isQuestionLimit) {
                val quizQuestions = newSelectedQuestions.map {
                    it.toQuizQuestionReportData().copy(sourceType = QuestionSource.QBANK)
                }

                adapter.addItems(quizQuestions)
                Constant.isQuestionLimit -= newSelectedQuestions.size
                adapter2.markAsImported(newSelectedQuestions)
                alertDialog.dismiss()
            } else {
                Constant.showErrorAlert(
                    this,
                    getString(R.string.alert),
                    getString(R.string.question_limit_reached)
                )
            }
        }





//        lblImportQuestion.setOnClickListener {
//            val selectedQuestions = adapter2.getSelected()
//            Log.d("selectedQuestions",selectedQuestions.toString())
//            Log.d("selectedQuestionssize",selectedQuestions.size.toString())
//
//            if (Constant.isQuestionLimit > 0) {
//                if (selectedQuestions.size <= Constant.isQuestionLimit) {
//                    //  within limit
//                    Constant.isQuestionLimit -= selectedQuestions.size
//
//                    val quizQuestions = selectedQuestions.map {
//                        it.toQuizQuestionReportData().copy(sourceType = QuestionSource.QBANK)
//                    }
//                    adapter.updateItems(quizQuestions)
//
//                    //  now make those permanent
//                    adapter2.markAsImported(selectedQuestions)
//
//                    alertDialog.dismiss()
//                } else {
//                    // limit exceeded
//                    Constant.showErrorAlert(
//                        this,
//                        getString(R.string.alert),
//                        getString(R.string.question_limit_reached)
//                    )
//                }
//            } else {
//                Constant.showErrorAlert(
//                    this,
//                    getString(R.string.alert),
//                    getString(R.string.question_limit_reached)
//                )
//            }
//        }



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