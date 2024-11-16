package com.vs.schoolmessenger.util

import android.util.Log
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object VimeoVideoDownload {

    private const val VIMEO_API_BASE_URL = "https://api.vimeo.com/videos/"
    private const val ACCESS_TOKEN =
        "8d74d8bf6b5742d39971cc7d3ffbb51a" // Replace with your actual access token

    // Define the callback interface
    interface VimeoDownloadCallback {
        fun onDownloadUrlRetrieved(quality: String, downloadUrl: String)
        fun onError(errorMessage: String)
    }

    fun getVimeoDownloadUrl(videoId: String, callback: VimeoDownloadCallback) {
        val client = OkHttpClient()

        // Create request
        val request = Request.Builder()
            .url("$VIMEO_API_BASE_URL$videoId")
            .addHeader("Authorization", "Bearer $ACCESS_TOKEN")
            .build()

        // Send request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Failed to retrieve video: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    Log.d("jsonResponse", jsonResponse.orEmpty())

                    if (!jsonResponse.isNullOrEmpty()) {
                        try {
                            // Parse JSON response to extract download links
                            val jsonObject = JsonParser.parseString(jsonResponse).asJsonObject
                            if (jsonObject.has("download")) {
                                for (download in jsonObject["download"].asJsonArray) {
                                    val quality = download.asJsonObject["quality"].asString
                                    val downloadUrl = download.asJsonObject["link"].asString

                                    Log.d("downloadUrl", downloadUrl)

                                    // Pass download URL back to the caller through the callback
                                    callback.onDownloadUrlRetrieved(quality, downloadUrl)
                                }
                            } else {
                                callback.onError("No downloadable URLs available for this video.")
                            }
                        } catch (e: Exception) {
                            callback.onError("Error parsing video info: ${e.message}")
                        }
                    } else {
                        callback.onError("Empty response received from Vimeo API.")
                    }
                } else {
                    callback.onError("Failed to fetch video info: ${response.message}")
                }
            }
        })
    }
}
