package com.vs.schoolmessenger.Dashboard.Settings.Faq

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.graphics.Color
import android.os.Build
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private lateinit var llBottomText: View
    private lateinit var underline: View
    private lateinit var txtEmpowering: View
    private lateinit var txtSchoolCount: View
    private lateinit var txtConnecting: View

    private val confettiColors = listOf(
        Color.parseColor("#FF6B9D"), Color.parseColor("#4ECDC4"), Color.parseColor("#FFD93D"),
        Color.parseColor("#A8E6CF"), Color.parseColor("#B4A7D6"), Color.parseColor("#F7CAC9"),
        Color.parseColor("#6C5CE7"), Color.parseColor("#74B9FF"), Color.parseColor("#FFA502"),
        Color.parseColor("#55E6C1"), Color.parseColor("#FDA7DF"), Color.parseColor("#95E1D3"),
        Color.parseColor("#F38181"), Color.parseColor("#AA96DA"), Color.parseColor("#FCBAD3"),
        Color.parseColor("#FFB6C1"), Color.parseColor("#87CEEB"), Color.parseColor("#98D8C8"),
        Color.parseColor("#F7DC6F"), Color.parseColor("#BB8FCE")
    )
    override fun getViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        isToolBarNoticeCallTheme()
        llBottomText = binding.llBottomText
        underline = binding.underline
        txtEmpowering = binding.txtEmpowering
        txtSchoolCount = binding.txtSchoolCount
        txtConnecting = binding.txtConnecting
        txtConnecting.alpha = 0f
        binding.dot1.alpha = 0f
        binding.dot2.alpha = 0f
        binding.dot3.alpha = 0f
        binding.imgLogo.alpha = 0f
        binding.imgLogo.scaleX = 1f
        binding.imgLogo.scaleY = 1f
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
        binding.confettiContainer.post { playBubbleAnimation() }
        //animateRemainingDots()

        binding.root.postDelayed({
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 3000)
    }

    private fun playBubbleAnimation() {
        val container = binding.confettiContainer
        val centerX = container.width / 2f
        val centerY = container.height / 2f
        val bubbleCount = 30
        val bubbleDuration = 1000L
        val bubbleDelay = 20L

        repeat(bubbleCount) { i ->
            val isLastBubble = i == bubbleCount - 1
            container.postDelayed({ createBubble(container, centerX, centerY, bubbleDuration, isLastBubble)
            }, i * bubbleDelay)
        }
    }

    private fun createBubble(container: FrameLayout, centerX: Float, centerY: Float, duration: Long, isLastBubble: Boolean) {
        val bubble = View(this)
        val size = (10..15).random()
        bubble.layoutParams = FrameLayout.LayoutParams(size, size)
        bubble.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(confettiColors.random())
        }
        val (startX, startY) = when ((1..4).random()) {
            1 -> 0f to (0..container.height).random().toFloat()
            2 -> container.width.toFloat() to (0..container.height).random().toFloat()
            3 -> (0..container.width).random().toFloat() to 0f
            else -> (0..container.width).random().toFloat() to container.height.toFloat()
        }
        bubble.x = startX
        bubble.y = startY
        container.addView(bubble)
        val clusterSpread = 70
        val finalX = centerX + (-clusterSpread..clusterSpread).random() - size / 2
        val finalY = centerY + (-clusterSpread..clusterSpread).random() - size / 2
        bubble.animate().x(finalX).y(finalY).alpha(0f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { container.removeView(bubble)
                if (isLastBubble) {
                    container.visibility = View.GONE
                    startWaveAnimation()
//                    startAllSplashAnimations()
                }
            }
            .start()

        startAllSplashAnimations()

    }


    private fun startWaveAnimation() {
        val waves = listOf(binding.wave1, binding.wave2, binding.wave3)
        waves.forEachIndexed { index, wave ->
            wave.scaleX = 0f
            wave.scaleY = 0f
            wave.alpha = 0f
            wave.visibility = View.VISIBLE
            wave.animate().alpha(0.4f).scaleX(1.7f).scaleY(1.7f).setStartDelay(index * 400L).setDuration(1600).withEndAction { wave.animate().alpha(0f).scaleX(2.2f).scaleY(2.2f).setDuration(900).withEndAction { startWaveAnimation() }.start() }.start()
        }
    }


    private fun startAllSplashAnimations() {
        animateTopText()
        animateBottomText()
        animateRemainingDots()
        animateLogoPulse()
        binding.imgLogo.visibility = View.VISIBLE
        binding.imgLogo.alpha = 0f
        binding.imgLogo.scaleX = 0f
        binding.imgLogo.scaleY = 0f
        binding.imgLogo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).setInterpolator(OvershootInterpolator(1.4f)).start()
    }

    private fun animateTopText() {
        txtConnecting.translationY = -60f
        txtConnecting.alpha = 0f
        val fadeIn = ObjectAnimator.ofFloat(txtConnecting, "alpha", 0f, 1f)
        val slideDown = ObjectAnimator.ofFloat(txtConnecting, "translationY", -60f, 0f)
        AnimatorSet().apply {
            playTogether(fadeIn, slideDown)
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun animateBottomText() {
        llBottomText.translationY = 80f
        llBottomText.alpha = 0f
        val fadeIn = ObjectAnimator.ofFloat(llBottomText, "alpha", 0f, 1f)
        val slideUp = ObjectAnimator.ofFloat(llBottomText, "translationY", 80f, 0f)
        AnimatorSet().apply {
            playTogether(fadeIn, slideUp)
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        underline.alpha = 1f
        underline.scaleX = 0f
        underline.pivotX = underline.width / 2f

        val expand = ObjectAnimator.ofFloat(underline, "scaleX", 1f)
        expand.duration = 1000
        expand.interpolator = AccelerateDecelerateInterpolator()
        expand.start()
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

            binding.dot1.postDelayed({
                val scaleX = ObjectAnimator.ofFloat(binding.dot1, "scaleX", 1f, 1.4f)
                scaleX.repeatCount = ObjectAnimator.INFINITE
                scaleX.repeatMode = ObjectAnimator.REVERSE
                val scaleY = ObjectAnimator.ofFloat(binding.dot1, "scaleY", 1f, 1.4f)
                scaleY.repeatCount = ObjectAnimator.INFINITE
                scaleY.repeatMode = ObjectAnimator.REVERSE
                AnimatorSet().apply {
                    playTogether(scaleX, scaleY)
                    duration = 300
                    start()
                }
            }, 300)
        }, 500)
    }

    private fun animateRemainingDots() {
        binding.root.postDelayed({
            val alpha = ObjectAnimator.ofFloat(binding.dot2, "alpha", 0f, 1f)
            alpha.duration = 300
            alpha.start()
            val scaleX = ObjectAnimator.ofFloat(binding.dot2, "scaleX", 1f, 1.4f)
            scaleX.repeatCount = ObjectAnimator.INFINITE
            scaleX.repeatMode = ObjectAnimator.REVERSE
            val scaleY = ObjectAnimator.ofFloat(binding.dot2, "scaleY", 1f, 1.4f)
            scaleY.repeatCount = ObjectAnimator.INFINITE
            scaleY.repeatMode = ObjectAnimator.REVERSE
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
        }, 1000)

        binding.root.postDelayed({
            val alpha = ObjectAnimator.ofFloat(binding.dot3, "alpha", 0f, 1f)
            alpha.duration = 300
            alpha.start()
            val scaleX = ObjectAnimator.ofFloat(binding.dot3, "scaleX", 1f, 1.4f)
            scaleX.repeatCount = ObjectAnimator.INFINITE
            scaleX.repeatMode = ObjectAnimator.REVERSE
            val scaleY = ObjectAnimator.ofFloat(binding.dot3, "scaleY", 1f, 1.4f)
            scaleY.repeatCount = ObjectAnimator.INFINITE
            scaleY.repeatMode = ObjectAnimator.REVERSE
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
        }, 1250)
    }

    private fun animateLogoPulse() {
//        binding.root.postDelayed({
            val pulseScaleX = ObjectAnimator.ofFloat(binding.imgLogo, "scaleX", 1f, 1.05f, 1f)
            val pulseScaleY = ObjectAnimator.ofFloat(binding.imgLogo, "scaleY", 1f, 1.05f, 1f)
            AnimatorSet().apply {
                playTogether(pulseScaleX, pulseScaleY)
                duration = 800
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
//        }, 1000)
    }

    override fun isToolBarNoticeCallTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.light_sky_blue_color)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)

        }
    }
}