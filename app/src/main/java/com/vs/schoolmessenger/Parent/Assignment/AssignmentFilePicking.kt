package com.vs.schoolmessenger.Parent.Assignment

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.ImagePdfBinding

class AssignmentFilePicking : BaseActivity<ImagePdfBinding>(), View.OnClickListener {

    override fun getViewBinding(): ImagePdfBinding {
        return ImagePdfBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
    }

    override fun onClick(p0: View?) {

    }
}
