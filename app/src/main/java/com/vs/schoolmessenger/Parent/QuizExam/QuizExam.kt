package com.vs.schoolmessenger.Parent.QuizExam

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.QuizExamBinding

class QuizExam : BaseActivity<QuizExamBinding>(), View.OnClickListener {

    private lateinit var questionList: List<QuestionData>
    private var currentQuestionIndex = 0
    private lateinit var selectedAnswers: IntArray
    private lateinit var optionsArray: Array<TextView>

    override fun getViewBinding(): QuizExamBinding {
        return QuizExamBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        optionsArray = arrayOf(binding.option1, binding.option2, binding.option3, binding.option4)

        questionList = listOf(
            QuestionData("Which planet is known as the Red Planet?", "Venus", "Mars", "Jupiter", "Saturn"),
            QuestionData("Which element has the symbol 'O'?", "Oxygen", "Ozone", "Osmium", "Opium"),
            QuestionData("What is 2+2?", "2", "3", "4", "5"),
            QuestionData("What is 5+3?", "6", "7", "8", "9"),
            QuestionData("What is 10-7?", "1", "2", "3", "4")
        )

        selectedAnswers = IntArray(questionList.size) { -1 }
        displayQuestion()

        optionsArray.forEachIndexed { index, option ->
            option.setOnClickListener { selectOption(index) }
        }

        binding.nextButton.setOnClickListener(this)
        binding.prevButton.setOnClickListener(this)
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
        }
    }

    private fun displayQuestion() {
        val q = questionList[currentQuestionIndex]


        resetOptionColors()

        if (selectedAnswers[currentQuestionIndex] != -1) {
            optionsArray[selectedAnswers[currentQuestionIndex]].apply {
                setTextColor(resources.getColor(R.color.white))
                setBackgroundResource(R.drawable.quiz_option_selected_bg)
            }
            binding.nextButton.isEnabled = true
        } else {
            binding.nextButton.isEnabled = false
        }

        updateProgressBar()
        binding.prevButton.isEnabled = currentQuestionIndex > 0
        binding.nextButton.text = if (currentQuestionIndex == questionList.size - 1) "Submit" else "Next"
    }

    private fun selectOption(index: Int) {
        resetOptionColors()
        optionsArray[index].apply {
            setTextColor(resources.getColor(R.color.white))
            setBackgroundResource(R.drawable.quiz_option_selected_bg)
        }
        selectedAnswers[currentQuestionIndex] = index
        binding.nextButton.isEnabled = true
        updateProgressBar()
    }

    private fun resetOptionColors() {
        optionsArray.forEach {
            it.setBackgroundResource(R.drawable.quiz_option_bg)
            it.setTextColor(ContextCompat.getColor(this, R.color.azure_radiance))
        }
    }

    private fun updateProgressBar() {
        val answeredCount = selectedAnswers.count { it != -1 }
        val progress = (answeredCount.toFloat() / questionList.size * 100).toInt()
        binding.progressBar.progress = progress
        binding.questionCounter.text = "$answeredCount/${questionList.size}"
    }

    private fun showSubmitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Submit Quiz")
            .setMessage("Are you sure you want to submit the quiz?")
            .setPositiveButton("Yes") { _, _ -> showQuizCompletion() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showQuizCompletion() {
        binding.apply {
            questionText.visibility = View.GONE
            questionCounter.visibility = View.GONE
            option1.visibility = View.GONE
            option2.visibility = View.GONE
            option3.visibility = View.GONE
            option4.visibility = View.GONE
            nextButton.visibility = View.GONE
            prevButton.visibility = View.GONE
            progressBar.visibility = View.GONE
            quizStatus.visibility = View.VISIBLE
        }
    }
}
