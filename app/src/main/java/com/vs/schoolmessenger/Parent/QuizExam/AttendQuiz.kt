package com.vs.schoolmessenger.Parent.QuizExam

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion.GetQuestionDetails
import com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion.GetQuizQuestionsData
import com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion.QuestionData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.AttachmentMediaAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.QuizExamBinding
import me.relex.circleindicator.CircleIndicator2

class AttendQuiz : BaseActivity<QuizExamBinding>(), View.OnClickListener {

    private lateinit var isAllQuestionData: List<GetQuizQuestionsData>
    private lateinit var isQuestionList: List<GetQuestionDetails>
    private lateinit var questionList: List<QuestionData>
    private var currentQuestionIndex = 0
    private lateinit var selectedAnswers: IntArray
    private lateinit var optionsArray: Array<TextView>

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    var isQuizID=""
    var isUnansweredCount=0
    private val selectedAnswersMap = mutableMapOf<String, Int>()


    override fun getViewBinding(): QuizExamBinding {
        return QuizExamBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryTheme1(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

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
        isQuizID = intent.getStringExtra(Constant.isRSQuizId).toString()

        appViewModel?.isGetQuestion?.observe(this) { response ->

            if(response != null){
                if (response.status) {
                    Constant.hideLoading(this)
                    isAllQuestionData=response.data
                    isQuestionList=isAllQuestionData.get(0).question_details
                    Log.d("isQuestionList",isQuestionList.toString())
                    isSetQuestion(isQuestionList)
                    binding.lnrQuiz.visibility=View.VISIBLE
                    binding.quizStatus.visibility=View.GONE
                    binding.lytList.visibility=View.GONE

                }
                else{
                    Constant.hideLoading(this)
                    binding.lnrQuiz.visibility=View.GONE
                    binding.quizStatus.visibility=View.GONE
                    binding.lytList.visibility=View.VISIBLE
                    ErrorMessage(response.message)
                }
            }
            else{
                Constant.hideLoading(this)
                binding.lnrQuiz.visibility=View.GONE
                binding.quizStatus.visibility=View.GONE
                binding.lytList.visibility=View.VISIBLE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }

        appViewModel!!.isSubmitQuiz?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@AttendQuiz)
                    Constant.showDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                    binding.apply {
                        lnrQuiz.visibility=View.GONE
                        quizStatus.visibility = View.VISIBLE
                    }
                } else {
                    Constant.hideLoading(this@AttendQuiz)
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        isFetchQuizQuestionList()


        binding.nextButton.setOnClickListener(this)
        binding.prevButton.setOnClickListener(this)

    }

    private fun isSetQuestion(isQuestionList: List<GetQuestionDetails>) {
        questionList = isQuestionList.map { question ->
            QuestionData(
                id = question.id,
                question = question.question,
                option1 = question.options.getOrNull(0) ?: "",
                option2 = question.options.getOrNull(1) ?: "",
                option3 = question.options.getOrNull(2) ?: "",
                option4 = question.options.getOrNull(3) ?: "",
                filePath = question.file_path
            )
        }
        optionsArray = arrayOf(binding.option1, binding.option2, binding.option3, binding.option4)

        selectedAnswers = IntArray(questionList.size) { -1 }
        displayQuestion()

    }

    fun ErrorMessage(errorMessage:String){
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }

    private fun isFetchQuizQuestionList() {
        Constant.showLoading(this)
        appViewModel?.isGetQuestions(isAccessToken ?: "",isQuizID)
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.nextButton -> {
                if (currentQuestionIndex < questionList.size - 1) {
                    currentQuestionIndex++
                    displayQuestion()
                } else {
                    showSubmitDialog()
                }
            }

            R.id.prevButton -> {
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex--
                    displayQuestion()
                }
            }

            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }



    private fun CircleIndicator2.attachToRecyclerView(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return
        this.createIndicators(adapter.itemCount, 0)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                this@attachToRecyclerView.animatePageSelected(firstVisible)
            }
        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                this@attachToRecyclerView.createIndicators(adapter.itemCount, 0)
            }
        })
    }

    private fun displayQuestion() {
        val currentQuestion = questionList[currentQuestionIndex]

        // Set question text
        binding.questionText.text ="${currentQuestionIndex+1}) ${currentQuestion.question}"

        val options = listOf(
            currentQuestion.option1,
            currentQuestion.option2,
            currentQuestion.option3,
            currentQuestion.option4
        )

        // Bind options dynamically
        options.forEachIndexed { index, option ->
            if (index < optionsArray.size) {
                optionsArray[index].text = option
                optionsArray[index].visibility = View.VISIBLE
            }
        }

        // Show / hide attachments
        if (!currentQuestion.filePath.isNullOrEmpty()) {
            binding.indicator.visibility = View.VISIBLE
            binding.rcAttachement.visibility = View.VISIBLE
            binding.rcAttachement.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rcAttachement.adapter = AttachmentMediaAdapter(
                currentQuestion.filePath,
                this,
                Constant.isShimmerViewDisable
            )
            binding.indicator.attachToRecyclerView(binding.rcAttachement)
        } else {
            binding.indicator.visibility = View.GONE
            binding.rcAttachement.visibility = View.GONE
        }

        // Reset all option colors first
        resetOptionColors()

        // Restore previously selected answer (0 = unanswered)
        val selectedValue = selectedAnswersMap[currentQuestion.id] ?: 0
        if (selectedValue != 0) {
            val selectedIndex = selectedValue - 1
            optionsArray[selectedIndex].apply {
                setTextColor(resources.getColor(R.color.white))
                setBackgroundResource(R.drawable.quiz_option_selected_bg)
            }
        }

        // Always allow Next button (user can move even if unanswered)
        binding.nextButton.isEnabled = true

        // Set click listeners for options
        optionsArray.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                val prevSelected = selectedAnswersMap[currentQuestion.id] ?: 0

                if (prevSelected == index + 1) {
                    // Deselect if clicked again → unanswered
                    selectedAnswersMap[currentQuestion.id] = 0
                    resetOptionColors()
                } else {
                    // Select option (store 1–4)
                    selectedAnswersMap[currentQuestion.id] = index + 1
                    resetOptionColors()
                    textView.apply {
                        setTextColor(resources.getColor(R.color.white))
                        setBackgroundResource(R.drawable.quiz_option_selected_bg)
                    }
                }

                binding.nextButton.isEnabled = true
                updateProgressBar()
            }
        }

        // Update progress and navigation buttons
        updateProgressBar()
        binding.prevButton.isEnabled = currentQuestionIndex > 0
        binding.nextButton1.text =
            if (currentQuestionIndex == questionList.size - 1) getString(R.string.SUBMIT) else getString(R.string.NEXT)
        binding.nextButton.background.setTint(ContextCompat.getColor(this,R.color.navi_blue1))


        if (currentQuestionIndex == 0){
            binding.prevButton.isEnabled=false
            binding.prevButton.background.setTint(ContextCompat.getColor(this,R.color.light_gray))
            binding.igLeftImage.setColorFilter(ContextCompat.getColor(this, R.color.white))
            binding.prevButton1.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        else{
            binding.prevButton.isEnabled=true
            binding.prevButton.background.setTint(ContextCompat.getColor(this,R.color.navi_blue1))
            binding.igLeftImage.setColorFilter(ContextCompat.getColor(this, R.color.white))
            binding.prevButton1.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    private fun updateQuestionCounter(current: Int, total: Int) {
        val text = "$current/$total"
        val spannable = SpannableString(text)

        // Apply blue color only to part before "/"
        val slashIndex = text.indexOf("/")
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.PrimaryColor)), // your blue color
            0,
            slashIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set black color for rest (after slash)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.black)),
            slashIndex,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.questionCounter.text = spannable
    }


    private fun resetOptionColors() {
        optionsArray.forEach { textView ->
            textView.setTextColor(resources.getColor(R.color.azure_radiance))
            textView.setBackgroundResource(R.drawable.quiz_option_bg)
        }
    }

    private fun updateProgressBar() {
        val answeredCount = selectedAnswersMap.values.count { it != 0 }
        val totalQuestions = questionList.size

        binding.progressBar.max = totalQuestions
        binding.progressBar.progress = answeredCount
        binding.questionCounter.text = "$answeredCount  /  $totalQuestions"
        updateQuestionCounter(answeredCount,totalQuestions) //Just Changing the Colour in UI
    }

    private fun buildAnswerJson(): JsonObject {
        val json = JsonObject()
        json.addProperty(APIKeyNames.id, isQuizID)

        val answersObj = JsonObject()

        // Ensure all questions are included
        questionList.forEach { question ->
            val selectedValue = selectedAnswersMap[question.id] ?: 0
            answersObj.addProperty(question.id, selectedValue.toString())
        }

        json.add(APIKeyNames.answers, answersObj)
        return json
    }


    private fun showSubmitDialog() {
        isUnansweredCount = questionList.size - selectedAnswersMap.values.count { it != 0 }
        if (isUnansweredCount>0){
            var ques=if (isUnansweredCount==1)getString(R.string.question_) else getString(R.string.questions_)
            Constant.showSendConfirmationDialog(
                this,
                getString(R.string.confirmation),
                getString(R.string.anyway_submit),
                getString(R.string.Cancel),
                "",
                "${getString(R.string.Are_you_sure_you_want_to_submit_the_quiz_because_you_not_answered)} ${isUnansweredCount} ${ques}! "
            ) { confirmed ->
                if (confirmed) {
                    Constant.showLoading(this)
                    val jsonObject=buildAnswerJson()
                    Log.d("FinalAnswer",jsonObject.toString())
                    appViewModel?.isSubmitQuiz(isAccessToken!!, jsonObject)
                }
            }
        }
        else{
            Constant.showSendConfirmationDialog(
                this,
                getString(R.string.confirmation),
                getString(R.string.submit),
                getString(R.string.Cancel),
                "",
                getString(R.string.Are_you_sure_you_want_to_submit_the_quiz)
            ) { confirmed ->
                if (confirmed) {
                    Constant.showLoading(this)
                    val jsonObject=buildAnswerJson()
                    Log.d("FinalAnswer",jsonObject.toString())
                    appViewModel?.isSubmitQuiz(isAccessToken!!, jsonObject)
                }
            }
        }
    }

}

