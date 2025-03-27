package com.vs.schoolmessenger.Utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.vs.schoolmessenger.Auth.Country.Country
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.PasswordUpdateData
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationData
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpData
import com.vs.schoolmessenger.Auth.Splash.Splash
import com.vs.schoolmessenger.R

object Constant {

    var isDeviceType = "Android"
    var isVersionId = 93
    var isShimmerViewShow = true
    var isShimmerViewDisable = false
    var isShimmerViewDisablenew = false  // added by murugan
    var isShimmerView = false  // added by murugan to development branch
    var handler = Handler(Looper.getMainLooper())
    val delayTime = 1500
    var isParentChoose = false
    var country_details: Country? = null
    var user_details: UserDetails? = null
    var user_data: List<UserValidationData>? = null
    var isStaffDetails: List<StaffDetails>? = null
    var isChildDetails: List<ChildDetails>? = null
    var isPasswordCreation: Boolean? = false
//    var isLogOut = false
    var forgotData: List<ForgetOtpData>? = null
    var isForgotPassword: Boolean? = false
    var isMobileNumber: String? = ""

    var SplashScreen: Int? = 1
    var MobileNumberScreen: Int? = 2
    var SignInScreen: Int? = 3
    var PasswordScreen: Int? = 4
    var pageType: Int? = 0

    fun isInternetAvailable(activity: Activity): Boolean {
        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    fun setGridViewHeight(gridView: GridView, columns: Int) {
        val adapter = gridView.adapter ?: return  // Check if adapter is not null

        var totalHeight = 0
        val items = adapter.count
        val rows = (items + columns - 1) / columns  // Calculate the number of rows

        // Loop through each item to get its height
        for (i in 0 until items) {
            val listItem = adapter.getView(i, null, gridView)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(gridView.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
            )
            totalHeight += listItem.measuredHeight
        }

        // Calculate total height by adding row heights and spacing between rows
        totalHeight += (gridView.verticalSpacing * (rows - 1))

        val params = gridView.layoutParams
        params.height = totalHeight
        gridView.layoutParams = params
        gridView.requestLayout()  // Request layout update
    }

    fun redirectToDialPad(context: Context, contactNo: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:" + contactNo) // Replace with the phone number
        context.startActivity(intent)
    }

    fun redirectToMail(context: Context, mail: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Ensures only email apps handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(mail)) // Recipient email address
            putExtra(Intent.EXTRA_SUBJECT, "") // Subject
            putExtra(Intent.EXTRA_TEXT, "") // Email body
        }
// Verify that there is an email app to handle the intent
        val emailApps = context.packageManager.queryIntentActivities(intent, 0)
        if (emailApps.isNotEmpty()) {
            context.startActivity(intent)
        } else {
            context.startActivity(intent)
            //   Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }


    fun redirectToMessage(context: Context, phoneNumber: String) {
        val smsUri = Uri.parse("sms:" + phoneNumber)
        val intent = Intent(Intent.ACTION_VIEW, smsUri).apply {
            putExtra("", "")
        }
        context.startActivity(intent)
    }

    fun editTextCounter(
        context: Context,
        editText: EditText,
        maxLength: Int,
        counterLabel: TextView
    ) {

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // You can add logic here if needed
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // You can add logic here if needed
            }

            override fun afterTextChanged(editable: Editable?) {
                counterLabel.setText(editable!!.length.toString() + " of " + maxLength.toString())
                if (editable != null && editable.length > maxLength) {
                    // Restrict to the max length by trimming the input
                    editable.delete(maxLength, editable.length)
                    // Optionally, show a Toast or error message
                }
            }
        })
    }

    fun executeAfterDelay(task: () -> Unit) {
        handler.postDelayed({
            task()
        }, delayTime.toLong())
    }

    fun stopDelay() {
        handler.removeCallbacksAndMessages(null)
    }

    fun loadWebView(context: Context, webView: WebView, url: String) {
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
    }

    fun getAndroidSecureId(activity: Activity): String {
        return Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "Empty"
    }

    fun errorAlert(activity: Activity, title: String, content: String) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.custom_error_alert, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
        alertDialog.show()
        // Access views
        val titleText = dialogView.findViewById<TextView>(R.id.alertTitle)
        val messageText = dialogView.findViewById<TextView>(R.id.alertMessage)
        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        messageText.text = content
        okButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) == 1
    }

    fun setKeyboardListener(rootView: View, onKeyboardStateChanged: (Boolean) -> Unit) {
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val isKeyboardOpened = keypadHeight > screenHeight * 0.15
            onKeyboardStateChanged(isKeyboardOpened)
        }
    }
}