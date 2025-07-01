package com.vs.schoolmessenger.Utils

import com.vs.schoolmessenger.Repository.APIKeyNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit


fun fetchVimeoThumbnail(videoId: String, callback: (String?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val client = OkHttpClient()

        repeat(30) { attempt -> // try up to ~90 seconds
            val request = Request.Builder()
                .url("https://api.vimeo.com/videos/$videoId")
                .addHeader("Authorization", "Bearer ${Constant.isVimeoToken}")
                .addHeader("Accept", "application/vnd.vimeo.*+json;version=3.4")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (response.isSuccessful && body != null) {
                val json = JSONObject(body)
                val duration = json.optInt("duration", 0)
                val pictures = json.optJSONObject("pictures")
                val type = pictures?.optString("type", "default")
                val sizes = pictures?.optJSONArray("sizes")

                if (duration > 0 && type != "default" && sizes != null && sizes.length() > 0) {
                    val best = sizes.getJSONObject(sizes.length() - 1)
                    val thumbnailUrl = best.getString("link")
                    if (!thumbnailUrl.contains("default-")) {
                        withContext(Dispatchers.Main) {
                            callback(thumbnailUrl)
                        }
                        return@launch
                    }
                }
            }

            delay(3000) // wait before next attempt
        }

        // Fallback or timeout
        withContext(Dispatchers.Main) {
            callback(null)
        }
    }
}


//fun fetchVimeoThumbnail(videoUrl: String, callback: (String?) -> Unit) {
//    // Run the network call in a coroutine to avoid blocking the main thread
//    CoroutineScope(Dispatchers.IO).launch {
//        try {
//            // Create an OkHttpClient with timeouts
//            val client = OkHttpClient.Builder()
//                .connectTimeout(30, TimeUnit.SECONDS)  // Set connection timeout
//                .readTimeout(30, TimeUnit.SECONDS)     // Set read timeout
//                .build()
//
//            // Prepare the Vimeo API request URL
//            val apiUrl = "https://vimeo.com/api/oembed.json?url=$videoUrl"
//            val request = Request.Builder().url(apiUrl).build()
//
//            // Execute the request
//            val response = client.newCall(request).execute()
//
//            // Check if the response is successful
//            if (response.isSuccessful) {
//                val responseBody = response.body?.string()
//                println("Raw Response: $responseBody")
//
//                // Parse the response to extract the thumbnail URL
//                val jsonResponse = JSONObject(responseBody)
//                val thumbnailUrl = jsonResponse.optString(APIKeyNames.thumbnail_url, null)
//
//                withContext(Dispatchers.Main) {
//                    // Return the thumbnail URL to the callback
//                    callback(thumbnailUrl)
//                }
//            } else {
//                // Log error if the response is not successful
//                println("Error fetching thumbnail: ${response.code}")
//                withContext(Dispatchers.Main) {
//                    callback(null)
//                }
//            }
//
//        } catch (e: Exception) {
//            // Log the exception
//            e.printStackTrace()
//            withContext(Dispatchers.Main) {
//                println("Error fetching thumbnail: ${e.message}")
//                callback(null)
//            }
//        }
//    }
//}

