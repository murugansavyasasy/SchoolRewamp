package com.vs.schoolmessenger.Utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import java.io.File


object AppDataCleaner {

    fun clearOldDataIfNeeded(context: Context) : Boolean {
        val prefs = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
        val savedVersion = prefs.getInt("last_version", -1)
        Log.d("savedVersion",savedVersion.toString())
        val packageInfo = packageManager(context).getPackageInfo("com.vs.schoolmessenger.SchoolChimesRewamp", 0)
        val currentVersion = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            packageInfo.versionCode
        }

        Log.d("currentVersion",currentVersion.toString())

       return if (savedVersion < currentVersion) {
            // 1. Clear old SharedPreferences (add more if you had multiple files)
            clearPreferences(context, "SH_PREF")
            clearPreferences(context, "SP_MESSENGER_SCHOOLS")
            clearPreferences(context, "school_SH")
            clearPreferences(context, "Language")
            clearPreferences(context, "APP_TERMS")
            clearPreferences(context, "spNameinstall")
            clearPreferences(context, "SP_SCHOOLS_COUNTRY")

            // 2. Delete old databases
//            context.deleteDatabase("school.db")
//            context.deleteDatabase("students.db") // add more if you had

            // 3. Delete internal files
            deleteDir(context.filesDir)

            // 4. Delete cache
            deleteDir(context.cacheDir)

            // 4. Delete external storage (/Android/data/<package>/files + cache)
            context.getExternalFilesDir(null)?.let { deleteDir(it) }
            context.externalCacheDir?.let { deleteDir(it) }

           //5. if folder created

           val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

           val myFolder = File(downloads, "SchoolChimes")
           if (myFolder.exists()) {
               Log.d("File Exist","exist")
               deleteDir(myFolder)
           }


           // (Optional) clear specific external dirs like Downloads, Pictures
            val externalStorage = Environment.getExternalStorageDirectory()
//            val appFolder = File(externalStorage, "Android/data/${context.packageName}")
            val appFolder = File(externalStorage, "Android/data/com.vs.schoolmessenger.SchoolChimesRewamp")
            deleteDir(appFolder)

            // 5. Save new version to avoid running again
            prefs.edit().putInt("last_version", currentVersion).apply()

           true
        }
        else{
            false
       }
    }

    private fun clearPreferences(context: Context, name: String) {
        val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            dir.listFiles()?.forEach { child ->
                deleteDir(child)
            }
        }
        return dir?.delete() ?: false
    }
}