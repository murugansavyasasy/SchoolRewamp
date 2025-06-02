package com.vs.schoolmessenger.Vimeo

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class VimeoVideoUploadTask(
    val activity: Activity,
    val title: String,
    val description: String,
    val authToken: String,
    val videoFilePath: String,
    val listener: UploadCompletionListener?
) : AsyncTask<Void, Int, Boolean>() {
    var upload = false

    interface UploadCompletionListener {
        fun onProgressUpdate(percentage: Int, Uploading: Boolean) {}
        fun onUploadComplete(success: Boolean, iframe: String, link: String)
        fun onUploadFailed(error: String) {}

    }

    private interface VimeoUploadURLListener {
        fun onUploadURLGenerated(uploadLink: String, iframe: String, link: String) {}
        fun onFailure(errorMessage: String) {}
    }

    private interface VimeoUploadListener {
        fun onUploadComplete(success: Boolean, iframe: String, link: String) {}
        fun onFailure(errorMessage: String)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        values[0]?.let { percentage ->
            Log.d("percentage", percentage.toString())
            listener!!.onProgressUpdate(percentage, upload)
        }
    }


    override fun doInBackground(vararg p0: Void?): Boolean {

        createVimeoUploadURL(title,
            description,
            authToken,
            videoFilePath,
            object : VimeoUploadURLListener {
                override fun onUploadURLGenerated(
                    uploadLink: String, iframe: String, link: String
                ) {
                    uploadVideoToVimeo(iframe,
                        link,
                        uploadLink,
                        videoFilePath,
                        authToken,
                        object : VimeoUploadListener {
                            override fun onUploadComplete(
                                success: Boolean, iframe: String, link: String
                            ) {
                                listener?.let {
                                    it.onUploadComplete(success, iframe, link)
                                    upload = true
                                }
                            }

                            override fun onFailure(errorMessage: String) {
                                upload = false
                            }
                        })
                }

                override fun onFailure(errorMessage: String) {
                    Log.e("VimeoUploader", "Failed to create upload URL: $errorMessage")
                    listener?.let {
                        it.onUploadComplete(false, "", "")
                        upload = false
                    }
                }
            })

        return true
    }

    private fun uploadVideoToVimeo(
        iframe: String,
        link: String,
        uploadLink: String,
        videoFilePath: String,
        authToken: String,
        listener: VimeoUploadListener
    ) {
        object : AsyncTask<Void, Void, Boolean>() {
            override fun doInBackground(vararg voids: Void): Boolean? {
                var fileInputStream: FileInputStream? = null
                try {
                    val videoFile = File(videoFilePath)
                    val fileSize = videoFile.length()

                    fileInputStream = FileInputStream(videoFile)

                    val url = URL(uploadLink)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "PATCH"
                    conn.setRequestProperty("Authorization", "Bearer $authToken")
                    conn.setRequestProperty("Content-Type", "application/offset+octet-stream")
                    conn.setRequestProperty("Upload-Offset", "0")
                    conn.setRequestProperty("Tus-Resumable", "1.0.0")
                    conn.doOutput = true

                    val buffer = ByteArray(5 * 1024 * 1024) // Chunk size
                    var bytesRead: Int
                    while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                        conn.outputStream.write(buffer, 0, bytesRead)
                    }

                    val responseCode = conn.responseCode
                    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        listener.onUploadComplete(true, iframe, link)
                    } else if (responseCode == HttpURLConnection.HTTP_PRECON_FAILED) {
                        val serverOffset = conn.getHeaderField("Upload-Offset")
                        if (serverOffset != null) {
                            conn.setRequestProperty("Upload-Offset", serverOffset)
                            conn.outputStream.write(buffer, 0, bytesRead)
                            upload = true
                        } else {
                            listener.onFailure("Failed to upload chunk: Precondition Failed")
                        }
                    } else {
                        listener.onFailure("Failed to upload chunk, status code: $responseCode")
                    }
                } catch (e: IOException) {
                    upload = false
                    e.printStackTrace()
                    listener.onFailure(e.message ?: "IOException occurred")
                } finally {
                    fileInputStream?.close()
                }
                return upload
            }
        }.execute()
    }

    private fun createVimeoUploadURL(
        isTitle: String,
        isDescription: String,
        authToken: String,
        videoFilePath: String,
        listener: VimeoUploadURLListener
    ) {
        object : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg voids: Void): String? {
                try {
                    val videoFile = File(videoFilePath)
                    val fileSize = videoFile.length()

                    val url = URL("https://api.vimeo.com/me/videos")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Authorization", "Bearer $authToken")
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("Accept", "application/vnd.vimeo.*+json;version=3.4")
                    conn.doOutput = true

                    val jsonParam = JSONObject().apply {
                        put("upload", JSONObject().apply {
                            put("approach", "tus")
                            put("size", fileSize)
                        })
                        put("privacy", JSONObject().apply {
                            put("view", "unlisted")
                        })
                        put("embed", JSONObject().apply {
                            put("buttons", JSONObject().apply {
                                put("share", "false")
                            })
                        })
                        put("name", if (isTitle.isEmpty()) "videoTitle" else isTitle)
                        put(
                            "description",
                            if (isDescription.isEmpty()) "videoDesc" else isDescription
                        )
                    }

                    Log.d("Vimeo_Request", jsonParam.toString())

                    conn.outputStream.use { os ->
                        OutputStreamWriter(os).use { writer ->
                            writer.write(jsonParam.toString())
                        }
                    }

                    val responseCode = conn.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                        val jsonResponse = JSONObject(response)
                        val upload = jsonResponse.getJSONObject("upload")
                        val embed = jsonResponse.getJSONObject("embed")
                        val link = jsonResponse.getString("link")
                        val uploadLink = upload.getString("upload_link")
                        val iframe = embed.getString("html")
                        this@VimeoVideoUploadTask.upload = true
                        listener.onUploadURLGenerated(uploadLink, iframe, link)
                    } else {
                        listener.onFailure("HTTP error code: $responseCode")
                    }

                    for (i in 1..5) {
                        publishProgress(i * 20)
                        Thread.sleep(1000)
                        this@VimeoVideoUploadTask.upload = true
                    }
                } catch (e: IOException) {
                    this@VimeoVideoUploadTask.upload = false
                    e.printStackTrace()
                    listener.onFailure(e.message ?: "IOException occurred")
                } catch (e: JSONException) {
                    this@VimeoVideoUploadTask.upload = false
                    e.printStackTrace()
                    listener.onFailure(e.message ?: "JSONException occurred")
                }
                return null
            }
        }.execute()
    }
}