package com.vs.schoolmessenger.Dashboard.Settings.Faq

import android.view.View
import android.widget.ImageView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.FaqBinding

class Faq : BaseActivity<FaqBinding>(), View.OnClickListener {

    override fun getViewBinding(): FaqBinding {
        return FaqBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.arrowIcon1.setOnClickListener(this)
        binding.arrowIcon2.setOnClickListener(this)
        binding.arrowIcon3.setOnClickListener(this)
        binding.arrowIcon4.setOnClickListener(this)
        binding.arrowIcon5.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.arrow_icon_1 -> {
                isEnterYourAnswer(binding.arrowIcon1)
            }

            R.id.arrow_icon_2 -> {
                isEnterYourAnswer(binding.arrowIcon2)
            }

            R.id.arrow_icon_3 -> {
                isEnterYourAnswer(binding.arrowIcon3)
            }

            R.id.arrow_icon_4 -> {
                isEnterYourAnswer(binding.arrowIcon4)
            }

            R.id.arrow_icon_5 -> {
                isEnterYourAnswer(binding.arrowIcon5)
            }
        }
    }

    private fun isEnterYourAnswer(isDownArrows: ImageView?) {

        binding.answer1.visibility = View.GONE
        binding.answer2.visibility = View.GONE
        binding.answer3.visibility = View.GONE
        binding.answer4.visibility = View.GONE
        binding.answer5.visibility = View.GONE

        when (isDownArrows) {
            binding.arrowIcon1 -> {
                binding.answer1.visibility = View.VISIBLE
            }

            binding.arrowIcon2 -> {
                binding.answer2.visibility = View.VISIBLE
            }

            binding.arrowIcon3 -> {
                binding.answer3.visibility = View.VISIBLE
            }

            binding.arrowIcon4 -> {
                binding.answer4.visibility = View.VISIBLE
            }

            binding.arrowIcon5 -> {
                binding.answer5.visibility = View.VISIBLE
            }
        }
    }
}