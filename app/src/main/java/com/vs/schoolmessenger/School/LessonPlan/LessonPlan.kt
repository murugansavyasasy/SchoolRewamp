package com.vs.schoolmessenger.School.LessonPlan

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LessonPlan.Model.AllClassData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LessonPlanBinding

class LessonPlan : BaseActivity<LessonPlanBinding>(),
    View.OnClickListener, LessonPlanChartClickListener {

    override fun getViewBinding(): LessonPlanBinding {
        return LessonPlanBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var lessonplanAdapter: LessonPlanPicChartAdapter

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblParentToolBar.text = "School Strength"
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)

        appViewModel?.getlpStaffReport?.observe(this) { response ->
            if (response != null && response.status) {
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.rcyLessonPlan.visibility = View.VISIBLE
                islpStaffData(response.data)
            } else {
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.rcyLessonPlan.visibility = View.GONE
            }
        }

        // Default call
        loadlpAllClassdata("myclass")
    }

    private fun islpStaffData(data: List<AllClassData>?) {
        lessonplanAdapter = LessonPlanPicChartAdapter(data, this, this, Constant.isShimmerViewDisable)
        binding.rcyLessonPlan.adapter = lessonplanAdapter
    }

    private fun loadlpAllClassdata(requestType: String) {
        lessonplanAdapter = LessonPlanPicChartAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyLessonPlan.layoutManager = LinearLayoutManager(this)
        binding.rcyLessonPlan.isNestedScrollingEnabled = false
        binding.rcyLessonPlan.adapter = lessonplanAdapter

        appViewModel!!.getlpStaffReport(isAccessToken!!, requestType, this@LessonPlan)

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCreate -> {
                // MyClasses
                binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnCreate.background = ContextCompat.getDrawable(this, R.drawable.bg_blue)
                binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.btnHistory.background = null

                loadlpAllClassdata("myclass")
            }

            R.id.btnHistory -> {
                // AllClasses
                binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnHistory.background = ContextCompat.getDrawable(this, R.drawable.bg_blue)
                binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.btnCreate.background = null

                loadlpAllClassdata("allclass")
            }

            R.id.imgBack -> onBackPressed()
        }
    }

    override fun onItem(data: AllClassData) {
        val intent = Intent(this@LessonPlan, LessonPlanViewDetails::class.java)
        startActivity(intent)
    }
}
