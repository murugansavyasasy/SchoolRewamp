package com.vs.schoolmessenger.School.QuizExam

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDelete
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.QuizExam.Adapter.ExamQuizReport.ExamQuizReportAdapter
import com.vs.schoolmessenger.School.QuizExam.Model.CreateQuiz.SaveCreateExamQuizDetails
import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData
import com.vs.schoolmessenger.School.QuizExam.QuizExamReport.AddQuestion
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamQuizBinding


class ExamQuiz : BaseActivity<ExamQuizBinding>(),
    View.OnClickListener, ExamQuizReportListener {

    override fun getViewBinding(): ExamQuizBinding {
        return ExamQuizBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isType = "2"
    var isNextLevelChecked = false
    var isDeletedId = ""

    private lateinit var adapter: ExamQuizReportAdapter
    private var isSubmission: List<GetQuizExamReportData>? = emptyList()


    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.rbNextLvl.buttonTintList = null
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnChooseRecipient.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

//        binding.edtTitle.filters = arrayOf(InputFilter.LengthFilter(Constant.isTitleLength))
//        binding.edtDescription.filters =
//            arrayOf(InputFilter.LengthFilter(Constant.isDescriptionLength))
//        Constant.editTextCounter(
//            this, binding.edtDescription, Constant.isDescriptionLength, binding.lblTextCount
//        )
//        Constant.editTextCounter(
//            this, binding.edtTitle, Constant.isTitleLength, binding.lblTitleTextCount
//        )

        val isQuizEditData =
            intent.getSerializableExtra(Constant.create_quiz_exam_data)
                    as? SaveCreateExamQuizDetails

        if (isQuizEditData?.type == "EDIT") {

            binding.tabOneName.text = getString(R.string.edit)
            binding.edtTitle.setText(isQuizEditData.title)
            binding.edtDescription.setText(isQuizEditData.description)
            binding.edtQuestion.setText(isQuizEditData.no_of_question)
            binding.rbNextLvl.isChecked = isQuizEditData.level_flag

            // visually + functionally disable
            binding.edtQuestion.isEnabled = false
            binding.rbNextLvl.isEnabled = false
            binding.edtQuestion.alpha = 0.4f
            binding.rbNextLvl.alpha = 0.4f

        } else {

            binding.tabOneName.text = getString(R.string.Create)
            binding.edtTitle.text = null
            binding.edtDescription.text = null
            binding.edtQuestion.text = null

            binding.rbNextLvl.isChecked = false
            binding.edtQuestion.isEnabled = true
            binding.rbNextLvl.isEnabled = true
            binding.edtQuestion.alpha = 1.0f
            binding.rbNextLvl.alpha = 1.0f
        }


        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)

            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)

            }
        }

//        binding.rbNextLvl.setOnClickListener {
//            isNextLevelChecked = !isNextLevelChecked
//            binding.rbNextLvl.isChecked = isNextLevelChecked
//        }

        binding.rbNextLvl.setOnCheckedChangeListener { _, isChecked ->
            isNextLevelChecked = isChecked
            Log.d("isNextLevelChecked", isNextLevelChecked.toString())
        }


        binding.txtSearch1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
                Log.d("Search", s.toString())


            }
        })


        appViewModel?.isGetQuizExamReport?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcQuizExamReport.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                    if (isType == "2") {
                        isLoadEQReport(response.data)
                        isSubmission = response.data
                    }
                } else {
                    binding.rlaQuizExamReport.visibility = View.VISIBLE
                    binding.rcQuizExamReport.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.rlaQuizExamReport.visibility = View.VISIBLE
                binding.rcQuizExamReport.visibility = View.GONE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }

        appViewModel?.isDeleteQuiz?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    Constant.showDataValidationNoDashboardRedirect(
                        resources.getString(R.string.success), response.message, this
                    )
                    onQuizDeletedSuccess(isDeletedId)
                }
                else {
                    Constant.showDataValidationNoDashboardRedirect(
                        resources.getString(R.string.Oops), response.message, this
                    )
                }
            } else {
                Constant.showDataValidationNoDashboardRedirect(
                    resources.getString(R.string.Oops),getString(R.string.something_went_wrong_please_try_again_later), this
                )
            }
        }



        binding.lnrTabOneName.setOnClickListener {
            binding.lnrTabOneName.isEnabled = false
            binding.lnrTabTwoName.isEnabled = true
            binding.line1.setBackgroundResource(R.color.iconBlue)
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.line2.setBackgroundResource(R.color.athens_gray)
            showTabOne()
        }

        binding.lnrTabTwoName.setOnClickListener {
            isType = "2"
            binding.lnrTabOneName.isEnabled = true
            binding.lnrTabTwoName.isEnabled = false
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.line2.setBackgroundResource(R.color.iconBlue)
            binding.line1.setBackgroundResource(R.color.athens_gray)
            showTabTwo()
            isFetchEQReport()

        }
    }

    private fun onQuizDeletedSuccess(deletedId: String) {

        //  Remove from original list (used for search/filter)
        isSubmission = isSubmission?.filterNot { it.id == deletedId }

        // Remove from adapter
        adapter.removeItemById(deletedId)

        // Handle empty state
        if (isSubmission.isNullOrEmpty()) {
            binding.rcQuizExamReport.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            if ( binding.rytSearch1.isVisible){
                binding.rytSearch1.visibility = View.GONE
            }
        }
        else {
            binding.rcQuizExamReport.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
            binding.rytSearch1.visibility = View.VISIBLE
            if ( binding.rytSearch1.isVisible){
                binding.rytSearch1.visibility = View.VISIBLE
            }
        }
    }


    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            isSubmission.orEmpty()
        } else {
            isSubmission.orEmpty().filter { isSubList ->
                val fieldsToSearch = mutableListOf(
                    isSubList.sent_by?.lowercase().orEmpty(),
                    isSubList.title?.lowercase().orEmpty(),
                    isSubList.description?.lowercase().orEmpty(),
                    isSubList.subject?.lowercase().orEmpty(),
                    isSubList.sent_time?.lowercase().orEmpty(),
                    isSubList.level.toString()?.lowercase().orEmpty(),
                )

                searchWords.all { word ->
                    fieldsToSearch.any { field -> field.contains(word) }
                }
            }
        }

        // Update UI
        if (filteredList.isNotEmpty()) {
            ShowData()
            adapter.updateData(filteredList)
        } else {
            binding.rcQuizExamReport.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }

    fun ShowData() {
        binding.rcQuizExamReport.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    private fun isLoadEQReport(data: List<GetQuizExamReportData>) {
        if (data.isNotEmpty()) {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            adapter = ExamQuizReportAdapter(data, this,this, Constant.isShimmerViewDisable)
            binding.rcQuizExamReport.layoutManager = LinearLayoutManager(this)
            binding.rcQuizExamReport.adapter = adapter
            binding.rcQuizExamReport.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.rcQuizExamReport.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        }
    }

    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }


    private fun isFetchEQReport() {
        adapter = ExamQuizReportAdapter(null, this,this, Constant.isShimmerViewShow)
        binding.rcQuizExamReport.layoutManager = LinearLayoutManager(this)
        binding.rcQuizExamReport.adapter = adapter

        appViewModel?.isGetQuizExamReport(isAccessToken ?: "", isType)
    }


    private fun isQuizBasicValidationPassed(): Boolean {

        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        val noOfQuestions = binding.edtQuestion.text.toString().trim()

        if (title.isEmpty()) {
            binding.edtTitle.error = getString(R.string.This_field_required)
            binding.edtTitle.requestFocus()
            return false
        }

        if (description.isEmpty()) {
            binding.edtDescription.error = getString(R.string.This_field_required)
            binding.edtDescription.requestFocus()
            return false
        }

        if (noOfQuestions.isEmpty()) {
            binding.edtQuestion.error = getString(R.string.This_field_required)
            binding.edtQuestion.requestFocus()
            return false
        }

        if (noOfQuestions.toInt() <= 0) {
            binding.edtQuestion.error = getString(R.string.no_of_question_greater_than_zero)
            binding.edtQuestion.requestFocus()
            return false
        }

        return true
    }


    private fun showTabOne() {
        binding.lytList.visibility = View.GONE
        binding.txtSearch1.text.clear()
        binding.rytSearch1.visibility = View.GONE
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)
        binding.rlaQuizExamReport.visibility = View.GONE
        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        binding.svOverallCreateQE.visibility = View.VISIBLE
    }

    private fun showTabTwo() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)
        binding.lytList.visibility = View.GONE
        binding.svOverallCreateQE.visibility = View.GONE
        binding.rlaQuizExamReport.visibility = View.VISIBLE
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnChooseRecipient -> {
                val title = binding.edtTitle.text.toString().trim()
                val description = binding.edtDescription.text.toString().trim()
                val no_of_questions = binding.edtQuestion.text.toString().trim()

                if (!isQuizBasicValidationPassed()) {
                    return
                }

                val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
                val builder = AlertDialog.Builder(this)
                builder.setView(dialogView)
                val alertDialog = builder.create()

                alertDialog.setCancelable(false)
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                alertDialog.show()

                val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
                val lblalertTitle = dialogView.findViewById<TextView>(R.id.alertTitle)
                val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
                val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
                val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)

                alertMessage.text = getString(R.string.you_haven_t_added_questions_to_this_quiz_yet_would_you_like_to_add_them_now_or_do_it_later)
                okButton.text = getString(R.string.add_now)
                lblalertTitle.text = getString(R.string.alert)
                btnCancel.text = getString(R.string.later)

                lblSelectTarget.visibility = View.GONE

                okButton.setOnClickListener {
                    alertDialog.dismiss()
                    val SaveCreateExamQuizDetails = SaveCreateExamQuizDetails(title, description, no_of_questions, isNextLevelChecked,"ADD_NOW")
                    val intent = Intent(this, AddQuestion::class.java)
                    intent.putExtra(Constant.create_quiz_exam_data, SaveCreateExamQuizDetails)
                    startActivity(intent)


                }

                btnCancel.setOnClickListener {
                    alertDialog.dismiss()
                    val SaveCreateExamQuizDetails = SaveCreateExamQuizDetails(title, description, no_of_questions, isNextLevelChecked,"LATER")
                    Log.d("SaveCreateExamQuizDetails", SaveCreateExamQuizDetails.toString())
                    val intent = Intent(this, RecipientActivity::class.java)
                    intent.putExtra(Constant.create_quiz_exam_data, SaveCreateExamQuizDetails)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onEditClick(
        data: GetQuizExamReportData,
        position: Int
    ) {
        Log.d("Edit","Quiz Data: ${data} Postion: ${position}")
        val SaveCreateExamQuizDetails = SaveCreateExamQuizDetails(data.title, data.description, data.no_of_questions.toString(),true,"EDIT")
        val intent = Intent(this, ExamQuiz::class.java)
        intent.putExtra(Constant.create_quiz_exam_data, SaveCreateExamQuizDetails)
        startActivity(intent)

    }

    override fun onDeleteClick(id: String, position: Int) {
        Log.d("Delete","Quiz id: ${id} Postion: ${position}")

        val request = JsonObject().apply {
            addProperty("id", id)
        }

        Constant.showSendConfirmationDialog(
            this,
            getString(R.string.confirmation),
            getString(R.string.delete),
            getString(R.string.Cancel),
            "",
            getString(R.string.are_you_sure_you_want_to_delete_this_quiz)
        ) { confirmed ->
            if (confirmed) {
                Constant.showLoading(this)
                isDeletedId = id
                appViewModel?.isDeleteQuiz(isAccessToken!!, request)
            }
        }
    }
}