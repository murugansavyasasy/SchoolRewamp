package com.vs.schoolmessenger.Parent.Coupon.CouponView

import android.content.Intent
import android.text.Html
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Coupon.CouponCredentials.AppCredentials
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCouponSummary.ActivateCouponSummary
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
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
    private var coupon_code: String = ""
    private var merchant_logo: String = ""

    private var howToUseText: String? = ""

    private var termsAndConditions: String? = ""
    private var earnedPoints: Int = 0
    private var spentPoints: Int = 0
    private var remainingPoints: Int = 0
    private var pointspercoupon: Int = 0

    private var isExpanded = false
    private var isExpanded1 = false

    private var bottomSheetBehavior: BottomSheetBehavior<View?>? = null
    private var isAccessToken: String? = null

    override fun setupViews() {
        super.setupViews()
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            binding.statusBarBackground.layoutParams =
                (binding.statusBarBackground.layoutParams as ConstraintLayout.LayoutParams).apply {
                    height = statusBars.top
                }

            (binding.btnActivateCoupon.layoutParams as ViewGroup.MarginLayoutParams).apply {
                bottomMargin = navBars.bottom + resources.getDimensionPixelSize(R.dimen.fourty)
            }.also {
                binding.btnActivateCoupon.layoutParams = it
            }

            insets
        }


        bottomSheetBehavior = BottomSheetBehavior.from<View?>(binding.bottomLayout.bottomSheet)

        val screenHeight = getResources().displayMetrics.heightPixels

        val params: ViewGroup.LayoutParams = binding.imageBanner.layoutParams
        params.height = screenHeight / 2
        binding.imageBanner.layoutParams = params

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
        category_name = intent.getStringExtra(Constant.category_name) ?: ""
        discount = intent.getStringExtra(Constant.discount) ?: ""
        merchant_name = intent.getStringExtra(Constant.merchant_name) ?: ""
        thumbnail = intent.getStringExtra(Constant.thumbnail) ?: ""
        source_link = intent.getStringExtra(Constant.source_link) ?: ""
        coupon_status = intent.getStringExtra(Constant.coupon_status) ?: ""
        coupon_code = intent.getStringExtra(Constant.coupon_code) ?: ""
        merchant_logo = intent.getStringExtra(Constant.merchant_logo) ?: ""

        earnedPoints = intent.getIntExtra(Constant.earnedPoints, 0)
        spentPoints = intent.getIntExtra(Constant.spentPoints, 0)
        remainingPoints = intent.getIntExtra(Constant.remainingPoints, 0)
        pointspercoupon = intent.getIntExtra(Constant.pointspercoupon, 0)


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        fetchactivatecoupondata(source_link)
        Log.d("coupon_status", coupon_status)
        if (Constant.activated.equals(coupon_status, ignoreCase = true)) {
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
                val jsonObject = JsonObject().apply {
                    addProperty(Constant.user_type, 1)
                    addProperty(Constant.mobile_number, AppCredentials.isMobileNumber)
                    addProperty(Constant.coupon_id, coupon_code)
                    addProperty(Constant.coupon_link, source_link)
                }
                appViewModel?.isSpentPoints(isAccessToken ?: "", jsonObject,this)

                val intent = Intent(this, CouponOrderActivity::class.java).apply {
                    putExtra(Constant.coupon_code, data.coupon_code)
                    putExtra(Constant.qr_code, data.coupons?.getOrNull(0)?.qr_code)
                    putExtra(Constant.expiry_date, data.coupons?.getOrNull(0)?.expiry_date)
                    putExtra(Constant.merchant_logo, data.merchant_logo)
                    putExtra(Constant.offer, data.offer)
                    putExtra(Constant.redirect_url, data.redirect_url)
                    putExtra(Constant.isCTAvalid, data.isCTAvalid)
                    putExtra(Constant.CTAname, data.cTAname)
                    putExtra(Constant.CTAredirect, data.cTAredirect)
                    putExtra(Constant.category_name, category_name)
                    putExtra(Constant.how_to_use, howToUseText)
                    putExtra(Constant.Terms_and_Conditions, termsAndConditions)
                    putExtra(Constant.thumbnail, thumbnail)
                    putExtra(Constant.merchant_name, merchant_name)
                    putExtra(Constant.offer_show, binding.bottomLayout.offerText.text.toString())
                }
                startActivity(intent)
                finish()
            }
        }



        binding.btnActivateCoupon.setOnClickListener {

            if (remainingPoints < pointspercoupon || remainingPoints == 0) {
                Toast.makeText(
                    this@CouponActivateActivity,
                    Constant.Youneedmore,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            binding.btnActivateCoupon.isEnabled = false
            binding.isProgressBar.visibility = View.VISIBLE
            appViewModel?.sendactivatecoupon(
                source_link,
                "91${AppCredentials.isMobileNumber}",
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
        binding.bottomLayout.offerText4.text =
            "${getString(R.string.valid_until)} ${data.expiry_date}"
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