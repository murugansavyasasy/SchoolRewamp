package com.vs.schoolmessenger.Utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


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


