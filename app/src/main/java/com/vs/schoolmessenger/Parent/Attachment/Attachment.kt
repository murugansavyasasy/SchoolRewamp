package com.vs.schoolmessenger.Parent.Attachment

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentClickListener
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentAttachmentBinding

class Attachment : BaseActivity<ParentAttachmentBinding>(), View.OnClickListener, AttachmentClickListener {

    override fun getViewBinding(): ParentAttachmentBinding {
        return ParentAttachmentBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    lateinit var mAdapter: AttachmentAdapter
    private var hasFetchedMore = false
    private var allAttachmentData = mutableListOf<AttachmentData>()

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.seeMoreLabel.setOnClickListener(this)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.lblAttachment)
        binding.toolbarLayout.lblStudentSection.text =
            "${childDetails?.standard_name} - ${childDetails?.section_name}"

        appViewModel = ViewModelProvider(this).get(App::class.java).apply { init() }

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        observeAttachmentResponse()
        showInitialShimmer()
        fetchInitialData()
    }

    private fun observeAttachmentResponse() {
        appViewModel?.isAttachmentResponse?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.txtNoData.visibility = View.GONE
                binding.recycleracademic.visibility = View.VISIBLE
                appendData(response.data)
            } else {
                showEmptyState(response?.message ?: "No data found")
            }
        }

        appViewModel?.isAttachmentResponseArchive?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                appendData(response.data)
            }
        }
    }

    private fun showInitialShimmer() {
        mAdapter = AttachmentAdapter(null, this, this, isLoading = true)
        binding.recycleracademic.layoutManager = LinearLayoutManager(this)
        binding.recycleracademic.adapter = mAdapter
    }

    private fun showEmptyState(message: String) {
        binding.recycleracademic.visibility = View.GONE
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
    }

    private fun fetchInitialData() {
        appViewModel?.getAttachment(isAccessToken.orEmpty(), this)
    }

    private fun fetchMoreData() {
        appViewModel?.getAttachmentArchive(isAccessToken.orEmpty(), this)
    }

    private fun appendData(newData: List<AttachmentData>) {
        allAttachmentData.addAll(newData)
        if (mAdapter == null || mAdapter?.isLoading == true) {
            mAdapter = AttachmentAdapter(allAttachmentData, this, this, isLoading = false)
            binding.recycleracademic.adapter = mAdapter
        } else {
            mAdapter?.updateList(allAttachmentData)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.seeMoreLabel -> {
                if (!hasFetchedMore) {
                    hasFetchedMore = true
                    binding.seeMoreLabel.visibility = View.GONE
                    fetchMoreData()
                }
            }
        }
    }

    override fun onItemClick(data: AttachmentData, holder: AttachmentAdapter.DataViewHolder) {

    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = "No matching attachment found"
            binding.recycleracademic.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.recycleracademic.visibility = View.VISIBLE
        }
    }

    override fun onUpdateArchiveStatus(type: String?, detailId: String?) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, type)
            addProperty(APIKeyNames.detail_id, detailId)
        }
        isAccessToken?.let {
            appViewModel?.isUpdateStatusArchive(it, jsonObject, this)
        }
    }

    override fun onUpdateAttachmentStatus(type: String?, detailId: String?) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, type)
            addProperty(APIKeyNames.detail_id, detailId)
        }

        isAccessToken?.let {
            appViewModel?.isUpdateStatusCommunication(it, jsonObject, this)
        }
    }

    override fun onResume() {
        super.onResume()
        allAttachmentData.clear()
        fetchInitialData()
    }
}
