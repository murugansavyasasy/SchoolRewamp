package com.vs.schoolmessenger.CommonScreens

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.CustomCalendarFragement.CustomCalendarFragment
import com.vs.schoolmessenger.databinding.ActivityWhatsNewBinding
import com.vs.schoolmessenger.databinding.AttendanceMarkBinding

class WhatsNewActivity : BaseActivity<ActivityWhatsNewBinding>(), View.OnClickListener {

    override fun getViewBinding(): ActivityWhatsNewBinding {
        return ActivityWhatsNewBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        binding.btnClose.setOnClickListener(this)

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnClose -> {
                onBackPressed()
            }
        }
    }
}

