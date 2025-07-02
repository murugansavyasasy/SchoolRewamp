package com.yourpackage.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object VimeoThumbnailUploader {

    fun uploadThumbnailToVimeo(
        context: Context,
        videoId: String,
        videoPath: String,
        accessToken: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = extractThumbnailBitmap(context, videoPath)
                if (bitmap == null) {
                    Log.e("Thumbnail", "❌ Failed to extract bitmap")
                    withContext(Dispatchers.Main) { onResult(false, null) }
                    return@launch
                }

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                val imageData = outputStream.toByteArray()

                // Step 1: Request a new picture container (NO "active": true!)
                val picturesUrl = URL("https://api.vimeo.com/videos/$videoId/pictures")
                val createConn = (picturesUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.use {
                        val writer = OutputStreamWriter(it)
                        writer.write(JSONObject().put("type", "custom").toString())
                        writer.flush()
                    }
                }

                val response = createConn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val uploadLink = json.getJSONObject("upload").getString("link")
                val pictureUri = json.getString("uri")
                Log.d("Thumbnail", "✅ Got upload link: $uploadLink")

                // Step 2: Upload image to that link
                val uploadConn = (URL(uploadLink).openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "image/jpeg")
                    doOutput = true
                    outputStream.write(imageData)
                }
                uploadConn.inputStream.close()
                Log.d("Thumbnail", "✅ Thumbnail uploaded successfully")

                // Step 3: Activate the uploaded thumbnail
                val activateUrl = URL("https://api.vimeo.com$pictureUri")
                val activateConn = (activateUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PATCH"
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.use {
                        val writer = OutputStreamWriter(it)
                        writer.write(JSONObject().put("active", true).toString())
                        writer.flush()
                    }
                }
                activateConn.inputStream.close()
                Log.d("Thumbnail", "✅ Thumbnail activated")

                // Step 4: Confirm final thumbnail URL
                val finalThumbUrl = fetchVimeoThumbnail(videoId, accessToken)
                withContext(Dispatchers.Main) { onResult(true, finalThumbUrl) }

            } catch (e: Exception) {
                Log.e("Thumbnail", "❌ Upload failed or URL not found: ${e.message}")
                withContext(Dispatchers.Main) { onResult(false, null) }
            }
        }
    }

    private fun extractThumbnailBitmap(context: Context, videoPath: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            if (videoPath.startsWith("content://")) {
                retriever.setDataSource(context, Uri.parse(videoPath))
            } else {
                val file = File(videoPath)
                if (!file.exists()) {
                    Log.e("Thumbnail", "❌ File not found: $videoPath")
                    return null
                }
                retriever.setDataSource(videoPath)
            }
            val bitmap = retriever.getFrameAtTime(1_000_000)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            Log.e("Thumbnail", "❌ getFrameAtTime failed: ${e.message}")
            null
        }
    }

    private fun fetchVimeoThumbnail(videoId: String, accessToken: String): String? {
        return try {
            val url = URL("https://api.vimeo.com/videos/$videoId?fields=pictures.sizes.link")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")

            val response = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            val sizes = json.getJSONObject("pictures").getJSONArray("sizes")
            val lastSize = sizes.getJSONObject(sizes.length() - 1)
            lastSize.getString("link")
        } catch (e: Exception) {
            Log.e("Thumbnail", "❌ Failed to fetch thumbnail URL: ${e.message}")
            null
        }
    }
}
