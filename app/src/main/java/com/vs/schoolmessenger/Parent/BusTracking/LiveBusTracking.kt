package com.vs.schoolmessenger.Parent.BusTracking

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.BusListData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LiveBusTrackingBinding

class LiveBusTracking : BaseActivity<LiveBusTrackingBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): LiveBusTrackingBinding {
        return LiveBusTrackingBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    var userDetails: UserDetails? = null


    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)
            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )
            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }

        userDetails = SharedPreference.getUserDetails(this)

        val busData = intent.getParcelableExtra<BusListData>("bus_data")

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }


        binding.toolbarLayout.lblStudentName.visibility=View.GONE
        binding.toolbarLayout.lblStudentName.text = Constant.isSelectedMenuName

        appViewModel = ViewModelProvider(this)[App::class.java].apply {
            init()
        }

        observeLiveBusResponse()
    }

    private fun observeLiveBusResponse() {

        appViewModel?.isGetLiveBusData?.observe(this) { response ->

            Constant.hideLoading(this)

            if (response != null) {

                if (response.status) {

                    if (response.data.isNotEmpty()) {

                        binding.lytList.visibility = View.GONE
                        binding.WVLiveBus.visibility = View.VISIBLE
                        binding.toolbarLayout.lblStudentSection.text = response.data[0].thing_id?:""
                        val trackingUrl = response.data[0].tracking_url?:""

                        setupWebView(trackingUrl)

                    } else {

                        binding.WVLiveBus.visibility = View.GONE
                        binding.lytList.visibility = View.VISIBLE
                        binding.txtNoData.text = getString(R.string.no_data_found)
                    }

                } else {

                    binding.WVLiveBus.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.text = response.message
                }

            } else {

                binding.WVLiveBus.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }
    }

    private fun setupWebView(trackingUrl: String) {

        binding.WVLiveBus.settings.javaScriptEnabled = true
        binding.WVLiveBus.settings.domStorageEnabled = true
        binding.WVLiveBus.settings.loadWithOverviewMode = true
        binding.WVLiveBus.settings.useWideViewPort = true
        binding.WVLiveBus.settings.builtInZoomControls = false
        binding.WVLiveBus.settings.displayZoomControls = false

        binding.WVLiveBus.webViewClient = object : WebViewClient() {

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)

                Constant.showLoading(this@LiveBusTracking)
            }

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                super.onPageFinished(view, url)
                Constant.hideLoading(this@LiveBusTracking)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)

                Constant.hideLoading(this@LiveBusTracking)

                binding.WVLiveBus.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        binding.WVLiveBus.loadUrl(trackingUrl)
    }

    private fun isLiveBus() {
        Constant.showLoading(this)
        appViewModel!!.isLiveBus(isAccessToken!!, this)
    }

    override fun onClick(v: View?) {

    }

    override fun onPause() {
        binding.WVLiveBus.onPause()
        binding.WVLiveBus.pauseTimers()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        binding.WVLiveBus.onResume()
        binding.WVLiveBus.resumeTimers()

        isLiveBus()
    }

    override fun onDestroy() {

        binding.WVLiveBus.apply {
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            onPause()
            removeAllViews()
            destroy()
        }

        super.onDestroy()
    }
}