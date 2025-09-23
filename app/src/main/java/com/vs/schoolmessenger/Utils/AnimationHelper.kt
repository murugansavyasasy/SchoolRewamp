package com.vs.schoolmessenger.Utils

import android.R.attr.repeatCount
import android.R.attr.repeatMode
import com.vs.schoolmessenger.R
import android.animation.*
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat

class AnimationHelper(private val context: Context) {

    /** Logo Glow + Scale Pulse Animation */
//    fun startLogoRingAnimation(bellImageView: ImageView, glowView: View) {
//        // Scale animation for bell
//        val scaleX = ObjectAnimator.ofFloat(bellImageView, "scaleX", 1f, 1.2f)
//        val scaleY = ObjectAnimator.ofFloat(bellImageView, "scaleY", 1f, 1.2f)
//        scaleX.repeatMode = ValueAnimator.REVERSE
//        scaleX.repeatCount = ValueAnimator.INFINITE
//        scaleY.repeatMode = ValueAnimator.REVERSE
//        scaleY.repeatCount = ValueAnimator.INFINITE
//
//        // Glow animation (scale + fade)
//        val glowScaleX = ObjectAnimator.ofFloat(glowView, "scaleX", 1f, 1.5f)
//        val glowScaleY = ObjectAnimator.ofFloat(glowView, "scaleY", 1f, 1.5f)
//        val glowAlpha = ObjectAnimator.ofFloat(glowView, "alpha", 0.5f, 0f)
//        glowScaleX.repeatMode = ValueAnimator.REVERSE
//        glowScaleX.repeatCount = ValueAnimator.INFINITE
//        glowScaleY.repeatMode = ValueAnimator.REVERSE
//        glowScaleY.repeatCount = ValueAnimator.INFINITE
//        glowAlpha.repeatMode = ValueAnimator.REVERSE
//        glowAlpha.repeatCount = ValueAnimator.INFINITE
//
//        AnimatorSet().apply {
//            playTogether(scaleX, scaleY, glowScaleX, glowScaleY, glowAlpha)
//            duration = 800
//            start()
//        }
//    }

    /** Galaxy Rings Animation */
//    fun addGalaxyAnimation(parentView: FrameLayout, ringCount: Int = 3) {
//        val animationDuration = 3000L
//
//        for (i in 0 until ringCount) {
//            val ring = View(context).apply {
//                layoutParams = FrameLayout.LayoutParams(200, 200)
//                background = ContextCompat.getDrawable(context, R.drawable.glow_background)
//                alpha = 0f
//            }
//            parentView.addView(ring)
//
//            val scaleX = ObjectAnimator.ofFloat(ring, "scaleX", 0.6f, 1.8f)
//            val scaleY = ObjectAnimator.ofFloat(ring, "scaleY", 0.6f, 1.8f)
//            val alphaAnim = ObjectAnimator.ofFloat(ring, "alpha", 0.7f, 0f)
//
//            AnimatorSet().apply {
//                playTogether(scaleX, scaleY, alphaAnim)
//                duration = animationDuration
//                startDelay = i * (animationDuration / ringCount)
//                repeatCount = ValueAnimator.INFINITE
//                start()
//            }
//        }
//    }

    /** Loading Dots Animation */
//    fun startLoadingDotsAnimation(dots: List<View>) {
//        dots.forEachIndexed { index, dot ->
//            dot.alpha = 0f
//            val scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 1f, 1.2f)
//            val scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 1f, 1.2f)
//            val alphaAnim = ObjectAnimator.ofFloat(dot, "alpha", 0f, 1f)
//
//            AnimatorSet().apply {
//                playTogether(scaleX, scaleY, alphaAnim)
//                duration = 600
//                repeatMode = ValueAnimator.REVERSE
//                repeatCount = ValueAnimator.INFINITE
//                startDelay = index * 200L
//                start()
//            }
//        }
//    }
}


