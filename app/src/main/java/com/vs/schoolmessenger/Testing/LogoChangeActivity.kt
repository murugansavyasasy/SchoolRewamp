package com.vs.schoolmessenger.Testing

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.LogoChangeBinding

class LogoChangeActivity : BaseActivity<LogoChangeBinding>(), View.OnClickListener {

    companion object {
        private const val ICON_URL = "https://sasydevtest.s3.ap-south-1.amazonaws.com/IMG_6973.JPG"
    }

    override fun getViewBinding(): LogoChangeBinding {
        return LogoChangeBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        // Load the image into ImageView
        Glide.with(this)
            .load(ICON_URL)
            .into(binding.imageViewLogo)

        // Set up button click listener
        binding.buttonUpdateLogo.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonUpdateLogo -> downloadAndSetShortcutIcon()
        }
    }

    private fun downloadAndSetShortcutIcon() {
        Glide.with(this)
            .asBitmap()
            .load(ICON_URL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createAppShortcut(resource)
                    } else {
                        updateAppIcon()
                    }
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAppShortcut(iconBitmap: Bitmap) {
        val shortcutManager = getSystemService(ShortcutManager::class.java)

        if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported) {
            Toast.makeText(this, "Shortcuts not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val shortcut = ShortcutInfo.Builder(this, "dynamic_shortcut")
            .setShortLabel("Client App")
            .setIcon(Icon.createWithBitmap(iconBitmap))
            .setIntent(Intent(Intent.ACTION_MAIN, null, this, LogoChangeActivity::class.java))
            .build()

        shortcutManager.dynamicShortcuts = listOf(shortcut)
        Toast.makeText(this, "Shortcut updated successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun updateAppIcon() {
        val packageManager = packageManager
        val componentName = ComponentName(this, "com.vs.schoolmessenger.Testing.LogoChangeActivity")

        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        Toast.makeText(this, "App icon updated (Requires restart)", Toast.LENGTH_SHORT).show()
    }
}
