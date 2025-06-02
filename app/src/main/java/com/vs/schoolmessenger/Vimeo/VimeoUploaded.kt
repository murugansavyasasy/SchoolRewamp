package com.vs.schoolmessenger.util

import android.app.Activity
import android.app.ProgressDialog
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object VimeoUploader {

    fun uploadVideo(
        activity: Activity,
        title: String,
        description: String,
        authToken: String?,
        videoFilePath: String,
        listener: UploadCompletionListener?
    ) {
        val mProgressDialog = ProgressDialog(activity)
        mProgressDialog.setIndeterminate(true)
        mProgressDialog.setMessage("Uploading..... Please wait.")
        mProgressDialog.setCancelable(false)

        if (!activity.isFinishing) mProgressDialog.show()

        createVimeoUploadURL(
            title,
            description,
            authToken,
            videoFilePath,
            object : VimeoUploadURLListener {
                override fun onUploadURLGenerated(
                    uploadLink: String?,
                    iframe: String?,
                    link: String?
                ) {
                    uploadVideoToVimeo(
                        activity,
                        iframe,
                        link,
                        uploadLink,
                        videoFilePath,
                        authToken,
                        object : VimeoUploadListener {
                            override fun onUploadComplete(
                                success: Boolean,
                                iframe: String?,
                                link: String?
                            ) {
                                if (listener != null) {
                                    if (mProgressDialog.isShowing) mProgressDialog.dismiss()
                                    listener.onUploadComplete(success, iframe, link)
                                }
                            }

                            override fun onFailure(errorMessage: String?) {
                                if (mProgressDialog.isShowing) mProgressDialog.dismiss()
                                Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
                                listener?.onFailure(errorMessage)
                            }
                        })
                }

                override fun onFailure(errorMessage: String?) {
                    Log.e("VimeoUploader", "Failed to create upload URL: $errorMessage")
                    listener?.onFailure(errorMessage)
                    if (mProgressDialog.isShowing) mProgressDialog.dismiss()
                }
            })
    }

    private fun createVimeoUploadURL(
        isTitle: String,
        isDescription: String,
        authToken: String?,
        videoFilePath: String,
        listener: VimeoUploadURLListener
    ) {
        object : AsyncTask<Void?, Void?, String?>() {
            override fun doInBackground(vararg voids: Void?): String? {
                try {
                    val url = URL("https://api.vimeo.com/me/videos")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Authorization", "Bearer $authToken")
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("Accept", "application/vnd.vimeo.*+json;version=3.4")
                    conn.doOutput = true

                    val jsonParam = JSONObject()
                    val uploadObj = JSONObject()
                    uploadObj.put("approach", "tus")
                    uploadObj.put("size", File(videoFilePath).length().toString())
                    jsonParam.put("upload", uploadObj)

                    val privacy = JSONObject()
                    privacy.put("view", "unlisted")
                    privacy.put("download", true)
                    jsonParam.put("privacy", privacy)

                    jsonParam.put("name", isTitle.ifEmpty { "videoTitle" })
                    jsonParam.put("description", isDescription.ifEmpty { "videoDesc" })

                    val out = OutputStreamWriter(conn.outputStream)
                    out.write(jsonParam.toString())
                    out.close()

                    val responseCode = conn.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        val inputStream = conn.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val response = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        reader.close()
                        val jsonResponse = JSONObject(response.toString())
                        val upload = jsonResponse.getJSONObject("upload")
                        val embed = jsonResponse.getJSONObject("embed")
                        val link = jsonResponse.getString("link")

                        val uploadLink = upload.getString("upload_link")
                        val iframe = embed.getString("html")

                        listener.onUploadURLGenerated(uploadLink, iframe, link)
                    } else {
                        listener.onFailure("HTTP error code: $responseCode")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    listener.onFailure(e.message)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    listener.onFailure(e.message)
                }
                return null
            }
        }.execute()
    }

    private fun uploadVideoToVimeo(
        activity: Activity,
        iframe: String?,
        link: String?,
        uploadLink: String?,
        videoFilePath: String,
        authToken: String?,
        listener: VimeoUploadListener
    ) {
        object : AsyncTask<Void?, Void?, Boolean?>() {
            override fun doInBackground(vararg voids: Void?): Boolean? {
                var inputStream: InputStream? = null
                try {
                    val offsetConn = (URL(uploadLink).openConnection() as HttpURLConnection).apply {
                        requestMethod = "HEAD"
                        setRequestProperty("Tus-Resumable", "1.0.0")
                        connect()
                    }
                    val uploadOffset = offsetConn.getHeaderField("Upload-Offset")?.toLongOrNull() ?: 0
                    Log.d("VimeoUploader", "Resuming upload at offset: $uploadOffset")

                    inputStream = if (videoFilePath.startsWith("content://")) {
                        activity.contentResolver.openInputStream(Uri.parse(videoFilePath))
                    } else {
                        FileInputStream(File(videoFilePath))
                    }

                    inputStream?.skip(uploadOffset)

                    val url = URL(uploadLink)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "PATCH"
                    conn.setRequestProperty("Authorization", "Bearer $authToken")
                    conn.setRequestProperty("Content-Type", "application/offset+octet-stream")
                    conn.setRequestProperty("Upload-Offset", uploadOffset.toString())
                    conn.setRequestProperty("Tus-Resumable", "1.0.0")
                    conn.doOutput = true

                    val buffer = ByteArray(5 * 1024 * 1024)
                    var bytesRead: Int
                    val output = conn.outputStream

                    while ((inputStream?.read(buffer).also { bytesRead = it ?: -1 }) != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                    output.close()

                    val responseCode = conn.responseCode
                    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        listener.onUploadComplete(true, iframe, link)
                    } else {
                        listener.onFailure("Failed to upload chunk, status code: $responseCode")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    listener.onFailure(e.message)
                } finally {
                    try {
                        inputStream?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return null
            }
        }.execute()
    }

    interface UploadCompletionListener {
        fun onUploadComplete(success: Boolean, iframe: String?, link: String?)
        fun onFailure(errorMessage: String?)
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