package com.vs.schoolmessenger.Auth.Splash
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.Introduction.Introduction
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.OtpScreenBinding
import com.vs.schoolmessenger.databinding.SplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Splash : BaseActivity<SplashBinding>(), View.OnClickListener {

    override fun getViewBinding(): SplashBinding {
        return SplashBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID

        isToolBarTheme()
        GlobalScope.launch {
            delay(2000) // 3-second delay
            withContext(Dispatchers.Main) {
                val intent = Intent(this@Splash, Introduction::class.java)
                startActivity(intent)
                finish() // Close the current activity
            }
        }
    }

    private fun isToolBarTheme(){
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.white)
            window.navigationBarColor = this.resources.getColor(R.color.white)
        }
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}