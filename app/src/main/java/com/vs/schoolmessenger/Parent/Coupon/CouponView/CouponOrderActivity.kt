package com.vs.schoolmessenger.Parent.Coupon.CouponView

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.BottomSheetOrderBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

class CouponOrderActivity : BaseActivity<BottomSheetOrderBinding>(), View.OnClickListener {

    private lateinit var appViewModel: App

    override fun getViewBinding(): BottomSheetOrderBinding {
        return BottomSheetOrderBinding.inflate(layoutInflater)
    }

    private var category_name: String = ""
    private var coupon_code: String = ""
    private var merchant_name: String = ""
    private var thumbnail: String = ""
    private var qr_code: String = ""
    private var expiry_date: String = ""
    private var merchant_logo: String = ""

    private var offer: String? = ""
    private var redirect_url: String? = ""
    private var cTAname: String? = ""
    private var cTAredirect: String? = ""
    private var how_to_use: String? = ""
    private var TermsandConditions: String? = ""
    private var offer_show: String? = ""
    private var isExpanded = false
    private var isExpanded1 = false
    private var bottomSheetBehavior: BottomSheetBehavior<View?>? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomLayout.bottomSheet)

        val screenHeight = resources.displayMetrics.heightPixels
        val topGap = 230

        binding.imageBanner.layoutParams.height = screenHeight / 2
        binding.imageBanner.requestLayout()

        binding.bottomLayout.bottomSheet.layoutParams.height = screenHeight - topGap
        binding.bottomLayout.bottomSheet.requestLayout()

        bottomSheetBehavior?.setFitToContents(true)
        bottomSheetBehavior?.peekHeight = (screenHeight * 0.6).toInt()

        // Set click listeners
        binding.bottomLayout.remember.setOnClickListener(this)
        binding.bottomLayout.remember1.setOnClickListener(this)
        binding.bottomLayout.rememberSymbol.setOnClickListener(this)
        binding.bottomLayout.copylinearlayout.setOnClickListener(this)
        binding.bottomLayout.rememberSymbol1.setOnClickListener(this)
        binding.imageTopLeft.setOnClickListener {
            onBackPressed()
        }

        binding.bottomLayout.btnActivateCoupon2.setOnClickListener {
            val intent = Intent(this, CouponDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        binding.bottomLayout.copylinearlayout.setOnClickListener {
            copyTextToClipboard()
        }


        coupon_code = intent.getStringExtra(Constant.coupon_code) ?: ""
        qr_code = intent.getStringExtra(Constant.qr_code) ?: ""
        expiry_date = intent.getStringExtra(Constant.expiry_date) ?: ""
        merchant_logo = intent.getStringExtra(Constant.merchant_logo) ?: ""
        offer = intent.getStringExtra(Constant.offer) ?: ""
        redirect_url = intent.getStringExtra(Constant.redirect_url) ?: ""
        cTAname = intent.getStringExtra(Constant.CTAname) ?: ""
        cTAredirect = intent.getStringExtra(Constant.CTAredirect) ?: ""
        category_name = intent.getStringExtra(Constant.category_name) ?: ""
        how_to_use = intent.getStringExtra(Constant.how_to_use) ?: ""
        TermsandConditions = intent.getStringExtra(Constant.Terms_and_Conditions) ?: ""
        thumbnail = intent.getStringExtra(Constant.thumbnail) ?: ""
        merchant_name = intent.getStringExtra(Constant.merchant_name) ?: ""
        offer_show = intent.getStringExtra(Constant.offer_show) ?: ""


        binding.bottomLayout.frameText.text = coupon_code
        binding.bottomLayout.expandableText.text = convertHtmlToBullets(how_to_use)
        binding.bottomLayout.expandableText1.text = convertHtmlToBullets(TermsandConditions)
        binding.bottomLayout.offerText2.text = offer_show
        binding.bottomLayout.offerText1.text = merchant_name
        binding.bottomLayout.descText.text = category_name

        Glide.with(this)
            .load(thumbnail)
            .into(binding.imageBanner)

        Glide.with(this)
            .load(merchant_logo)
            .into(binding.bottomLayout.thumbnail)

        try {
            val apiFormat = SimpleDateFormat(Constant.yyyy_MM_dd, Locale.getDefault())
            val expiryDateParsed = apiFormat.parse(expiry_date)

            val calendar = Calendar.getInstance()
            calendar.time = expiryDateParsed!!

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month =
                SimpleDateFormat(Constant.MMMM, Locale.getDefault()).format(expiryDateParsed)
            val suffix = getDaySuffix(day)

            val displayText = "${getString(R.string.expires_on)} $day$suffix $month"
            binding.bottomLayout.expiryText.text = displayText
        } catch (e: ParseException) {
            e.printStackTrace()
            binding.bottomLayout.expiryText.text = getString(R.string.Invalid_expiry_date)
        }

        showFullScreenConfetti()
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

    private fun copyTextToClipboard() {
        val textToCopy = binding.bottomLayout.frameText.text.toString()

        if (textToCopy.isEmpty()) {
            Toast.makeText(this, getString(R.string.nothing_to_copy), Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(getString(R.string.copied_text), textToCopy)
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.failed_to_access_clipboard), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showFullScreenConfetti() {
        val container = findViewById<FrameLayout?>(R.id.particle_container)

        container.post(Runnable {
            val colors = intArrayOf(
                Color.RED, Color.YELLOW, Color.BLUE,
                Color.GREEN, Color.MAGENTA, Color.CYAN,
                Color.parseColor("#FFA500"),  // orange
                Color.parseColor("#FF69B4") // pink
            )
            val particleCount = 120
            val screenWidth = container.width
            val screenHeight = container.height

            for (i in 0..<particleCount) {
                val particle = View(this)

                // Random size
                val size = getRandom(12, 30)
                val params = FrameLayout.LayoutParams(size, size)
                particle.setLayoutParams(params)

                // Set round shape with color
                val shape = GradientDrawable()
                shape.setShape(GradientDrawable.OVAL)
                shape.setSize(size, size)
                val color = colors[Random().nextInt(colors.size)]
                shape.setColor(color)
                particle.background = shape

                val startX = getRandom(0, screenWidth).toFloat()
                val startY = getRandom(-300, -100).toFloat() // start from above screen
                particle.x = startX
                particle.y = startY

                container.addView(particle)

                // Animate to bottom with some sway and rotation
                val endY = (screenHeight + getRandom(100, 300)).toFloat()
                val endX = startX + getRandom(-100, 100)

                particle.animate()
                    .x(endX)
                    .y(endY)
                    .rotationBy(getRandom(360, 1440).toFloat())
                    .setDuration(getRandom(2200, 3200).toLong())
                    .withEndAction(Runnable { container.removeView(particle) })
                    .start()
            }
            Handler(Looper.getMainLooper()).postDelayed(
                Runnable { container.removeAllViews() },
                3500
            )
        })
    }

    private fun getDaySuffix(day: Int): String {
        if (day >= 11 && day <= 13) {
            return Constant.th
        }
        when (day % 10) {
            1 -> return Constant.st
            2 -> return Constant.nd
            3 -> return Constant.rd
            else -> return Constant.th
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

    private fun getRandom(min: Int, max: Int): Int {
        return Random().nextInt((max - min) + 1) + min
    }
}
