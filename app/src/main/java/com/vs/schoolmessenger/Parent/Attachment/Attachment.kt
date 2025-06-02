package com.vs.schoolmessenger.Parent.Attachment

import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentAttachmentBinding

class Attachment : BaseActivity<ParentAttachmentBinding>(), View.OnClickListener {

    override fun getViewBinding(): ParentAttachmentBinding {
        return ParentAttachmentBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    var mAdapter: AttachmentAdapter? = null

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.lblAttachment)
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails.standard_name + " - " + isChildDetails.section_name

        appViewModel?.isAttachmentResponse?.observe(this) { response ->
//            if (response?.status == true) {
//                appendData(response.data, archiveFlag = true)
//            } else {
//                checkAndShowNoData(message = response?.message)
//            }
        }

        appViewModel?.isAttachmentResponseArchive?.observe(this) { response ->
//            if (response?.status == true) {
//                appendData(response.data, archiveFlag = true)
//            } else {
//                checkAndShowNoData(message = response?.message)
//            }
        }

    }

    override fun onClick(p0: View?) {

    }

    private fun isGetAttachment() {

        appViewModel?.getAttachment(
            isAccessToken.orEmpty(), activity = this
        )
    }

    private fun isGetAttachmentArchive() {
        appViewModel?.getAttachmentArchive(
            isAccessToken.orEmpty(), activity = this
        )
    }


    override fun onResume() {
        isGetAttachment()
        super.onResume()
    }
}