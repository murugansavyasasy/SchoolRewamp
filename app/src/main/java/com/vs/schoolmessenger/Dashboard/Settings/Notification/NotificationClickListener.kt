package com.vs.schoolmessenger.Dashboard.Settings.Notification

import android.view.View

interface NotificationClickListener {

    fun onClickListener(data: NotificationDataClass, anchorView: View, adapterPosition: Int)

}