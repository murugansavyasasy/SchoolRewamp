package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanClickListener
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanData
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEdit.LessonPlanEditActivity
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary.LessonPlan
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryDetail
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LessonplanViewDetailsBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LessonPlanViewDetails : BaseActivity<LessonplanViewDetailsBinding>(),
    View.OnClickListener, LessonPlanClickListener, OnDateSelectedListener {

    override fun getViewBinding(): LessonplanViewDetailsBinding {
        return LessonplanViewDetailsBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var lessonplanViewAdapter: LessonPlanAdapter
    private lateinit var lessonplandetailViewAdapter: LessonPlanDetailAdapter
    private var sectionSubjectId: String? = null
    private var request_type: String? = null
    private var currentStatus: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblParentToolBar.text = "Lesson Plan"
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.allbutton.setOnClickListener(this)
        binding.ytsbutton.setOnClickListener(this)
        binding.inprogressbutton.setOnClickListener(this)
        binding.completedbutton.setOnClickListener(this)

        sectionSubjectId = intent.getStringExtra("section_subject_id")
        request_type = intent.getStringExtra("request_type")

        if (sectionSubjectId.isNullOrEmpty() || request_type.isNullOrEmpty()) {
            finish()
            return
        }


        setupRecycler()
        highlightSelectedTab(binding.allbutton)
        loadLessonPlanData(currentStatus, sectionSubjectId)

        appViewModel?.getlpViewReport?.observe(this) { response ->
            if (response != null && response.status) {
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.rcyLessonViewPlan.visibility = View.VISIBLE
                islpViewData(response.data)
            } else {
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.rcyLessonViewPlan.visibility = View.GONE
            }
        }

        appViewModel!!.islessonplandelete?.observe(this) { response ->
            Constant.hideLoading(this@LessonPlanViewDetails)

            if (response != null) {
                if (response.status) {
                    Log.d("UpdateSuccess", "Lesson plan updated successfully")
                    showTopAlertLessonPlanViewPopup(response.message, this)
                } else {
                    Log.w("UpdateFailed", "Lesson plan update failed: ${response.message}")
                    showTopAlertLessonPlanViewPopup(
                        response.message ?: "Update failed. Try again later.", this
                    )
                }
            } else {
                Log.e("UpdateError", "Null response received from server.")
                showTopAlertLessonPlanViewPopup("Something went wrong. Please try again later.", this)
            }
        }

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::lessonplanViewAdapter.isInitialized) {
                    lessonplanViewAdapter.filter.filter(s)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })


    }

    private fun setupRecycler() {
        lessonplanViewAdapter = LessonPlanAdapter(
            null, this, this, Constant.isShimmerViewShow, request_type ?: ""
        )

        binding.rcyLessonViewPlan.layoutManager = LinearLayoutManager(this)
        binding.rcyLessonViewPlan.isNestedScrollingEnabled = false
        binding.rcyLessonViewPlan.adapter = lessonplanViewAdapter
    }

    private fun loadLessonPlanData(status: Int, sectionSubjectId: String?) {
        lessonplanViewAdapter = LessonPlanAdapter(
            null, this, this, Constant.isShimmerViewShow, request_type ?: ""
        )

        binding.rcyLessonViewPlan.adapter = lessonplanViewAdapter

        appViewModel?.getlpViewReport(
            isToken = isAccessToken!!,
            section_subject_id = sectionSubjectId!!,
            lesson_plan_status = status,
            activity = this@LessonPlanViewDetails
        )
    }


    private fun islpViewData(data: List<LessonPlanViewSummaryItem>?) {
        lessonplanViewAdapter = LessonPlanAdapter(
            data, this, this, Constant.isShimmerViewDisable, request_type ?: ""
        )

        binding.rcyLessonViewPlan.adapter = lessonplanViewAdapter
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {

            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = "No matching details found"
            binding.rcyLessonViewPlan.visibility = View.GONE
        } else {

            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcyLessonViewPlan.visibility = View.VISIBLE
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.allbutton -> {
                currentStatus = 0
                highlightSelectedTab(binding.allbutton)
                loadLessonPlanData(currentStatus, sectionSubjectId)
            }

            R.id.ytsbutton -> {
                currentStatus = 1
                highlightSelectedTab(binding.ytsbutton)
                loadLessonPlanData(currentStatus, sectionSubjectId)
            }

            R.id.inprogressbutton -> {
                currentStatus = 2
                highlightSelectedTab(binding.inprogressbutton)
                loadLessonPlanData(currentStatus, sectionSubjectId)
            }

            R.id.completedbutton -> {
                currentStatus = 3
                highlightSelectedTab(binding.completedbutton)
                loadLessonPlanData(currentStatus, sectionSubjectId)
            }
        }
    }

    private fun highlightSelectedTab(selectedView: View) {
        binding.allbutton.isEnabled = true
        binding.ytsbutton.isEnabled = true
        binding.inprogressbutton.isEnabled = true
        binding.completedbutton.isEnabled = true

        binding.allbutton.setBackgroundResource(R.drawable.light_gray_radius)
        binding.ytsbutton.setBackgroundResource(R.drawable.light_gray_radius)
        binding.inprogressbutton.setBackgroundResource(R.drawable.light_gray_radius)
        binding.completedbutton.setBackgroundResource(R.drawable.light_gray_radius)

        selectedView.setBackgroundResource(R.drawable.theme_colour_radius)
        selectedView.isEnabled = false
    }

    override fun onEditItem(data: LessonPlanViewSummaryItem) {
        val intent = Intent(this@LessonPlanViewDetails, LessonPlanEditActivity::class.java)
        intent.putExtra("particular_id", data.particular_id)
        intent.putExtra("request_type",request_type)
        intent.putExtra("section_subject_id",sectionSubjectId)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showTopDeleteAlertPopup(data: LessonPlanViewSummaryItem) {
        val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.delete_update, null)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)

        val dimView = View(this).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            val margin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics
            ).toInt()
            setMargins(margin, 0, margin, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            val requestJson = JSONObject().apply {
                put("particular_id", data.particular_id)
            }
            Log.d("LessonPlanUpdateRequest", requestJson.toString())
            val requestBody = requestJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            appViewModel?.islessonplandelete(isAccessToken ?: "", requestBody, this)
            Constant.showLoading(this)
            closePopup()
        }

        btnCancel.setOnClickListener {
            closePopup()
        }
    }

    override fun onDeleteItem(data: LessonPlanViewSummaryItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showTopDeleteAlertPopup(data)
        } else {
            Toast.makeText(this, "Delete popup requires Android O or higher", Toast.LENGTH_SHORT).show()
        }
    }





    @RequiresApi(Build.VERSION_CODES.O)
    fun showTopAlertLessonPlanViewPopup(message: String, activity: Activity) {
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
            isClickable = true // prevent clicks on background
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
            isAwsUploadedFiles.clear()
            selectedFiles.clear()
            val intent = Intent(activity, LessonPlanViewDetails::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("section_subject_id", sectionSubjectId)
            intent.putExtra("request_type", request_type)
            activity.startActivity(intent)
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true

    }

    override fun onDateSelected(date: String) {


    }

}
