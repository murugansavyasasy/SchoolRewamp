package com.vs.schoolmessenger.Dashboard.Parent

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.databinding.ChildDashboardBinding

class ParentDashboard : BaseActivity<ChildDashboardBinding>(), View.OnClickListener {

    override fun getViewBinding(): ChildDashboardBinding {
        return ChildDashboardBinding.inflate(layoutInflater)
    }
    var authViewModel: Auth? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true

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

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", "Token: $token")
                    isUpdateDeviceToken(token)
                }
            }

        authViewModel!!.isDeviceToken?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
            }
        }
    }


    override fun onClick(v: View?) {

    }

    private fun isUpdateDeviceToken(token: String) {
        val jsonObject = JsonObject()
        val isSecureId = Constant.getAndroidSecureId(this)

        jsonObject.addProperty(APIKeyNames.Req_mobile_number, Constant.isMobileNumber)
        jsonObject.addProperty(APIKeyNames.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(APIKeyNames.Req_device_token, token)
        jsonObject.addProperty(APIKeyNames.Req_secure_id, isSecureId)
        jsonObject.add(APIKeyNames.device_info, Constant.getDeviceDetails(this))

        authViewModel!!.isDeviceToken(jsonObject, this)
    }

}