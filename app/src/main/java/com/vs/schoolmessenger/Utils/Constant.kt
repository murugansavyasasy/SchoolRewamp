package com.vs.schoolmessenger.Utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.GridView
import android.widget.Toast
import com.vs.schoolmessenger.Dashboard.Settings.ContactUs.ContactUs

object Constant {

    var isOtpRedirection = 1
    var isShimmerViewShow = true
    var isShimmerViewDisable = false

    var isShimmerViewDisablenew = false  // added by murugan
    var isShimmerView = false  // added by murugan to development branch


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

    fun redirectToDialPad(context: Context, contactNo: String){
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:"+contactNo) // Replace with the phone number
        context.startActivity(intent)
    }

    fun redirectToMail(context: Context, mail: String){
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
}