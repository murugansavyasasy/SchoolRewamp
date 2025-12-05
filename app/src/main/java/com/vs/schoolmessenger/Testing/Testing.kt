package com.vs.schoolmessenger.Testing

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.ParentHomeworkActivityBinding

class Testing : BaseActivity<ParentHomeworkActivityBinding>(), View.OnClickListener {

    override fun getViewBinding(): ParentHomeworkActivityBinding {
        return ParentHomeworkActivityBinding.inflate(layoutInflater)
    }


    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

    }

    override fun onClick(v: View?) {

    }
}

