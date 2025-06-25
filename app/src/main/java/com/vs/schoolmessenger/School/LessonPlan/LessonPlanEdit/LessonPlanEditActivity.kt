package com.vs.schoolmessenger.School.LessonPlan.LessonPlanEdit

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.EditClassData
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.LessonPlanEditClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LessonPlanEditBinding

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

    private lateinit var lessonplaneditAdapter: LessonPlanEditAdapter

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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}
