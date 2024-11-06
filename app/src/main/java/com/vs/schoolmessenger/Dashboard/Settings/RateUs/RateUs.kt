package com.vs.schoolmessenger.Dashboard.Settings.RateUs

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordGeneration
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.RateUsBinding

class RateUs : BaseActivity<RateUsBinding>(), View.OnClickListener {

    private var isRatingValue = 0
    private var isRatingData: List<GetRatingData> = ArrayList()
    private var inPutRatingContent: ArrayList<String> = ArrayList()

    override fun getViewBinding(): RateUsBinding {
        return RateUsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()

        binding.lblAppUi.setOnClickListener(this)
        binding.lblWatchUi.setOnClickListener(this)
        binding.lbPricing.setOnClickListener(this)
        binding.lblConnection.setOnClickListener(this)
        binding.lblPairing.setOnClickListener(this)
        binding.lblWatchFaces.setOnClickListener(this)
        binding.lblWatchHardware.setOnClickListener(this)
        binding.lblAlumniAssistance.setOnClickListener(this)
        binding.lblLoginuser.setOnClickListener(this)
        binding.lblRegistration.setOnClickListener(this)
        binding.lblOthers.setOnClickListener(this)
        binding.imgback.setOnClickListener(this)
        binding.btnsubmit.setOnClickListener(this)
        binding.lblMayBeLater.setOnClickListener(this)

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
                        binding.consRatingType.visibility = View.GONE
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
                        binding.consRatingType.visibility = View.VISIBLE
                        binding.btnsubmit.isEnabled = true
                    }
                }
            }
    }

    private fun isRating() {
        binding.imgFeedBack.visibility = View.GONE
        binding.lblMayBeLater.visibility = View.GONE
        binding.lnrRatingContent.visibility = View.VISIBLE
    }

    fun isMaybeLater() {
        binding.imgFeedBack.visibility = View.VISIBLE
        binding.lblMayBeLater.visibility = View.VISIBLE
        binding.lnrRatingContent.visibility = View.GONE
    }


    private fun getRatingContent(isStarType: Int) {

        for (i in isRatingData.indices) {
            if (isRatingData[i].rating == isStarType) {
                binding.lblContent.text = isRatingData[i].content
                //  inPutRatingContent = isRatingData[i].input_content!!
                binding.consRatingType.visibility = View.VISIBLE
                binding.btnsubmit.isEnabled = true
                for (k in inPutRatingContent.indices) {

                    binding.lblAppUi.text = inPutRatingContent[0]
                    binding.lblWatchUi.text = inPutRatingContent[1]
                    binding.lbPricing.text = inPutRatingContent[2]
                    binding.lblConnection.text = inPutRatingContent[3]
                    binding.lblPairing.text = inPutRatingContent[4]
                    binding.lblWatchFaces.text = inPutRatingContent[5]
                    binding.lblWatchHardware.text = inPutRatingContent[6]
                    binding.lblAlumniAssistance.text = inPutRatingContent[7]

                    if (inPutRatingContent.size > 8) {
                        binding.lblLoginuser.visibility = View.VISIBLE
                        binding.lblLoginuser.text = inPutRatingContent[8]
                    } else {
                        binding.lblLoginuser.visibility = View.GONE
                    }

                    if (inPutRatingContent.size > 9) {
                        binding.lblRegistration.visibility = View.VISIBLE
                        binding.lblRegistration.text = inPutRatingContent[9]
                    } else {
                        binding.lblRegistration.visibility = View.GONE
                    }
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun isBackRoundChange(isRatingId: TextView) {

        if (isRatingId.background.constantState == getResources().getDrawable(R.drawable.bg_outline_light_blue).constantState) {
            isRatingId.setBackgroundResource(R.drawable.bg_choose_rating)
            isRatingId.setTextColor(resources.getColor(R.color.white))
//            isRatingType.add(isRatingId.text.toString())
        } else {
            isRatingId.setBackgroundResource(R.drawable.bg_outline_light_blue)
            isRatingId.setTextColor(resources.getColor(R.color.navi_blue3))
//            isRatingType.remove(isRatingId.text.toString())
        }
    }


    private fun isBackRoundFullChange() {

        // Don't delete

//        binding.lblAppUi.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lblAppUi.setTextColor(resources.getColor(R.color.black))
//
//        binding.lblWatchUi.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lblWatchUi.setTextColor(resources.getColor(R.color.black))
//
//        binding.lbPricing.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lbPricing.setTextColor(resources.getColor(R.color.black))
//
//        binding.lblConnection.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lblConnection.setTextColor(resources.getColor(R.color.black))
//
//        binding.lblPairing.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lblPairing.setTextColor(resources.getColor(R.color.black))
//
//        binding.lblWatchFaces.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lblWatchFaces.setTextColor(resources.getColor(R.color.black))
//
//        binding.lblWatchHardware.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lblWatchHardware.setTextColor(resources.getColor(R.color.black))
//
//        binding.lblAlumniAssistance.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lblAlumniAssistance.setTextColor(resources.getColor(R.color.black))
//
//        binding.lblLoginuser.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lblLoginuser.setTextColor(resources.getColor(R.color.black))
//
//        binding.lblRegistration.setBackgroundResource(R.drawable.bg_outline_black)
//        binding.lblRegistration.setTextColor(resources.getColor(R.color.black))

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.imgback -> {
                onBackPressed()
            }

            R.id.lblAppUi -> {
                isBackRoundChange(binding.lblAppUi)
            }

            R.id.lblWatchUi -> {
                isBackRoundChange(binding.lblWatchUi)
            }

            R.id.lbPricing -> {
                isBackRoundChange(binding.lbPricing)
            }

            R.id.lblConnection -> {
                isBackRoundChange(binding.lblConnection)
            }

            R.id.lblPairing -> {
                isBackRoundChange(binding.lblPairing)
            }

            R.id.lblWatchFaces -> {
                isBackRoundChange(binding.lblWatchFaces)
            }

            R.id.lblWatchHardware -> {
                isBackRoundChange(binding.lblWatchHardware)
            }

            R.id.lblAlumniAssistance -> {
                isBackRoundChange(binding.lblAlumniAssistance)
            }

            R.id.lblLoginuser -> {
                isBackRoundChange(binding.lblLoginuser)
            }

            R.id.lblRegistration -> {
                isBackRoundChange(binding.lblRegistration)
            }

            R.id.lblOthers -> {
                isBackRoundChange(binding.lblOthers)
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