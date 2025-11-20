package com.vs.schoolmessenger.Dashboard.Settings.Faq

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var llBottomText: View
    private lateinit var underline: View
    private lateinit var txtEmpowering: View
    private lateinit var txtSchoolCount: View

    private lateinit var txtConnecting: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        llBottomText = binding.root.findViewById(R.id.llBottomText)
        underline = binding.root.findViewById(R.id.underline)
        txtEmpowering = binding.root.findViewById(R.id.txtEmpowering)
        txtSchoolCount = binding.root.findViewById(R.id.txtSchoolCount)
        txtConnecting = binding.root.findViewById(R.id.txtConnecting)
        txtConnecting.alpha = 0f
        binding.dot1.alpha = 0f
        binding.dot2.alpha = 0f
        binding.dot3.alpha = 0f
        binding.imgLogo.alpha = 0f
        binding.imgLogo.scaleX = 0.3f
        binding.imgLogo.scaleY = 0.3f
        binding.bigCard.scaleX = 0.9f
        binding.bigCard.scaleY = 0.9f
        binding.bigCard.alpha = 0f
        llBottomText.alpha = 0f
        underline.alpha = 0f

        startVideoStyleAnimation()
    }

    private fun startVideoStyleAnimation() {
        animateCardEntrance()
        animateFirstDotWithEmphasis()
        animateRemainingDots()
        animateBubbleBurst()
        animateLogo()
        animateConnectingText()
        animateBottomText()
        animateLogoPulse()

        binding.root.postDelayed({
            startActivity(Intent(this, Faq::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 4500)
    }

    private fun animateCardEntrance() {
        val scaleX = ObjectAnimator.ofFloat(binding.bigCard, "scaleX", 0.9f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.bigCard, "scaleY", 0.9f, 1f)
        val alpha = ObjectAnimator.ofFloat(binding.bigCard, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 400
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun animateFirstDotWithEmphasis() {
        binding.root.postDelayed({
            // First dot has special emphasis - larger expansion
            val alpha = ObjectAnimator.ofFloat(binding.dot1, "alpha", 0f, 1f)
            val scaleX = ObjectAnimator.ofFloat(binding.dot1, "scaleX", 0f, 2.5f, 1f)
            val scaleY = ObjectAnimator.ofFloat(binding.dot1, "scaleY", 0f, 2.5f, 1f)
            val rotation = ObjectAnimator.ofFloat(binding.dot1, "rotation", 0f, 360f)

            AnimatorSet().apply {
                playTogether(alpha, scaleX, scaleY, rotation)
                duration = 600
                interpolator = OvershootInterpolator(1.5f)
                start()
            }

            // Ripple effect after first dot
            binding.dot1.postDelayed({
                val rippleScale = ObjectAnimator.ofFloat(binding.dot1, "scaleX", 1f, 1.4f, 1f)
                val rippleScaleY = ObjectAnimator.ofFloat(binding.dot1, "scaleY", 1f, 1.4f, 1f)
                AnimatorSet().apply {
                    playTogether(rippleScale, rippleScaleY)
                    duration = 300
                    start()
                }
            }, 300)
        }, 500)
    }

    private fun animateRemainingDots() {
        // Dot 2 - from center
        binding.root.postDelayed({
            val alpha = ObjectAnimator.ofFloat(binding.dot2, "alpha", 0f, 1f)
            val scaleX = ObjectAnimator.ofFloat(binding.dot2, "scaleX", 0f, 1.5f, 1f)
            val scaleY = ObjectAnimator.ofFloat(binding.dot2, "scaleY", 0f, 1.5f, 1f)
            val translationX = ObjectAnimator.ofFloat(binding.dot2, "translationX", -30f, 0f)

            AnimatorSet().apply {
                playTogether(alpha, scaleX, scaleY, translationX)
                duration = 500
                interpolator = OvershootInterpolator()
                start()
            }
        }, 1000)

        // Dot 3 - from center
        binding.root.postDelayed({
            val alpha = ObjectAnimator.ofFloat(binding.dot3, "alpha", 0f, 1f)
            val scaleX = ObjectAnimator.ofFloat(binding.dot3, "scaleX", 0f, 1.5f, 1f)
            val scaleY = ObjectAnimator.ofFloat(binding.dot3, "scaleY", 0f, 1.5f, 1f)
            val translationX = ObjectAnimator.ofFloat(binding.dot3, "translationX", -60f, 0f)

            AnimatorSet().apply {
                playTogether(alpha, scaleX, scaleY, translationX)
                duration = 500
                interpolator = OvershootInterpolator()
                start()
            }
        }, 1250)
    }

    private fun animateLogo() {
        binding.root.postDelayed({
            binding.imgLogo.visibility = View.VISIBLE

            val alpha = ObjectAnimator.ofFloat(binding.imgLogo, "alpha", 0f, 1f)
            val scaleX = ObjectAnimator.ofFloat(binding.imgLogo, "scaleX", 0.5f, 1f)
            val scaleY = ObjectAnimator.ofFloat(binding.imgLogo, "scaleY", 0.5f, 1f)
            val rotation = ObjectAnimator.ofFloat(binding.imgLogo, "rotation", 0f, 8f, -8f, 0f)

            AnimatorSet().apply {
                playTogether(alpha, scaleX, scaleY, rotation)
                duration = 1200   // smoother slow entrance
                interpolator = OvershootInterpolator(1.2f)
                start()
            }
        }, 2000)
    }


    private fun animateBottomText() {
        binding.root.postDelayed({
            // Fade in the entire bottom text section
            val fadeIn = ObjectAnimator.ofFloat(llBottomText, "alpha", 0f, 1f)
            val slideUp = ObjectAnimator.ofFloat(llBottomText, "translationY", 30f, 0f)

            AnimatorSet().apply {
                playTogether(fadeIn, slideUp)
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }

            // Animate underline width expansion
            binding.root.postDelayed({
                underline.alpha = 1f
                val widthAnim = ObjectAnimator.ofFloat(underline, "scaleX", 0f, 1f)
                widthAnim.duration = 400
                widthAnim.interpolator = AccelerateDecelerateInterpolator()
                widthAnim.start()
            }, 300)
        }, 2700)
    }

    private fun animateLogoPulse() {
        binding.root.postDelayed({
            val pulseScaleX = ObjectAnimator.ofFloat(binding.imgLogo, "scaleX", 1f, 1.05f, 1f)
            val pulseScaleY = ObjectAnimator.ofFloat(binding.imgLogo, "scaleY", 1f, 1.05f, 1f)

            AnimatorSet().apply {
                playTogether(pulseScaleX, pulseScaleY)
                duration = 800
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }, 2700)
    }

    private fun animateConnectingText() {
        binding.root.postDelayed({
            val fadeIn = ObjectAnimator.ofFloat(txtConnecting, "alpha", 0f, 1f)
            val slideUp = ObjectAnimator.ofFloat(txtConnecting, "translationY", 40f, 0f)

            AnimatorSet().apply {
                playTogether(fadeIn, slideUp)
                duration = 800
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }, 3000)
    }

    private fun animateBubbleBurst() {
        binding.root.postDelayed({

            binding.bubbleView.alpha = 1f
            binding.bubbleView.scaleX = 0f
            binding.bubbleView.scaleY = 0f

            val scaleX = ObjectAnimator.ofFloat(binding.bubbleView, "scaleX", 0f, 6f)
            val scaleY = ObjectAnimator.ofFloat(binding.bubbleView, "scaleY", 0f, 6f)
            val alpha = ObjectAnimator.ofFloat(binding.bubbleView, "alpha", 1f, 0f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, alpha)
                duration = 700
                interpolator = OvershootInterpolator(1.5f)
                start()
            }

        }, 1600)
    }


}