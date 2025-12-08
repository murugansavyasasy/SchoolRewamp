package com.vs.schoolmessenger.Dashboard.Parent

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.Fragments.HolidaysFragment
import com.vs.schoolmessenger.Dashboard.Fragments.ParentHomeFragment
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.ParentProfileRewampFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SettingsFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ChildDashboardBinding
import com.vs.schoolmessenger.databinding.NavHeaderBinding

class ParentDashboard : BaseActivity<ChildDashboardBinding>(), View.OnClickListener {

    override fun getViewBinding(): ChildDashboardBinding {
        return ChildDashboardBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null
    var appViewModel: App? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    var childDetails: ChildDetails? = null
    var userDetails: UserDetails? = null
    var access_token = ""
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()


        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            // Android 10 and below → use legacy fullscreen flags
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            // Android 11 and above → handle insets with customBottomNav
            ViewCompat.setOnApplyWindowInsetsListener(binding.statusBarBackground) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updateLayoutParams { height = systemBars.top }
                WindowInsetsCompat.CONSUMED
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.customBottomNav) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(bottom = systemBars.bottom)
                insets
            }
        }






        Constant.isParentChoose = true
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        childDetails = SharedPreference.getChildDetails(this)
        userDetails = SharedPreference.getUserDetails(this)
        access_token = childDetails!!.access_token
        val headerBinding = NavHeaderBinding.bind(binding.navigationView.getHeaderView(0))
        headerBinding.username.text = childDetails!!.name

        Glide.with(headerBinding.imgProfile.context)
            .load(childDetails!!.profile)
            .placeholder(R.drawable.default_profile)
            .error(R.drawable.default_profile)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(headerBinding.imgProfile)

        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView

        val menu = navigationView.menu
        val menuItem = menu.findItem(R.id.role_click)
        if (userDetails!!.is_parent && userDetails!!.is_staff) {
            menuItem.isVisible = true  // show
        } else if (userDetails!!.is_parent) {
            if (userDetails!!.child_details.size > 1) {
                menuItem.isVisible = true  // show
            } else {
                menuItem.isVisible = false  // hide
            }
        } else if (userDetails!!.is_staff) {
            if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                if (userDetails!!.staff_details.size > 1) {
                    menuItem.isVisible = true  // show
                } else {
                    menuItem.isVisible = false  // hide
                }
            } else {
                menuItem.isVisible = false
            }
        }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard_view -> {
                    loadFragment(this, ParentHomeFragment())
                    updateNavBar(R.id.icon_home)
                }

                R.id.view_profile -> {
                    loadFragment(this, ParentProfileRewampFragment())
                    updateNavBar(R.id.icon_profile)
                }

                R.id.setting_click -> {
                    loadFragment(this, SettingsFragment())
                    updateNavBar(R.id.icon_settings)
                }

                R.id.help_click -> {
                    loadFragment(this, HolidaysFragment())
                    updateNavBar(R.id.icon_help)
                }

                R.id.role_click -> {
                    val intent = Intent(this, PrioritySelection::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

                R.id.log_out -> {
                    isShowLogoutPopup()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // ✅ Use correct lifecycle-aware callback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container)
                when (currentFragment) {
                    is ParentHomeFragment -> {
                        // Exit app when on HomeFragment
                        finish()
                    }

                    else -> {
                        // Navigate to HomeFragment
                        updateNavBar(R.id.icon_home)
                        supportFragmentManager?.beginTransaction()
                            ?.replace(R.id.fragment_container, ParentHomeFragment())?.commit()
                    }
                }
            }
        })

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
                }
            }


    }


    override fun onClick(v: View?) {

    }

    private fun isShowLogoutPopup() {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.logout_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )

        dimBehind(popupWindow)
        val btnCancel: TextView = popupView.findViewById(R.id.btnCancel)
        val rlaLogout: RelativeLayout = popupView.findViewById(R.id.rlaLogout)
        btnCancel.setOnClickListener {
            clearDim()
            popupWindow.dismiss()
        }

        rlaLogout.setOnClickListener {
            clearDim()
            popupWindow.dismiss()
            isLogout(
                activity = this,
                viewModel = authViewModel,
                secure_id = Constant.getAndroidSecureId(this),
                device_type = Constant.isDeviceType,
                mobile_number = SharedPreference.getMobileNumber(this).toString()
            ) { isSuccess, message ->

                if (isSuccess) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    //            SharedPreference.putMobileNumberPassWord(requireActivity(), "", "")
                    SharedPreference.putLogout(this, true)
                    SharedPreference.setLoggedIn(this, false)
                    //            SharedPreference.setFingerprintEnabled(requireActivity(), false)
                    startActivity(Intent(this, Login::class.java))
                } else {
//                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    Constant.showErrorAlert(this, getString(R.string.Oops), message)
                }
            }


////            SharedPreference.putMobileNumberPassWord(requireActivity(), "", "")
//            SharedPreference.putLogout(this, true)
//            SharedPreference.setLoggedIn(this, false)
////            SharedPreference.setFingerprintEnabled(requireActivity(), false)
//            startActivity(Intent(this, Login::class.java))
        }

        val rootView = this.window.decorView.rootView
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)

        popupWindow.setOnDismissListener {
            clearDim()
        }
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

    fun openDrawer() {
        if (::drawerLayout.isInitialized) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onResume() {
        super.onResume()
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            // Android 10 and below → use legacy fullscreen flags
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            // Android 11 and above → handle insets with customBottomNav
            ViewCompat.setOnApplyWindowInsetsListener(binding.statusBarBackground) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updateLayoutParams { height = systemBars.top }
                WindowInsetsCompat.CONSUMED
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.customBottomNav) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(bottom = systemBars.bottom)
                insets
            }
        }
    }

}