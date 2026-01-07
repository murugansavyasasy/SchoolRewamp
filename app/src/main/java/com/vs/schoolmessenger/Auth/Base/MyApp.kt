package com.vs.schoolmessenger.Auth.Base

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vs.schoolmessenger.Utils.LocalHelperForLanguage
import com.vs.schoolmessenger.Utils.SharedPreference

class MyApp : Application(), LifecycleObserver {

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
    }
}
