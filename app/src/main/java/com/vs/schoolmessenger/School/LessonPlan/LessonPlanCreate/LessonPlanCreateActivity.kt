package com.vs.schoolmessenger.School.LessonPlan.LessonPlanCreate

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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanCreateModel.LessonPlanCreateClickListener
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanCreateModel.LessonPlanTemplate
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary.LessonPlanViewDetails
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LessonPlanCreateBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LessonPlanCreateActivity : BaseActivity<LessonPlanCreateBinding>(), View.OnClickListener, LessonPlanCreateClickListener {

    override fun getViewBinding(): LessonPlanCreateBinding {
        return LessonPlanCreateBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var particularId: String = ""
    private var requestType: String = ""
    private var sectionSubjectId: String = ""
    private lateinit var lessonplancreateAdapter: LessonPlanCreateAdapter


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token
        binding.toolbarLayout.lblParentToolBar.text = "Create Lesson Plan"
        binding.toolbarLayout.lblSchoolName.apply {
            visibility = View.VISIBLE
            text = isStaffDetails?.school_name
        }



        sectionSubjectId = intent.getStringExtra(Constant.section_subject_id) ?: ""
        particularId = intent.getStringExtra(Constant.particular_id) ?: ""
        requestType = intent.getStringExtra(Constant.request_type) ?: ""
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.createbutton.setOnClickListener(this)
        binding.cancelbutton.setOnClickListener(this)


        appViewModel?.getlpcreateReport?.observe(this) { response ->
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


        appViewModel!!.iscreatelessonplan?.observe(this) { response ->
            Constant.hideLoading(this@LessonPlanCreateActivity)

            if (response != null) {
                if (response.status) {
                    Log.d("Created Successfully", "Lesson plan Created successfully")
                    showTopLessonPlanAlertPopup(response.message, this)
                } else {
                    Log.w("Createfailed", "Lesson plan Create failed: ${response.message}")
                    showTopLessonPlanAlertPopup(
                        response.message ?: "Create failed. Try again later.", this
                    )
                }
            } else {
                Log.e("UpdateError", "Null response received from server.")
                showTopLessonPlanAlertPopup(getString(R.string.something_went_wrong_please_try_again_later), this)
            }
        }

    }


    private fun bindLessonPlanData(data: List<LessonPlanTemplate>?) {
        lessonplancreateAdapter = LessonPlanCreateAdapter(
            data ?: emptyList(), this, this, false, particularId, requestType
        )
        binding.rcyLessonPlanEdit.adapter = lessonplancreateAdapter
    }


    private fun loadLessonPlanData() {
        lessonplancreateAdapter = LessonPlanCreateAdapter(
            emptyList(), this, this, true, particularId, requestType
        )
        binding.rcyLessonPlanEdit.apply {
            layoutManager = LinearLayoutManager(this@LessonPlanCreateActivity)
            isNestedScrollingEnabled = false
            adapter = lessonplancreateAdapter
        }
        appViewModel?.getlpcreateReport(isAccessToken ?: "", requestType, this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.createbutton -> {
                showTopEditAlertPopup()
            }
            R.id.cancelbutton -> {
                lessonplaneditcancel()
            }
        }
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
    fun lessonplaneditupdate() {
        val keyValueData = lessonplancreateAdapter.getUpdatedFieldsForApi()

        if (keyValueData.length() == 0) {
            Toast.makeText(
                this, "No data to update",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val requestJson = JSONObject().apply {
            put(APIKeyNames.section_subject_id, sectionSubjectId)
            put(APIKeyNames.key_value_data, keyValueData)
        }

        Log.d("LessonPlanCreateRequest", requestJson.toString())

        val requestBody = requestJson.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        appViewModel?.iscreatelessonplan(isAccessToken ?: "", requestBody, this)
        Constant.showLoading(this@LessonPlanCreateActivity)
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
                intent.putExtra(Constant.section_subject_id, sectionSubjectId)
                intent.putExtra(Constant.request_type, requestType)
                activity.startActivity(intent)
                activity.finish()
            } catch (e: Exception) {
                Log.e("LessonPlanPopup", "Redirection failed: ${e.localizedMessage}")
                Toast.makeText(activity,
                    getString(R.string.oops_couldn_t_go_back), Toast.LENGTH_SHORT).show()
            } finally {
                closePopup()
            }
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true
    }


    fun lessonplaneditcancel() {
        onBackPressed()
    }

}
