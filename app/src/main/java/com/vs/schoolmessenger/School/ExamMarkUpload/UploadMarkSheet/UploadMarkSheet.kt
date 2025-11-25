package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet


import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App

import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.UploadMarkSheetBinding


class UploadMarkSheet : BaseActivity<UploadMarkSheetBinding >(), View.OnClickListener {

    override fun getViewBinding(): UploadMarkSheetBinding {
        return UploadMarkSheetBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null



    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.cardUploadImage.setOnClickListener(this)

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text=isStaffDetails!!.school_name

        setBulletText(binding.lblIns1, getString(R.string.student_names_and_roll_numbers))
        setBulletText(binding.lblIns2, getString(R.string.subject_columns_and_marks))
        setBulletText(binding.lblIns3, getString(R.string.table_structure_and_layout))



    }

    fun setBulletText(textView: TextView, text: String) {
        val fullText = "• $text"
        val spannable = SpannableString(fullText)

        // Get colors from resources
        val bulletColor = ContextCompat.getColor(textView.context, R.color.dark_bg_orange_2)
        val textColor = ContextCompat.getColor(textView.context, R.color.black)

        // Make bullet (•) red
        spannable.setSpan(
            ForegroundColorSpan(bulletColor),
            0, 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Remaining text custom color
        spannable.setSpan(
            ForegroundColorSpan(textColor),
            2, fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }

    fun AppCompatActivity.dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            this.resources.displayMetrics
        ).toInt()
    }






    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.cardUploadImage -> {
                val bg = binding.lnrUploadImage.background as GradientDrawable
                val imgBg = binding.imgUpload.background as GradientDrawable
                bg.mutate()
                imgBg.mutate()

                if (!binding.lnrContainer.isVisible) {
                    binding.lnrContainer.visibility = View.VISIBLE

                    bg.setStroke(dp(2), ContextCompat.getColor(this, R.color.orange))
                    imgBg.setColor(ContextCompat.getColor(this, R.color.orange))

                    binding.imgUpload.setColorFilter(
                        ContextCompat.getColor(this, android.R.color.white),
                        PorterDuff.Mode.SRC_IN
                    )

                } else {
                    binding.lnrContainer.visibility = View.GONE

                    bg.setStroke(dp(2), ContextCompat.getColor(this, android.R.color.white))
                    imgBg.setColor(ContextCompat.getColor(this, R.color.very_light_gray_14))

                    binding.imgUpload.setColorFilter(
                        ContextCompat.getColor(this, android.R.color.black),
                        PorterDuff.Mode.SRC_IN
                    )
                }
            }


        }
    }


}