package com.vs.schoolmessenger.Dashboard.School

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.SharedPreference
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


    override fun getViewBinding(): SchoolDashboardBinding {
        return SchoolDashboardBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID

//        if (Build.VERSION.SDK_INT >= 21) {
//            val window = this.window
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            window.statusBarColor = this.resources.getColor(R.color.primary_light)
//            window.navigationBarColor = this.resources.getColor(R.color.primary_light)
//        }
        userDetails = SharedPreference.getUserDetails(this)
        access_token = userDetails!!.staff_details[0].access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()

        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        setUpGradientSchool()


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