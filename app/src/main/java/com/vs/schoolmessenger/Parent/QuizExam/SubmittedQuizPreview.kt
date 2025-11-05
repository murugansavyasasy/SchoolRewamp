package com.vs.schoolmessenger.Parent.QuizExam

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
    var isSubmittedOn=""
    var isSubject=""


    override fun getViewBinding(): SubmittedQuizPreviewBinding {
        return SubmittedQuizPreviewBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        // Toolbar setup
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblLeftSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblRightSideBar.setOnClickListener(this)
//        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken=isChildDetails!!.access_token

        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        isQuizID = intent.getStringExtra(Constant.isRSSubmittedQuizId).toString()
        isSubject = intent.getStringExtra(Constant.isRSSubmittedSubject).toString()
        isSubmittedOn = intent.getStringExtra(Constant.isRSSubmittedSubmittedOn).toString()

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

        val rightAnswer = data.get(0).right_answer
        val wrongAnswer =  data.get(0).wrong_answer
        val unAnswer =  data.get(0).un_answer

        val rightParts = rightAnswer.split("/")
        val wrongParts = wrongAnswer.split("/")
        val unParts = unAnswer.split("/")

        val right = rightParts[0].toInt()
        val total = rightParts[1].toInt()

        val wrong = wrongParts[0].toInt()
        val un_answer = unParts[0].toInt()

        val percentage = if (total > 0) {
            (right * 100) / total
        } else 0


        binding.lblCompletedAt.text="${getString(R.string.completed_at)} ${Constant.convertDateFormatType(isSubmittedOn)}"
        binding.lblSubjectTitle.text=isSubject
        binding.lblQuizPercent.text=percentage.toString()
        animateProgress(binding.quizPercent,percentage, 100)

        binding.lblCorrectAnswer.text="${getString(R.string.correct)}: ${rightAnswer}"
        if (total==right){

            binding.lblWrongAnswer.text="${getString(R.string.wrong)}: ${wrong}"
            binding.lblNotAnswer.text="${getString(R.string.not_answered)}${un_answer}"
        }
        else{
            binding.lblWrongAnswer.text="${getString(R.string.wrong)}: ${wrongAnswer}"
            binding.lblNotAnswer.text="${getString(R.string.not_answered)} ${unAnswer}"
        }

        if (data.get(0).quiz_details.size>0){
            binding.lytList.visibility = View.GONE
            binding.rcSubmitedQuiz.visibility = View.VISIBLE
            adapter1 = QuizCompletedAdapter(data.get(0).quiz_details,this, false)
            binding.rcSubmitedQuiz.layoutManager = object : LinearLayoutManager(this) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
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
        binding.rcSubmitedQuiz.layoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

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

    fun animateProgress(progressBar: ProgressBar, current: Int, max: Int, duration: Long = 1000) {
        val safeMax = if (max <= 0) 1 else max           // Avoid divide by zero
        val safeCurrent = current.coerceIn(0, safeMax)   // Clamp current within valid range

        val percentage = ((safeCurrent.toFloat() / safeMax) * 100).toInt()

        progressBar.max = 100
        val animator = ObjectAnimator.ofInt(progressBar, Constant.progress, 0, percentage)
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }


}
