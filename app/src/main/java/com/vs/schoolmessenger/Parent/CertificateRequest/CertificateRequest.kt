package com.vs.schoolmessenger.Parent.CertificateRequest

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.CertificateRequestParentBinding

class CertificateRequest  : BaseActivity<CertificateRequestParentBinding>() {

    override fun getViewBinding(): CertificateRequestParentBinding {
        return CertificateRequestParentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}