package com.vs.schoolmessenger.Auth.Base

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.system.exitProcess

class MyApp : Application(), LifecycleObserver {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log to Firebase
            FirebaseCrashlytics.getInstance().recordException(throwable)
            // Optional: Log locally
            Log.e("CRASH", "Uncaught: ${throwable.message}", throwable)
            // Optional: Kill app or restart
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        // App goes to background
        Log.d("AppStatus", "onAppBackgrounded")
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putLong("last_close_time", System.currentTimeMillis()).apply()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        // Optional: app comes to foreground, calculate difference
        Log.d("AppStatus", "onAppForegrounded")
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastCloseTime = prefs.getLong("last_close_time", -1)
        if (lastCloseTime != -1L) {
            val diff = System.currentTimeMillis() - lastCloseTime
            Log.d("AppResume", "Time since last close: ${diff / 1000} seconds")
        }
    }
}