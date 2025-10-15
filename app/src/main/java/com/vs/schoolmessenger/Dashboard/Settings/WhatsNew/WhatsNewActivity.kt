package com.vs.schoolmessenger.Dashboard.Settings.WhatsNew

import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.Model.WhatsNewUpdateData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesStudentListDetailAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeData
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary.LessonPlanPicChartAdapter
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel.AllClassData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ActivityWhatsNewBinding

class WhatsNewActivity : BaseActivity<ActivityWhatsNewBinding>(), View.OnClickListener {
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var whatsnewAdapter: WhatsNewAdapter? = null
    private var currentPosition = 0

    override fun getViewBinding(): ActivityWhatsNewBinding {
        return ActivityWhatsNewBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.imgBack.setOnClickListener(this)
        val isStaffDetails = SharedPreference.getStaffDetails(this)
        val childDetails = SharedPreference.getChildDetails(this)

        isAccessToken = when {
            !isStaffDetails?.access_token.isNullOrEmpty() -> isStaffDetails?.access_token
            !childDetails?.access_token.isNullOrEmpty() -> childDetails?.access_token
            else -> null
        }

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        loadwhatsnewdata()
        appViewModel?.getdashboardnewupdates?.observe(this) { response ->
            if (response != null && response.status) {
                binding.rcywhatsnew.visibility = View.VISIBLE
                getWhatsNewData(response.data)
            } else {
                binding.rcywhatsnew.visibility = View.GONE
            }
        }
    }

    private fun loadwhatsnewdata() {
        whatsnewAdapter = WhatsNewAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcywhatsnew.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcywhatsnew.adapter = whatsnewAdapter
        appViewModel!!.getdashboardnewupdates(isAccessToken!!, Constant.user_details!!.staff_role)
    }

    private fun getWhatsNewData(data: List<WhatsNewUpdateData>?) {
        whatsnewAdapter = WhatsNewAdapter(data, this, isLoading = false)
        binding.rcywhatsnew.adapter = whatsnewAdapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcywhatsnew.layoutManager = layoutManager

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rcywhatsnew)

        binding.rcywhatsnew.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val view = snapHelper.findSnapView(layoutManager)
                val position =
                    if (view != null) layoutManager.getPosition(view) else RecyclerView.NO_POSITION
                if (position != RecyclerView.NO_POSITION && position != currentPosition) {
                    currentPosition = position
                    updateDotIndicator(position)
                }
            }
        })

        data?.let {
            if (it.size > 1) {
                setupDots(it.size)
                binding.dotIndicatorContainer.visibility = View.VISIBLE
            } else {
                binding.dotIndicatorContainer.visibility = View.GONE
                currentPosition = 0
            }
        }
    }

    private fun setupDots(count: Int) {
        binding.dotIndicatorContainer.removeAllViews()

        for (i in 0 until count) {
            val dot = ImageView(this)
            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            dot.setImageResource(R.drawable.dot_unselected)
            binding.dotIndicatorContainer.addView(dot)
        }
        updateDotIndicator(0)
    }

    private fun updateDotIndicator(position: Int) {
        for (i in 0 until binding.dotIndicatorContainer.childCount) {
            val dot = binding.dotIndicatorContainer.getChildAt(i) as ImageView
            dot.setImageResource(
                if (i == position) R.drawable.dot_selected else R.drawable.dot_unselected
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }
}
