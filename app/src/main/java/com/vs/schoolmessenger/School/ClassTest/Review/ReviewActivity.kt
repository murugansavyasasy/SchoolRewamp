package com.vs.schoolmessenger.School.ClassTest.Review

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ClassTest.Standard.StandardActivity
import com.vs.schoolmessenger.School.ClassTest.StepIndicatorHelper
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary.LessonPlanViewDetails
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ReviewActivityBinding


class ReviewActivity : BaseActivity<ReviewActivityBinding>() {

    private lateinit var adapter: ReviewAdapter

    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    private var isStaffDetails: StaffDetails? = null

    override fun getViewBinding(): ReviewActivityBinding {
        return ReviewActivityBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        window.statusBarColor = resources.getColor(R.color.PrimaryColor, theme)
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            binding.statusBarBackground.layoutParams.height =
                resources.getDimensionPixelSize(resourceId)
            binding.statusBarBackground.requestLayout()
        }
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        setupStepIndicator()
        loadReviewData()
        setupButtons()
        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        appViewModel!!.isCreateClasstest?.observe(this) { response ->
            showLoading(false)
            if (response != null) {
                if (response.status) {
                    Constant.clearClassTestFlowData()
                    showReviewAlertPopup(response.message, this)
                } else {
                    showReviewAlertPopup(
                        response.message ?: getString(R.string.create_failed_try_again_later), this
                    )
                }
            } else {
                showReviewAlertPopup(getString(R.string.something_went_wrong_please_try_again_later), this)
            }
        }
    }


    fun showReviewAlertPopup(message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            try {
                val intent = Intent(activity, StandardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                activity.finish()
            } catch (e: Exception) {
                Toast.makeText(
                    activity,
                    getString(R.string.oops_couldn_t_go_back), Toast.LENGTH_SHORT
                ).show()
            } finally {
                closePopup()
            }
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true
    }

    private fun setupStepIndicator() {
        StepIndicatorHelper.setStep(binding.stepIndicator.root, currentStep = 5)
    }

    private fun loadReviewData() {
        val items = Constant.isClassTestItems
        if (items.isNullOrEmpty()) {
            showError("No test data found")
            return
        }
        binding.examName.text = Constant.isExamName

        adapter = ReviewAdapter(
            items = items,
            onRemoveTest = { subjectIndex, testIndex ->
                val subject = items[subjectIndex]
                if (testIndex in subject.tests.indices) {
                    subject.tests.removeAt(testIndex)
                }
                adapter.notifyItemChanged(subjectIndex)
                updateSubmitButtonState()
            }
        )
        binding.rcReviewList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ReviewActivity.adapter
        }
        binding.rcReviewList.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE

        updateSubmitButtonState()
    }

    private fun updateSubmitButtonState() {
        val items = Constant.isClassTestItems ?: emptyList()
        val totalTests = items.sumOf { it.tests.size }
        val hasEmptySubject = items.any { it.tests.isEmpty() }
        val canSubmit = totalTests > 0 && !hasEmptySubject

        binding.btnSubmit.isEnabled = canSubmit
        binding.btnSubmit.alpha = if (canSubmit) 1f else 0.5f
        binding.btnSubmit.text = when {
            totalTests == 0     -> "No activities to create"
            hasEmptySubject     -> "Remove empty subjects to continue"
            else                -> "  Create $totalTests Activities"
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnSubmit.setOnClickListener { submitClassTest() }
    }

    private fun submitClassTest() {
        val items = Constant.isClassTestItems
        val examName = Constant.isExamName

        if (items.isNullOrEmpty()) {
            Toast.makeText(this, "No test data found", Toast.LENGTH_SHORT).show()
            return
        }
        if (items.sumOf { it.tests.size } == 0) {
            Toast.makeText(this, "Please add at least one activity before submitting", Toast.LENGTH_SHORT).show()
            return
        }
        if (items.any { it.tests.isEmpty() }) {
            Toast.makeText(this, "Some subjects have no activities. Remove them first.", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonArray = JsonArray()
        items.forEach { subject ->
            subject.tests.forEach { test ->
                val obj = JsonObject().apply {
                    addProperty("exam_name", examName)
                    addProperty("section_id", subject.sectionId)
                    addProperty("subject_id", subject.subjectId)
                    addProperty("date", formatDateForApi(test.testDate))
                    addProperty("session", test.session)
                    addProperty("activity_name", test.examName)
                    addProperty("max_mark", test.maxMarks.toIntOrNull() ?: 0)
                    addProperty("min_mark", test.minMarks.toIntOrNull() ?: 0)
                    addProperty("syllabus", test.syllabus)
                }
                jsonArray.add(obj)
            }
        }
        showLoading(true)
        appViewModel?.isCreateClasstest(isAccessToken ?: "", jsonArray, this)
    }


    private fun formatDateForApi(date: String): String {
        return date.replace("/", "-")
    }



    private fun showLoading(show: Boolean) {
        binding.btnSubmit.isEnabled = !show
        if (show) {
            binding.btnSubmit.text = "Submitting..."
        } else {
            updateSubmitButtonState()
        }
    }

    private fun showError(message: String) {
        binding.rcReviewList.visibility = View.GONE
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = message
    }
}