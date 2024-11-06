package com.vs.schoolmessenger.Auth.Introduction

import android.content.Intent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.Country.CountryScreen
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.IntroductionBinding

class Introduction : BaseActivity<IntroductionBinding>(), View.OnClickListener {

    override fun getViewBinding(): IntroductionBinding {
        return IntroductionBinding.inflate(layoutInflater)
    }

    private lateinit var slideInRight: Animation
    private var isCompleteIntro = false

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        binding.btnNext.setOnClickListener(this)
        binding.btnPrevious.setOnClickListener(this)
        binding.btnSkip.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {

        when (p0?.id) {
            R.id.btnNext -> {
                if (!isCompleteIntro) {
                    binding.btnPrevious.visibility = View.VISIBLE
                    slideInRight = AnimationUtils.loadAnimation(this, R.anim.right_to_left)
                    binding.btnPrevious.startAnimation(slideInRight)
                    binding.lblIntroContent.text = getString(R.string.Introduction2)
                    binding.lblIntroContent.startAnimation(slideInRight)
                    isCompleteIntro = true
                } else {
                    isGoingToCountryActivity()
                }
            }

            R.id.btnPrevious -> {
                isCompleteIntro = false
                binding.btnPrevious.visibility = View.GONE
                slideInRight = AnimationUtils.loadAnimation(this, R.anim.left_to_right)
                binding.lblIntroContent.text = getString(R.string.Introduction)
                binding.lblIntroContent.startAnimation(slideInRight)
            }

            R.id.btnSkip -> {
                isGoingToCountryActivity()
            }
        }
    }

    private fun isGoingToCountryActivity() {
        val intent = Intent(this@Introduction, CountryScreen::class.java)
        startActivity(intent)
    }
}