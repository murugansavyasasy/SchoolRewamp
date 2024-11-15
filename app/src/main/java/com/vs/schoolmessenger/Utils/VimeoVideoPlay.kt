package com.vs.schoolmessenger.Utils

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.VimeoVideoPlayBinding

class VimeoVideoPlay : BaseActivity<VimeoVideoPlayBinding>() {

    override fun getViewBinding(): VimeoVideoPlayBinding {
        return VimeoVideoPlayBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarBlackTheme()
        val videoUrl = intent.getStringExtra("VIDEO_URL")
        val videoTitle = intent.getStringExtra("VIDEO_TITLE")

        binding.lblVideoTitle.text = videoTitle
        if (videoUrl != null) {
            val embedUrl = "https://player.vimeo.com/video/${videoUrl.split("/").last()}"
            binding.webViewVideo.settings.javaScriptEnabled = true
            binding.webViewVideo.settings.domStorageEnabled = true
            binding.webViewVideo.settings.setSupportZoom(false) // Disable zoom if needed
            binding.webViewVideo.settings.loadWithOverviewMode = true
            binding.webViewVideo.settings.useWideViewPort = true
            binding.webViewVideo.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }
            binding.webViewVideo.loadUrl(embedUrl)
        } else {
            Toast.makeText(this, R.string.VideoURLnotfound, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webViewVideo)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

}
