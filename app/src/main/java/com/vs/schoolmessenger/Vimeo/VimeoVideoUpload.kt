package com.vs.schoolmessenger.util

import android.app.Activity
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Utils.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object VimeoVideoUpload {

    fun uploadVideo(
        activity: Activity,
        title: String,
        description: String,
        videoFilePath: String,
        listener: UploadCompletionListener?
    ) {
        createVimeoUploadURL(
            activity, title, description, videoFilePath, object : VimeoUploadURLListener {
                override fun onUploadURLGenerated(
                    uploadLink: String?, iframe: String?, link: String?
                ) {
                    uploadVideoToVimeo(
                        activity,
                        iframe,
                        link,
                        uploadLink,
                        videoFilePath,
                        object : VimeoUploadListener {
                            override fun onUploadComplete(
                                success: Boolean, iframe: String?, link: String?
                            ) {
                                if (listener != null) {
                                    listener.onUploadComplete(success, iframe, link)
                                }
                            }

                            override fun onFailure(errorMessage: String?) {
                                listener?.onFailure(errorMessage)
                            }
                        })
                }

                override fun onFailure(errorMessage: String?) {
                    listener?.onFailure(errorMessage)
                }
            })
    }

    private fun createVimeoUploadURL(
        activity: Activity,
        isTitle: String,
        isDescription: String,
        videoFilePath: String,
        listener: VimeoUploadURLListener
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sizeInBytes = if (videoFilePath.startsWith(Constant.content)) {
                    val uri = videoFilePath.toUri()
                    val cursor = activity.contentResolver.query(uri, null, null, null, null)
                    val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE) ?: -1
                    var size: Long = 0
                    if (cursor != null && sizeIndex != -1) {
                        cursor.moveToFirst()
                        size = cursor.getLong(sizeIndex)
                        cursor.close()
                    }
                    size
                } else {
                    File(videoFilePath).length()
                }

                val url = URL(Constant.isVimeoUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = Constant.POST
                conn.setRequestProperty(
                    APIKeyNames.Authorization, APIKeyNames.Bearer + Constant.isVimeoToken
                )
                conn.setRequestProperty(Constant.Content_Type, Constant.application_json)
                conn.setRequestProperty(Constant.Accept, Constant.application_vimeo_jsonversion)
                conn.doOutput = true

                val jsonParam = JSONObject().apply {
                    put(Constant.upload, JSONObject().apply {
                        put(Constant.approach, Constant.tus)
                        put(Constant.size, sizeInBytes.toString())
                    })
                    put(Constant.privacy, JSONObject().apply {
                        put(Constant.view, Constant.unlisted)
                        put(Constant.download, true)
                    })
                    put(APIKeyNames.name, isTitle.ifEmpty { Constant.videoTitle })
                    put(APIKeyNames.description, isDescription.ifEmpty { Constant.videoDesc })
                }

                OutputStreamWriter(conn.outputStream).use { it.write(jsonParam.toString()) }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    Log.d("isVimeoResponse", response)
                    val jsonResponse = JSONObject(response)
                    val upload = jsonResponse.getJSONObject(APIKeyNames.upload)
                    val embed = jsonResponse.getJSONObject(APIKeyNames.embed)
                    val link = jsonResponse.getString(APIKeyNames.link)

                    val uploadLink = upload.getString(APIKeyNames.upload_link)
                    val iframe = embed.getString(APIKeyNames.html)

                    withContext(Dispatchers.Main) {
                        listener.onUploadURLGenerated(uploadLink, iframe, link)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        listener.onFailure("HTTP error code: $responseCode")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    listener.onFailure(e.message)
                }
            }
        }
    }

    private fun uploadVideoToVimeo(
        activity: Activity,
        iframe: String?,
        link: String?,
        uploadLink: String?,
        videoFilePath: String,
        listener: VimeoUploadListener
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var inputStream: InputStream? = null
            try {
                val videoFile = File(videoFilePath)
                val videoLength = if (videoFilePath.startsWith(Constant.content)) {
                    val uri = videoFilePath.toUri()
                    val cursor = activity.contentResolver.query(uri, null, null, null, null)
                    val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE) ?: -1
                    var size: Long = 0
                    if (cursor != null && sizeIndex != -1) {
                        cursor.moveToFirst()
                        size = cursor.getLong(sizeIndex)
                        cursor.close()
                    }
                    size
                } else {
                    videoFile.length()
                }

                inputStream = if (videoFilePath.startsWith(Constant.content)) {
                    activity.contentResolver.openInputStream(videoFilePath.toUri())
                } else {
                    FileInputStream(videoFile)
                }

                val offsetConn = URL(uploadLink).openConnection() as HttpURLConnection
                offsetConn.requestMethod = Constant.HEAD
                offsetConn.setRequestProperty(
                    Constant.HETus_ResumableAD, Constant.HETus_ResumableAD_Version
                )
                offsetConn.setRequestProperty(
                    APIKeyNames.Authorization, APIKeyNames.Bearer + Constant.isVimeoToken
                )
                val offset = offsetConn.getHeaderField(Constant.Upload_Offset)?.toLongOrNull() ?: 0L
                offsetConn.disconnect()

                val conn = URL(uploadLink).openConnection() as HttpURLConnection
                conn.requestMethod = Constant.PATCH
                conn.setRequestProperty(
                    APIKeyNames.Authorization, APIKeyNames.Bearer + Constant.isVimeoToken
                )
                conn.setRequestProperty(
                    Constant.Content_Type, Constant.application_offset_octet_stream
                )
                conn.setRequestProperty(Constant.Upload_Offset, offset.toString())
                conn.setRequestProperty(
                    Constant.HETus_ResumableAD, Constant.HETus_ResumableAD_Version
                )
                conn.doOutput = true

                inputStream?.skip(offset)
                val outputStream = conn.outputStream

                val buffer = ByteArray(1024 * 1024) // 1MB chunks
                var bytesRead: Int
                var totalUploaded = offset
                var lastPercent = -1

                while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalUploaded += bytesRead

                    val percent = ((totalUploaded * 100) / videoLength).toInt()
                    if (percent != lastPercent && percent in 1..100) {
                        lastPercent = percent
                        withContext(Dispatchers.Main) {
                            (listener as? UploadCompletionListener)?.onProgressUpdate(percent)
                        }
                        Log.d("VimeoUploadProgress", "Progress: $percent%")
                    }
                }

                outputStream.flush()
                outputStream.close()

                val responseCode = conn.responseCode
                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        listener.onUploadComplete(true, iframe, link)
                    } else {
                        listener.onFailure("Failed to upload chunk, status code: $responseCode")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    listener.onFailure("IOException: ${e.message}")
                }
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    interface UploadCompletionListener {
        fun onUploadComplete(success: Boolean, iframe: String?, link: String?)
        fun onFailure(errorMessage: String?)
        fun onProgressUpdate(percent: Int)
    }

    private interface VimeoUploadURLListener {
        fun onUploadURLGenerated(uploadLink: String?, iframe: String?, link: String?)
        fun onFailure(errorMessage: String?)
    }

    private interface VimeoUploadListener {
        fun onUploadComplete(success: Boolean, iframe: String?, link: String?)
        fun onFailure(errorMessage: String?)
    }
}
