package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet

import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.OnExamSelectListener


import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.getExamListData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.getSubjectData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.adapter.ExamListAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamListBinding
import com.vs.schoolmessenger.databinding.UploadMarkSheetBinding
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.orEmpty

class UploadMarkSheet : BaseActivity<UploadMarkSheetBinding >(), View.OnClickListener {

    override fun getViewBinding(): UploadMarkSheetBinding {
        return UploadMarkSheetBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: ExamListAdapter
    private var isClassList: List<getExamListData>? = emptyList()
    private var selectedExam: getExamListData? = null


    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text=isStaffDetails!!.school_name


    }




    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }


}