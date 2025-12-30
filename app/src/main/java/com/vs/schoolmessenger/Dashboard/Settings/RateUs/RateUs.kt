package com.vs.schoolmessenger.Dashboard.Settings.RateUs

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.ReviewData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.RateUsBinding

class RateUs : BaseActivity<RateUsBinding>(), View.OnClickListener {

    private var isRatingValue = 0
    private var isRatingData: List<GetRatingData> = ArrayList()
    private var inPutRatingContent: ArrayList<String> = ArrayList()
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isChildDetails: ChildDetails? = null
    private var isStaffDetails: StaffDetails? = null
    var userDetails: UserDetails? = null

    override fun getViewBinding(): RateUsBinding {
        return RateUsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()


        binding.lblClose.setOnClickListener(this)
        binding.btnsubmit.setOnClickListener(this)
        binding.lblMayBeLater.setOnClickListener(this)


        isChildDetails = SharedPreference.getChildDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        userDetails = SharedPreference.getUserDetails(this)

        if (Constant.isParentChoose) {
            isAccessToken = isChildDetails?.access_token
        } else {
            if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                isAccessToken = isStaffDetails!!.access_token
            } else {
                isAccessToken = userDetails!!.staff_details[0].access_token
            }
        }

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()


        loadrateusdata()


        appViewModel?.getreviewlist?.observe(this) { response ->
            if (response != null && response.status && response.data.isNotEmpty()) {
                val review = response.data[0]
                getrateusData(review)

            } else {

                Log.d("Reviewlist loaded failed", " Review list has been not loaded")
            }
        }

        isRatingData = listOf(
            GetRatingData(1, "Super"),
            GetRatingData(1, "Nice"),
            GetRatingData(3, "Better"),
            GetRatingData(4, "Well done"),
            GetRatingData(5, "Not bad")
        )

        binding.ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, v, b ->
                Log.d("isRating", ratingBar.rating.toString())
                isRatingValue = ratingBar.rating.toInt()
                when (ratingBar.rating.toInt()) {
                    0 -> {
                        isMaybeLater()
                        binding.btnsubmit.isEnabled = false
                    }

                    1 -> {
                        isRating()
                        getRatingContent(1)
                        isBackRoundFullChange()
                    }

                    2 -> {
                        isRating()
                        getRatingContent(2)
                        isBackRoundFullChange()
                    }

                    3 -> {
                        isRating()
                        getRatingContent(3)
                        isBackRoundFullChange()
                    }

                    4 -> {
                        isRating()
                        getRatingContent(4)
                        isBackRoundFullChange()
                    }

                    5 -> {
                        isRating()
                        getRatingContent(5)
                        isBackRoundFullChange()
                    }

                    else -> {
//                        binding.consRatingType.visibility = View.VISIBLE
                        binding.btnsubmit.isEnabled = true
                    }
                }
            }
    }

    private fun openAppInPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.setPackage("com.android.vending")
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Play Store not installed, open in browser
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            context.startActivity(intent)
        }

    }


    private fun loadrateusdata() {
        if (Constant.isParentChoose) {
            appViewModel!!.getreviewlist(isAccessToken!!, isChildDetails!!.whatsapp_number,this)
        } else {
            appViewModel!!.getreviewlist(
                isAccessToken!!,
                Constant.user_details!!.staff_details[0].mobile_no,this
            )
        }
    }


    private fun getrateusData(data: ReviewData) {

        try {
            binding.ratingBar.rating = data.rating.toFloat()
            isRatingValue = data.rating
            binding.edtSuggestions.setText(data.description)
            getRatingContent(data.rating)
            isRating()
            binding.btnsubmit.isEnabled = true
            isBackRoundFullChange()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RateUs", "Error binding review data: ${e.message}")
        }

    }


    private fun isRating() {
        binding.imgFeedBack.visibility = View.GONE
        binding.lblMayBeLater.visibility = View.GONE
        binding.lnrRatingContent.visibility = View.VISIBLE
    }

    private fun isMaybeLater() {
        binding.imgFeedBack.visibility = View.VISIBLE
        binding.lblMayBeLater.visibility = View.VISIBLE
        binding.lnrRatingContent.visibility = View.GONE
    }


    private fun getRatingContent(isStarType: Int) {

        for (i in isRatingData.indices) {
            if (isRatingData[i].rating == isStarType) {
                binding.lblContent.text = isRatingData[i].content
//                binding.consRatingType.visibility = View.VISIBLE
                binding.btnsubmit.isEnabled = true
                for (k in inPutRatingContent.indices) {


                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun isBackRoundChange(isRatingId: TextView) {

        if (isRatingId.background.constantState == resources.getDrawable(R.drawable.bg_outline_light_blue).constantState) {
            isRatingId.setBackgroundResource(R.drawable.bg_choose_rating)
            isRatingId.setTextColor(resources.getColor(R.color.white))
        } else {
            isRatingId.setBackgroundResource(R.drawable.bg_outline_light_blue)
            isRatingId.setTextColor(resources.getColor(R.color.navi_blue3))
        }
    }


    private fun isBackRoundFullChange() {


    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.lblClose -> {
                onBackPressed()
            }


            R.id.btnsubmit -> {
                val intent = Intent(this@RateUs, RatingSuccess::class.java)
                startActivity(intent)
            }

            R.id.lblMayBeLater -> {
                onBackPressed()
            }
        }
    }
}