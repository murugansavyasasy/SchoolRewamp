package com.vs.schoolmessenger.Parent.Noticeboard

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.WebView
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
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


        appViewModel?.isNoticeBoardReport?.observe(this) { response ->
            if (response!!.status) {
                binding.rcyNoticeBoard.visibility = View.VISIBLE
                isloadhomeworkData(response.data)
            } else {
                binding.nomessage.visibility = View.VISIBLE
                binding.rcyNoticeBoard.visibility = View.GONE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response.message
            }
        }
    }


    private fun isloadhomeworkData(newData: List<Notice>?) {
        mAdapter =
            NoticeBoardAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyNoticeBoard.adapter = mAdapter
    }

    override fun onClick(p0: View?) {


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