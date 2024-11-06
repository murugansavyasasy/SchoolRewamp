package com.vs.schoolmessenger.Auth.Base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.vs.schoolmessenger.Dashboard.Fragments.HelpFragment
import com.vs.schoolmessenger.Dashboard.Fragments.ProfileFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SchoolHomeFragment
import com.vs.schoolmessenger.Dashboard.Fragments.SettingsFragment
import com.vs.schoolmessenger.R


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


    }

    open fun setupViews() {
        // Optionally overridden in child activities to perform actions on views
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


    // Example: Setup common toolbar
    protected open fun setupToolbar() {

        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.primary_light)
            window.navigationBarColor = this.resources.getColor(R.color.primary_light)
        }
        // Toolbar setup or any common UI setup code
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


        loadFragment(SchoolHomeFragment())
        updateNavBar(icon_home)

        nav_home.setOnClickListener {
            loadFragment(SchoolHomeFragment())
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


                if (Build.VERSION.SDK_INT >= 21) {
                    val window = this.window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = this.resources.getColor(R.color.mild4)
                    window.navigationBarColor = this.resources.getColor(R.color.mild4)
                }

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
                isToolBarWhiteTheme()
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


// Any other common functionality can be added here
}