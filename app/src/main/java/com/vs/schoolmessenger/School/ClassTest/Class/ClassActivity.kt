package com.vs.schoolmessenger.School.ClassTest.Class.ClassActivity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ClassTest.Class.ClassAdapter
import com.vs.schoolmessenger.School.ClassTest.Class.Models.ClassTestItem
import com.vs.schoolmessenger.School.ClassTest.Class.Models.SelectedSubject
import com.vs.schoolmessenger.School.ClassTest.Report.ExamReportActivity
import com.vs.schoolmessenger.School.ClassTest.Review.ReviewActivity
import com.vs.schoolmessenger.School.ClassTest.StepIndicatorHelper
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ClassActivityBinding

class ClassActivity : BaseActivity<ClassActivityBinding>(), View.OnClickListener  {

    private lateinit var adapter: ClassAdapter

    override fun getViewBinding(): ClassActivityBinding {
        return ClassActivityBinding.inflate(layoutInflater)
    }

    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleExitAttempt()
        }
    }


    override fun setupViews() {
        super.setupViews()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.statusBarBackground.layoutParams.height = statusBarHeight
            binding.statusBarBackground.requestLayout()

            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val baseBottomMargin = resources.getDimensionPixelSize(R.dimen.five)
            val lytContentParams =
                binding.lytContent.layoutParams as LinearLayout.LayoutParams
            lytContentParams.bottomMargin = baseBottomMargin + navBarHeight
            binding.lytContent.layoutParams = lytContentParams

            insets
        }
        setupStepIndicator()
        restoreExamNameIfAny()
        loadSubjectData()
        setupContinueButton()
//        setupKeyboardScrollBehavior()
        binding.viewreporttext.setOnClickListener(this)
        onBackPressedDispatcher.addCallback(this, backPressCallback)
        binding.imgBack.setOnClickListener { handleExitAttempt() }

    }


    override fun onPause() {
        super.onPause()
        saveCurrentStateToCache()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun restoreExamNameIfAny() {
        Constant.isSavedExamNameState?.let {
            binding.etExamName.setText(it)
        }
    }

    private fun isTestFilled(t: com.vs.schoolmessenger.School.ClassTest.Class.Models.TestEntry): Boolean {
        return t.examName.isNotBlank() ||
                t.testDate.isNotBlank() ||
                t.maxMarks.isNotBlank() ||
                t.minMarks.isNotBlank() ||
                t.syllabus.isNotBlank()
    }


    private fun hasValidMarksRange(t: com.vs.schoolmessenger.School.ClassTest.Class.Models.TestEntry): Boolean {
        val maxVal = t.maxMarks.trim().toDoubleOrNull()
        val minVal = t.minMarks.trim().toDoubleOrNull()
        if (maxVal == null || minVal == null) return true
        return minVal < maxVal
    }

    private fun saveCurrentStateToCache() {
        if (!::adapter.isInitialized) return

        Constant.isSavedExamNameState = binding.etExamName.text?.toString()

        val snapshot = adapter.getAllItems().map { item ->
            item.copy(
                tests = item.tests
                    .filter { isTestFilled(it) }
                    .map { it.copy() }
                    .toMutableList()
            )
        }
        Constant.isSavedClassTestState = snapshot.toMutableList()
    }

//    private fun setupKeyboardScrollBehavior() {
//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
//            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
//            if (imeVisible) {
//                val focused = currentFocus
//                if (focused is EditText) {
//                    focused.post {
//                        val location = IntArray(2)
//                        focused.getLocationInWindow(location)
//                        val rootLocation = IntArray(2)
//                        binding.rcClassList.getLocationInWindow(rootLocation)
//
//                        val focusedBottomInList = location[1] - rootLocation[1] + focused.height
//                        val listVisibleBottom = binding.rcClassList.height
//
//                        if (focusedBottomInList > listVisibleBottom) {
//                            binding.rcClassList.smoothScrollBy(
//                                0,
//                                focusedBottomInList - listVisibleBottom + 40
//                            )
//                        }
//                    }
//                }
//            }
//            insets
//        }
//    }

    private fun setupStepIndicator() {
        StepIndicatorHelper.setStep(binding.stepIndicator.root, currentStep = 4)
    }

    private fun handleExitAttempt() {
        if (hasUnsavedData()) {
            showExitConfirmationDialog()
        } else {
            finish()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.discard_changes))
            .setMessage(getString(R.string.you_have_unsaved_test_details_if_you_exit_now_this_information_will_be_lost))
            .setPositiveButton(getString(R.string.exit)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun hasUnsavedData(): Boolean {
        val examName = binding.etExamName.text?.toString()?.trim().orEmpty()
        if (examName.isNotBlank()) return true

        if (!::adapter.isInitialized) return false

        return adapter.getAllItems().any { item ->
            item.tests.any { t ->
                t.examName.isNotBlank() ||
                        t.testDate.isNotBlank() ||
                        t.maxMarks.isNotBlank() ||
                        t.minMarks.isNotBlank() ||
                        t.syllabus.isNotBlank()
            }
        }
    }

    private fun loadSubjectData() {
        val selectedSubjects: List<SelectedSubject> = Constant.isSelectedSubjectss ?: emptyList()
        if (selectedSubjects.isEmpty()) {
            showError(getString(R.string.no_subjects_selected))
            return
        }

        val savedItems = Constant.isSavedClassTestState

        val matchesSelection = savedItems != null &&
                savedItems.size == selectedSubjects.size &&
                savedItems.map { it.subjectId to it.sectionId }.toSet() ==
                selectedSubjects.map { it.subjectId to it.sectionId }.toSet()

        val items = if (matchesSelection) {
            savedItems!!.mapIndexed { index, saved ->
                saved.copy(
                    tests = saved.tests
                        .filter { isTestFilled(it) }
                        .map { it.copy() }
                        .toMutableList(),
                    isExpanded = index == 0
                )
            }
        } else {
            selectedSubjects.mapIndexed { index, s ->
                ClassTestItem(
                    subjectId = s.subjectId,
                    subjectName = s.subjectName,
                    sectionId = s.sectionId,
                    sectionLabel = getString(R.string.section, s.sectionName),
                    isMerged = false,
                    mergedSections = emptyList(),
                    mergedSectionIds = emptyList(),
                    isExpanded = index == 0
                )
            }
        }

        buildAdapter(items)
    }

    private fun buildAdapter(items: List<ClassTestItem>) {
        items.firstOrNull()?.isExpanded = true
        adapter = ClassAdapter(this,items.toMutableList())
        binding.rcClassList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ClassActivity.adapter
        }
        binding.rcClassList.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }
    private fun setupContinueButton() {
        binding.btnBack.setOnClickListener { handleExitAttempt() }

        binding.btnContinue.setOnClickListener {
            if (!::adapter.isInitialized) return@setOnClickListener
            val globalExamName = binding.etExamName.text?.toString()?.trim() ?: ""
            if (globalExamName.isBlank()) {
                binding.etExamName.background = ContextCompat.getDrawable(
                    this, R.drawable.input_field_bg_error
                )
                binding.etExamName.error = getString(R.string.exam_name_is_required)
                binding.etExamName.requestFocus()
                Toast.makeText(this,
                    getString(R.string.please_enter_the_exam_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                binding.etExamName.background = ContextCompat.getDrawable(
                    this, R.drawable.input_field_bg
                )
            }

            val allItems = adapter.getAllItems()

            val touchedItems = allItems.filter { item ->
                item.tests.any { t ->
                    t.examName.isNotBlank() &&
                            t.minMarks.isNotBlank() &&
                            t.maxMarks.isNotBlank()
                }
            }

            if (touchedItems.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.please_fill_in_at_least_one_subject_s_test_details),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val hasIncomplete = touchedItems.any { item ->
                item.tests.any { t ->
                    t.examName.isBlank() || t.minMarks.isBlank() || t.maxMarks.isBlank()
                }
            }

            if (hasIncomplete) {
                Toast.makeText(
                    this,
                    getString(R.string.please_fill_activity_name_and_marks_for_every_test),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val hasInvalidMarks = touchedItems.any { item ->
                item.tests.any { t -> !hasValidMarksRange(t) }
            }

            if (hasInvalidMarks) {
                Toast.makeText(
                    this,
                    getString(R.string.min_marks_must_be_less_than_max_marks_for_every_activity),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Constant.isExamName = globalExamName
            Constant.isClassTestItems = touchedItems
            startActivity(Intent(this, ReviewActivity::class.java))
        }

        binding.etExamName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!s.isNullOrBlank()) {
                    binding.etExamName.background = ContextCompat.getDrawable(
                        this@ClassActivity, R.drawable.input_field_bg
                    )
                }
            }
        })
    }

    private fun showError(message: String) {
        binding.rcClassList.visibility = View.GONE
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = message
    }

    private fun RedirectToReport() {
        val intent = Intent(this, ExamReportActivity::class.java)
        startActivity(intent)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.viewreporttext-> RedirectToReport()
        }
    }
}