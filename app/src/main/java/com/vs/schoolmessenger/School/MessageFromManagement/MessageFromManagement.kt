package com.vs.schoolmessenger.School.MessageFromManagement

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.MessageFromStaffAdapter
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
import com.vs.schoolmessenger.School.QuizExam.Adapter.ExamQuizReport.ExamQuizReportAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MessageFromManagementBinding

class MessageFromManagement : BaseActivity<MessageFromManagementBinding>(),
    View.OnClickListener {

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var appViewModel: App? = null
    private lateinit var adapter: MessageFromStaffAdapter


    override fun getViewBinding(): MessageFromManagementBinding {
        return MessageFromManagementBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        isGetMessageFromStaff()

        appViewModel?.isGetMessageStaff?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcMessageStaff.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                    isLoadMsgStaff(response.data)
                }
                else {
                    binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                    binding.rcMessageStaff.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                binding.rcMessageStaff.visibility = View.GONE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }

        appViewModel?.isGetMessageStaffAchieve?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcMessageStaffAchieve.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                    isLoadMsgStaffAchieve(response.data)
                }
                else {
                    binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                    binding.rcMessageStaffAchieve.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                binding.rcMessageStaffAchieve.visibility = View.GONE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }
        binding.lblSeeMore.setOnClickListener{
            binding.lblSeeMore.text="See less"
            isGetMessageFromStaffAchieve()
        }


    }

    private fun isLoadMsgStaff(data: List<GetMessagesStaffData>) {

        if (data.isNotEmpty()) {
            adapter = MessageFromStaffAdapter(data, this, Constant.isShimmerViewDisable)
            binding.rcMessageStaff.layoutManager = LinearLayoutManager(this)
            binding.rcMessageStaff.adapter = adapter
            binding.rcMessageStaff.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        } else {
            binding.rcMessageStaff.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        }
    }

    private fun isLoadMsgStaffAchieve(data: List<GetMessagesStaffData>) {

        if (data.isNotEmpty()) {
            adapter = MessageFromStaffAdapter(data, this, Constant.isShimmerViewDisable)
            binding.rcMessageStaff.layoutManager = LinearLayoutManager(this)
            binding.rcMessageStaff.adapter = adapter
            binding.rcMessageStaff.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        } else {
            binding.rcMessageStaff.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        }
    }

    fun isGetMessageFromStaff(){
        adapter = MessageFromStaffAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcMessageStaff.layoutManager = LinearLayoutManager(this)
        binding.rcMessageStaff.adapter = adapter
        appViewModel?.isGetMessageStaff(isAccessToken ?: "")
    }

    fun isGetMessageFromStaffAchieve(){
        adapter = MessageFromStaffAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcMessageStaffAchieve.layoutManager = LinearLayoutManager(this)
        binding.rcMessageStaffAchieve.adapter = adapter
        appViewModel?.isGetMessageStaffAchieve(isAccessToken ?: "")
    }


    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}