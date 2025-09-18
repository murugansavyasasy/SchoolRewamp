package com.vs.schoolmessenger.Auth.Base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.Dashboard.Fragments.HelpFragment
import com.vs.schoolmessenger.Dashboard.Fragments.ParentHomeFragment
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.ParentProfileRewampFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SchoolHomeFragment
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.SchoolProfileRewampFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SettingsFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
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


    fun isToolBarPrimaryTheme1(
        mainViewId: Int,
        statusBarBgView: View
    ) {
        enableEdgeToEdge()
        val mainView = findViewById<View>(mainViewId)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)

            statusBarBgView.updateLayoutParams {
                height = systemBars.top
            }
            insets
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
            loadFragment(this, HelpFragment())
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
                activity.supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment != null && currentFragment::class == fragment::class) {
                return
            }
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
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

}