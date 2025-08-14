package com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary

import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary.LessonPlanViewDetails
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel.AllClassData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LessonPlanBinding

class LessonPlan : BaseActivity<LessonPlanBinding>(), View.OnClickListener,
    LessonPlanChartClickListener {

    override fun getViewBinding(): LessonPlanBinding {
        return LessonPlanBinding.inflate(layoutInflater)
    }

    private var currentRequestType = "allclass"

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var lessonplanAdapter: LessonPlanPicChartAdapter

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()
        binding.toolbarLayout.rytSearch.visibility = View.GONE

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)

        appViewModel?.getlpStaffReport?.observe(this) { response ->
            if (response != null && response.status) {
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.rytSearch1.visibility = View.VISIBLE
                binding.rcyLessonPlan.visibility = View.VISIBLE
                islpStaffData(response.data, currentRequestType)
            } else {
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.rytSearch1.visibility = View.GONE
                binding.rcyLessonPlan.visibility = View.GONE
            }
        }

        loadlpAllClassdata("allclass")


        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::lessonplanAdapter.isInitialized) {
                    lessonplanAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


    }

    private fun islpStaffData(data: List<AllClassData>?, requestType: String) {
        lessonplanAdapter = LessonPlanPicChartAdapter(
            data, this, this, Constant.isShimmerViewDisable, requestType
        )
        binding.rcyLessonPlan.adapter = lessonplanAdapter
    }

    private fun loadlpAllClassdata(requestType: String) {
        if (requestType == "allclass") {
            binding.btnCreate.isEnabled = false
            binding.btnHistory.isEnabled = true
        }
        if (requestType == "myclass") {
            binding.btnHistory.isEnabled = false
            binding.btnCreate.isEnabled = true
        }
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
            binding.txtNoData.text = "No matching lesson plan found"
            binding.rcyLessonPlan.visibility = View.GONE
        } else {

            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcyLessonPlan.visibility = View.VISIBLE
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCreate -> {
                binding.btnCreate.setTextColor(Color.BLACK)
                binding.btnCreate.background =
                    ContextCompat.getDrawable(this, R.drawable.white_radious)
                binding.btnHistory.setTextColor(Color.BLACK)
                binding.btnHistory.setBackgroundResource(R.drawable.bg_light_blue)
                loadlpAllClassdata("allclass")
            }

            R.id.btnHistory -> {

                binding.btnHistory.setTextColor(Color.BLACK)
                binding.btnHistory.background =
                    ContextCompat.getDrawable(this, R.drawable.white_radious)
                binding.btnCreate.setTextColor(Color.BLACK)
                binding.btnCreate.setBackgroundResource(R.drawable.bg_light_blue)
                loadlpAllClassdata("myclass")
            }

            R.id.imgBack -> onBackPressed()
        }
    }

    override fun onItem(data: AllClassData, requestType: String) {
        val intent = Intent(this@LessonPlan, LessonPlanViewDetails::class.java)
        intent.putExtra("section_subject_id", data.section_subject_id)
        Log.d("section_subject_id", data.section_subject_id.toString())
        intent.putExtra("request_type", requestType)
        Log.d("request_type", requestType)
        startActivity(intent)
    }
}