package com.vs.schoolmessenger.School.MarkYourAttendance

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AddLocationActivityBinding

class AddLocationActivity : BaseActivity<AddLocationActivityBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): AddLocationActivityBinding {
        return AddLocationActivityBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}