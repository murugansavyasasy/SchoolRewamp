package com.vs.schoolmessenger.School.QuizExam

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.QuizExam.Adapter.ExamQuizReport.ExamQuizReportAdapter
import com.vs.schoolmessenger.School.QuizExam.Model.CreateQuiz.SaveCreateExamQuizDetails
import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamQuizBinding


class ExamQuiz : BaseActivity<ExamQuizBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): ExamQuizBinding {
        return ExamQuizBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isType = "2"
    var isNextLevelChecked = false
    private lateinit var adapter: ExamQuizReportAdapter
    private var isSubmission: List<GetQuizExamReportData>? = emptyList()


    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnChooseRecipient.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text=isStaffDetails!!.school_name

//        binding.edtTitle.filters = arrayOf(InputFilter.LengthFilter(Constant.isTitleLength))
//        binding.edtDescription.filters =
//            arrayOf(InputFilter.LengthFilter(Constant.isDescriptionLength))
//        Constant.editTextCounter(
//            this, binding.edtDescription, Constant.isDescriptionLength, binding.lblTextCount
//        )
//        Constant.editTextCounter(
//            this, binding.edtTitle, Constant.isTitleLength, binding.lblTitleTextCount
//        )

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

        binding.rbNextLvl.setOnClickListener {
            isNextLevelChecked = !isNextLevelChecked
            binding.rbNextLvl.isChecked = isNextLevelChecked
        }

        binding.txtSearch1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
                Log.d("Search",s.toString())


            }
        })


        appViewModel?.isGetQuizExamReport?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcQuizExamReport.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                    if (isType == "2") {
                        isLoadEQReport(response.data)
                        isSubmission=response.data
                    }
                }
                else {
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
        binding.rcQuizExamReport.visibility=View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    private fun isLoadEQReport(data: List<GetQuizExamReportData>) {
        if (data.isNotEmpty()) {
            binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
            adapter = ExamQuizReportAdapter(data, this, Constant.isShimmerViewDisable)
            binding.rcQuizExamReport.layoutManager = LinearLayoutManager(this)
            binding.rcQuizExamReport.adapter = adapter
            binding.rcQuizExamReport.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
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
        adapter = ExamQuizReportAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcQuizExamReport.layoutManager = LinearLayoutManager(this)
        binding.rcQuizExamReport.adapter = adapter

        appViewModel?.isGetQuizExamReport(isAccessToken ?: "", isType)
    }

    private fun isRedirectToSectionStudents() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        val no_of_questions = binding.edtQuestion.text.toString().trim()
        if (title.isEmpty()) {
            binding.edtTitle.error = getString(R.string.This_field_required)
            binding.edtTitle.requestFocus()
            return
        }
        if (description.isEmpty()) {
            binding.edtDescription.error = getString(R.string.This_field_required)
            binding.edtDescription.requestFocus()
            return
        }
        if (no_of_questions.isEmpty()) {
            binding.edtQuestion.error = getString(R.string.This_field_required)
            binding.edtQuestion.requestFocus()
            return
        }
        if (no_of_questions=="0") {
            binding.edtQuestion.error = getString(R.string.no_of_question_greater_than_zero)
            binding.edtQuestion.requestFocus()
            return
        }

        val SaveCreateExamQuizDetails =
            SaveCreateExamQuizDetails(title, description, no_of_questions, isNextLevelChecked)
        Log.d("SaveCreateExamQuizDetails",SaveCreateExamQuizDetails.title)
        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.create_quiz_exam_data, SaveCreateExamQuizDetails)
        startActivity(intent)
    }

    private fun showTabOne() {
        binding.lytList.visibility = View.GONE
        binding.txtSearch1.text.clear()
        binding.rytSearch1.visibility = View.GONE
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)
        binding.rlaQuizExamReport.visibility = View.GONE
        binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
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
                isRedirectToSectionStudents()
            }
        }
    }
}