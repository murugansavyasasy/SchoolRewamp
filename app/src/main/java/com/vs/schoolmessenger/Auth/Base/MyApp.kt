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
import com.vs.schoolmessenger.Utils.LocalHelperForLanguage
import com.vs.schoolmessenger.Utils.SharedPreference
import kotlin.system.exitProcess

class MyApp : Application(), LifecycleObserver {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }
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

        // ✅ Apply language *here*, not in attachBaseContext
        val isAppLanguage = SharedPreference.getLanguage(this) ?: "en"
        LocalHelperForLanguage.wrapContext(this, isAppLanguage)
    }
}