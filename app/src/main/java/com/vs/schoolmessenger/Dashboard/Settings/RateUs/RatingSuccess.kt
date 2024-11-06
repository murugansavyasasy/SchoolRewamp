package com.vs.schoolmessenger.Dashboard.Settings.RateUs

import android.view.View
import androidx.activity.OnBackPressedCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.RatingSuccessBinding

class RatingSuccess : BaseActivity<RatingSuccessBinding>(), View.OnClickListener {

    override fun getViewBinding(): RatingSuccessBinding {
        return RatingSuccessBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()
        binding.btnBackHome.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnBackHome -> {
                onBackPressed()
            }
        }
    }
}