package com.vs.schoolmessenger.Dashboard.School

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.view.ViewGroupCompat
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.Fragments.HolidaysFragment
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.SchoolProfileRewampFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SchoolHomeFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SettingsFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.NavHeaderBinding
import com.vs.schoolmessenger.databinding.SchoolDashboardBinding

class SchoolDashboard : BaseActivity<SchoolDashboardBinding>(), View.OnClickListener {


    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = ChangeLanguage.getPersistedLanguage(newBase)
        val context = ChangeLanguage.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }

    private lateinit var contactPermissionLauncher: ActivityResultLauncher<String>
    var authViewModel: Auth? = null
    private var appViewModel: App? = null
    var userDetails: UserDetails? = null
    var access_token = ""
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView


    override fun getViewBinding(): SchoolDashboardBinding {
        return SchoolDashboardBinding.inflate(layoutInflater)


    }

    override fun setupViews() {
        super.setupViews()
        userDetails = SharedPreference.getUserDetails(this)
        access_token = userDetails!!.staff_details[0].access_token
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { rootView, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.statusBarBackground.updateLayoutParams { height = systemBars.top }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                binding.customBottomNav.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomMargin = systemBars.bottom
                }
            } else {
                binding.customBottomNav.updatePadding(bottom = systemBars.bottom)
            }

            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.statusBarBackground) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams { height = systemBars.top }
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.customBottomNav) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = systemBars.bottom
            }
            WindowInsetsCompat.CONSUMED
        }



        Constant.isParentChoose = false

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()

        val headerBinding = NavHeaderBinding.bind(binding.navigationView.getHeaderView(0))
        headerBinding.username.text = userDetails!!.staff_details[0].name

        Glide.with(headerBinding.imgProfile.context)
            .load(userDetails!!.staff_details[0].staff_profile)
            .placeholder(R.drawable.default_profile)
            .error(R.drawable.default_profile)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(headerBinding.imgProfile)

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView


        val menu = navigationView.menu
        val menuItem = menu.findItem(R.id.role_click)
        if(userDetails!!.is_parent && userDetails!!.is_staff){
            menuItem.isVisible = true  // show
        }
        else if(userDetails!!.is_parent){
            if(userDetails!!.child_details.size > 1){
                menuItem.isVisible = true  // show
            }
            else{
                menuItem.isVisible = false  // hide
            }
        }
        else if(userDetails!!.is_staff){
            if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                if(userDetails!!.staff_details.size > 1){
                    menuItem.isVisible = true  // show
                }
                else{
                    menuItem.isVisible = false  // hide
                }
            }
            else{
                menuItem.isVisible = false
            }
        }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard_view -> {
                    loadFragment(this, SchoolHomeFragment())
                    updateNavBar(R.id.icon_home)
                }

                R.id.view_profile -> {
                    loadFragment(this, SchoolProfileRewampFragment())
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
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                when (currentFragment) {
                    is SchoolHomeFragment -> {
                        // Exit app when on HomeFragment
                        finish()
                    }
                    else -> {
                        // Navigate to HomeFragment
                        updateNavBar(R.id.icon_home)
                        supportFragmentManager?.beginTransaction()?.replace(R.id.fragment_container, SchoolHomeFragment())?.commit()
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

        authViewModel!!.isDeviceToken?.observe(this) { response ->
            if (response != null) {
                response.status
                response.message
            }
        }

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            response?.data?.let { academicList ->
                val data = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYearList == data) return@observe
                isAcademicYearList = data
            }
        }

        contactPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                Log.d("Permission", "Contact granted: $isGranted")

            }
        requestContactPermission()

        isGetAcademicYear()
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
            SharedPreference.putLogout(this, true)
            SharedPreference.setLoggedIn(this, false)
            startActivity(Intent(this, Login::class.java))
        }

        val rootView = this.window.decorView.rootView
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)

        popupWindow.setOnDismissListener {
            clearDim()
        }
    }

    fun openDrawer() {
        if (::drawerLayout.isInitialized) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun isGetAcademicYear() {
        appViewModel!!.isGetAcademicYear(access_token, this)

    }


    private fun requestContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Show rationale if user previously denied
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                Constant.showNotificationPermissionDialog(
                    packageName,
                    this,
                    "Contact Permission Required",
                    "This app needs access to your contacts to function properly."
                )

            } else {
                // No rationale needed, ask directly
                contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        } else {

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

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}