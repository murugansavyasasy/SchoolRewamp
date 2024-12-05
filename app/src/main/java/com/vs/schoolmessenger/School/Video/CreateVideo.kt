package com.vs.schoolmessenger.School.Video
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.CreateVideoBinding

class CreateVideo  : BaseActivity<CreateVideoBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): CreateVideoBinding {
        return CreateVideoBinding.inflate(layoutInflater)
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