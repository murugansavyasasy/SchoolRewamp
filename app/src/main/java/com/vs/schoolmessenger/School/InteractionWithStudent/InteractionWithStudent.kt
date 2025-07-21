package com.vs.schoolmessenger.School.InteractionWithStudent

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.IntrectionWithStudentBinding

class InteractionWithStudent : BaseActivity<IntrectionWithStudentBinding>(), View.OnClickListener,
    InteractionWithStudentListener {

    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private lateinit var mAdapter: InteractionWithStudentAdapter


    override fun getViewBinding(): IntrectionWithStudentBinding {
        return IntrectionWithStudentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val staffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = staffDetails?.access_token

        fetchStudentData()

        appViewModel?.getstudentdetailsforchat?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI("Something went wrong. Please try again.")
                return@observe
            }
            if (response.status) {
                isLoadStaffData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }

    }


    private fun fetchStudentData() {
        appViewModel?.getstudentdetailsforchat(
            isAccessToken ?: "", this
        )
    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcystudentdata.visibility = View.GONE
    }

    private fun isLoadStaffData(data: List<StudentChatData>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI("No staff data available")
            return
        }

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcystudentdata.visibility = View.VISIBLE
        binding.rcystudentdata.layoutManager = LinearLayoutManager(this)

        mAdapter = InteractionWithStudentAdapter(data, this, this, false)
        binding.rcystudentdata.adapter = mAdapter
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_notices_found)
            binding.rcystudentdata.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcystudentdata.visibility = View.VISIBLE
        }
    }

    override fun onClickItem(data: StudentChatData) {
        TODO("Not yet implemented")
    }
}