package com.vs.schoolmessenger.Dashboard.Parent

<<<<<<< HEAD
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Utils.Constant
=======
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
>>>>>>> 7012a55835488863f07c2a567ea44f5bea104637
import com.vs.schoolmessenger.databinding.ChildDashboardBinding

class ParentDashboard : BaseActivity<ChildDashboardBinding>(), View.OnClickListener {

    override fun getViewBinding(): ChildDashboardBinding {
        return ChildDashboardBinding.inflate(layoutInflater)
    }
<<<<<<< HEAD
    var authViewModel: Auth? = null
=======
>>>>>>> 7012a55835488863f07c2a567ea44f5bea104637

    override fun setupViews() {
        super.setupViews()

<<<<<<< HEAD
        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true


=======
>>>>>>> 7012a55835488863f07c2a567ea44f5bea104637
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
<<<<<<< HEAD

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
=======
>>>>>>> 7012a55835488863f07c2a567ea44f5bea104637
    }


    override fun onClick(v: View?) {

    }
<<<<<<< HEAD

    private fun isUpdateDeviceToken(token: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(RequestKeys.Req_mobile_number, Constant.isMobileNumber)
        jsonObject.addProperty(RequestKeys.Req_device_type, Constant.isDeviceType)

        jsonObject.addProperty(RequestKeys.Req_device_token, token)
        authViewModel!!.isDeviceToken(jsonObject, this)
    }

=======
>>>>>>> 7012a55835488863f07c2a567ea44f5bea104637
}