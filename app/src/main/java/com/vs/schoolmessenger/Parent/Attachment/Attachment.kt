package com.vs.schoolmessenger.Parent.Attachment

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter
import com.vs.schoolmessenger.R
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Attachment.AttachmentReportAdapter
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportData
import com.vs.schoolmessenger.School.Attachment.OnAttachmentReportClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentAttachmentBinding

class Attachment : BaseActivity<ParentAttachmentBinding>(), View.OnClickListener,OnAttachmentReportClickListener{

    override fun getViewBinding(): ParentAttachmentBinding {
        return ParentAttachmentBinding.inflate(layoutInflater)
    }

    var mAttachmentReportAdapter: AttachmentReportAdapter? = null
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    lateinit var mAdapter: AttachmentAdapter

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.imgFilter.setOnClickListener(this)
        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.lblStudentSection.text =
            childDetails?.standard_name + " - " + childDetails?.section_name
        binding.linearlayout1.visibility = View.VISIBLE
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mAttachmentReportAdapter?.filter?.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.txtSearchMenu.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Trigger search
                val query = binding.txtSearchMenu.text.toString()
                mAttachmentReportAdapter?.filter?.filter(query)

                // Hide keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)

                binding.txtSearchMenu.clearFocus()
                true
            } else {
                false
            }
        }




        appViewModel?.isAttachmentResponse?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.txtNoData.visibility = View.GONE
                binding.recycleracademic.visibility = View.VISIBLE
                isLoadData(response.data)
            } else {
                showEmptyState(response?.message ?: getString(R.string.no_data_found))
            }
        }
        isGetAttachment()
    }
    fun isLoadData(data: List<AttachmentReportData>) {
        mAttachmentReportAdapter = AttachmentReportAdapter(
            data,
            this,
            this,
            Constant.isShimmerViewDisable,
            binding.nomessage,
            binding.txtNoData
        )
        binding.recycleracademic.layoutManager = LinearLayoutManager(this)
        binding.recycleracademic.isNestedScrollingEnabled = false
        binding.recycleracademic.adapter = mAttachmentReportAdapter
    }


    private fun isGetAttachment() {

        mAttachmentReportAdapter =
            AttachmentReportAdapter(
                emptyList(),
                this,
                this,
                Constant.isShimmerView
            )
        binding.recycleracademic.layoutManager = LinearLayoutManager(this)
        binding.recycleracademic.isNestedScrollingEnabled = false
        binding.recycleracademic.adapter = mAttachmentReportAdapter

        appViewModel?.getAttachment(isAccessToken.orEmpty(), this)
    }

    private fun showEmptyState(message: String) {
        binding.recycleracademic.visibility = View.GONE
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onItemClick(
        isData: List<AttachmentReportData>,
        view: View,
        isPosition: Int
    ) {

    }

    override fun onReadStatusClick(isData: List<AttachmentReportData>, isPosition: Int) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, Constant.ATTACHMENT)
            addProperty(APIKeyNames.detail_id, isData[isPosition].id)
        }
        appViewModel?.isUpdateStatusCommunication(isAccessToken!!, jsonObject, this)
    }
}