package com.vs.schoolmessenger.Auth.Base

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vs.schoolmessenger.Utils.Constant.clearAllLocalStorage
import com.vs.schoolmessenger.Utils.Constant.restartApp
import com.vs.schoolmessenger.Utils.Constant.shouldResetApp
import com.vs.schoolmessenger.Utils.LocalHelperForLanguage
import com.vs.schoolmessenger.Utils.SharedPreference

class MyApp : Application(), LifecycleObserver {

    companion object {
        const val CHANNEL_ID = "school_chimes_notification"
        const val CHANNEL_NAME= "School Notifications"
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)


        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            FirebaseCrashlytics.getInstance().recordException(throwable)
            Log.e("CRASH", "Uncaught: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }


        val isAppLanguage = SharedPreference.getLanguage(this) ?: "en"
        LocalHelperForLanguage.wrapContext(this, isAppLanguage)

//        if (shouldResetApp(this)) {
//            clearAllLocalStorage(this)
//            restartApp(this)
//        }
        createCallChannel()

    }

    private fun createCallChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }

            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }
}