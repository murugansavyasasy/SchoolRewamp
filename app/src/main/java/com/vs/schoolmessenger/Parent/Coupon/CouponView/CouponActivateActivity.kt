package com.vs.schoolmessenger.Parent.Coupon.CouponView

import android.content.Intent
import android.text.Html
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Coupon.CouponCredentials.AppCredentials
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCouponSummary.ActivateCouponSummary
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.databinding.BottomSheetBinding

class CouponActivateActivity : BaseActivity<BottomSheetBinding>(), View.OnClickListener {

    private lateinit var appViewModel: App

    override fun getViewBinding(): BottomSheetBinding {
        return BottomSheetBinding.inflate(layoutInflater)
    }

    private var category_name: String = ""
    private var discount: String = ""
    private var merchant_name: String = ""
    private var thumbnail: String = ""
    private var source_link: String = ""
    private var coupon_status: String = ""
    private var merchant_logo: String = ""

    private var howToUseText: String? = ""

    private var termsAndConditions: String? = ""

    private var isExpanded = false
    private var isExpanded1 = false

    private var bottomSheetBehavior: BottomSheetBehavior<View?>? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        bottomSheetBehavior = BottomSheetBehavior.from<View?>(binding.bottomLayout.bottomSheet)

        val screenHeight = getResources().displayMetrics.heightPixels
        val topGap = 230


        val params: ViewGroup.LayoutParams = binding.imageBanner.layoutParams
        params.height = screenHeight / 2
        binding.imageBanner.setLayoutParams(params)

        val bottomSheetParams: ViewGroup.LayoutParams =
            binding.bottomLayout.bottomSheet.layoutParams
        bottomSheetParams.height = screenHeight - topGap
        binding.bottomLayout.bottomSheet.setLayoutParams(bottomSheetParams)


        bottomSheetBehavior?.setFitToContents(true)
        bottomSheetBehavior?.peekHeight = (screenHeight * 0.6).toInt()

        binding.bottomLayout.remember.setOnClickListener(this)
        binding.bottomLayout.remember1.setOnClickListener(this)
        binding.bottomLayout.rememberSymbol.setOnClickListener(this)
        binding.bottomLayout.rememberSymbol1.setOnClickListener(this)
        binding.btnActivateCoupon.setOnClickListener(this)
        binding.imageTopLeft.setOnClickListener {
            onBackPressed()
        }
        category_name = intent.getStringExtra("category_name") ?: ""
        discount = intent.getStringExtra("discount") ?: ""
        merchant_name = intent.getStringExtra("merchant_name") ?: ""
        thumbnail = intent.getStringExtra("thumbnail") ?: ""
        source_link = intent.getStringExtra("source_link") ?: ""
        coupon_status = intent.getStringExtra("coupon_status") ?: ""
        merchant_logo = intent.getStringExtra("merchant_logo") ?: ""

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        fetchactivatecoupondata(source_link)

        coupon_status = intent.getStringExtra("coupon_status") ?: ""

        Log.d("coupon_status", coupon_status)
        if ("activated".equals(coupon_status, ignoreCase = true)) {
            binding.btnActivateCoupon.visibility = View.GONE
        } else {
            binding.btnActivateCoupon.visibility = View.VISIBLE
        }

        appViewModel.getCouponDetails?.observe(this) { response ->
            val campaignDetails = response?.data?.campaign_details
            if (campaignDetails == null) {
                Log.d("coupon_menu", "Coupon Menu Data Not Available")
            } else {
                isLoadCouponActiveData(campaignDetails)
            }
        }

        appViewModel.sendactivatecoupon?.observe(this) { response ->
            binding.btnActivateCoupon.isEnabled = true
            binding.isProgressBar.visibility = View.GONE
            response?.data?.let { data ->
                val intent = Intent(this, CouponOrderActivity::class.java).apply {
                    putExtra("coupon_code", data.coupon_code)
                    putExtra("qr_code", data.coupons?.getOrNull(0)?.qr_code)
                    putExtra("expiry_date", data.coupons?.getOrNull(0)?.expiry_date)
                    putExtra("merchant_logo", data.merchant_logo)
                    putExtra("offer", data.offer)
                    putExtra("redirect_url", data.redirect_url)
                    putExtra("isCTAvalid", data.isCTAvalid)
                    putExtra("CTAname", data.cTAname)
                    putExtra("CTAredirect", data.cTAredirect)
                    putExtra("category_name", category_name)
                    putExtra("how_to_use", howToUseText)
                    putExtra("Terms and Conditions", termsAndConditions)
                    putExtra("thumbnail", thumbnail)
                    putExtra("merchant_name", merchant_name)
                    putExtra("offer_show", binding.bottomLayout.offerText.text.toString())
                }
                startActivity(intent)
                finish()
            }
        }


        binding.btnActivateCoupon.setOnClickListener {
            binding.btnActivateCoupon.isEnabled = false
            binding.isProgressBar.visibility = View.VISIBLE
            appViewModel?.sendactivatecoupon(
                source_link, "91${AppCredentials.isMobileNumber}",
                AppCredentials.PARTNER_NAME,
                AppCredentials.API_KEY
            )
        }
    }

    private fun fetchactivatecoupondata(source_link: String) {
        appViewModel.getCouponDetails(
            source_link,
            "91${AppCredentials.isMobileNumber}",
            AppCredentials.PARTNER_NAME,
            AppCredentials.API_KEY
        )
    }

    private fun isLoadCouponActiveData(data: ActivateCouponSummary) {

        binding.bottomLayout.headerTextview.text = category_name
        binding.bottomLayout.offerText.text = data.offer_to_show
        binding.bottomLayout.offerText1.text = data.merchant_name
        binding.bottomLayout.offerText4.text = "Valid Until: " + data.expiry_date
        binding.bottomLayout.expandableText.text = convertHtmlToBullets(data.how_to_use)
        binding.bottomLayout.expandableText1.text = convertHtmlToBullets(data.terms_and_conditions)


        Glide.with(this).load(thumbnail).into(binding.imageBanner)

        Glide.with(this).load(data.merchant_logo).into(binding.bottomLayout.thumbnail)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.remember -> {
                expandhowtouse()
            }

            R.id.remember1 -> {
                expandtermsandcondition()
            }


            R.id.remember_symbol -> {
                expandhowtouse()
            }

            R.id.remember_symbol1 -> {
                expandtermsandcondition()
            }
        }
    }

    private fun expandhowtouse() {

        if (!isExpanded) {
            binding.bottomLayout.expandableText.visibility = View.VISIBLE
            binding.bottomLayout.rememberSymbol.setImageResource(R.drawable.ic_up_arrow)

            binding.bottomLayout.expandableText1.visibility = View.GONE
            binding.bottomLayout.rememberSymbol1.setImageResource(R.drawable.ic_down_black)
            isExpanded1 = false
        } else {
            binding.bottomLayout.expandableText.visibility = View.GONE
            binding.bottomLayout.rememberSymbol.setImageResource(R.drawable.ic_down_black)
        }
        isExpanded = !isExpanded
    }


    private fun expandtermsandcondition() {

        if (!isExpanded1) {
            binding.bottomLayout.expandableText1.visibility = View.VISIBLE
            binding.bottomLayout.rememberSymbol1.setImageResource(R.drawable.ic_up_arrow)

            binding.bottomLayout.expandableText.visibility = View.GONE
            binding.bottomLayout.rememberSymbol.setImageResource(R.drawable.ic_down_black)
            isExpanded = false
        } else {
            binding.bottomLayout.expandableText1.visibility = View.GONE
            binding.bottomLayout.rememberSymbol1.setImageResource(R.drawable.ic_down_black)
        }
        isExpanded1 = !isExpanded1

    }

    private fun convertHtmlToBullets(htmlContent: String?): String {
        val cleaned = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT).toString()

        val lines = cleaned.split("\\n|(?<=\\.)\\s*".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        val builder = StringBuilder()
        for (line in lines) {
            var line = line
            line = line.trim { it <= ' ' }
            if (!line.isEmpty()) {
                builder.append("• ").append(line).append("\n")
            }
        }
        return builder.toString().trim { it <= ' ' }
    }

}
