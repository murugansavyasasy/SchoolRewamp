package com.vs.schoolmessenger.FCM

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object DevicePermissionHelper {

    fun showPermissionDialog(context: Context) {

        val brand =
            Build.MANUFACTURER.lowercase()

        val model =
            Build.MODEL

        val message = when {

            brand.contains("vivo") -> {

                "Your Device : Vivo $model\n\n" +
                        "Please enable these permissions:\n\n" +
                        "• Show on Lock Screen\n" +
                        "• Popup Window\n" +
                        "• Open New Windows While Running in Background\n" +
                        "• Auto Start\n\n" +
                        "Path:\n" +
                        "App Info → Other Permissions"
            }

            brand.contains("oppo") -> {

                "Your Device : Oppo $model\n\n" +
                        "Please enable these permissions:\n\n" +
                        "• Display over Lock Screen\n" +
                        "• Popup Window\n" +
                        "• Auto Launch\n" +
                        "• Run in Background\n\n" +
                        "Path:\n" +
                        "App Info → Permissions"
            }

            brand.contains("realme") -> {

                "Your Device : Realme $model\n\n" +
                        "Please enable these permissions:\n\n" +
                        "• Display over Lock Screen\n" +
                        "• Popup Window\n" +
                        "• Auto Launch\n" +
                        "• Background Popup\n\n" +
                        "Path:\n" +
                        "App Info → Permissions"
            }

            brand.contains("xiaomi") ||
                    brand.contains("redmi") ||
                    brand.contains("poco") -> {

                "Your Device : Xiaomi $model\n\n" +
                        "Please enable these permissions:\n\n" +
                        "• Show on Lock Screen\n" +
                        "• Floating Window\n" +
                        "• Popup Notification\n" +
                        "• Auto Start\n\n" +
                        "Path:\n" +
                        "App Info → Other Permissions"
            }

            brand.contains("samsung") -> {

                "Your Device : Samsung $model\n\n" +
                        "Please enable:\n\n" +
                        "• Full Screen Notifications\n" +
                        "• Allow on Lock Screen\n" +
                        "• Notification Pop-up\n\n" +
                        "Path:\n" +
                        "Settings → Notifications"
            }

            brand.contains("huawei") -> {

                "Your Device : Huawei $model\n\n" +
                        "Please enable:\n\n" +
                        "• Launch Automatically\n" +
                        "• Secondary Launch\n" +
                        "• Run in Background\n" +
                        "• Show on Lock Screen\n\n" +
                        "Path:\n" +
                        "App Launch Settings"
            }

            brand.contains("oneplus") -> {

                "Your Device : OnePlus $model\n\n" +
                        "Please enable:\n\n" +
                        "• Display on Lock Screen\n" +
                        "• Auto Launch\n" +
                        "• Background Activity\n\n" +
                        "Path:\n" +
                        "App Info → Permissions"
            }

            brand.contains("motorola") -> {

                "Your Device : Motorola $model\n\n" +
                        "Please allow notification popup and lock screen access."
            }

            brand.contains("nokia") -> {

                "Your Device : Nokia $model\n\n" +
                        "Please allow notification popup and lock screen access."
            }

            brand.contains("google") ||
                    brand.contains("pixel") -> {

                "Your Device : Google Pixel $model\n\n" +
                        "Please allow full screen notifications."
            }

            else -> {

                "Your Device : $model\n\n" +
                        "Please allow:\n\n" +
                        "• Lock Screen Display\n" +
                        "• Popup Window\n" +
                        "• Auto Start\n\n" +
                        "to receive voice alerts properly."
            }
        }

        AlertDialog.Builder(context)
            .setTitle("Enable Important Permissions")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(
                "Open Settings"
            ) { _, _ ->

                openPermissionSettings(context)
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog, _ ->

                dialog.dismiss()
            }
            .show()
    }

    fun openPermissionSettings(context: Context) {

        try {

            when {

                Build.MANUFACTURER.equals(
                    "vivo",
                    true
                ) -> {

                    val intent = Intent()

                    intent.component =
                        ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"
                        )

                    intent.putExtra(
                        "packagename",
                        context.packageName
                    )

                    context.startActivity(intent)
                }

                Build.MANUFACTURER.equals(
                    "oppo",
                    true
                ) -> {

                    val intent = Intent()

                    intent.component =
                        ComponentName(
                            "com.coloros.securitypermission",
                            "com.coloros.securitypermission.permission.PermissionAppAllPermissionActivity"
                        )

                    intent.putExtra(
                        "packageName",
                        context.packageName
                    )

                    context.startActivity(intent)
                }

                Build.MANUFACTURER.equals(
                    "realme",
                    true
                ) -> {

                    val intent = Intent()

                    intent.component =
                        ComponentName(
                            "com.realme.securitypermission",
                            "com.realme.securitypermission.permission.PermissionAppAllPermissionActivity"
                        )

                    intent.putExtra(
                        "packageName",
                        context.packageName
                    )

                    context.startActivity(intent)
                }

                Build.MANUFACTURER.equals(
                    "xiaomi",
                    true
                ) -> {

                    val intent = Intent()

                    intent.component =
                        ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.permissions.PermissionsEditorActivity"
                        )

                    intent.putExtra(
                        "extra_pkgname",
                        context.packageName
                    )

                    context.startActivity(intent)
                }

                else -> {

                    openAppInfo(context)
                }
            }

        } catch (e: Exception) {

            openAppInfo(context)
        }
    }

    private fun openAppInfo(
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
}