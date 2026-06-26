package com.vs.schoolmessenger.School.ClassTest

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.R

object StepIndicatorHelper {

    fun setStep(indicatorView: View, currentStep: Int) {
        val context = indicatorView.context

        val stepViews = listOf(
            indicatorView.findViewById<TextView>(R.id.step1),
            indicatorView.findViewById<TextView>(R.id.step2),
            indicatorView.findViewById<TextView>(R.id.step3),
            indicatorView.findViewById<TextView>(R.id.step4),
            indicatorView.findViewById<TextView>(R.id.step5),
        )

        val labelViews = listOf(
            indicatorView.findViewById<TextView>(R.id.lblStep1),
            indicatorView.findViewById<TextView>(R.id.lblStep2),
            indicatorView.findViewById<TextView>(R.id.lblStep3),
            indicatorView.findViewById<TextView>(R.id.lblStep4),
            indicatorView.findViewById<TextView>(R.id.lblStep5),
        )

        val lineViews = listOf(
            indicatorView.findViewById<View>(R.id.line1),
            indicatorView.findViewById<View>(R.id.line2),
            indicatorView.findViewById<View>(R.id.line3),
            indicatorView.findViewById<View>(R.id.line4),
        )

        val primaryColor = ContextCompat.getColor(context, R.color.PrimaryColor)
        val greyColor    = ContextCompat.getColor(context, R.color.grey)
        val whiteColor   = ContextCompat.getColor(context, R.color.white)

        stepViews.forEachIndexed { index, stepView ->
            val stepNumber = index + 1
            val labelView  = labelViews[index]

            when {
                stepNumber < currentStep -> {
                    stepView.background = ContextCompat.getDrawable(context, R.drawable.bg_step_completed)
                    stepView.text = "✓"
                    stepView.setTextColor(whiteColor)
                    labelView.setTextColor(primaryColor)
                    labelView.setTypeface(null, Typeface.BOLD)
                }
                stepNumber == currentStep -> {
                    stepView.background = ContextCompat.getDrawable(context, R.drawable.bg_step_active)
                    stepView.text = stepNumber.toString()
                    stepView.setTextColor(primaryColor)
                    labelView.setTextColor(primaryColor)
                    labelView.setTypeface(null, Typeface.BOLD)
                }
                else -> {
                    stepView.background = ContextCompat.getDrawable(context, R.drawable.bg_step_inactive)
                    stepView.text = stepNumber.toString()
                    stepView.setTextColor(greyColor)
                    labelView.setTextColor(greyColor)
                    labelView.setTypeface(null, Typeface.NORMAL)
                }
            }
            if (index < lineViews.size) {
                lineViews[index].setBackgroundColor(
                    if (stepNumber < currentStep) primaryColor else greyColor
                )
            }
        }
    }
}