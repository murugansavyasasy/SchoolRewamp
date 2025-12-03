package com.vs.schoolmessenger.Auth.Base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.google.gson.JsonObject
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.Dashboard.Fragments.HolidaysFragment
import com.vs.schoolmessenger.Dashboard.Fragments.ParentHomeFragment
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.ParentProfileRewampFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SchoolHomeFragment
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.SchoolProfileRewampFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SettingsFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.LocalHelperForLanguage
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
    protected abstract fun getViewBinding(): VB



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
        setupViews()

    }




    override fun attachBaseContext(newBase: Context) {
        var isAppLanguage = SharedPreference.getLanguage(newBase)?: "en"
        val context = LocalHelperForLanguage.wrapContext(newBase, isAppLanguage.toString())
        super.attachBaseContext(context)
    }

    fun changeLanguage(lang: String) {
        SharedPreference.putLanguage(this, lang)
        recreate()
    }

    fun isLogout(
        activity: AppCompatActivity,
        viewModel: Auth?,
        secure_id: String,
        device_type: String,
        mobile_number: String,
        onResult: (Boolean, String) -> Unit
    ) {

        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.Req_mobile_number, mobile_number)
            addProperty(APIKeyNames.Req_device_type, device_type)
            addProperty(APIKeyNames.Req_secure_id, secure_id)
        }

        // Call API
        viewModel!!.isLogout(jsonObject, activity)
        Constant.showLoading(activity)

        // Observe API response
        viewModel!!.isLogout?.observe(activity) { response ->
            Constant.hideLoading(activity)

            if (response != null ) {
                if (response.status) {
                    onResult(true, response.message ?: "Success")
                }
                else {
                    onResult(false, response?.message ?: "Something went wrong")
                }
            }
        }
    }

    open fun setupViews() {
        // Optionally overridden in child activities to perform actions on views
    }

    // Example: Setup common toolbar
    protected open fun setupToolbar() {
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.setBackgroundDrawableResource(R.drawable.gradient_theme_school)
    }

    protected open fun setupToolbarBlueWhite() {
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(R.color.PrimaryColor)
        window.navigationBarColor = this.resources.getColor(R.color.white)
        window.setBackgroundDrawableResource(R.drawable.gradient_theme_school)
    }

    fun saveDrawableToCache(drawableResId: Int): String? {
        val drawable = ContextCompat.getDrawable(this, drawableResId) ?: return null
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 100
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }


    fun isToolBarWhiteTheme() {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.white)
            window.navigationBarColor = this.resources.getColor(R.color.white)
        }
    }

    fun isToolBarPrimaryTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.PrimaryColor)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)

        }
    }

    open fun isToolBarNoticeCallTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.grey_color)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)

        }
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    fun isToolBarPrimaryTheme1(mainViewId: Int, statusBarBgView: View) {
        // Enables edge-to-edge rendering
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout = findViewById<View?>(R.id.toolbarLayout)
        val headerView = findViewById<View?>(R.id.rytHeader)

        // Apply window insets to the main view (safe call)
        mainView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(
                    left = systemBars.left,
                    right = systemBars.right,
                    bottom = systemBars.bottom
                )
                // Adjust status bar background height
                statusBarBgView.updateLayoutParams {
                    height = systemBars.top
                }
                insets
            }
        }

        // Apply window insets to toolbarLayout if it exists
        toolbarLayout?.let { toolbar ->
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
                insets // no custom handling, just consume
            }
        }

        // Apply window insets to headerView if it exists
        headerView?.let { header ->
            ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(top = systemBars.top)
                WindowInsetsCompat.CONSUMED
            }
        }

        // Customize window colors and theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.PrimaryColor, theme)
            window.navigationBarColor = resources.getColor(R.color.bpWhite, theme)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }

//    when navigation bar is in gesture this code below is not working only for button it is working
//    @SuppressLint("UseCompatLoadingForColorStateLists")
//    fun isToolBarPrimaryIntroduction(mainViewId: Int, statusBarBgView: View) {
//        // Enables edge-to-edge rendering
//        enableEdgeToEdge()
//
//        val mainView = findViewById<View>(mainViewId)
//        val toolbarLayout = findViewById<View?>(R.id.toolbarLayout)
//        val headerView = findViewById<View?>(R.id.rytHeader)
//
//        // Apply window insets to the main view (safe call)
//        mainView?.let { view ->
//            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
//                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//                v.updatePadding(
//                    left = systemBars.left,
//                    right = systemBars.right,
//                    bottom = systemBars.bottom
//                )
//                // Adjust status bar background height
//                statusBarBgView.updateLayoutParams {
//                    height = systemBars.top
//                }
//                insets
//            }
//        }
//
//        // Apply window insets to toolbarLayout if it exists
//        toolbarLayout?.let { toolbar ->
//            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
//                insets // no custom handling, just consume
//            }
//        }
//
//        // Apply window insets to headerView if it exists
//        headerView?.let { header ->
//            ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
//                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//                v.updatePadding(top = systemBars.top)
//                WindowInsetsCompat.CONSUMED
//            }
//        }
//
//        // Customize window colors and theme
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            val window = this.window
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            window.statusBarColor = resources.getColor(R.color.bpWhite, theme)
//            window.navigationBarColor = resources.getColor(R.color.PrimaryColor, theme)
//            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
//        }
//    }



    @SuppressLint("UseCompatLoadingForColorStateLists")
    fun isToolBarPrimaryIntroduction(mainViewId: Int, statusBarBgView: View) {
        // Enable edge-to-edge (safe on all versions, no-op before API 29)
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout = findViewById<View?>(R.id.toolbarLayout)
        val headerView = findViewById<View?>(R.id.rytHeader)

        // Find the navigation bar background view and frame container (MUST exist in your layout)
        val navigationBarBgView = findViewById<View>(R.id.navigationBarBackground)
        val frameContainer = findViewById<View>(R.id.frameContainer)

        // Apply insets to root (only left/right, NO bottom padding here anymore)
        mainView?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                // Only apply side padding to root
                root.updatePadding(
                    left = systemBars.left,
                    right = systemBars.right
                    // Do NOT apply bottom padding here → that was causing white flash!
                )

                // Status bar background height
                statusBarBgView.updateLayoutParams {
                    height = systemBars.top
                }

                // Navigation bar background height → fixes white in gesture mode
                navigationBarBgView.updateLayoutParams {
                    height = systemBars.bottom
                }

                // Push actual content (RecyclerView + buttons) away from navigation bar
                frameContainer.updatePadding(
                    left = systemBars.left,
                    right = systemBars.right,
                    bottom = systemBars.bottom
                )

                insets
            }
        }

        // Toolbar & header handling (unchanged)
        toolbarLayout?.let { toolbar ->
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { _, insets -> insets }
        }

        headerView?.let { header ->
            ViewCompat.setOnApplyWindowInsetsListener(header) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                header.updatePadding(top = systemBars.top)
                WindowInsetsCompat.CONSUMED
            }
        }

        // Window colors – works perfectly from Android 5.0 up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            window.statusBarColor = resources.getColor(R.color.bpWhite, theme)
            window.navigationBarColor = resources.getColor(R.color.PrimaryColor, theme)

            // Optional: nice gradient background
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }

        // Optional but recommended: better scrim on Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }


    @SuppressLint("UseCompatLoadingForColorStateLists")
    fun isToolBarPrimaryThemePassword(mainViewId: Int, statusBarBgView: View) {
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout = findViewById<View?>(R.id.toolbarLayout)
        val headerView = findViewById<View?>(R.id.rytHeader)

        mainView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(
                    left = systemBars.left,
                    right = systemBars.right,
                    bottom = systemBars.bottom
                )
                statusBarBgView.updateLayoutParams {
                    height = systemBars.top
                }
                insets
            }
        }

        toolbarLayout?.let { toolbar ->
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
                insets
            }
        }

        headerView?.let { header ->
            ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->

                WindowInsetsCompat.CONSUMED
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.PrimaryColor, theme)
            window.navigationBarColor = resources.getColor(R.color.bpWhite, theme)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }


    fun isToolBarPrimarySchool(mainViewId: Int, statusBarBgView: View) {
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout = findViewById<View>(R.id.toolbarLayout)
        val headerView = findViewById<View>(R.id.rytHeader)


        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )

            statusBarBgView.updateLayoutParams {
                height = systemBars.top
            }
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { v, insets ->
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(headerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)

            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.PrimaryColor)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }


    fun isPTMToolBarPrimarySchool(mainViewId: Int, statusBarBgView: View) {
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout = findViewById<View>(R.id.toolbarLayout)
        val headerView = findViewById<View>(R.id.cltDateSelection)


        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )

            statusBarBgView.updateLayoutParams {
                height = systemBars.top
            }
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { v, insets ->
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(headerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)

            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.PrimaryColor)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }



    fun isPrioritySelection(mainViewId: Int, statusBarBgView: View) {
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout = findViewById<View>(R.id.toolbarLayout)
        val headerView = findViewById<View>(R.id.lytLogout)


        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )

            statusBarBgView.updateLayoutParams {
                height = systemBars.top
            }
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { v, insets ->
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(headerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)

            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.bpWhite)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }


    fun isToolBarPrimaryParent(mainViewId: Int, statusBarBgView: View) {
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout = findViewById<View>(R.id.toolbarLayout)
        val headerView = findViewById<View>(R.id.rytHeader)


        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )

            statusBarBgView.updateLayoutParams {
                height = systemBars.top
            }
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { v, insets ->
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(headerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)

            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.PrimaryColor)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }
    fun isToolBarTimeTable(mainViewId: Int, statusBarBgView: View) {
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout = findViewById<View>(R.id.toolbarLayout)
        val headerView = findViewById<View>(R.id.rytHeader)


        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )

            statusBarBgView.updateLayoutParams {
                height = systemBars.top
            }
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { v, insets ->
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(headerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)

            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.PrimaryColor)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }


    fun isToolBarCoupon(mainViewId: Int, statusBarBgView: View) {
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val headerView = findViewById<View>(R.id.rytHeader)


        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )

            statusBarBgView.updateLayoutParams {
                height = systemBars.top
            }
            insets
        }


        ViewCompat.setOnApplyWindowInsetsListener(headerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)

            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.PrimaryColor)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }







    fun isToolBarPrimaryParentInteractionwithStaff(mainViewId: Int, statusBarBgView: View) {
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout = findViewById<View>(R.id.toolbarLayout)
        val headerView = findViewById<View>(R.id.rytHeader)

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val combinedType = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            val combinedInsets = insets.getInsets(combinedType)

            v.updatePadding(
                left = combinedInsets.left,
                right = combinedInsets.right,
                bottom = combinedInsets.bottom
            )

            statusBarBgView.updateLayoutParams {
                height = combinedInsets.top
            }

            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { _, insets ->
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(headerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.PrimaryColor)
            window.navigationBarColor = resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }





    // Method to allow child activities to access specific views
    protected fun <T : ViewBinding> accessChildView(
        binding: T,
        nav_home: Int,
        nav_help: Int,
        nav_profile: Int,
        nav_settings: Int,
        icon_home: Int,
        icon_help: Int,
        icon_settings: Int,
        icon_profile: Int,
        lbl_home: Int,
        lbl_help: Int,
        lbl_settings: Int,
        lbl_profile: Int,
        frm: Int,
        isBottomMenu: Int
    ) {
        // Use reflection or a specific method to access views
        val nav_home = binding.root.findViewById<LinearLayout>(nav_home)
        val nav_help = binding.root.findViewById<LinearLayout>(nav_help)
        val nav_settings = binding.root.findViewById<LinearLayout>(nav_settings)
        val nav_profile = binding.root.findViewById<LinearLayout>(nav_profile)
        binding.root.findViewById<FrameLayout>(frm)
        val isBottomMenu = binding.root.findViewById<LinearLayout>(isBottomMenu)

        isBottomMenu.setBackgroundResource(R.drawable.white_bg_card)

        if (Constant.isParentChoose) {
            loadFragment(this, ParentHomeFragment())
        } else {
            loadFragment(this, SchoolHomeFragment())
        }
        updateNavBar(icon_home)

        nav_home.setOnClickListener {
            if (Constant.isParentChoose) {
                loadFragment(this, ParentHomeFragment())
            } else {
                loadFragment(this, SchoolHomeFragment())
            }
            updateNavBar(icon_home)
        }
        nav_help.setOnClickListener {
            loadFragment(this, HolidaysFragment())
            updateNavBar(icon_help)
        }
        nav_settings.setOnClickListener {
            loadFragment(this, SettingsFragment())
            updateNavBar(icon_settings)
        }
        nav_profile.setOnClickListener {


            if (Constant.isParentChoose) {
                loadFragment(this, ParentProfileRewampFragment())
            } else {
                loadFragment(this, SchoolProfileRewampFragment())
            }
            updateNavBar(icon_profile)


        }

    }

    fun showAcademicDropdown(
        anchor: View,
        activity: Activity,
        academicYearList: List<AcademicYear>?,
        onAcademicYearSelected: (AcademicYear) -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.e("DropdownMenu", "Activity is not valid for showing the popup.")
            return
        }

        if (academicYearList.isNullOrEmpty()) {
            Log.e("DropdownMenu", "Academic year list is empty or null.")
            return
        }

        val inflater = LayoutInflater.from(anchor.context)
        val dropdownView = inflater.inflate(R.layout.dropdown_menu, null)
        val popupWindow = PopupWindow(
            dropdownView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        dimBehind(popupWindow)

        val yearNames = academicYearList.map { it.year }
        val listView: ListView = dropdownView.findViewById(R.id.dropdownListView)
        val adapter = ArrayAdapter(anchor.context, android.R.layout.simple_list_item_1, yearNames)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedYear = academicYearList[position]
            onAcademicYearSelected(selectedYear)

            Log.d(
                "DropdownMenu",
                "Selected Academic Year:\nID = ${selectedYear.id},\nYear = ${selectedYear.year},\nCurrent = ${selectedYear.current_academic_year}"
            )
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor)

        popupWindow.setOnDismissListener {
            clearDim()
        }
    }

    fun dimBehind(popupWindow: PopupWindow) {
        val window = this.window
        val layoutParams = window.attributes
        layoutParams.alpha = 0.4f // Lower alpha to dim the background
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams
    }

    fun clearDim() {
        val window = this.window
        val layoutParams = window.attributes
        layoutParams.alpha = 1.0f
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams
    }

    fun updateNavBar(selectedItemId: Int) {
        // Reset all icons
        findViewById<ImageView>(R.id.icon_home).setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.grey
            ), PorterDuff.Mode.SRC_IN
        )
        findViewById<TextView>(R.id.lblHome).setTextColor(
            ContextCompat.getColor(
                this,
                R.color.grey
            )
        )

        findViewById<ImageView>(R.id.icon_help).setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.grey
            ), PorterDuff.Mode.SRC_IN
        )
        findViewById<TextView>(R.id.lblHelp).setTextColor(
            ContextCompat.getColor(
                this,
                R.color.grey
            )
        )
        findViewById<ImageView>(R.id.icon_profile).setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.grey
            ), PorterDuff.Mode.SRC_IN
        )
        findViewById<TextView>(R.id.lblProfile).setTextColor(
            ContextCompat.getColor(
                this,
                R.color.grey
            )
        )
        findViewById<ImageView>(R.id.icon_settings).setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.grey
            ), PorterDuff.Mode.SRC_IN
        )
        findViewById<TextView>(R.id.lblSettings).setTextColor(
            ContextCompat.getColor(
                this,
                R.color.grey
            )
        )

        findViewById<ImageView>(R.id.icon_settings).background = null
        findViewById<ImageView>(R.id.icon_profile).background = null
        findViewById<ImageView>(R.id.icon_help).background = null
        findViewById<ImageView>(R.id.icon_home).background = null

        // Set color for selected icon

        when (selectedItemId) {
            R.id.icon_home -> {
                zoomOutToZoomIn(binding.root.findViewById(R.id.icon_home))
                findViewById<ImageView>(R.id.icon_home).setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.PrimaryColor
                    ), PorterDuff.Mode.SRC_IN
                )
                findViewById<TextView>(R.id.lblHome).setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.PrimaryColor
                    )
                )

            }

            R.id.icon_help -> {
                zoomOutToZoomIn(binding.root.findViewById(R.id.icon_help))
                findViewById<ImageView>(R.id.icon_help).setColorFilter(
                    ContextCompat.getColor(this, R.color.PrimaryColor),
                    PorterDuff.Mode.SRC_IN
                )

                findViewById<TextView>(R.id.lblHelp).setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.PrimaryColor
                    )
                )
            }

            R.id.icon_profile -> {
                zoomOutToZoomIn(binding.root.findViewById(R.id.icon_profile))
                findViewById<ImageView>(R.id.icon_profile).setColorFilter(
                    ContextCompat.getColor(this, R.color.PrimaryColor),
                    PorterDuff.Mode.SRC_IN
                )

                findViewById<TextView>(R.id.lblProfile).setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.PrimaryColor
                    )
                )
            }

            R.id.icon_settings -> {
                zoomOutToZoomIn(binding.root.findViewById(R.id.icon_settings))
                findViewById<ImageView>(R.id.icon_settings).setColorFilter(
                    ContextCompat.getColor(this, R.color.PrimaryColor),
                    PorterDuff.Mode.SRC_IN
                )

                findViewById<TextView>(R.id.lblSettings).setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.PrimaryColor
                    )
                )
            }
        }
    }

    private fun zoomOutToZoomIn(imageView: ImageView, duration: Long = 500) {
        // Create ObjectAnimators for scaling down
        val scaleDownX = ObjectAnimator.ofFloat(imageView, Constant.scaleX, 0.7f) // Zoom out to 70%
        val scaleDownY = ObjectAnimator.ofFloat(imageView, Constant.scaleY, 0.7f) // Zoom out to 70%

        // Create ObjectAnimators for scaling up
        val scaleUpX = ObjectAnimator.ofFloat(imageView, Constant.scaleX, 1f) // Zoom back to 100%
        val scaleUpY = ObjectAnimator.ofFloat(imageView, Constant.scaleY, 1f) // Zoom back to 100%

        // Set durations for the animations
        scaleDownX.duration = duration / 2
        scaleDownY.duration = duration / 2
        scaleUpX.duration = duration / 2
        scaleUpY.duration = duration / 2

        // Create an AnimatorSet to play animations together
        AnimatorSet().apply {
            play(scaleDownX).with(scaleDownY) // Play scale down animations together
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    scaleUpX.start() // Start scaling up after scaling down
                    scaleUpY.start() // Start scaling up after scaling down
                }
            })
            start() // Start the animation
        }
    }

    companion object {
        @JvmStatic
        fun loadFragment(activity: FragmentActivity, fragment: Fragment) {
            val currentFragment =
                activity?.supportFragmentManager?.findFragmentById(R.id.fragment_container)
            if (currentFragment != null && currentFragment::class == fragment::class) {
                return
            }
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragment_container, fragment)?.commit()

        }
    }


    fun showTimePickerDialog(context: Context, listener: TimeSelectedListener) {
        // Get current time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create and show TimePickerDialog
        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour < 12) Constant.AM else Constant.PM
                val hourIn12Format =
                    if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour
                listener.onTimeSelected(hourIn12Format, selectedMinute, amPm)
            },
            hour,
            minute,
            false // Use 12-hour format
        )
        timePickerDialog.show()
    }







    fun showDatePickerDialog(
        context: Context,
        listener: OnDateSelectedListener
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val cal = Calendar.getInstance()
                cal.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = sdf.format(cal.time)
                listener.onDateSelected(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }



    fun dailycollectionshowDatePickerDialog(
        context: Context,
        listener: OnDateSelectedListener,
        isFromDate: Boolean,
        fromDateMillis: Long,
        preSelectedDate: String? = null
    ) {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        if (!preSelectedDate.isNullOrEmpty()) {
            try {
                val parsed = sdf.parse(preSelectedDate)
                if (parsed != null) calendar.time = parsed
            } catch (e: Exception) {}
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            context,
            { _, y, m, d ->
                val cal = Calendar.getInstance()
                cal.set(y, m, d)
                listener.onDateSelected(sdf.format(cal.time))
            },
            year, month, day
        )

        // 🚫 Block FUTURE DATES
        dialog.datePicker.maxDate = System.currentTimeMillis()

        // 🚫 Restrict To-Date minimum date
        if (!isFromDate) {
            dialog.datePicker.minDate = fromDateMillis
        }

        dialog.show()
    }






    fun CustomshowDatePickerDialog(
        context: Context,
        listener: OnDateSelectedListener
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val cal = Calendar.getInstance()
                cal.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = sdf.format(cal.time)
                listener.onDateSelected(formattedDate)
            },
            year, month, day
        )

        // Restrict past dates
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }


    fun AssignmentCustomshowDatePickerDialog(
        context: Context,
        listener: OnDateSelectedListener,
        preSelectedDate: String? = null
    ) {
        val calendar = Calendar.getInstance()
        if (!preSelectedDate.isNullOrEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val date = sdf.parse(preSelectedDate)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val cal = Calendar.getInstance()
                cal.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = sdf.format(cal.time)
                listener.onDateSelected(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

        datePickerDialog.show()
    }


    fun isToolBarPrimaryInteractionwithStaff(mainViewId: Int, statusBarBgView: View) {
        enableEdgeToEdge()

        val mainView = findViewById<View>(mainViewId)
        val toolbarLayout =
            findViewById<View>(R.id.toolbarlayout)  // Fixed: lowercase 'l' to match XML
        val headerView = findViewById<View>(R.id.rytHeader)

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val combinedType = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            val combinedInsets = insets.getInsets(combinedType)

            v.updatePadding(
                left = combinedInsets.left,
                right = combinedInsets.right,
                bottom = combinedInsets.bottom
            )

            // Size status bar background to cover the status bar area
            statusBarBgView.updateLayoutParams {
                height = combinedInsets.top
            }

            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { _, insets ->
            insets  // Passthrough: No specific handling needed for toolbar
        }

        ViewCompat.setOnApplyWindowInsetsListener(headerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Fixed: No top padding needed—rytHeader is already positioned below statusBarBackground.
            // Adding it caused double-offset (status bar height x2). Handle other insets if needed in future.
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right
            )
            // Optionally add bottom padding if RecyclerView needs it, but mainView already handles global bottom.
            WindowInsetsCompat.CONSUMED  // Consume to prevent propagation to children
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.PrimaryColor)
            window.navigationBarColor = resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }
    }




    fun lsrwshowDatePickerDialog(
        context: Context,
        listener: OnDateSelectedListener,
        preselectedDate: String? = null
    ) {
        val calendar = Calendar.getInstance()

        if (!preselectedDate.isNullOrEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val date = sdf.parse(preselectedDate)
                calendar.time = date!!
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val cal = Calendar.getInstance()
                cal.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = sdf.format(cal.time)
                listener.onDateSelected(formattedDate)
            },
            year, month, day
        )


        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

        datePickerDialog.show()
    }




    //Homework report sender
    fun showDatePickerDialogSelectedDate(
        context: Context,
        isSelectedDate: String?, // "dd-MM-yyyy" or null
        listener: OnDateSelectedListener
    ) {
        val calendar = Calendar.getInstance()

        // Try to parse last selected date if available
        if (!isSelectedDate.isNullOrEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val selectedDate = sdf.parse(isSelectedDate)
                calendar.time = selectedDate!!
            } catch (e: Exception) {
                e.printStackTrace() // fallback to current date
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedCalendar.time)
                listener.onDateSelected(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis


        datePickerDialog.show()
    }

    fun showSuccessPopup(message: String, status: Boolean){

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        messageText.text = message

        val rootView = this.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(this).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true // prevent clicks on background
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, this.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            if(status) {
                closePopup()
                finish()
            }else{
                closePopup()
            }
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true


    }
}