package com.vs.schoolmessenger.Parent.QuizExam

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.QuizExam.Adapter.CircuitImageAdapter
import com.vs.schoolmessenger.Parent.QuizExam.Adapter.OptionAdapter
import com.vs.schoolmessenger.Parent.QuizExam.Model.OptionModel
import com.vs.schoolmessenger.Parent.QuizExam.Model.QuizQuestion
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ActivityQuizBinding
import kotlin.collections.emptyList

class QuizActivity : BaseActivity<ActivityQuizBinding>(), View.OnClickListener {

    private lateinit var optionAdapter: OptionAdapter
    private lateinit var circuitAdapter: CircuitImageAdapter

    private val optionList = mutableListOf<OptionModel>()
    private var currentQuestionIndex = 0

    private val questions = listOf(

        QuizQuestion(
            questionText = "Analyze the provided circuit diagram (Attachment A). Which graph represents the voltage V(t)?",
            attachments = listOf(
                "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/noticeboard/7044/01-12-2025/IMG_1764565790571.jpg",
                "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/homework/7044/01-12-2025/IMG_1764566006478.jpg",
                "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/files/7044/01-12-2025/file-sample_150kB.pdf"
            ),
            options = listOf(
                OptionModel(
                    "a",
                    "Graph 1 shows exponential rise",
                    graphImageUrl = "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/noticeboard/7044/01-12-2025/IMG_1764565790571.jpg"
                ),
                OptionModel(
                    "b",
                    "Graph 2 shows step voltage",
                    graphImageUrl = "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/homework/7044/01-12-2025/IMG_1764566006478.jpg"
                ),
                OptionModel(
                    "c",
                    "Graph 3 shows linear increase",
                    graphImageUrl = "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/noticeboard/7044/01-12-2025/IMG_1764565790571.jpg"
                )
            )
        ),

        QuizQuestion(
            questionText = "Which graph correctly represents the capacitor discharge?",
            attachments = listOf(
                "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/events/7044/01-12-2025/IMG_1764566593247.jpg",
                "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/homework/7044/01-12-2025/IMG_1764566006478.jpg",
                "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/files/7044/01-12-2025/file-sample_150kB.pdf"
            ),
            options = listOf(
                OptionModel(
                    "a",
                    "Linear decay",
                    graphImageUrl = "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/homework/7044/01-12-2025/IMG_1764566006478.jpg"
                ),
                OptionModel(
                    "b",
                    "Exponential decay",
                    graphImageUrl = "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/noticeboard/7044/01-12-2025/IMG_1764565790571.jpg"
                ),
                OptionModel(
                    "c",
                    "Constant voltage",
                    graphImageUrl = "https://schoolchimes-activities.s3.ap-south-1.amazonaws.com/noticeboard/7044/01-12-2025/IMG_1764565790571.jpg"
                )
            )
        ),

        QuizQuestion(
            questionText = "What is the SI unit of capacitance?",
            attachments = emptyList(),
            options = listOf(
                OptionModel("a", "Farad"),
                OptionModel("b", "Ohm"),
                OptionModel("c", "Henry")
            )
        )
    )

    override fun getViewBinding(): ActivityQuizBinding {
        return ActivityQuizBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        setupCircuitImagesRecycler()
        setupOptionsRecycler()

        binding.btnNextQuestion.setOnClickListener(this)
        binding.btnPreviousQuestion.setOnClickListener(this)

        loadQuestion()
    }

    private fun loadQuestion() {
        val question = questions[currentQuestionIndex]

        binding.tvQuestionNumber.text =
            "Question ${currentQuestionIndex + 1}/${questions.size}"

        binding.tvQuestionText.text = question.questionText

        if (question.attachments.isNullOrEmpty()) {
            binding.cardCircuitDiagram.visibility = View.GONE
        } else {
            binding.cardCircuitDiagram.visibility = View.VISIBLE
            circuitAdapter.updateData(question.attachments)
        }
        val layoutManager = if (question.attachments.size == 1) {
            LinearLayoutManager(this@QuizActivity, LinearLayoutManager.HORIZONTAL, false).apply {
            }
        } else {
            GridLayoutManager(this@QuizActivity, 3).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) = 1
                }
            }
        }

        binding.rvCircuitImages.layoutManager = layoutManager

        optionList.clear()
        optionList.addAll(question.options.map { it.copy(isSelected = false) })
        optionAdapter.notifyDataSetChanged()

        binding.btnPreviousQuestion.visibility =
            if (currentQuestionIndex == 0) View.GONE else View.VISIBLE

        if (currentQuestionIndex == questions.size - 1) {
            binding.btnNextQuestion.text = "Done"
        } else {
            binding.btnNextQuestion.text = "Next Question"
        }
    }

    private fun setupOptionsRecycler() {
        optionAdapter = OptionAdapter(optionList) { position ->
            selectOption(position)
        }

        binding.rvOptions.apply {
            layoutManager = LinearLayoutManager(this@QuizActivity)
            adapter = optionAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupCircuitImagesRecycler() {
        circuitAdapter = CircuitImageAdapter(mutableListOf()) { clickedPosition ->
            val fileUrl = questions[currentQuestionIndex].attachments[clickedPosition]
            val type = when {
                fileUrl.lowercase().endsWith(".pdf") -> Constant.PDF
                fileUrl.lowercase().endsWith(".mp4") || fileUrl.lowercase()
                    .contains("video") -> Constant.VIDEO

                else -> Constant.IMAGE
            }
            val intent = Intent(this, FilesViewActivity::class.java)
            Constant.commonFileList.clear()
            Constant.commonFileList.add(CommonFileData(type = type, path = fileUrl))
            Constant.selectedFileIndex = 0
            startActivity(intent)
        }

        binding.rvCircuitImages.apply {
            isNestedScrollingEnabled = false
        }
        binding.rvCircuitImages.adapter = circuitAdapter
    }

    private fun selectOption(position: Int) {
        optionList.forEachIndexed { index, option ->
            option.isSelected = index == position
        }
        optionAdapter.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnNextQuestion -> {
                if (currentQuestionIndex == questions.size - 1) {
                    finish()
                } else {
                    currentQuestionIndex++
                    loadQuestion()
                }
            }

            R.id.btnPreviousQuestion -> {
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex--
                    loadQuestion()
                }
            }
        }
    }
}
