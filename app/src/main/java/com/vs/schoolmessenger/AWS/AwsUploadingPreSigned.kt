package com.vs.schoolmessenger.AWS

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.vs.schoolmessenger.AWS.S3Uploader.UploadCallbackResponse
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.Utils.SharedPreference
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Response
import java.io.File

class AwsUploadingPreSigned {
    var isBucket: String = ""

    fun getPreSignedUrl(
        isFilePathUrl: String,
        instituteID: String?,
        isFileType: String,
        activity: Activity,
        isCountryId: Int,
        isCommunication: Boolean,
        isProfilePage: Boolean,
        uploadCallback: UploadCallback
    ) {

//        if (isCountryId == "4") {
//            if (isProfilePage) {
//                if (isCommunication) {
//                    isBucket = AWSKeys.THAI_SCHOOL_PHOTOS
//                    bucketPath = instituteID
//                } else {
//                    isBucket = AWSKeys.THAI_SCHOOL_DOCS
//                    bucketPath = instituteID + "/" + "profile"
//                }
//            } else {
//                if (isCommunication) {
//                    isBucket = AWSKeys.THAI_SCHOOL_CHIMES_COMMUNICATION
//                    bucketPath = currentDate + "/" + instituteID
//                } else {
//                    isBucket = AWSKeys.THAI_SCHOOL_CHIMES_LMS
//                    bucketPath = instituteID + "/" + "lsrw"
//                }
//            }
//        } else {
//            if (isProfilePage) {
//                if (isCommunication) {
//                    isBucket = AWSKeys.SCHOOL_PHOTOS
//                    bucketPath = instituteID
//                } else {
//                    isBucket = AWSKeys.SCHOOL_DOCS
//                    bucketPath = instituteID + "/" + "profile"
//                }
//            } else {
//                if (isCommunication) {
//                    isBucket = AWSKeys.SCHOOL_CHIMES_COMMUNICATION
//                    bucketPath = currentDate + "/" + instituteID
//                } else {
//                    isBucket = AWSKeys.SCHOOL_CHIMES_LMS
//                    bucketPath = instituteID + "/" + "lsrw"
//                }
        // }
//        }


        var bucketPath: String? = ""
        val currentDate: String? = CurrentDatePicking.currentDate
        var fileExtension: String?

        isBucket = AWSKeys.SCHOOL_CHIMES_COMMUNICATION
        val isFolderName = "communication"
        bucketPath = "$isFolderName/$instituteID/$currentDate"

        Log.d("isBucket", isBucket)

        var mediaType: MediaType? = null
        fileExtension = getFileExtensionFromUri(activity, isFilePathUrl.toUri())
        try {
            mediaType = getMediaType(fileExtension)
            Log.d("MediaType", mediaType.toString())
        } catch (e: UnsupportedOperationException) {
            Log.e("MediaTypeError", e.message.toString())
        }

        val baseURL = "https://api.schoolchimes.com/nodejs/api/MergedApi/"
        RestClient.changeApiBaseUrl(baseURL)
        val apiService = RestClient.apiInterfaces

        val isFileName = getFileNameFromPath(activity, isFilePathUrl)
        Log.d("isFileName", isFileName)
        val call =
            apiService.getPreSignedUrl(isBucket, isFileName, bucketPath, mediaType.toString())
        call!!.enqueue(object : retrofit2.Callback<PreSignedUrl?> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<PreSignedUrl?>, response: Response<PreSignedUrl?>) {
                Log.d("UploadFile:code-res", response.code().toString() + " - " + response)

                if (response.isSuccessful && response.body() != null) {
                    val preSignedUrlResponse = response.body()
                    if (preSignedUrlResponse!!.status == 1) {
                        val presignedUrl = preSignedUrlResponse.data!!.presignedUrl.toString()
                        val isFileUrl = preSignedUrlResponse.data!!.fileUrl
                        Log.d("presignedUrl", presignedUrl)

                        // Upload the file
                        isAwsUpload(
                            activity,
                            presignedUrl,
                            isFilePathUrl,
                            isFileUrl,
                            uploadCallback
                        )
                    } else {
                        val isBaseUrl = SharedPreference.getBaseUrl(activity)
                        RestClient.changeApiBaseUrl(isBaseUrl.toString())
                        uploadCallback.onUploadError(preSignedUrlResponse.message)
                    }
                } else {
                    Toast.makeText(activity, "Check InterNet", Toast.LENGTH_SHORT).show()
                    val errorMessage = response.message() ?: "Unknown error"
                    Log.e("Response Error", errorMessage)
                    val isBaseUrl = SharedPreference.getBaseUrl(activity)
                    RestClient.changeApiBaseUrl(isBaseUrl.toString())
                    uploadCallback.onUploadError(errorMessage)
                }
            }

            override fun onFailure(call: Call<PreSignedUrl?>?, t: Throwable?) {
                Log.e("Response Failure", t?.message ?: "Unknown error")
                Toast.makeText(activity, "Check InterNet", Toast.LENGTH_SHORT).show()
                uploadCallback.onUploadError(t?.message)
            }
        })
    }

    fun getFileNameFromPath(context: Context, filePath: String): String {
        return if (filePath.startsWith("content://")) {
            try {
                val uri = Uri.parse(filePath)
                var result: String? = null
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            result = it.getString(nameIndex)
                        }
                    }
                }
                result ?: "unknown_file"
            } catch (e: Exception) {
                Log.e("FileNameError", "Error getting name: ${e.message}")
                "unknown_file"
            }
        } else {
            File(filePath).name
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun isAwsUpload(
        activity: Activity,
        presignedUrl: String?,
        filePath: String,
        isFileUploadUrl: String?,
        uploadCallback: UploadCallback
    ) {

        val imageData = getImageData(activity, filePath)
        val fileExtension = getFileExtensionFromUri(activity, filePath.toUri())
        var mediaType: MediaType? = null
        try {
            mediaType = getMediaType(fileExtension)
        } catch (e: UnsupportedOperationException) {
            Log.e("MediaTypeError", e.message.toString())
        }

        val uploader = S3Uploader()
        uploader.uploadImageToS3(
            presignedUrl,
            imageData,
            mediaType.toString(),
            object : UploadCallbackResponse {
                override fun onSuccess(message: String?) {
                    Log.d("S3Upload", message ?: "Upload success")
                    uploadCallback.onUploadSuccess(message, isFileUploadUrl)
                }

                override fun onError(error: Exception?) {
                    Log.e("UploadError", error!!.message.toString())
                    uploadCallback.onUploadError(error.message)
                }
            })
    }

    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown_file"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getImageData(context: Context, path: String): ByteArray? {
        return try {
            if (path.startsWith("content://")) {
                val uri = Uri.parse(path)
                context.contentResolver.openInputStream(uri)?.readBytes()
            } else {
                val file = File(path)
                java.nio.file.Files.readAllBytes(file.toPath())
            }
        } catch (e: Exception) {
            Log.e("FileReadError", "Error reading file data: ${e.message}")
            null
        }
    }


    fun getFileExtensionFromUri(context: Context, uri: Uri): String {
        val fileName = getFileName(context, uri)
        return fileName.substringAfterLast('.', "").lowercase()
    }

    fun getMediaType(fileExtension: String): MediaType? {
        return when (fileExtension.lowercase()) {
            // Images
            "jpg", "jpeg" -> "image/jpeg".toMediaTypeOrNull()
            "png" -> "image/png".toMediaTypeOrNull()
            "bmp" -> "image/bmp".toMediaTypeOrNull()
            "webp" -> "image/webp".toMediaTypeOrNull()

            // Audio
            "mp3" -> "audio/mpeg".toMediaTypeOrNull()
            "wav" -> "audio/wav".toMediaTypeOrNull()
            "3gp" -> "audio/3gpp".toMediaTypeOrNull()
            "m4a" -> "audio/mp4".toMediaTypeOrNull()

            // Documents
            "pdf" -> "application/pdf".toMediaTypeOrNull()
            "doc" -> "application/msword".toMediaTypeOrNull()
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaTypeOrNull()
            "ppt" -> "application/vnd.ms-powerpoint".toMediaTypeOrNull()
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation".toMediaTypeOrNull()
            "xls" -> "application/vnd.ms-excel".toMediaTypeOrNull()
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaTypeOrNull()
            "txt" -> "text/plain".toMediaTypeOrNull()

            else -> {
                Log.w("MediaTypeFallback", "Unknown type: $fileExtension, using fallback.")
                "application/octet-stream".toMediaTypeOrNull()
            }
        }
    }
}