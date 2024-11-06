package com.vs.schoolmessenger.Auth.TermsConditions
import android.view.View
import android.webkit.WebViewClient
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.TermsAndConditionsBinding

class TermsAndConditions  : BaseActivity<TermsAndConditionsBinding>(), View.OnClickListener {

    override fun getViewBinding(): TermsAndConditionsBinding {
        return TermsAndConditionsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID
        setupToolbar()
        // Enable JavaScript
        binding.webView.settings.javaScriptEnabled = true
        // Enable zoom controls (optional)
        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false
        // Set WebViewClient to handle page navigation within the WebView
        binding.webView.webViewClient = WebViewClient()
        // Load a URL
        binding.webView.loadUrl("https://schoolchimes.com/vs_web/terms_conditions/")
        binding.imgBack.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
               finish()
            }
        }
    }
}