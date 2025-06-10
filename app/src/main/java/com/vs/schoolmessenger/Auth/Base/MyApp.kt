package com.vs.schoolmessenger.Auth.Base

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.system.exitProcess

class MyApp  : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

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
}