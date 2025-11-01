package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary

import android.app.Activity
import android.content.Context
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
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanClickListener
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanCreate.LessonPlanCreateActivity
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEdit.LessonPlanEditActivity
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
import androidx.core.view.isVisible

class LessonPlanViewDetails : BaseActivity<LessonplanViewDetailsBinding>(), View.OnClickListener,
    LessonPlanClickListener, OnDateSelectedListener {

    override fun getViewBinding(): LessonplanViewDetailsBinding {
        return LessonplanViewDetailsBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var lessonplanViewAdapter: LessonPlanAdapter
    private var sectionSubjectId: String? = null
    private var request_type: String? = null
    private var currentStatus: Int = 0

    private var fullLessonPlanList: List<LessonPlanViewSummaryItem> = listOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.lesson_plan)
        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.allbutton1.setOnClickListener(this)
        binding.ytsbutton1.setOnClickListener(this)
        binding.inprogressbutton1.setOnClickListener(this)
        binding.completedbutton1.setOnClickListener(this)



        sectionSubjectId = intent.getStringExtra(Constant.section_subject_id)
        request_type = intent.getStringExtra(Constant.request_type)

        if (request_type == Constant.allclass) {
            Log.d("Request Type",request_type.toString())
            binding.createlp.visibility = View.GONE
        } else {
            Log.d("Request Type",request_type.toString())
            binding.createlp.visibility = View.VISIBLE
        }


        binding.createlp.setOnClickListener(this)


        if (sectionSubjectId.isNullOrEmpty() || request_type.isNullOrEmpty()) {
            finish()
            return
        }

        setupRecycler()
        highlightSelectedTab(binding.allbutton1)
        fetchLessonPlanData(sectionSubjectId)

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.isVisible) {
                binding.txtSearchMenu1.text.clear()
                binding.rytSearch1.visibility = View.GONE
                binding.root.hideKeyboard()
            } else {
                binding.txtSearchMenu1.text.clear()
                binding.rytSearch1.visibility = View.VISIBLE
            }
        }

        appViewModel?.getlpViewReport?.observe(this) { response ->
            if (response != null && response.status) {
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                binding.rcyLessonViewPlan.visibility = View.VISIBLE
                binding.tabLayout1.visibility = View.VISIBLE

                islpViewData(response.data)
            } else {
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                binding.tabLayout1.visibility = View.GONE
                binding.rcyLessonViewPlan.visibility = View.GONE
            }
        }

        appViewModel!!.islessonplandelete?.observe(this) { response ->
            Constant.hideLoading(this@LessonPlanViewDetails)
            if (response != null) {
                showTopAlertLessonPlanViewPopup(response.message, this)
            } else {
                showTopAlertLessonPlanViewPopup(
                    getString(R.string.something_went_wrong_please_try_again_later), this
                )
            }
        }

        binding.txtSearchMenu1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::lessonplanViewAdapter.isInitialized) {
                    lessonplanViewAdapter.filter.filter(s)
                    Log.d("Search", s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_details_found)
            binding.rcyLessonViewPlan.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcyLessonViewPlan.visibility = View.VISIBLE
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setupRecycler() {
        lessonplanViewAdapter = LessonPlanAdapter(
            null, this, this, Constant.isShimmerViewShow, request_type ?: ""
        )
        binding.rcyLessonViewPlan.layoutManager = LinearLayoutManager(this)
        binding.rcyLessonViewPlan.isNestedScrollingEnabled = false
        binding.rcyLessonViewPlan.adapter = lessonplanViewAdapter
    }

    private fun fetchLessonPlanData(sectionSubjectId: String?) {
        lessonplanViewAdapter = LessonPlanAdapter(
            null, this, this, Constant.isShimmerViewShow, request_type ?: ""
        )
        binding.rcyLessonViewPlan.adapter = lessonplanViewAdapter

        appViewModel?.getlpViewReport(
            isToken = isAccessToken!!,
            section_subject_id = sectionSubjectId!!,
            lesson_plan_status = 0,
            activity = this@LessonPlanViewDetails
        )
    }

    private fun islpViewData(data: List<LessonPlanViewSummaryItem>?) {
        fullLessonPlanList = data ?: listOf()
        filterAndShowData(currentStatus)
    }

    private fun filterAndShowData(status: Int) {
        val filteredList = when (status) {
            1 -> fullLessonPlanList.filter { it.lesson_plan_status == 1 }
            2 -> fullLessonPlanList.filter { it.lesson_plan_status == 2 }
            3 -> fullLessonPlanList.filter { it.lesson_plan_status == 3 }
            else -> fullLessonPlanList
        }

        if (filteredList.isEmpty()) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_lesson_plans_found)
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.rcyLessonViewPlan.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            binding.rcyLessonViewPlan.visibility = View.VISIBLE
        }

        lessonplanViewAdapter = LessonPlanAdapter(
            filteredList, this, this, Constant.isShimmerViewDisable, request_type ?: ""
        )
        binding.rcyLessonViewPlan.adapter = lessonplanViewAdapter
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.allbutton1 -> {
                binding.txtSearchMenu1.text.clear()
                currentStatus = 0
                highlightSelectedTab(binding.allbutton1)
                filterAndShowData(currentStatus)
            }

            R.id.ytsbutton1 -> {
                binding.txtSearchMenu1.text.clear()
                currentStatus = 1
                highlightSelectedTab(binding.ytsbutton1)
                filterAndShowData(currentStatus)

            }

            R.id.inprogressbutton1 -> {
                binding.txtSearchMenu1.text.clear()
                currentStatus = 2
                highlightSelectedTab(binding.inprogressbutton1)
                filterAndShowData(currentStatus)
            }

            R.id.completedbutton1 -> {
                binding.txtSearchMenu1.text.clear()
                currentStatus = 3
                highlightSelectedTab(binding.completedbutton1)
                filterAndShowData(currentStatus)
            }

            R.id.createlp -> {
                RedirectToCreateLp()
            }
        }
    }

//    private fun highlightSelectedTab(selectedView: View) {
//        val buttons = listOf(
//            binding.allbutton, binding.ytsbutton, binding.inprogressbutton, binding.completedbutton
//        )
//        buttons.forEach {
//            it.isEnabled = true
//            it.setBackgroundResource(R.drawable.light_gray_radius)
//        }
//        selectedView.setBackgroundResource(R.drawable.theme_colour_radius)
//        selectedView.isEnabled = false
//    }


    private fun RedirectToCreateLp() {
        val intent = Intent(this@LessonPlanViewDetails, LessonPlanCreateActivity::class.java)
        intent.putExtra(Constant.section_subject_id, sectionSubjectId)
        Log.d("section_subject_id", sectionSubjectId.toString())
        intent.putExtra(Constant.request_type, request_type)
        Log.d("request_type", request_type.toString())
        startActivity(intent)
    }

    private fun highlightSelectedTab(selectedView: View) {
        // Each tab = container, imageView, textView
        val tabs = listOf(
            Triple(binding.allbutton1, binding.imgAll, binding.allbutton),
            Triple(binding.ytsbutton1, binding.imgYet, binding.ytsbutton),
            Triple(binding.inprogressbutton1, binding.imgProgress, binding.inprogressbutton),
            Triple(binding.completedbutton1, binding.imgComplete, binding.completedbutton)
        )

        tabs.forEach { (container, imageView, textView) ->
            if (container == selectedView) {
                // Selected background
                val drawable =
                    ContextCompat.getDrawable(this, R.drawable.theme_colour_radius)?.mutate()
                drawable?.setTint(ContextCompat.getColor(this, R.color.PrimaryColor))
                container.background = drawable

                // Selected text
                textView.setTextColor(ContextCompat.getColor(this, R.color.white))
                container.isEnabled = false

                // Selected image
                imageView?.setColorFilter(ContextCompat.getColor(this, R.color.white))
            } else {
                // Unselected background
                val drawable =
                    ContextCompat.getDrawable(this, R.drawable.light_gray_radius)?.mutate()
                container.background = drawable
                container.isEnabled = true

                // Unselected text
                textView.setTextColor(ContextCompat.getColor(this, R.color.black))

                // Reset image tint
                when (imageView?.id) {
                    R.id.imgAll -> imageView.setColorFilter(
                        ContextCompat.getColor(
                            this,
                            R.color.black
                        )
                    )

                    R.id.imgYet -> imageView.setColorFilter(
                        ContextCompat.getColor(
                            this,
                            R.color.dark_orange
                        )
                    )

                    R.id.imgProgress -> imageView.setColorFilter(
                        ContextCompat.getColor(
                            this,
                            R.color.PrimaryColor
                        )
                    )

                    R.id.imgComplete -> imageView.setColorFilter(
                        ContextCompat.getColor(
                            this,
                            R.color.green
                        )
                    )
                }
            }
        }
    }


    override fun onEditItem(data: LessonPlanViewSummaryItem) {
        val intent = Intent(this@LessonPlanViewDetails, LessonPlanEditActivity::class.java)
        intent.putExtra(Constant.particular_id, data.particular_id)
        intent.putExtra(Constant.request_type, request_type)
        intent.putExtra(Constant.section_subject_id, sectionSubjectId)
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
                put(Constant.particular_id, data.particular_id)
            }
            val requestBody = requestJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            appViewModel?.islessonplandelete(isAccessToken ?: "", requestBody, this)
            Constant.showLoading(this)
            closePopup()
        }

        btnCancel.setOnClickListener { closePopup() }
    }

    override fun onDeleteItem(data: LessonPlanViewSummaryItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showTopDeleteAlertPopup(data)
        } else {
            Toast.makeText(
                this,
                getString(R.string.delete_popup_requires_android_o_or_higher), Toast.LENGTH_SHORT
            )
                .show()
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
            isAwsUploadedFiles.clear()
            selectedFiles.clear()
            val intent = Intent(activity, LessonPlanViewDetails::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(Constant.section_subject_id, sectionSubjectId)
            intent.putExtra(Constant.request_type, request_type)
            activity.startActivity(intent)
            closePopup()
        }
    }

    override fun onDateSelected(date: String) {}

    override fun onResume() {
        super.onResume()
        fetchLessonPlanData(sectionSubjectId)
    }
}

