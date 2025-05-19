package com.vs.schoolmessenger.CommonScreens

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ImageViewActivityBinding
import java.net.URLEncoder

class ImageViewActivity : BaseActivity<ImageViewActivityBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): ImageViewActivityBinding {
        return ImageViewActivityBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        val isFileUrl = intent.getStringExtra(Constant.isFileUrl) ?: "No Title"
        val isFileType = intent.getStringExtra(Constant.isFileType) ?: "No Link"

        Log.d("isFileUrl", isFileUrl)
        Log.d("isFileType", isFileType)
        binding.toolbarLayout.lblParentToolBar.text = "Images"
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"

        if (isFileType != Constant.IMAGE) {
            binding.webView.visibility = View.VISIBLE
            binding.webView.visibility = View.VISIBLE
            binding.webView.settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
            }

            binding.webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(
                    view: android.webkit.WebView?,
                    url: String?,
                    favicon: Bitmap?
                ) {
                    binding.loadingBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    binding.loadingBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    binding.loadingBar.visibility = View.GONE
                    Log.e("WebViewError", "Error loading file: ${error?.description}")
                }
            }

            binding.webView.webChromeClient = WebChromeClient()
            binding.webView.loadUrl(isFileUrl)
        } else {
            Log.d("isFileURL", isFileUrl)
            binding.webView.visibility = View.GONE
            binding.imageView.visibility = View.VISIBLE
            Glide.with(this)
                .load(isFileUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.loadingBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.loadingBar.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.imageView)
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