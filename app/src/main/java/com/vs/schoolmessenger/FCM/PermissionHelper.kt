package com.vs.schoolmessenger.FCM

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object PermissionHelper {


    fun openAutoStartSettings(context: Context) {

        try {

            val intent =
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                )

            intent.data =
                Uri.parse(
                    "package:${context.packageName}"
                )

            context.startActivity(intent)

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun openAppSettings(
        context: Context
    ) {

        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            )

        intent.data =
            Uri.parse(
                "package:${context.packageName}"
            )

        context.startActivity(intent)
    }

    fun requestBatteryOptimization(
        context: Context
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            try {

                val intent =
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    )

                intent.data =
                    Uri.parse(
                        "package:${context.packageName}"
                    )

                context.startActivity(intent)

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }
}