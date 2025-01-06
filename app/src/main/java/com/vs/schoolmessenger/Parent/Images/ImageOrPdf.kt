package com.vs.schoolmessenger.Parent.Images

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.ImageOrPdfBinding

class ImageOrPdf : BaseActivity<ImageOrPdfBinding>() {

    override fun getViewBinding(): ImageOrPdfBinding {
        return ImageOrPdfBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}