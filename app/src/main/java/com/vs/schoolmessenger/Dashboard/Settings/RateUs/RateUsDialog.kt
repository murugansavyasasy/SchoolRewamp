package com.vs.schoolmessenger.Dashboard.Settings.RateUs

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.ReviewData
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.SubmitReviewRequest
import com.vs.schoolmessenger.Parent.Coupon.CouponCredentials.AppCredentials.isMobileNumber
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.RateUsBinding

class RateUsDialog : DialogFragment(), View.OnClickListener {

    private var _binding: RateUsBinding? = null
    private val binding get() = _binding!!

    private var ratingValue = 0
    private val ratingData = listOf(
        GetRatingData(1, "Unsatisfactory"),
        GetRatingData(2, "Needs Improvement"),
        GetRatingData(3, "Fair Experience"),
        GetRatingData(4, "Very Good"),
        GetRatingData(5, "Outstanding")
    )

    private lateinit var appViewModel: App
    private var mobileNumber: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
            attributes = attributes.apply { dimAmount = 0.6f }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupUI()
        observeReviewList()
        loadRateUsData()
    }

    private fun setupViewModel() {
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()
    }

    private fun setupUI() = with(binding) {
        listOf(lblClose, btnsubmit, lblMayBeLater, btnBackHome).forEach {
            it.setOnClickListener(this@RateUsDialog)
        }

        ratingBar.progressTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.light_yellow)
        )

        edtSuggestions.setOnTouchListener { v, event ->
            if (v.hasFocus()) {
                v.parent.requestDisallowInterceptTouchEvent(event.action != MotionEvent.ACTION_UP)
            }
            false
        }

        ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { _, rating, _ ->
                ratingValue = rating.toInt()
                if (ratingValue == 0) {
                    showMaybeLater()
                    btnsubmit.isEnabled = false
                } else {
                    showRatingUI()
                    updateRatingContent(ratingValue)
                }
            }
    }

    private fun loadRateUsData() {
        mobileNumber = SharedPreference.getMobileNumber(requireActivity()).orEmpty()
        if (mobileNumber.isBlank()) {
            Log.w("AppCredentials", "Mobile number is empty or null")
            return
        }
        appViewModel.getreviewlist("", mobileNumber)
    }

    private fun observeReviewList() {
        appViewModel.getreviewlist!!.observe(viewLifecycleOwner) { response ->
            if (response?.status == true && response.data.isNotEmpty()) {
                bindReviewData(response.data.first())
            } else {
                Log.d("RateUsDialog", "No previous reviews found.")
            }
        }
    }

    private fun observeSubmitReviewResponse() {
        appViewModel.reviewpost!!.observe(viewLifecycleOwner) { response ->
            if (response?.status == true) {
                binding.rateUs.visibility = View.GONE
                binding.rateusSuccess.visibility = View.VISIBLE
            } else {
                Toast.makeText(
                    requireContext(),
                    response?.message ?: "Submission failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun bindReviewData(data: ReviewData) = with(binding) {
        try {
            ratingBar.rating = data.rating.toFloat()
            ratingValue = data.rating
            edtSuggestions.setText(data.description)
            updateRatingContent(data.rating)
            showRatingUI()
            btnsubmit.isEnabled = true
        } catch (e: Exception) {
            Log.e("RateUsDialog", "Error binding review data", e)
        }
    }

    private fun updateRatingContent(stars: Int) {
        ratingData.find { it.rating == stars }?.let { item ->
            binding.lblContent.text = item.content
            binding.view.visibility = View.GONE
            binding.btnsubmit.isEnabled = true
        }
    }

    private fun showRatingUI() = with(binding) {
        imgFeedBack.visibility = View.VISIBLE
        lblMayBeLater.visibility = View.GONE
        lnrRatingContent.visibility = View.VISIBLE
    }

    private fun showMaybeLater() = with(binding) {
        imgFeedBack.visibility = View.VISIBLE
        lblMayBeLater.visibility = View.VISIBLE
        lnrRatingContent.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.lblClose, R.id.lblMayBeLater, R.id.btnBackHome -> dismiss()

            R.id.btnsubmit -> submitReview()
        }
    }

    private fun submitReview() {
        val description = binding.edtSuggestions.text.toString().trim()

        if (mobileNumber.isBlank()) {
            Toast.makeText(requireContext(), "Mobile number not found", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty("mobile_number", mobileNumber)
            addProperty("rating", ratingValue)
            addProperty("description", description)
        }

        appViewModel.reviewpost("", jsonObject)
        observeSubmitReviewResponse()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

