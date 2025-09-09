package com.vs.schoolmessenger.School.QuizExam.QuizExamReport
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.QuizExam.Adapter.SubmitReport.QuizSubmitReportAdapter
import com.vs.schoolmessenger.School.QuizExam.Model.QuizSubmissionList.GetQuizSubmissionListData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.QuizSubmitReportBinding

class SubmitReport : BaseActivity<QuizSubmitReportBinding>(),
    View.OnClickListener {

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var appViewModel: App? = null
    private lateinit var adapter: QuizSubmitReportAdapter
    private var isSubmission: List<GetQuizSubmissionListData>? = emptyList()
    var isQuizID=""

    override fun getViewBinding(): QuizSubmitReportBinding {
        return QuizSubmitReportBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.quiz_submission_list)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name
        isQuizID = intent.getStringExtra("quiz_Id").toString()



        isGetSubmittedList()

        appViewModel?.isGetQuizSubmissionList?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcSubmitReport.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                    isLoadisSubList(response.data)
                    isSubmission=response.data

                }
                else {
                    binding.rlaSubmitReport.visibility = View.VISIBLE
                    binding.rcSubmitReport.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.rlaSubmitReport.visibility = View.VISIBLE
                binding.rcSubmitReport.visibility = View.GONE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }


        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearch1.text.clear()
            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtSearch1.text.clear()

            }
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

    }

    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            isSubmission.orEmpty()
        } else {
            isSubmission.orEmpty().filter { isSubList ->
                val fieldsToSearch = mutableListOf(
                    isSubList.student_name?.lowercase().orEmpty(),
                    isSubList.standard?.lowercase().orEmpty(),
                    isSubList.section?.lowercase().orEmpty(),
                    isSubList.submitted_on?.lowercase().orEmpty(),
                )

                searchWords.all { word ->
                    fieldsToSearch.any { field -> field.contains(word) }
                }
            }
        }

        // 🔹 Update UI
        if (filteredList.isNotEmpty()) {
            ShowData()
            adapter.updateData(filteredList)
        } else {
            binding.rlaSubmitReport.visibility = View.VISIBLE
            binding.rcSubmitReport.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }


    fun ShowData() {
        binding.rlaSubmitReport.visibility = View.VISIBLE
        binding.rcSubmitReport.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }






    private fun isLoadisSubList(data: List<GetQuizSubmissionListData>) {

        if (data.isNotEmpty()) {
            adapter = QuizSubmitReportAdapter(data,this, Constant.isShimmerViewDisable)
            binding.rcSubmitReport.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            binding.rcSubmitReport.adapter = adapter
            binding.rcSubmitReport.isNestedScrollingEnabled = false
            binding.rcSubmitReport.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        } else {
            binding.rcSubmitReport.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        }
    }



    fun isGetSubmittedList(){
        adapter = QuizSubmitReportAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcSubmitReport.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.rcSubmitReport.adapter = adapter
        binding.rcSubmitReport.isNestedScrollingEnabled = false
        appViewModel?.isGetQuizSubmissionList(isAccessToken ?: "",isQuizID)
    }


    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }

}