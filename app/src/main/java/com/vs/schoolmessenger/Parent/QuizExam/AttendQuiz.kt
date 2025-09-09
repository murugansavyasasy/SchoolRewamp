package com.vs.schoolmessenger.Parent.QuizExam

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
        isQuizID = intent.getStringExtra("isRSQuizId").toString()

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

        optionsArray.forEachIndexed { index, option ->
            option.setOnClickListener { selectOption(index) }
        }
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
        binding.questionText.text = currentQuestion.question

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
            if (currentQuestionIndex == questionList.size - 1) "Submit" else "Next"

        binding.prevButton.visibility =
            if (currentQuestionIndex == 0) View.GONE else View.VISIBLE
    }

    private fun resetOptionColors() {
        optionsArray.forEach { textView ->
            textView.setTextColor(resources.getColor(R.color.black))
            textView.setBackgroundResource(R.drawable.quiz_option_bg)
        }
    }

    private fun updateProgressBar() {
        val answeredCount = selectedAnswersMap.values.count { it != 0 }
        val totalQuestions = questionList.size

        binding.progressBar.max = totalQuestions
        binding.progressBar.progress = answeredCount
        binding.questionCounter.text = "Questions $answeredCount / $totalQuestions"
    }

    private fun selectOption(index: Int) {
        resetOptionColors()
        optionsArray[index].apply {
            setTextColor(resources.getColor(R.color.white))
            setBackgroundResource(R.drawable.quiz_option_selected_bg)
        }

        val currentQuestion = questionList[currentQuestionIndex]
        selectedAnswersMap[currentQuestion.id] = index + 1 // store 1–4

        binding.nextButton.isEnabled = true
        updateProgressBar()
    }

    private fun buildAnswerJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("id", isQuizID) // quiz id or paper id

        val answersObj = JsonObject()

        // Ensure all questions are included
        questionList.forEach { question ->
            val selectedValue = selectedAnswersMap[question.id] ?: 0
            answersObj.addProperty(question.id, selectedValue.toString())
        }

        json.add("answers", answersObj)
        Log.d("FinalAnswer", json.toString())
        return json
    }

//    private fun displayQuestion() {
//        val currentQuestion = questionList[currentQuestionIndex]
//
//        // Set question text
//        binding.questionText.text = currentQuestion.question
//
//        val options = listOf(
//            currentQuestion.option1,
//            currentQuestion.option2,
//            currentQuestion.option3,
//            currentQuestion.option4
//        )
//
//        // Bind options dynamically
//        options.forEachIndexed { index, option ->
//            if (index < optionsArray.size) {
//                optionsArray[index].text = option
//                optionsArray[index].visibility = View.VISIBLE
//            }
//        }
//
//        // Show / hide attachments
//        if (!currentQuestion.filePath.isNullOrEmpty()) {
//            binding.indicator.visibility = View.VISIBLE
//            binding.rcAttachement.visibility = View.VISIBLE
//            binding.rcAttachement.layoutManager =
//                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//            binding.rcAttachement.adapter = AttachmentMediaAdapter(
//                currentQuestion.filePath,
//                this,
//                Constant.isShimmerViewDisable
//            )
//            binding.indicator.attachToRecyclerView(binding.rcAttachement)
//        } else {
//            binding.indicator.visibility = View.GONE
//            binding.rcAttachement.visibility = View.GONE
//        }
//
//        // Reset all option colors first
//        resetOptionColors()
//
//        // Restore previously selected answer (if exists)
//        val selectedIndex = selectedAnswersMap[currentQuestion.id]
//        if (selectedIndex != null && selectedIndex != -1) {
//            optionsArray[selectedIndex].apply {
//                setTextColor(resources.getColor(R.color.white))
//                setBackgroundResource(R.drawable.quiz_option_selected_bg)
//            }
//            binding.nextButton.isEnabled = true
//        } else {
//            binding.nextButton.isEnabled = false
//        }
//
//        // Set click listeners for options
//        optionsArray.forEachIndexed { index, textView ->
//            textView.setOnClickListener {
//                val prevSelectedIndex = selectedAnswersMap[currentQuestion.id]
//
//                if (prevSelectedIndex == index) {
//                    // Deselect if clicked again
//                    selectedAnswersMap[currentQuestion.id] = -1
//                    resetOptionColors()
//                    binding.nextButton.isEnabled = false
//                } else {
//                    // Select new option
//                    selectedAnswersMap[currentQuestion.id] = index
//                    resetOptionColors()
//                    textView.apply {
//                        setTextColor(resources.getColor(R.color.white))
//                        setBackgroundResource(R.drawable.quiz_option_selected_bg)
//                    }
//                    binding.nextButton.isEnabled = true
//                }
//
//                // Always update progress after change
//                updateProgressBar()
//            }
//        }
//
//        // Update progress and navigation buttons
//        updateProgressBar()
//        binding.prevButton.isEnabled = currentQuestionIndex > 0
//        binding.nextButton1.text =
//            if (currentQuestionIndex == questionList.size - 1) "Submit" else "Next"
//
//        binding.prevButton.visibility =
//            if (currentQuestionIndex == 0) View.GONE else View.VISIBLE
//    }
//
//    private fun resetOptionColors() {
//        optionsArray.forEach { textView ->
//            textView.setTextColor(resources.getColor(R.color.black))
//            textView.setBackgroundResource(R.drawable.quiz_option_bg)
//        }
//    }
//
//    private fun updateProgressBar() {
//        val answeredCount = selectedAnswersMap.values.count { it != -1 }
//        val totalQuestions = questionList.size
//
//        binding.progressBar.max = totalQuestions
//        binding.progressBar.progress = answeredCount
//
//        // Show answered status
//        binding.questionCounter.text = "Answered $answeredCount / $totalQuestions"
//    }
//
//
//
//    private fun selectOption(index: Int) {
//        resetOptionColors()
//        optionsArray[index].apply {
//            setTextColor(resources.getColor(R.color.white))
//            setBackgroundResource(R.drawable.quiz_option_selected_bg)
//        }
//
//        // save answer with question id
//        val currentQuestion = questionList[currentQuestionIndex]
//        selectedAnswersMap[currentQuestion.id] = index
//
//        binding.nextButton.isEnabled = true
//        updateProgressBar()
//    }


//    private fun buildAnswerJson(): JsonObject {
//        val json = JsonObject()
//        json.addProperty("id", isQuizID) // quiz id or paper id
//
//        val answersObj = JsonObject()
//        selectedAnswersMap.forEach { (questionId, selectedIndex) ->
//            answersObj.addProperty(questionId, selectedIndex.toString())
//        }
//
//        json.add("answers", answersObj)
//        Log.d("FinalAnswer",json.toString())
//        return json
//    }


    private fun showSubmitDialog() {
        isUnansweredCount = questionList.size - selectedAnswersMap.values.count { it != 0 }
        if (isUnansweredCount>0){
            var ques=if (isUnansweredCount==1)"question" else "questions"
            AlertDialog.Builder(this)
                .setTitle("Submit Quiz")

                .setMessage("Are you sure you want to submit the quiz? because you not answered ${isUnansweredCount} ${ques}! ")
                .setPositiveButton("Anyway submit") { _, _ -> showQuizCompletion() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }
        else{
            AlertDialog.Builder(this)
                .setTitle("Submit Quiz")
                .setMessage("Are you sure you want to submit the quiz?")
                .setPositiveButton("Yes") { _, _ -> showQuizCompletion() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()

        }
    }

    private fun showQuizCompletion() {

        val jsonObject=buildAnswerJson()
        Log.d("FinalAnswer",jsonObject.toString())
//        appViewModel?.isSubmitQuiz(isAccessToken!!, jsonObject)

    }
}



//Old Source
//package com.vs.schoolmessenger.Parent.QuizExam
//
//import android.util.Log
//import android.view.View
//import android.widget.TextView
//import androidx.appcompat.app.AlertDialog
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
//import com.vs.schoolmessenger.Parent.QuizExam.Adapter.QuizUpcomingAdapter
//import com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion.GetQuestionDetails
//import com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion.GetQuizQuestionsData
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Repository.App
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.SharedPreference
//import com.vs.schoolmessenger.databinding.QuizExamBinding
//
//class QuizExam : BaseActivity<QuizExamBinding>(), View.OnClickListener {
//
//    private lateinit var isAllQuestionData: List<GetQuizQuestionsData>
//    private lateinit var isQuestionList: List<GetQuestionDetails>
//    private lateinit var questionList: List<QuestionData>
//    private var currentQuestionIndex = 0
//    private lateinit var selectedAnswers: IntArray
//    private lateinit var optionsArray: Array<TextView>
//
//    private var appViewModel: App? = null
//    private var isAccessToken: String? = null
//    private var isChildDetails: ChildDetails? = null
//    var isQuizID=""
//
//    override fun getViewBinding(): QuizExamBinding {
//        return QuizExamBinding.inflate(layoutInflater)
//    }
//
//    override fun setupViews() {
//        super.setupViews()
//        isToolBarPrimaryTheme()
//        appViewModel = ViewModelProvider(this).get(App::class.java)
//        appViewModel?.init()
//
//        // Toolbar setup
//        binding.toolbarLayout.imgBack.setOnClickListener(this)
//        binding.toolbarLayout.lblLeftSideBar.setOnClickListener(this)
//        binding.toolbarLayout.lblRightSideBar.setOnClickListener(this)
//        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
//        binding.toolbarLayout.rytSearch.visibility = View.GONE
//        isChildDetails = SharedPreference.getChildDetails(this)
//        isAccessToken=isChildDetails!!.access_token
//
//        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
//        binding.toolbarLayout.lblStudentSection.text =
//            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
//        isQuizID = intent.getStringExtra("isRSQuizId").toString()
//
//
//
//
//        optionsArray = arrayOf(binding.option1, binding.option2, binding.option3, binding.option4)
//
//        questionList = listOf(
//            QuestionData(
//                "Which planet is known as the Red Planet?",
//                "Venus",
//                "Mars",
//                "Jupiter",
//                "Saturn"
//            ),
//            QuestionData("Which element has the symbol 'O'?", "Oxygen", "Ozone", "Osmium", "Opium"),
//            QuestionData("What is 2+2?", "2", "3", "4", "5"),
//            QuestionData("What is 5+3?", "6", "7", "8", "9"),
//            QuestionData("What is 10-7?", "1", "2", "3", "4")
//        )
//
//        selectedAnswers = IntArray(questionList.size) { -1 }
//        displayQuestion()
//
//        optionsArray.forEachIndexed { index, option ->
//            option.setOnClickListener { selectOption(index) }
//        }
//
//        binding.nextButton.setOnClickListener(this)
//        binding.prevButton.setOnClickListener(this)
//
//
//        appViewModel?.isGetQuestion?.observe(this) { response ->
//
//            if(response != null){
//                if (response.status == true) {
//                    binding.lnrQuiz.visibility=View.VISIBLE
//                    binding.quizStatus.visibility=View.GONE
//                    binding.lytList.visibility=View.GONE
//                    isAllQuestionData=response.data
//                    isQuestionList=isAllQuestionData.get(0).question_details
//                    Log.d("isQuestionList",isQuestionList.toString())
//                }
//                else{
//                    binding.lnrQuiz.visibility=View.GONE
//                    binding.quizStatus.visibility=View.GONE
//                    binding.lytList.visibility=View.VISIBLE
//                    ErrorMessage(response.message)
//                }
//            }
//            else{
//                binding.lnrQuiz.visibility=View.GONE
//                binding.quizStatus.visibility=View.GONE
//                binding.lytList.visibility=View.VISIBLE
//                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
//            }
//        }
//
//        isFetchQuizQuestionList()
//    }
//
//    fun ErrorMessage(errorMessage:String){
//        binding.lytList.visibility = View.VISIBLE
//        binding.txtNoData.text = errorMessage
//    }
//
//    private fun isFetchQuizQuestionList() {
//        appViewModel?.isGetQuestions(isAccessToken ?: "",isQuizID)
//    }
//
//
//    override fun onClick(p0: View?) {
//        when (p0?.id) {
//            R.id.nextButton -> {
//                if (currentQuestionIndex < questionList.size - 1) {
//                    currentQuestionIndex++
//                    displayQuestion()
//                } else {
//                    showSubmitDialog()
//                }
//            }
//
//            R.id.prevButton -> {
//                if (currentQuestionIndex > 0) {
//                    currentQuestionIndex--
//                    displayQuestion()
//                }
//            }
//
//            R.id.imgBack -> {
//                onBackPressed()
//            }
//        }
//    }
//
//    private fun displayQuestion() {
//        questionList[currentQuestionIndex]
//
//
//        resetOptionColors()
//
//        if (selectedAnswers[currentQuestionIndex] != -1) {
//            optionsArray[selectedAnswers[currentQuestionIndex]].apply {
//                setTextColor(resources.getColor(R.color.white))
//                setBackgroundResource(R.drawable.quiz_option_selected_bg)
//            }
//            binding.nextButton.isEnabled = true
//        } else {
//            binding.nextButton.isEnabled = false
//        }
//
//        updateProgressBar()
//        binding.prevButton.isEnabled = currentQuestionIndex > 0
//        binding.nextButton.text =
//            if (currentQuestionIndex == questionList.size - 1) "Submit" else "Next"
//    }
//
//    private fun selectOption(index: Int) {
//        resetOptionColors()
//        optionsArray[index].apply {
//            setTextColor(resources.getColor(R.color.white))
//            setBackgroundResource(R.drawable.quiz_option_selected_bg)
//        }
//        selectedAnswers[currentQuestionIndex] = index
//        binding.nextButton.isEnabled = true
//        updateProgressBar()
//    }
//
//    private fun resetOptionColors() {
//        optionsArray.forEach {
//            it.setBackgroundResource(R.drawable.quiz_option_bg)
//            it.setTextColor(ContextCompat.getColor(this, R.color.azure_radiance))
//        }
//    }
//
//    private fun updateProgressBar() {
//        val answeredCount = selectedAnswers.count { it != -1 }
//        val progress = (answeredCount.toFloat() / questionList.size * 100).toInt()
//        binding.progressBar.progress = progress
//        binding.questionCounter.text = "$answeredCount/${questionList.size}"
//    }
//
//    private fun showSubmitDialog() {
//        AlertDialog.Builder(this)
//            .setTitle("Submit Quiz")
//            .setMessage("Are you sure you want to submit the quiz?")
//            .setPositiveButton("Yes") { _, _ -> showQuizCompletion() }
//            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
//            .show()
//    }
//
//    private fun showQuizCompletion() {
//        binding.apply {
//            questionText.visibility = View.GONE
//            questionCounter.visibility = View.GONE
//            option1.visibility = View.GONE
//            option2.visibility = View.GONE
//            option3.visibility = View.GONE
//            option4.visibility = View.GONE
//            nextButton.visibility = View.GONE
//            prevButton.visibility = View.GONE
//            progressBar.visibility = View.GONE
//            quizStatus.visibility = View.VISIBLE
//        }
//    }
//}
