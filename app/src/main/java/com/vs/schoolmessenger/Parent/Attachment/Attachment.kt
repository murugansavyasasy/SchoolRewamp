package com.vs.schoolmessenger.Parent.Attachment

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentClickListener
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentData
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentAttachmentBinding

class Attachment : BaseActivity<ParentAttachmentBinding>(), View.OnClickListener,
    AttachmentClickListener, OnChildItemClickListener {

    override fun getViewBinding(): ParentAttachmentBinding {
        return ParentAttachmentBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    lateinit var mAdapter: AttachmentAdapter
    private var hasFetchedMore = false
    var isFilterShow = false
    private var allAttachmentData = mutableListOf<AttachmentData>()
    private var filteredAttachmentData = mutableListOf<AttachmentData>()

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.seeMoreLabel.setOnClickListener(this)
        binding.imgFilter.setOnClickListener(this)
        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.lblStudentSection.text =
            childDetails?.standard_name + " - " + childDetails?.section_name
        binding.linearlayout1.visibility = View.VISIBLE
        appViewModel = ViewModelProvider(this).get(App::class.java).apply { init() }

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.rdgFiles.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            when (checkedId) {
                R.id.RdbAll -> filterAttachments(Constant.ALL)
                R.id.RdbImage -> filterAttachments(Constant.IMAGE)
                R.id.RdbVideo -> filterAttachments(Constant.VIDEO)
                R.id.RdbDocuments -> filterAttachments(Constant.DOCUMENT)
            }
        }
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
                showEmptyState(response?.message ?: getString(R.string.no_data_found))
            }
        }

        appViewModel?.isAttachmentResponseArchive?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                appendData(response.data)
            }
        }
    }

    private fun showInitialShimmer() {
        mAdapter = AttachmentAdapter(null, this, this, this, isLoading = true)
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
        filterAttachments(Constant.ALL)
    }

    private fun filterAttachments(filter: String) {
        filteredAttachmentData = when (filter) {
            Constant.IMAGE -> allAttachmentData.filter {
                it.file_path.any { file ->
                    file.type.equals(
                        Constant.IMAGE, true
                    )
                }
            }.toMutableList()

            Constant.VIDEO -> allAttachmentData.filter {
                it.file_path.any { file ->
                    file.type.equals(
                        Constant.VIDEO, true
                    )
                }
            }.toMutableList()

            Constant.DOCUMENT -> allAttachmentData.filter {
                it.file_path.any { file ->
                    file.type.equals(Constant.PDF, true) || file.type.equals(
                        Constant.DOCX,
                        true
                    ) || file.type.equals(Constant.DOC, true) || file.type.equals(
                        Constant.PPT,
                        true
                    ) || file.type.equals(Constant.PPTX, true) || file.type.equals(
                        Constant.XLS,
                        true
                    ) || file.type.equals(Constant.XLSX, true) || file.type.equals(Constant.TXT, true)
                }
            }.toMutableList()

            else -> allAttachmentData.toMutableList()
        }

        if (filteredAttachmentData.isEmpty()) {
            showEmptyState(getString(R.string.no_matching_attachment_found))
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.recycleracademic.visibility = View.VISIBLE
            mAdapter =
                AttachmentAdapter(filteredAttachmentData, this, this, this, isLoading = false)
            binding.recycleracademic.adapter = mAdapter
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

            R.id.imgFilter -> {
                if (!isFilterShow) {
                    isFilterShow = true
                    binding.rytFilter.visibility = View.VISIBLE
                } else {
                    isFilterShow = false
                    binding.rytFilter.visibility = View.GONE
                }
            }
        }
    }

    override fun onItemClick(data: AttachmentData, holder: AttachmentAdapter.DataViewHolder) {

        Log.d("isClickView", data.id)
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, Constant.ATTACHMENT)
            addProperty(APIKeyNames.detail_id, data.id)
        }
        if (data.is_archive) {
            isAccessToken?.let {
                appViewModel?.isUpdateStatusArchive(it, jsonObject, this)
            }
        } else {
            isAccessToken?.let {
                appViewModel?.isUpdateStatusCommunication(it, jsonObject, this)
            }
        }

    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_attachment_found)
            binding.recycleracademic.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.recycleracademic.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        allAttachmentData.clear()
        fetchInitialData()
    }

    override fun onChildItemClick(
        file: AttachmentFile,
        parentData: AttachmentData
    ) {
        Log.d("isClickView", parentData.id)
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, Constant.ATTACHMENT)
            addProperty(APIKeyNames.detail_id, parentData.id)
        }
        if (parentData.is_archive) {
            isAccessToken?.let {
                appViewModel?.isUpdateStatusArchive(it, jsonObject, this)
            }
        } else {
            isAccessToken?.let {
                appViewModel?.isUpdateStatusCommunication(it, jsonObject, this)
            }
        }
    }
}