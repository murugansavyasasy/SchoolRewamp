package com.vs.schoolmessenger.School.NoticeBoard
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.CreateNoticeBoardBinding

class CreateNoticeBoard : BaseActivity<CreateNoticeBoardBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): CreateNoticeBoardBinding {
        return CreateNoticeBoardBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}