package com.vs.schoolmessenger.School.OnlineMeeting

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.School.NoticeBoard.NoticeData
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter
import com.vs.schoolmessenger.databinding.OnlineMeetingBinding

class OnlineMeeting : BaseActivity<OnlineMeetingBinding>(),
    View.OnClickListener {

    lateinit var mAdapter: SchoolNoticeBoardAdapter
    private lateinit var noticeBoardList: List<NoticeData>

    override fun getViewBinding(): OnlineMeetingBinding {
        return OnlineMeetingBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {

    }
}