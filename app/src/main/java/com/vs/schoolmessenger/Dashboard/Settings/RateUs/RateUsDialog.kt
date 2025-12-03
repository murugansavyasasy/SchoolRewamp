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
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.FirebaseApp
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.CategoryItem
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.RemarkItem
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
    private lateinit var appViewModel: App
    private var mobileNumber = ""
    private var allRemarks: List<RemarkItem>? = null
    private var selectedRemark: RemarkItem? = null
    private var categoryAdapter: CategoryAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = RateUsBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
            attributes = attributes.apply { dimAmount = 0.6f }
        }
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
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

        ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { _, rating, _ ->
                ratingValue = rating.toInt()
                if (ratingValue == 0) {
                    showMaybeLater()
                } else {
                    showRatingUI()
                    loadRemarkForRating(ratingValue)
                }
            }
    }

    private fun loadRateUsData() {
        mobileNumber = SharedPreference.getMobileNumber(requireActivity()).orEmpty()
        if (mobileNumber.isBlank()) return
        appViewModel.getreviewlist("", mobileNumber)
    }

    private fun observeReviewList() {
        appViewModel.getreviewlist!!.observe(viewLifecycleOwner) { response ->
            val data = response?.data?.firstOrNull() ?: return@observe
            allRemarks = data.remarks
            if (data.rating != null) bindPreviousReview(data)
        }
    }

    private fun bindPreviousReview(data: ReviewData) {
        ratingValue = data.rating!!
        binding.ratingBar.rating = ratingValue.toFloat()
        binding.edtSuggestions.setText(data.description)
        loadRemarkForRating(ratingValue)

  
        val remark = allRemarks?.firstOrNull { it.rating == data.rating }
        remark?.category?.forEach { cat -> cat.selected = cat.selected == true }
    }

    private fun loadRemarkForRating(star: Int) {
        selectedRemark = allRemarks?.firstOrNull { it.rating == star }

        selectedRemark?.let { remark ->
            binding.lblTitle.text = remark.name
            bindCategoryList(remark.category ?: emptyList())
        }
    }

    private fun bindCategoryList(list: List<CategoryItem>) {
        val recycler = binding.recyclerCategories
        recycler.visibility = View.VISIBLE

        val layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        recycler.layoutManager = layoutManager
        categoryAdapter = CategoryAdapter(list.toMutableList()) {}
        recycler.adapter = categoryAdapter
    }

    private fun showRatingUI() = with(binding) {
        imgFeedBack.visibility = View.VISIBLE
        lblMayBeLater.visibility = View.GONE
        lnrRatingContent.visibility = View.VISIBLE
        recyclerCategories.visibility = View.VISIBLE
    }

    private fun showMaybeLater() = with(binding) {
        imgFeedBack.visibility = View.VISIBLE
        lblMayBeLater.visibility = View.VISIBLE
        lnrRatingContent.visibility = View.GONE
        recyclerCategories.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.lblClose, R.id.lblMayBeLater, R.id.btnBackHome -> dismiss()
            R.id.btnsubmit -> submitReview()
        }
    }

    private fun submitReview() {
        val description = binding.edtSuggestions.text.toString().trim()
        val selectedCategories = selectedRemark?.category?.filter { it.selected == true }?.map { it.name }

        val json = JsonObject().apply {
            addProperty("mobile_number", mobileNumber)
            addProperty("rating", ratingValue)
            addProperty("description", description)

            val arr = JsonArray()
            selectedCategories?.forEach { arr.add(it) }
            add("categories", arr)
        }

        appViewModel.reviewpost("", json)
        observeSubmitReviewResponse()
    }

    private fun observeSubmitReviewResponse() {
        appViewModel.reviewpost!!.observe(viewLifecycleOwner) { response ->
            if (response?.status == true) {
                binding.rateUs.visibility = View.GONE
                binding.rateusSuccess.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

