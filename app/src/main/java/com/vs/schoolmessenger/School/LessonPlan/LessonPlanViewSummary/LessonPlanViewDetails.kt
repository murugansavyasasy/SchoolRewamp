package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanClickListener
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanData
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEdit.LessonPlanEditActivity
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary.LessonPlan
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryDetail
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LessonplanViewDetailsBinding

class LessonPlanViewDetails : BaseActivity<LessonplanViewDetailsBinding>(),
    View.OnClickListener, LessonPlanClickListener, OnDateSelectedListener {

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

        sectionSubjectId = intent.getStringExtra("section_subject_id")!!
        request_type = intent.getStringExtra("request_type")!!

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
    }

    private fun setupRecycler() {
        lessonplanViewAdapter = LessonPlanAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyLessonViewPlan.layoutManager = LinearLayoutManager(this)
        binding.rcyLessonViewPlan.isNestedScrollingEnabled = false
        binding.rcyLessonViewPlan.adapter = lessonplanViewAdapter
    }

    private fun loadLessonPlanData(status: Int, sectionSubjectId: String?) {
        lessonplanViewAdapter = LessonPlanAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyLessonViewPlan.adapter = lessonplanViewAdapter

        appViewModel?.getlpViewReport(
            isToken = isAccessToken!!,
            section_subject_id = sectionSubjectId!!,
            lesson_plan_status = status,
            activity = this@LessonPlanViewDetails
        )
    }


    private fun islpViewData(data: List<LessonPlanViewSummaryItem>?) {
        lessonplanViewAdapter =
            LessonPlanAdapter(data, this, this, Constant.isShimmerViewDisable)
        binding.rcyLessonViewPlan.adapter = lessonplanViewAdapter
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
        startActivity(intent)
    }

    override fun onDeleteItem(data: LessonPlanData) {
        // Optional: Add delete logic here
    }

    override fun onDateSelected(date: String) {
        // Optional: Handle date filter here
    }

}
