package com.vs.schoolmessenger.Parent.Attachment

import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentClickListener
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentAttachmentBinding

class Attachment : BaseActivity<ParentAttachmentBinding>(), View.OnClickListener, AttachmentClickListener {

    override fun getViewBinding(): ParentAttachmentBinding {
        return ParentAttachmentBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var mAdapter: AttachmentAdapter? = null
    private var hasFetchedMore = false
    private var allAttachmentData = mutableListOf<AttachmentData>()

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.seeMoreLabel.setOnClickListener(this)

        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.lblAttachment)
        binding.toolbarLayout.lblStudentSection.text =
            "${childDetails?.standard_name} - ${childDetails?.section_name}"

        appViewModel = ViewModelProvider(this).get(App::class.java).apply { init() }

        observeAttachmentResponse()
        showInitialShimmer()
        fetchInitialData()
    }

    private fun observeAttachmentResponse() {
        appViewModel?.isAttachmentResponse?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.txtNoData.visibility = View.GONE
                binding.recycleracademic.visibility = View.VISIBLE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
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
        binding.toolbarLayout.rytSearch.visibility = View.GONE
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
        Toast.makeText(this, "Clicked: ${data.title}", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        allAttachmentData.clear()
        fetchInitialData()
    }
}
