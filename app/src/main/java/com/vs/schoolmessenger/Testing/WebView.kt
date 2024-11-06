package com.vs.schoolmessenger.Testing

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Toast
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.WebviewBinding

class WebView : BaseActivity<WebviewBinding>(), View.OnClickListener {


    private lateinit var webView: WebView
    private val url =
        "https://download-video.akamaized.net/v3-1/playback/cc3c9cca-d29d-4051-9ed4-270ae5d3c2bf/aac45bc4?__token__=st=1730890792~exp=1730905192~acl=%2Fv3-1%2Fplayback%2Fcc3c9cca-d29d-4051-9ed4-270ae5d3c2bf%2Faac45bc4%2A~hmac=c0550a019da9a0473d0bd2a04b014511337177d2996cbcfb746ec1b5c6288e9a&r=dXMtZWFzdDE%3D"


    override fun getViewBinding(): WebviewBinding {
        return WebviewBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()


        // Configure WebView settings
        binding.webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }
        // Load the URL in WebView
        binding.webView.webViewClient = WebViewClient()
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.loadUrl(url)

        // Set up DownloadListener to handle downloads
        binding.webView.setDownloadListener { url, _, _, _, _ ->
            downloadFile(this, url, "downloaded_video.mp4")
        }

        binding.btnDownload.setOnClickListener {
            downloadFile(this, url, "downloaded_video.mp4")

        }
    }

    override fun onClick(p0: View?) {

    }

    // Helper function to download file using DownloadManager
    fun downloadFile(context: Context, url: String, fileName: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)

        val request = DownloadManager.Request(uri).apply {
            setTitle("Downloading File")
            setDescription("Downloading $fileName")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        }

        downloadManager.enqueue(request)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    }
}