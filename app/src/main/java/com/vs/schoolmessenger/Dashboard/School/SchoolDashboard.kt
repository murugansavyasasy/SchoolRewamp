package com.vs.schoolmessenger.Dashboard.School

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.SchoolDashboardBinding

class SchoolDashboard : BaseActivity<SchoolDashboardBinding>(), View.OnClickListener {


    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = ChangeLanguage.getPersistedLanguage(newBase)
        val context = ChangeLanguage.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }

    var authViewModel: Auth? = null

    override fun getViewBinding(): SchoolDashboardBinding {
        return SchoolDashboardBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID

        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.primary_light)
            window.navigationBarColor = this.resources.getColor(R.color.primary_light)
        }

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        //setupToolbar()

        accessChildView(
            binding,
            R.id.nav_home,
            R.id.nav_help,
            R.id.nav_profile,
            R.id.nav_settings,
            R.id.icon_home,
            R.id.icon_help,
            R.id.icon_settings,
            R.id.icon_profile,
            R.id.fragment_container,
            R.id.customBottomNav
        )

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = it.result //this is the token retrieved
            isUpdateDeviceToken(token)
        }

        authViewModel!!.isDeviceToken?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
            }
        }
    }

    private fun isUpdateDeviceToken(token: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(RequestKeys.Req_mobile_number, Constant.isMobileNumber)
        jsonObject.addProperty(RequestKeys.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(RequestKeys.Req_device_token, token)
        authViewModel!!.isDeviceToken(jsonObject, this)
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}