package com.vs.schoolmessenger.School.LessonPlan.LessonPlanEdit

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.EditClassData
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.LessonPlanEditClickListener
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary.LessonPlanViewDetails
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LessonPlanEditBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LessonPlanEditActivity : BaseActivity<LessonPlanEditBinding>(),
    View.OnClickListener, LessonPlanEditClickListener {

    override fun getViewBinding(): LessonPlanEditBinding {
        return LessonPlanEditBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var particularId: String = ""
    private var requestType: String = ""
    private var sectionSubjectId: String = ""

    private lateinit var lessonplaneditAdapter: LessonPlanEditAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token
        binding.toolbarLayout.lblParentToolBar.text = "Edit Lesson Plan"
        binding.toolbarLayout.lblSchoolName.apply {
            visibility = View.VISIBLE
            text = isStaffDetails?.school_name
        }
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.updatebutton.setOnClickListener(this)
        binding.cancelbutton.setOnClickListener(this)
        sectionSubjectId = intent.getStringExtra("section_subject_id") ?: ""
        particularId = intent.getStringExtra("particular_id") ?: ""
        requestType = intent.getStringExtra("request_type") ?: ""
        Log.d("particular_id", particularId)
        Log.d("request_type", requestType)
        appViewModel?.getlpeditReport?.observe(this) { response ->
            if (response?.status == true) {
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.rcyLessonPlanEdit.visibility = View.VISIBLE
                bindLessonPlanData(response.data)
            } else {
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.rcyLessonPlanEdit.visibility = View.GONE
            }
        }
        loadLessonPlanData()

        appViewModel!!.isupdatelessonplan?.observe(this) { response ->
            Constant.hideLoading(this@LessonPlanEditActivity)

            if (response != null) {
                if (response.status) {
                    Log.d("UpdateSuccess", "Lesson plan updated successfully")
                    showTopLessonPlanAlertPopup(response.message, this)
                } else {
                    Log.w("UpdateFailed", "Lesson plan update failed: ${response.message}")
                    showTopLessonPlanAlertPopup(
                        response.message ?: "Update failed. Try again later.", this
                    )
                }
            } else {
                Log.e("UpdateError", "Null response received from server.")
                showTopLessonPlanAlertPopup("Something went wrong. Please try again later.", this)
            }
        }

    }

    private fun bindLessonPlanData(data: List<EditClassData>?) {
        lessonplaneditAdapter = LessonPlanEditAdapter(
            data ?: emptyList(), this, this, false, particularId, requestType
        )
        binding.rcyLessonPlanEdit.adapter = lessonplaneditAdapter
    }

    private fun loadLessonPlanData() {
        lessonplaneditAdapter = LessonPlanEditAdapter(
            emptyList(), this, this, true, particularId, requestType
        )
        binding.rcyLessonPlanEdit.apply {
            layoutManager = LinearLayoutManager(this@LessonPlanEditActivity)
            isNestedScrollingEnabled = false
            adapter = lessonplaneditAdapter
        }
        appViewModel?.getlpeditReport(isAccessToken ?: "", particularId, requestType, this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.updatebutton -> {
                showTopEditAlertPopup()
            }

            R.id.cancelbutton -> {
                lessonplaneditcancel()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun lessonplaneditupdate() {
        val keyValueData = lessonplaneditAdapter.getUpdatedFieldsForApi()
        if (keyValueData.length() == 0) {
            Toast.makeText(this, "No editable data to update", Toast.LENGTH_SHORT).show()
            return
        }
        val requestJson = JSONObject().apply {
            put("particular_id", particularId)
            put("key_value_data", keyValueData)
        }
        Log.d("LessonPlanUpdateRequest", requestJson.toString())
        val requestBody = requestJson.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        appViewModel?.isupdatelessonplan(isAccessToken ?: "", requestBody, this)
        Constant.showLoading(this@LessonPlanEditActivity)
    }

    fun lessonplaneditcancel() {
        onBackPressed()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun showTopEditAlertPopup() {
        val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.warning_update, null)
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
            lessonplaneditupdate()
            closePopup()
        }

        btnCancel.setOnClickListener {
            closePopup()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun showTopLessonPlanAlertPopup(message: String, activity: Activity) {
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
                isAwsUploadedFiles.clear()
                selectedFiles.clear()
                val intent = Intent(activity, LessonPlanViewDetails::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("section_subject_id", sectionSubjectId)
                intent.putExtra("request_type", requestType)
                activity.startActivity(intent)
                activity.finish()
            } catch (e: Exception) {
                Log.e("LessonPlanPopup", "Redirection failed: ${e.localizedMessage}")
                Toast.makeText(activity, "Oops! Couldn’t go back.", Toast.LENGTH_SHORT).show()
            } finally {
                closePopup()
            }
        }

        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true

    }

}
