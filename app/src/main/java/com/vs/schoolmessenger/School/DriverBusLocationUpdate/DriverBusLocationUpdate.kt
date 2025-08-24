package com.vs.schoolmessenger.School.DriverBusLocationUpdate

import android.content.Intent
import android.os.Build
import android.view.View
import android.webkit.WebViewClient
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.LocationService
import com.vs.schoolmessenger.databinding.DriverLocationUpdateScreenBinding

class DriverBusLocationUpdate : BaseActivity<DriverLocationUpdateScreenBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): DriverLocationUpdateScreenBinding {
        return DriverLocationUpdateScreenBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        binding.imgBack.setOnClickListener(this)


        val latitude = 12.9716
        val longitude = 77.5946
        val mapUrl = "https://www.google.com/maps?q=$latitude,$longitude"
        binding.mapWebView.settings.javaScriptEnabled = true
        binding.mapWebView.settings.domStorageEnabled = true
        binding.mapWebView.webViewClient = WebViewClient() // ensures it opens in app
        binding.mapWebView.loadUrl(mapUrl)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, LocationService::class.java))
        } else {
            startService(Intent(this, LocationService::class.java))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                finish()
            }
        }
    }
}