package com.vs.schoolmessenger.School.LessonPlan.LessonPlanEdit

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LessonPlanEditBinding

class LessonPlanEditActivity : BaseActivity<LessonPlanEditBinding>(),
    View.OnClickListener{
    override fun getViewBinding(): LessonPlanEditBinding {
        return LessonPlanEditBinding.inflate(layoutInflater)
    }
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var particular_id: String? = null
    private var request_type: String? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = "Edit Lesson Plan"
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        particular_id = intent.getStringExtra("particular_id")
        request_type = intent.getStringExtra("request_type")
        Log.d("particular_id",particular_id.toString())
        Log.d("request_type",request_type.toString())

    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

}
