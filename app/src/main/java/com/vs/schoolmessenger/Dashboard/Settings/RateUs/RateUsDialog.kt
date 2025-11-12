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
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.RateUsBinding

class RateUsDialog : DialogFragment(), View.OnClickListener {

    private var _binding: RateUsBinding? = null
    private val binding get() = _binding!!

    private var isRatingValue = 0
    private var isRatingData: List<GetRatingData> = ArrayList()


    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isChildDetails: ChildDetails? = null
    private var isStaffDetails: StaffDetails? = null
    var userDetails: UserDetails? = null




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
                dimAmount = 0.6f
            }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.lblClose.setOnClickListener(this)
        binding.btnsubmit.setOnClickListener(this)
        binding.lblMayBeLater.setOnClickListener(this)
        binding.btnBackHome.setOnClickListener(this)


        isChildDetails = SharedPreference.getChildDetails(requireActivity())
        isStaffDetails = SharedPreference.getStaffDetails(requireActivity())
        userDetails = SharedPreference.getUserDetails(requireActivity())

        isAccessToken = if (Constant.isParentChoose) {
            isChildDetails?.access_token
        } else {
            isStaffDetails?.access_token
        }

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()


        loadrateusdata()


        appViewModel?.getreviewlist?.observe(this) { response ->
            if (response != null && response.status && response.data.isNotEmpty()) {
                val review = response.data[0]
                getrateusData(review)

            } else {

                Log.d("Reviewlist loaded failed"," Review list has been not loaded")
            }
        }


        isRatingData = listOf(
            GetRatingData(1, "Unsatisfactory"),
            GetRatingData(2, "Needs Improvement"),
            GetRatingData(3, "Fair Experience"),
            GetRatingData(4, "Very Good"),
            GetRatingData(5, "Outstanding")
        )

        binding.ratingBar.progressTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.light_yellow)
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



    private fun loadrateusdata() {
        if(Constant.isParentChoose){
            appViewModel!!.getreviewlist(isAccessToken!!, isChildDetails!!.secondary_mobile)
        }
        else{
            appViewModel!!.getreviewlist(isAccessToken!!, Constant.user_details!!.staff_details[0].mobile_no)
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


    private fun observeSubmitReviewResponse() {
        appViewModel?.reviewpost?.observe(this) { response ->
            if (response != null && response.status) {
                binding.rateUs.visibility = View.GONE
                binding.rateusSuccess.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), response?.message ?: "Submission failed", Toast.LENGTH_SHORT).show()
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



    private fun isBackRoundFullChange() {
        // Add your full selection reset logic if needed
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lblClose, R.id.lblMayBeLater -> dismiss()


            R.id.btnsubmit -> {
                val description = binding.edtSuggestions.text.toString().trim()
                val rating = isRatingValue
                val mobile = if (Constant.isParentChoose) {
                    isChildDetails?.secondary_mobile
                } else {
                    Constant.user_details?.staff_details?.get(0)?.mobile_no
                }

                if (mobile.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Mobile number not found", Toast.LENGTH_SHORT).show()
                    return
                }

                val jsonObject = JsonObject().apply {
                    addProperty("mobile_number", mobile)
                    addProperty("rating", rating)
                    addProperty("description", description)
                }
                appViewModel?.reviewpost(isAccessToken!!, jsonObject)

                observeSubmitReviewResponse()
            }

            R.id.btnBackHome -> {
             dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
