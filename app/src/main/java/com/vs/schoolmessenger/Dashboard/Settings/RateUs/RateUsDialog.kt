package com.vs.schoolmessenger.Dashboard.Settings.RateUs

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.RateUsBinding

class RateUsDialog : DialogFragment(), View.OnClickListener {

    private var _binding: RateUsBinding? = null
    private val binding get() = _binding!!

    private var isRatingValue = 0
    private var isRatingData: List<GetRatingData> = ArrayList()
    private var inPutRatingContent: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RateUsBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% of screen width
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
            attributes = attributes.apply {
                dimAmount = 0.6f // background dim
            }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.imgback.setOnClickListener(this)
        binding.btnsubmit.setOnClickListener(this)
        binding.lblMayBeLater.setOnClickListener(this)

        isRatingData = listOf(
            GetRatingData(1, "Very Bad"),
            GetRatingData(2, "Fair"),
            GetRatingData(3, "Okay"),
            GetRatingData(4, "Good"),
            GetRatingData(5, "Loved It")
        )

        binding.edtSuggestions.setOnTouchListener { v, event ->
            if (v.hasFocus()) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }


        binding.ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, _, _ ->
                isRatingValue = ratingBar.rating.toInt()
                when (isRatingValue) {
                    0 -> {
                        isMaybeLater()
                        binding.btnsubmit.isEnabled = false
                    }

                    in 1..5 -> {
                        isRating()
                        getRatingContent(isRatingValue)
                        isBackRoundFullChange()
                    }
                }
            }
    }

    private fun isRating() {
        binding.imgFeedBack.visibility = View.VISIBLE
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
                binding.view.visibility = View.GONE
                binding.btnsubmit.isEnabled = true
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun isBackRoundChange(isRatingId: TextView) {
        if (isRatingId.background.constantState ==
            resources.getDrawable(R.drawable.bg_outline_light_blue).constantState
        ) {
            isRatingId.setBackgroundResource(R.drawable.bg_choose_rating)
            isRatingId.setTextColor(resources.getColor(R.color.white))
        } else {
            isRatingId.setBackgroundResource(R.drawable.bg_outline_light_blue)
            isRatingId.setTextColor(resources.getColor(R.color.navi_blue3))
        }
    }

    private fun isBackRoundFullChange() {
        // Add your full selection reset logic if needed
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgback, R.id.lblMayBeLater -> dismiss()


            R.id.btnsubmit -> {
                dismiss()
                startActivity(Intent(requireContext(), RatingSuccess::class.java))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
