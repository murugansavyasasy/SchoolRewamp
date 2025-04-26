package com.vs.schoolmessenger.Utils


import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList

class AppSignatureHelper(private val context: Context) {

    fun getAppSignatures(): ArrayList<String> {
        val appCodes = ArrayList<String>()

        try {
            val packageName = context.packageName
            val packageManager = context.packageManager
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo!!
                    .apkContentsSigners
            } else {
                TODO("VERSION.SDK_INT < P")
            }

            for (signature in signatures) {
                val hash = hash(packageName, signature.toCharsString())
                if (hash != null) {
                    appCodes.add(hash)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Package not found", e)
        }

        return appCodes
    }

    private fun hash(packageName: String, signature: String): String? {
        val appInfo = "$packageName $signature"
        try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(appInfo.toByteArray(Charsets.UTF_8))
            val hashSignature = messageDigest.digest()

            val truncatedHash = hashSignature.copyOfRange(0, 9)
            val base64Hash = Base64.encodeToString(truncatedHash, Base64.NO_PADDING or Base64.NO_WRAP)
            base64Hash?.let {
                Log.d(TAG, "Hash: $it")
            }
            return base64Hash
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "hash:NoSuchAlgorithm", e)
        }
        return null
    }

    companion object {
        private const val TAG = "AppSignatureHelper"
    }
}