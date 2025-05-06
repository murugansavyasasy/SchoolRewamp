package com.vs.schoolmessenger.Parent.Homework

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HomeWorkParentBinding

class HomeWork : BaseActivity<HomeWorkParentBinding>(), View.OnClickListener,
    HomeWorkDateClickListener, HomeWorkItemClick {

    private var isAccessToken: String? = null
    private var isStudentName: String? = null
    private var isStudentSectionName: String? = null
    private var isStudentStandardName: String? = null
    private var appViewModel: App? = null
    override fun getViewBinding(): HomeWorkParentBinding {
        return HomeWorkParentBinding.inflate(layoutInflater)
    }
    var mAdapter: HomeWorkAdapter? = null


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.HomeWork)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        isStudentName = isChildDetails?.name
        isStudentStandardName = isChildDetails?.standard_name
        isStudentSectionName = isChildDetails?.section_name.toString()
        Log.d("SectionID", isStudentSectionName.toString())

        isGetHomeWorkList()
        binding.toolbarLayout.lblStudentName.text = isStudentName
        binding.toolbarLayout.lblStudentSection.text =
            isStudentStandardName + " " + isStudentSectionName


        appViewModel?.isHomeWorkDetails?.observe(this) { response ->
            Log.d("GetHomework-response",response.toString())
            if (response?.status == true) {
                isloadhomeworkData(response.data)
                Log.d("GetHomeWorkDetails", response.data.toString())
                Log.d("Access Token",isAccessToken.toString())
            }
        }
    }

    private fun isloadhomeworkData(newData: ArrayList<GetDateWiseHomeworkData>?) {

        Log.d("GetHomeworkDataWise", newData.toString())

        mAdapter =
            HomeWorkAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyHomework.adapter = mAdapter

    }


    fun isGetHomeWorkList() {
        mAdapter = HomeWorkAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHomework.layoutManager = LinearLayoutManager(this)
        binding.rcyHomework.isNestedScrollingEnabled = false
        binding.rcyHomework.adapter = mAdapter
        appViewModel!!.isHomeWorkDetails(
            isAccessToken!!, this
        )
    }


    override fun onClick(p0: View?) {

    }

    override fun onItemClick(data: HomeWorkDateData, holder: HomeWorkAdapter.DataViewHolder) {
    }

    override fun onItemTextClick(data: HomeWorkList) {
    }

    override fun onItemImageClick(data: HomeWorkList) {
    }

    override fun onItemPDFClick(data: HomeWorkList) {
    }

    override fun onItemVoiceClick(data: HomeWorkList) {
    }

    override fun onItemVideoClick(data: HomeWorkList) {
    }
}