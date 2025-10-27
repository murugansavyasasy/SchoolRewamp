package com.vs.schoolmessenger.Parent.LSRW

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R

import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StudentlistRemarksubmitBinding

class MySubmissionView : BaseActivity<StudentlistRemarksubmitBinding>() {

    override fun getViewBinding(): StudentlistRemarksubmitBinding {
        return StudentlistRemarksubmitBinding.inflate(layoutInflater)
    }

    private lateinit var appViewModel: App
    private var isAccessToken: String? = null
    private var id: String = ""

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()


        id = intent.getStringExtra(Constant.id_) ?: ""

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = childDetails?.name
        binding.toolbarLayout.lblSchoolName.text = childDetails?.school_name

        binding.linearLayout.visibility = View.GONE
        binding.btnSubmitRemark.visibility = View.GONE
        binding.txtPercentage.visibility = View.GONE
        Log.d("Access Token Values", isAccessToken.toString())
        fetchMySubmissionList()




        appViewModel?.islsrwmysubmission?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response?.status == true && !response.data.isNullOrEmpty()) {

                val submission = response.data[0]

                val fileList = submission.file_path?.map {
                    GetFilePathDetails(url = it.url, type = it.type)
                } ?: emptyList()

                if (fileList.isNotEmpty()) {
                    val adapter = MySubmissionAdapter(
                        this,
                        fileList,
                        "English",
                        SELECTED_SCHOOL_MENU,
                        true
                    )

                    binding.rcChildHW.layoutManager =
                        GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
                    binding.rcChildHW.adapter = adapter

                    binding.rcChildHW.visibility = View.VISIBLE
                    binding.lytNoDataFound.visibility = View.GONE
                    binding.imageslabel.visibility = View.VISIBLE

                } else {
                    binding.rcChildHW.visibility = View.GONE
                    binding.lytNoDataFound.visibility = View.VISIBLE
                    binding.imageslabel.visibility = View.GONE
                    binding.noDataFound.text = "No attached image available"
                }

            } else {
                binding.rcChildHW.visibility = View.GONE
                binding.lytNoDataFound.visibility = View.VISIBLE
                binding.imageslabel.visibility = View.GONE
                binding.noDataFound.text = response?.message ?: Constant.NO_DATA_FOUND
            }
        }

    }

    private fun fetchMySubmissionList() {
        Constant.showLoading(this)
        appViewModel?.islsrwmysubmission(isAccessToken!!, id)
    }
}
