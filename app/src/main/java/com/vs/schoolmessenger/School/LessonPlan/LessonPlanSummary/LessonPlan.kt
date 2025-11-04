package com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.interaction.DragInteraction
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel.AllClassData
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary.LessonPlanViewDetails
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LessonPlanBinding
import androidx.core.view.isVisible

class LessonPlan : BaseActivity<LessonPlanBinding>(), View.OnClickListener,
    LessonPlanChartClickListener {

    override fun getViewBinding(): LessonPlanBinding {
        return LessonPlanBinding.inflate(layoutInflater)
    }

    private var currentRequestType = Constant.allclass

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var lessonplanAdapter: LessonPlanPicChartAdapter

    @SuppressLint("UseKtx")
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.rytSearch.visibility = View.GONE

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        if (Constant.user_details!!.staff_role == "p3") {
            binding.lnrTabOneName.visibility = View.GONE
            binding.line1.visibility = View.GONE
            binding.line2.visibility = View.GONE
            binding.tabTwoName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            binding.tabTwoName.gravity = Gravity.START
            loadlpAllClassdata(Constant.myclass)
        } else {
            binding.lnrTabOneName.visibility = View.VISIBLE
            binding.line1.visibility = View.VISIBLE
            loadlpAllClassdata(Constant.allclass)
        }


        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.isVisible) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearchMenu1.text.clear()
                binding.root.hideKeyboard()
            } else {
                binding.txtSearchMenu1.text.clear()
                binding.rytSearch1.visibility = View.VISIBLE
            }
        }

        appViewModel?.getlpStaffReport?.observe(this) { response ->
            if (response != null && response.status) {
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.rcyLessonPlan.visibility = View.VISIBLE
                islpStaffData(response.data, currentRequestType)
            } else {
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text=response?.message?:getString(R.string.no_list_found)
                binding.rcyLessonPlan.visibility = View.GONE
            }
        }




        binding.lnrTabOneName.setOnClickListener {
            hideKeyboard()
            binding.lnrTabOneName.isEnabled = false
            binding.lnrTabTwoName.isEnabled = true
            binding.line1.setBackgroundResource(R.color.iconBlue)
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.line2.setBackgroundResource(R.color.athens_gray)
            binding.txtSearchMenu1.text.clear()
            binding.rytSearch1.visibility = View.GONE
            loadlpAllClassdata(Constant.allclass)


        }

        binding.lnrTabTwoName.setOnClickListener {
            if (Constant.user_details!!.staff_role == "p3") {
                Log.d("Empty Click", "Empty")
            } else {
                hideKeyboard()
                binding.lnrTabOneName.isEnabled = true
                binding.lnrTabTwoName.isEnabled = false
                binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
                binding.line2.setBackgroundResource(R.color.iconBlue)
                binding.line1.setBackgroundResource(R.color.athens_gray)
                binding.txtSearchMenu1.text.clear()
                binding.rytSearch1.visibility = View.GONE
                loadlpAllClassdata(Constant.myclass)
            }
        }

        binding.txtSearchMenu1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::lessonplanAdapter.isInitialized) {
                    lessonplanAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


    }

    fun Activity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun islpStaffData(data: List<AllClassData>?, requestType: String) {
        if (data.isNullOrEmpty()) {
            binding.rytSearch1.visibility = View.GONE
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        } else {
            binding.rytSearch1.visibility = View.GONE
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            lessonplanAdapter = LessonPlanPicChartAdapter(
                data, this, this, Constant.isShimmerViewDisable, requestType
            )
            binding.rcyLessonPlan.adapter = lessonplanAdapter
        }
    }

    private fun loadlpAllClassdata(requestType: String) {
        currentRequestType = requestType
        lessonplanAdapter =
            LessonPlanPicChartAdapter(null, this, this, Constant.isShimmerViewShow, requestType)
        binding.rcyLessonPlan.layoutManager = LinearLayoutManager(this)
        binding.rcyLessonPlan.isNestedScrollingEnabled = false
        binding.rcyLessonPlan.adapter = lessonplanAdapter

        appViewModel!!.getlpStaffReport(isAccessToken!!, requestType, this@LessonPlan)
    }


    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {

            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_lesson_plan_found)
            binding.rcyLessonPlan.visibility = View.GONE
        } else {

            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcyLessonPlan.visibility = View.VISIBLE
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    override fun onItem(data: AllClassData, requestType: String) {
        val intent = Intent(this@LessonPlan, LessonPlanViewDetails::class.java)
        intent.putExtra(Constant.section_subject_id, data.section_subject_id)
        Log.d("section_subject_id", data.section_subject_id.toString())
        intent.putExtra(Constant.request_type, requestType)
        Log.d("request_type", requestType)
        intent.putExtra(Constant.subject_name, data.subject_name)
        Log.d("subject_name", data.subject_name)
        intent.putExtra(Constant.items_completed, data.items_completed)
        Log.d("items_completed", data.items_completed)
        intent.putExtra(Constant.completed_items, data.completed_items)
        Log.d("completed_items", data.completed_items)
        intent.putExtra(Constant.total_items, data.total_items)
        Log.d("total_items", data.total_items)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (Constant.user_details!!.staff_role == "p3") {
            binding.lnrTabOneName.visibility = View.GONE
            binding.line1.visibility = View.GONE
            binding.line2.visibility = View.GONE
            binding.tabTwoName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            binding.tabTwoName.gravity = Gravity.START
            binding.lnrTabOneName.isEnabled = true
            binding.lnrTabTwoName.isEnabled = false
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.line2.setBackgroundResource(R.color.iconBlue)
            binding.line1.setBackgroundResource(R.color.athens_gray)
            binding.txtSearchMenu1.text.clear()
            binding.rytSearch1.visibility = View.GONE
            loadlpAllClassdata(Constant.myclass)
        } else {
            binding.lnrTabOneName.visibility = View.VISIBLE
            binding.line1.visibility = View.VISIBLE
            binding.lnrTabOneName.isEnabled = false
            binding.lnrTabTwoName.isEnabled = true
            binding.line1.setBackgroundResource(R.color.iconBlue)
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.line2.setBackgroundResource(R.color.athens_gray)
            binding.txtSearchMenu1.text.clear()
            binding.rytSearch1.visibility = View.GONE
            loadlpAllClassdata(Constant.allclass)
        }

    }
}