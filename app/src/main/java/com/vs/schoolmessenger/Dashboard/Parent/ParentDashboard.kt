package com.vs.schoolmessenger.Dashboard.Parent

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.Fragments.HelpFragment
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.ParentProfileRewampFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SettingsFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ChildDashboardBinding

class ParentDashboard : BaseActivity<ChildDashboardBinding>(), View.OnClickListener {

    override fun getViewBinding(): ChildDashboardBinding {
        return ChildDashboardBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null
    var appViewModel: App? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true


        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.view_profile -> {
                    loadFragment(this, ParentProfileRewampFragment())
                    updateNavBar(R.id.icon_profile)
                }

                R.id.setting_click -> {
                    loadFragment(this, SettingsFragment())
                    updateNavBar(R.id.icon_settings)
                }

                R.id.help_click -> {
                    loadFragment(this, HelpFragment())
                    updateNavBar(R.id.icon_help)
                }

                R.id.role_click -> {
                    val intent = Intent(this, PrioritySelection::class.java)
                    startActivity(intent)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        appViewModel!!.isGlobalVariables?.observe(this) { response ->
            if (response != null) {
                response.status
                response.message

            }
        }


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
            R.id.lblHome,
            R.id.lblHelp,
            R.id.lblSettings,
            R.id.lblProfile,
            R.id.fragment_container,
            R.id.customBottomNav
        )

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", "Token: $token")
                    isUpdateDeviceToken(token)
                    isGlobalVariables(token)
                }
            }

        appViewModel!!.isGlobalVariables?.observe(this) { response ->
            if (response != null) {
                response.status
                response.message
                Constant.isGlobalVariableData=response.data[0]
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

    private fun isGlobalVariables(token: String) {
        val jsonObject = JsonObject()
        val jsonArray = JsonArray()
        jsonObject.add("key_names", jsonArray)
        appViewModel!!.isGetGlobalVariables(jsonObject, token, this)
    }


    fun openDrawer() {
        if (::drawerLayout.isInitialized) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }


}