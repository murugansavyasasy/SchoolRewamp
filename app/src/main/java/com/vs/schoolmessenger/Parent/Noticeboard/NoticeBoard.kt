package com.vs.schoolmessenger.Parent.Noticeboard

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.NoticeBoardAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.NoticeBoardBinding

class NoticeBoard : BaseActivity<NoticeBoardBinding>(), View.OnClickListener,
    NoticeBoardClickListener {

    override fun getViewBinding(): NoticeBoardBinding {
        return NoticeBoardBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: NoticeBoardAdapter
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        isGetNoticeBoardList()

        binding.toolbarLayout.lblParentToolBar.text = "Notice Board"
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        appViewModel?.isNoticeBoardReport?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyNoticeBoard.visibility = View.VISIBLE
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                isloadhomeworkData(response.data)
            } else {
                binding.rcyNoticeBoard.visibility = View.GONE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: "No data found"
            }
        }

    }


    private fun isloadhomeworkData(newData: List<Notice>?) {
        mAdapter =
            NoticeBoardAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyNoticeBoard.adapter = mAdapter
    }

    override fun onClick(p0: View?) {

        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }


    }


    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = "No matching notices found"
            binding.rcyNoticeBoard.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcyNoticeBoard.visibility = View.VISIBLE
        }
    }


    private fun isGetNoticeBoardList() {
        mAdapter = NoticeBoardAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyNoticeBoard.layoutManager = LinearLayoutManager(this)
        binding.rcyNoticeBoard.isNestedScrollingEnabled = false
        binding.rcyNoticeBoard.adapter = mAdapter
        appViewModel!!.isNoticeBoardReport(
            isAccessToken!!, this
        )
    }
}