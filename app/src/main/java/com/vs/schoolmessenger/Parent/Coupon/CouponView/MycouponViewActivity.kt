package com.vs.schoolmessenger.Parent.Coupon.CouponView

import android.content.ClipData
import android.content.ClipboardManager
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummary
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.MycouponViewBinding

class MycouponViewActivity : BaseActivity<MycouponViewBinding>(), View.OnClickListener {

    override fun getViewBinding(): MycouponViewBinding {
        return MycouponViewBinding.inflate(layoutInflater)
    }

    private var merchant_name: String = ""
    private var offer_to_show: String = ""
    private var how_to_use: String = ""
    private var coupon_code: String = ""
    private var cover_image: String = ""
    private var expiry_date: String = ""
    private var expiry_type: String = ""
    private var merchant_logo: String = ""

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        binding = MycouponViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener(this)
        binding.copyIcon.setOnClickListener(this)
        merchant_name = intent.getStringExtra("merchant_name") ?: ""
        offer_to_show = intent.getStringExtra("offer_to_show") ?: ""
        how_to_use = intent.getStringExtra("how_to_use") ?: ""
        coupon_code = intent.getStringExtra("coupon_code") ?: ""
        cover_image = intent.getStringExtra("cover_image") ?: ""
        expiry_date = intent.getStringExtra("expiry_date") ?: ""
        expiry_type = intent.getStringExtra("expiry_type") ?: ""
        merchant_logo = intent.getStringExtra("merchant_logo") ?: ""
        val locationList: ArrayList<TicketSummary.Location?>? =
            intent.getParcelableArrayListExtra("location_list")
        locationList?.forEach { location ->
            Log.d(
                "Location",
                "Name: ${location?.location_name}, Lat: ${location?.latitude}, Long: ${location?.longitude}"
            )
        }


        binding.header.text = merchant_name
        binding.offer.text = offer_to_show
        binding.description.text = convertHtmlToBullets(how_to_use)
        binding.couponCode.text = coupon_code
        binding.lblLocationName.text = merchant_name

        Glide.with(this@MycouponViewActivity).load(merchant_logo).into(binding.logo)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> onBackPressed()

            R.id.copyIcon -> {
                copyTextToClipboard()
            }

        }
    }


    private fun copyTextToClipboard() {
        val textToCopy = coupon_code

        if (textToCopy.isEmpty()) {
            Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("Copied Text", textToCopy)
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to access clipboard", Toast.LENGTH_SHORT).show()
        }
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