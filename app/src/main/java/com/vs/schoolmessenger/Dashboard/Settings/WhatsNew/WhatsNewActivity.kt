package com.vs.schoolmessenger.Dashboard.Settings.WhatsNew

import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
    var isAccessToken: String? = null


    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null
    private var whatsNewUpdateData: List<WhatsNewUpdateData> = emptyList()
    private var whatsnewAdapter: WhatsNewAdapter? = null

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


        val isStaffDetails = SharedPreference.getStaffDetails(this)
        val childDetails = SharedPreference.getChildDetails(this)

        isAccessToken = when {
            !isStaffDetails?.access_token.isNullOrEmpty() -> isStaffDetails?.access_token
            !childDetails?.access_token.isNullOrEmpty() -> childDetails?.access_token
            else -> null
        }




        binding.parentToolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
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

    private fun getWhatsNewData(data: List<WhatsNewUpdateData>?) {
        whatsnewAdapter = WhatsNewAdapter(
            data,
            this,
            isLoading = false
        )
        binding.rcywhatsnew.adapter = whatsnewAdapter
    }

    private fun loadwhatsnewdata() {
        whatsnewAdapter = WhatsNewAdapter(null, this, Constant.isShimmerViewShow)

        binding.rcywhatsnew.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.rcywhatsnew.isNestedScrollingEnabled = false
        binding.rcywhatsnew.adapter = whatsnewAdapter

        appViewModel!!.getdashboardnewupdates(isAccessToken!!, Constant.user_details!!.staff_role)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}