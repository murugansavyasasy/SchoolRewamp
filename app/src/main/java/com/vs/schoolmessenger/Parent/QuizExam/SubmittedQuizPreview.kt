package com.vs.schoolmessenger.Parent.QuizExam

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.QuizExam.Adapter.QuizCompletedAdapter
import com.vs.schoolmessenger.Parent.QuizExam.Model.MySubmission.GetMySubmissionData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SubmittedQuizPreviewBinding

class SubmittedQuizPreview : BaseActivity<SubmittedQuizPreviewBinding>(), View.OnClickListener {

    private lateinit var isMySubmission: List<GetMySubmissionData>
    private lateinit var adapter1: QuizCompletedAdapter

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    var isQuizID=""


    override fun getViewBinding(): SubmittedQuizPreviewBinding {
        return SubmittedQuizPreviewBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        // Toolbar setup
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblLeftSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblRightSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken=isChildDetails!!.access_token

        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        isQuizID = intent.getStringExtra("isRSSubmittedQuizId").toString()

        appViewModel?.isGetMySubmission?.observe(this) { response ->

            if(response != null){
                if (response.status) {
                    Constant.hideLoading(this)
                    isMySubmission=response.data
                    isLoadSubmittedQuiz(isMySubmission)
                    binding.lytList.visibility= View.GONE

                }
                else{
                    Constant.hideLoading(this)
                    binding.lytList.visibility= View.VISIBLE
                    ErrorMessage(response.message)
                }
            }
            else{
                Constant.hideLoading(this)
                binding.lytList.visibility= View.VISIBLE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }

        isFetchSubmittedQuiz()
    }

    private fun isLoadSubmittedQuiz(data: List<GetMySubmissionData>) {
        binding.txtCount.text=data.get(0).right_answer
        binding.txtCount1.text=data.get(0).wrong_answer
        binding.txtCount2.text=data.get(0).un_answer

        if (data.get(0).quiz_details.size>0){
            binding.lytList.visibility = View.GONE
            binding.rcSubmitedQuiz.visibility = View.VISIBLE
            adapter1 = QuizCompletedAdapter(data.get(0).quiz_details,this, false)
            binding.rcSubmitedQuiz.layoutManager = LinearLayoutManager(this)
            binding.rcSubmitedQuiz.adapter = adapter1
        }
        else{
            binding.lytList.visibility = View.VISIBLE
            binding.rcSubmitedQuiz.visibility = View.GONE
        }

    }


    fun ErrorMessage(errorMessage:String){
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }

    private fun isFetchSubmittedQuiz() {
        adapter1 = QuizCompletedAdapter(null, this,true)
        binding.rcSubmitedQuiz.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcSubmitedQuiz.adapter = adapter1

        appViewModel?.isGetMySubmission(isAccessToken ?: "",isQuizID)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }



}
