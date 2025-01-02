package com.vs.schoolmessenger.Auth.Base
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.vs.schoolmessenger.Dashboard.Fragments.HelpFragment
import com.vs.schoolmessenger.Dashboard.Fragments.HomeFragment
import com.vs.schoolmessenger.Dashboard.Fragments.ParentHomeFragment
import com.vs.schoolmessenger.Dashboard.Fragments.ProfileFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SettingsFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.TimePickerAdapter
import com.vs.schoolmessenger.Utils.TimeSelectedListener
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

        // Common setup for all activities
        // setupToolbar()
        setupViews()
      //  applyCustomFontToViews()
    }

    open fun setupViews() {
        // Optionally overridden in child activities to perform actions on views
    }

//    private fun applyCustomFontToViews() {
//        val customFont: Typeface? = ResourcesCompat.getFont(this, R.font.poppins_regular)
//        customFont?.let { font ->
//            // Apply the font to all TextViews in the root view
//            val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
//            setFontRecursively(rootView, font)
//        }
//    }
//
//    private fun setFontRecursively(viewGroup: ViewGroup, font: Typeface) {
//        for (i in 0 until viewGroup.childCount) {
//            val child = viewGroup.getChildAt(i)
//            when (child) {
//                is TextView -> child.typeface = font
//                is ViewGroup -> setFontRecursively(child, font)
//            }
//        }
//    }

    fun isToolBarWhiteTheme() {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.white)
            window.navigationBarColor = this.resources.getColor(R.color.white)
        }
    }

    fun isToolBarBlackTheme() {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.black)
            window.navigationBarColor = this.resources.getColor(R.color.white)
        }
    }

    // Example: Setup common toolbar
    protected open fun setupToolbar() {

        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.primary_light)
            window.navigationBarColor = this.resources.getColor(R.color.primary_light)
        }
    }


    // Method to allow child activities to access specific views
    protected fun <T : ViewBinding> accessChildView(
        binding: T, nav_home: Int, nav_help: Int, nav_profile: Int, nav_settings: Int,
        icon_home: Int, icon_help: Int, icon_settings: Int, icon_profile: Int, frm: Int
    ) {
        // Use reflection or a specific method to access views

        val nav_home = binding.root.findViewById<LinearLayout>(nav_home)
        val nav_help = binding.root.findViewById<LinearLayout>(nav_help)
        val nav_settings = binding.root.findViewById<LinearLayout>(nav_settings)
        val nav_profile = binding.root.findViewById<LinearLayout>(nav_profile)

        Constant.isParentChoose = true
        if (Constant.isParentChoose) {
            loadFragment(ParentHomeFragment())
        } else {
            loadFragment(HomeFragment())
        }
        updateNavBar(icon_home)

        nav_home.setOnClickListener {
            if (Constant.isParentChoose) {
                loadFragment(ParentHomeFragment())
            } else {
                loadFragment(HomeFragment())
            }
            updateNavBar(icon_home)
        }
        nav_help.setOnClickListener {
            loadFragment(HelpFragment())
            updateNavBar(icon_help)
        }
        nav_settings.setOnClickListener {
            loadFragment(SettingsFragment())
            updateNavBar(icon_settings)
        }
        nav_profile.setOnClickListener {
            loadFragment(ProfileFragment())
            updateNavBar(icon_profile)
        }

        // Perform actions on the view
    }

    fun showDropdownMenuSort(
        anchor: View,
        activity: Activity,
        items: List<String>,
        onItemSelected: (String) -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.e("DropdownMenu", "Activity is not valid for showing the popup.")
            return
        }

        val inflater = LayoutInflater.from(anchor.context)
        val dropdownView = inflater.inflate(R.layout.dropdown_menu, null)

        // Create a PopupWindow
        val popupWindow = PopupWindow(
            dropdownView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        dimBehind(popupWindow)
        // Set up the ListView in the dropdown
        val listView: ListView = dropdownView.findViewById(R.id.dropdownListView)
        val adapter =
            ArrayAdapter(anchor.context, R.layout.dropdown_spinner, items)
        listView.adapter = adapter

        // Handle item clicks
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = items[position]
            onItemSelected(selectedItem)
            clearDim()
            popupWindow.dismiss() // Close the dropdown
        }

        popupWindow.setOnDismissListener {
            clearDim()
        }

        try {
            popupWindow.showAsDropDown(anchor)
        } catch (e: Exception) {
            Log.e("DropdownMenu", "Failed to show dropdown menu", e)
        }
    }

    fun dimBehind(popupWindow: PopupWindow) {
        val window = this.window
        val layoutParams = window.attributes
        layoutParams.alpha = 0.4f // Lower alpha to dim the background
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams
    }

     fun clearDim() {
        val window = this.window
        val layoutParams = window.attributes
        layoutParams.alpha = 1.0f
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams
    }

    private fun updateNavBar(selectedItemId: Int) {
//        // Reset all icons
        findViewById<ImageView>(R.id.icon_home).setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.grey
            ), PorterDuff.Mode.SRC_IN
        )
        findViewById<ImageView>(R.id.icon_help).setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.grey
            ), PorterDuff.Mode.SRC_IN
        )
        findViewById<ImageView>(R.id.icon_profile).setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.grey
            ), PorterDuff.Mode.SRC_IN
        )
        findViewById<ImageView>(R.id.icon_settings).setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.grey
            ), PorterDuff.Mode.SRC_IN
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
                        R.color.colorPrimary
                    ), PorterDuff.Mode.SRC_IN
                )

                animateBackgroundColor(
                    binding.root.findViewById(R.id.icon_home),
                    R.color.sky_blue0,
                    R.color.sky_blue2
                )


                setupToolbar()


            }

            R.id.icon_help -> {

                zoomOutToZoomIn(binding.root.findViewById(R.id.icon_help))
                findViewById<ImageView>(R.id.icon_help).setColorFilter(
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
                animateBackgroundColor(
                    binding.root.findViewById(R.id.icon_help),
                    R.color.sky_blue0,
                    R.color.sky_blue2
                )
                setupToolbar()
            }

            R.id.icon_profile -> {
                zoomOutToZoomIn(binding.root.findViewById(R.id.icon_profile))

                findViewById<ImageView>(R.id.icon_profile).setColorFilter(
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
                animateBackgroundColor(
                    binding.root.findViewById(R.id.icon_profile),
                    R.color.sky_blue0,
                    R.color.sky_blue2
                )

                setupToolbar()
            }

            R.id.icon_settings -> {
                zoomOutToZoomIn(binding.root.findViewById(R.id.icon_settings))

                findViewById<ImageView>(R.id.icon_settings).setColorFilter(
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
                animateBackgroundColor(
                    binding.root.findViewById(R.id.icon_settings),
                    R.color.sky_blue0,
                    R.color.sky_blue2
                )

                setupToolbar()
            }
        }
    }

    private fun zoomOutToZoomIn(imageView: ImageView, duration: Long = 500) {
        // Create ObjectAnimators for scaling down
        val scaleDownX = ObjectAnimator.ofFloat(imageView, "scaleX", 0.7f) // Zoom out to 70%
        val scaleDownY = ObjectAnimator.ofFloat(imageView, "scaleY", 0.7f) // Zoom out to 70%

        // Create ObjectAnimators for scaling up
        val scaleUpX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f) // Zoom back to 100%
        val scaleUpY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f) // Zoom back to 100%

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


    private fun animateBackgroundColor(imageView: ImageView, colorStart: Int, colorEnd: Int) {
        // Get the current background drawable (or create a new one if not set)
        val background = (imageView.background as? GradientDrawable) ?: GradientDrawable().apply {
            cornerRadius = 50f // Set the radius to 20dp
        }

        // Get the current color from the drawable (fallback to colorStart if not available)
        val currentColor =
            (background.color?.defaultColor ?: ContextCompat.getColor(this, colorStart))

        // Create a ValueAnimator for smooth color transition
        val colorAnimation =
            ValueAnimator.ofArgb(currentColor, ContextCompat.getColor(this, colorEnd))
        colorAnimation.duration = 400 // Set duration for the transition (500ms)

        // Update the background color with the animated value
        colorAnimation.addUpdateListener { animator ->
            background.setColor(animator.animatedValue as Int)
            imageView.background = background // Apply the updated drawable with radius
        }

        colorAnimation.start() // Start the animation
    }


    private fun loadFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

    }


    // Example: Show a common loading dialog
    protected fun showLoadingDialog() {
        // Code to show loading dialog
    }

    // Example: Hide the loading dialog
    protected fun hideLoadingDialog() {
        // Code to hide loading dialog
    }



        fun showTimePickerDialog(context:Context,listener:TimeSelectedListener) {
            // Get current time
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Create and show TimePickerDialog
            val timePickerDialog = TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    val amPm = if (selectedHour < 12) "AM" else "PM"
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
        // Get the current date by default
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Show DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                listener.onDateSelected(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }
    fun changeDateFormat(inputDate: String): String {
        // Define the current format of the input date
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Define the desired output format
        val outputFormat = SimpleDateFormat("EEE dd MMM, yyyy", Locale.getDefault())

        // Parse the input date and reformat it
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    }




    fun showSpinnerTimePicker(
        context: Context,
        onTimeSelected: (hour: Int, minute: Int, isAm: Boolean) -> Unit
    ) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.custom_time_picker)

        // Ensure the dialog can handle large content
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val hourRecyclerView = dialog.findViewById<RecyclerView>(R.id.hourSpinner)
        val minuteRecyclerView = dialog.findViewById<RecyclerView>(R.id.minuteSpinner)
        val ampmRecyclerView = dialog.findViewById<RecyclerView>(R.id.ampmSpinner)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)

        val hours = (1..12).map { it.toString() }
        val minutes = (0..59).map { it.toString().padStart(2, '0') }
        val ampm = listOf("AM", "PM")

        // Get the current time
        val calendar = Calendar.getInstance()
        var selectedHour = calendar.get(Calendar.HOUR) // 12-hour format
        var selectedMinute = calendar.get(Calendar.MINUTE)
        var isAm = calendar.get(Calendar.AM_PM) == Calendar.AM
        tvTitle.paintFlags = tvTitle.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Adjust the selected hour for 1-12 format
        if (selectedHour == 0) {
            selectedHour = 12 // Convert 0 hour to 12 for 12-hour format
        }

        // Set up adapters with selected positions
        hourRecyclerView.layoutManager = LinearLayoutManager(context)
        hourRecyclerView.adapter = TimePickerAdapter(hours, { hourIndex ->
            selectedHour = hourIndex + 1 // Pass index directly (0-based to 1-12)
        }, selectedHour - 1) // Pass current hour index (1-12) adjusted for 0-based index

        minuteRecyclerView.layoutManager = LinearLayoutManager(context)
        minuteRecyclerView.adapter = TimePickerAdapter(minutes, { minuteIndex ->
            selectedMinute = minuteIndex // Pass selected minute directly
        }, selectedMinute) // Pass current minute index

        ampmRecyclerView.layoutManager = LinearLayoutManager(context)
        ampmRecyclerView.adapter = TimePickerAdapter(ampm, { isAmIndex ->
            isAm = isAmIndex == 0 // 0 for AM, 1 for PM
        }, if (isAm) 0 else 1) // Set AM/PM position based on current time
        ampmRecyclerView.visibility = View.VISIBLE // Optional for 12-hour format

        // Scroll to the current time in the RecyclerViews
        hourRecyclerView.scrollToPosition(selectedHour - 1) // 0-index for RecyclerView
        minuteRecyclerView.scrollToPosition(selectedMinute) // 0-index for RecyclerView

        // Handle buttons
        dialog.findViewById<RelativeLayout>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<RelativeLayout>(R.id.btnConfirm).setOnClickListener {
            onTimeSelected(selectedHour, selectedMinute, isAm)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun validateTimeWithAmPmLegacy(fromTime: String, toTime: String): String {
        val timeFormat = SimpleDateFormat("hh:mm a") // 12-hour format with AM/PM
        val fromDate = timeFormat.parse(fromTime)
        val toDate = timeFormat.parse(toTime)

        return when {
            fromDate == toDate -> {
                "The 'to time' is equal to the 'from time'. Please select a valid time."
            }

            toDate!!.before(fromDate) -> {
                "The 'to time' is before the 'from time'. Please select a valid time."
            }

            else -> {
                "The 'to time' is valid."
            }
        }
    }
}