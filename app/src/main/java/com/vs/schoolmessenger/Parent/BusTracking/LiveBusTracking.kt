package com.vs.schoolmessenger.Parent.BusTracking

import android.view.View
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Parent.BusTracking.Adapter.BusListAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LiveBusTrackingBinding


class LiveBusTracking : BaseActivity<LiveBusTrackingBinding>(), View.OnClickListener{

    override fun getViewBinding(): LiveBusTrackingBinding {
        return LiveBusTrackingBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    lateinit var mAdapter: BusListAdapter
    var userDetails: UserDetails? = null


    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)


        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblStudentSection.text =
            childDetails?.standard_name + " - " + childDetails?.section_name

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        appViewModel?.isGetLiveBusData?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    if (response.data.isNotEmpty()) {
                        binding.lytList.visibility = View.GONE
                        binding.WVLiveBus.visibility = View.VISIBLE
                        val trackingUrl = response.data[0].tracking_url
                        binding.WVLiveBus.webViewClient = WebViewClient()
                        binding.WVLiveBus.settings.javaScriptEnabled = true
                        binding.WVLiveBus.settings.domStorageEnabled = true
                        binding.WVLiveBus.settings.loadWithOverviewMode = true
                        binding.WVLiveBus.settings.useWideViewPort = true
                        binding.WVLiveBus.loadUrl(trackingUrl)

                    }
                    else {
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
        isLiveBus()

    }

    private fun isLiveBus() {
        Constant.showLoading(this)
        appViewModel!!.isLiveBus(isAccessToken!!, this)
    }

    override fun onClick(p0: View?) {

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

}
