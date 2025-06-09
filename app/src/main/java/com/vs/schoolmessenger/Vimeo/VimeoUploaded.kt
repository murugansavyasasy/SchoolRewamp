package com.vs.schoolmessenger.Vimeo

import android.content.Context
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.concurrent.Executors

class VimeoTusUploader(
    private val accessToken: String,
    private val context: Context
) {

    interface Callback {
        fun onProgress(progressPercent: Int)
        fun onSuccess(videoUrl: String, embedIframe: String)
        fun onError(errorMessage: String)
    }

    private val client = OkHttpClient()
    private val executor = Executors.newSingleThreadExecutor()

    fun uploadVideo(uri: Uri, callback: Callback) {
        executor.execute {
            try {
                val tempFile = copyUriToTempFile(uri)
                if (tempFile == null || !tempFile.exists()) {
                    callback.onError("Failed to access file from Uri")
                    return@execute
                }

                // Step 1: Request upload link from Vimeo
                val uploadLink = createVimeoUploadLink(tempFile.length())
                if (uploadLink == null) {
                    callback.onError("Failed to create Vimeo upload link")
                    return@execute
                }

                // Step 2: Upload file using TUS
                uploadWithTus(tempFile, uploadLink, callback)

            } catch (e: Exception) {
                callback.onError("Upload error: ${e.message}")
            }
        }
    }

    private fun copyUriToTempFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("vimeo_upload_", ".mp4", context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createVimeoUploadLink(fileSize: Long): String? {
        val jsonBody = JSONObject().apply {
            put("upload", JSONObject().apply {
                put("approach", "tus")
                put("size", fileSize)
            })
        }.toString()

        val request = Request.Builder()
            .url("https://api.vimeo.com/me/videos")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept", "application/vnd.vimeo.*+json;version=3.4")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }
                val responseBody = response.body?.string() ?: return null
                val json = JSONObject(responseBody)
                json.getJSONObject("upload").getString("upload_link")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun uploadWithTus(file: File, uploadUrl: String, callback: Callback) {
        val chunkSize = 1024 * 1024 // 1MB
        val totalSize = file.length()
        var offset = 0L

        val raf = RandomAccessFile(file, "r")
        try {
            while (offset < totalSize) {
                val remaining = totalSize - offset
                val bytesToRead = if (remaining < chunkSize) remaining.toInt() else chunkSize

                val buffer = ByteArray(bytesToRead)
                raf.seek(offset)
                raf.readFully(buffer)

                val request = Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Tus-Resumable", "1.0.0")
                    .addHeader("Upload-Offset", offset.toString())
                    .addHeader("Content-Type", "application/offset+octet-stream")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .patch(buffer.toRequestBody("application/offset+octet-stream".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.code != 204) {
                        callback.onError("Upload failed with code: ${response.code}")
                        return
                    }
                    val newOffset = response.header("Upload-Offset")?.toLongOrNull()
                    if (newOffset == null || newOffset <= offset) {
                        callback.onError("Invalid upload offset received")
                        return
                    }
                    offset = newOffset
                    val progress = ((offset.toDouble() / totalSize) * 100).toInt()
                    callback.onProgress(progress)
                }
            }

            val videoId = uploadUrl.split("/").last()
            val videoUrl = "https://vimeo.com/$videoId"
            val iframe = """<iframe src="https://player.vimeo.com/video/$videoId" width="640" height="360" frameborder="0" allowfullscreen></iframe>"""

            callback.onSuccess(videoUrl, iframe)
        } finally {
            raf.close()
        }
    }
}








//package com.vs.schoolmessenger.Vimeo
//
//import android.util.Log
//import com.vs.schoolmessenger.Repository.APIKeyNames
//import com.vs.schoolmessenger.Repository.ApiInterfaces
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.OkHttpClient
//import okhttp3.RequestBody
//import okhttp3.ResponseBody
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Call
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.io.FileInputStream
//import java.io.IOException
//
//
//class VimeoUploaded : android.os.AsyncTask<java.lang.Void?, Int?, Boolean?>() {
//    private var title: String? = null
//    private var content: String? = null
//    private var accessToken: String? = null
//    private var videoFile: java.io.File? = null
//    private var listener: UploadProgressListener? = null
//    private var isUpload = false
//
//    interface UploadProgressListener {
//        fun onProgressUpdate(percentage: Int, uploading: Boolean)
//        fun onUploadComplete(videoId: String?)
//        fun onUploadFailed(error: String?)
//    }
//
//    fun VimeoVideoUploadTask(
//        title: String?,
//        content: String?,
//        accessToken: String,
//        videoFile: java.io.File,
//        listener: UploadProgressListener
//    ) {
//        this.title = title
//        this.content = content
//        this.accessToken = accessToken
//        this.videoFile = videoFile
//        this.listener = listener
//    }
//
//    override fun onProgressUpdate(vararg values: Int?) {
//        super.onProgressUpdate(*values)
//        if (values.size > 0) {
//            listener!!.onProgressUpdate(values[0]!!, isUpload)
//        }
//    }
//
//    override fun doInBackground(vararg voids: java.lang.Void?): Boolean {
//        val thread = java.lang.Thread(java.lang.Runnable {
//            try {
//                val client: OkHttpClient = okhttp3.OkHttpClient.Builder()
//                    .connectTimeout(600, java.util.concurrent.TimeUnit.SECONDS)
//                    .readTimeout(40, java.util.concurrent.TimeUnit.MINUTES)
//                    .writeTimeout(40, java.util.concurrent.TimeUnit.MINUTES)
//                    .addInterceptor(okhttp3.Interceptor { chain: okhttp3.Interceptor.Chain? ->
//                        val request = chain!!.request().newBuilder()
//                            .addHeader("Authorization", accessToken!!)
//                            .build()
//                        chain.proceed(request)
//                    })
//                    .addInterceptor(
//                        HttpLoggingInterceptor()
//                            .setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY)
//                    )
//                    .build()
//
//                val retrofit: Retrofit = Retrofit.Builder()
//                    .baseUrl("https://api.vimeo.com/")
//                    .client(client)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build()
//
//                val vimeoApiService: ApiInterfaces =
//                    retrofit.create<ApiInterfaces>(ApiInterfaces::class.java)
//
//                val `object` = com.google.gson.JsonObject()
//                `object`.addProperty(APIKeyNames.name, title)
//                `object`.addProperty(APIKeyNames.description, content)
//
//                val createVideoUpload = vimeoApiService.isCreateVideoUrl(`object`)
//                createVideoUpload.enqueue(object : retrofit2.Callback<com.google.gson.JsonObject?> {
//                    override fun onResponse(
//                        call: retrofit2.Call<com.google.gson.JsonObject?>?,
//                        response: retrofit2.Response<com.google.gson.JsonObject?>?
//                    ) {
//                        if (response!!.isSuccessful()) {
//                            try {
//                                val object1 = org.json.JSONObject(response.body().toString())
//                                val obj = object1.getJSONObject("upload")
//                                val obj1 = object1.getJSONObject("embed")
//                                val upload_link = obj.getString("upload_link")
//                                val link = object1.getString("link")
//                                val iframe = obj1.getString("html")
//
//                                val videoIframe = iframe
//                                val videoUrl = link
//                                Log.d("videoIframe", videoIframe.toString())
//                                Log.d("videoUrl", videoUrl.toString())
//                                VIDEOUPLOAD(upload_link, videoFile.toString())
//                                isUpload = true
//                            } catch (e: java.lang.Exception) {
//                                android.util.Log.e("Exception", e.message!!)
//                                isUpload = false
//                            }
//                        } else {
//                            isUpload = false
//                            android.util.Log.d("Response_", response.errorBody().toString())
//                        }
//                    }
//
//                    override fun onFailure(
//                        call: retrofit2.Call<com.google.gson.JsonObject?>?,
//                        t: Throwable?
//                    ) {
//                        android.util.Log.d("Failure", "Failure")
//                        isUpload = false
//                    }
//                })
//
//                for (i in 1..5) {
//                    publishProgress(i * 20)
//                    java.lang.Thread.sleep(1000)
//                }
//            } catch (e: java.lang.Exception) {
//                android.util.Log.d(
//                    "Exception",
//                    java.util.Objects.requireNonNull<String?>(e.message)
//                )
//                isUpload = false
//            }
//        })
//        thread.start()
//        return isUpload
//    }
//
//    @android.annotation.SuppressLint("LongLogTag")
//    private fun VIDEOUPLOAD(upload_link: String, file: String?) {
//        val thread = java.lang.Thread(java.lang.Runnable {
//            try {
//                val separated =
//                    upload_link.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                val name = separated[0]
//                val FileName = separated[1]
//                val upload = name.replace("upload", "")
//                val id = FileName.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                val ticket_id = id[0]
//                val video_file_id = id[1]
//                val signature = id[2]
//                val v6 = id[3]
//                val redirect_url = id[4]
//                val seperate1 =
//                    ticket_id.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                val ticket: String? = seperate1[0]
//                val ticket2: String? = seperate1[1]
//                val seperate2 =
//                    video_file_id.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                val ticket1: String? = seperate2[0]
//                val ticket3: String? = seperate2[1]
//                val seper =
//                    signature.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                val ticke: String? = seper[0]
//                val tick: String? = seper[1]
//                val sepera = v6.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                val str: String? = sepera[0]
//                val str1: String? = sepera[1]
//                val sucess =
//                    redirect_url.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                val urlRIDERCT: String? = sucess[0]
//                val redirect_url123: String? = sucess[1]
//
//                val client1: OkHttpClient = okhttp3.OkHttpClient.Builder()
//                    .connectTimeout(600, java.util.concurrent.TimeUnit.SECONDS)
//                    .readTimeout(40, java.util.concurrent.TimeUnit.MINUTES)
//                    .writeTimeout(40, java.util.concurrent.TimeUnit.MINUTES)
//                    .build()
//
//                val retrofit: Retrofit = Retrofit.Builder()
//                    .client(client1)
//                    .baseUrl(upload)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build()
//
//                val service: ApiInterfaces =
//                    retrofit.create<ApiInterfaces>(ApiInterfaces::class.java)
//                var requestFile: RequestBody? = null
//
//                try {
//                    val inputStream = FileInputStream(file)
//                    val buf = ByteArray(inputStream.available())
//                    inputStream.read(buf)
//                    inputStream.close()
//
//                    val mediaType = "application/octet-stream".toMediaTypeOrNull()
//                    requestFile = RequestBody.create(mediaType, buf)
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//
//
//                val call: Call<ResponseBody?> = service.patchVimeoVideoMetaData(
//                    ticket2,
//                    ticket3.toString(),
//                    tick,
//                    str1,
//                    redirect_url123 + "www.voicesnapforschools.com",
//                    requestFile
//                ) as Call<ResponseBody?>
//                call.enqueue(object : retrofit2.Callback<ResponseBody?> {
//                    override fun onResponse(
//                        call: retrofit2.Call<ResponseBody?>?,
//                        response: retrofit2.Response<ResponseBody?>?
//                    ) {
//                        try {
//                            if (response!!.isSuccessful()) {
//                                isUpload = true
//                            } else {
//                                isUpload = false
//                            }
//                        } catch (e: java.lang.Exception) {
//                            isUpload = false
//                        }
//                    }
//
//                    override fun onFailure(
//                        call: retrofit2.Call<ResponseBody?>?,
//                        t: Throwable?
//                    ) {
//                        isUpload = false
//                    }
//                })
//            } catch (e: java.lang.Exception) {
//                isUpload = false
//            }
//        })
//        thread.start()
//    }
//}
