package com.vs.schoolmessenger.CommonScreens

import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.WebViewBinding

class WebView : BaseActivity<WebViewBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): WebViewBinding {
        return WebViewBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        val title = intent.getStringExtra("isTitle") ?: "No Title"
        val link = intent.getStringExtra("isWebLink") ?: "No Link"

        binding.toolbarLayout.lblParentToolBar.text = title
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"
        loadPdf(link)
    }

    private fun loadPdf(pdfUrl: String) {
        binding.loadingBar.visibility = View.VISIBLE

        binding.webPdf.apply {
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: Bitmap?
                ) {
                    binding.loadingBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.loadingBar.visibility = View.GONE
                }
            }

            webChromeClient = WebChromeClient()
            loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$pdfUrl")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}