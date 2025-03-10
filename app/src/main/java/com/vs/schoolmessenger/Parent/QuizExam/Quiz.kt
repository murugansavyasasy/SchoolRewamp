package com.vs.schoolmessenger.Parent.QuizExam


import com.vs.schoolmessenger.databinding.QuizBinding


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class Quiz : AppCompatActivity() {
    private lateinit var binding: QuizBinding

    private val questions = listOf(
        "What is the square root of 64?" to listOf("6", "7", "8", "9"),
        "What is 5 + 3?" to listOf("7", "8", "9", "10"),
        "Which planet is closest to the Sun?" to listOf("Earth", "Venus", "Mercury", "Mars"),
        "Who invented the light bulb?" to listOf("Nikola Tesla", "Albert Einstein", "Thomas Edison", "Isaac Newton"),
        "What is the capital of Japan?" to listOf("Beijing", "Seoul", "Bangkok", "Tokyo"),
        "What is H2O commonly known as?" to listOf("Oxygen", "Water", "Hydrogen", "Steam"),
        "How many continents are there?" to listOf("5", "6", "7", "8"),
        "Which gas do plants absorb?" to listOf("Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen"),
        "What is the largest mammal?" to listOf("Elephant", "Blue Whale", "Giraffe", "Hippo"),
        "What is the speed of light?" to listOf("300,000 km/s", "150,000 km/s", "500,000 km/s", "1,000,000 km/s")
    )

    private var currentQuestionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateQuestion()

        binding.btnNext.setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                updateQuestion()
            }
        }

        binding.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                updateQuestion()
            }
        }
    }

    private fun updateQuestion() {
        val (question, options) = questions[currentQuestionIndex]
        binding.tvQuestion.text = question
        binding.option1.text = options[0]
        binding.option2.text = options[1]
        binding.option3.text = options[2]
        binding.option4.text = options[3]

        // Update Progress Bar
        binding.progressBar.progress = currentQuestionIndex + 1
        binding.tvProgress.text = "${currentQuestionIndex + 1} / ${questions.size}"

        // Show/hide Previous button
        binding.btnPrevious.visibility = if (currentQuestionIndex == 0) View.GONE else View.VISIBLE
    }
}
