package com.vs.schoolmessenger.AWS

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.vs.schoolmessenger.AWS.S3Uploader.UploadCallbackResponse
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Response
import java.io.File

class AwsUploadingPreSigned {
    var isBucket: String = ""

    fun getPreSignedUrl(
        isPickingFileExtension: String,
        isFilePathUrl: String,
        instituteID: String?,
        isFileType: String,
        activity: Activity,
        isCountryId: String,
        isCommunication: Boolean,
        isProfilePage: Boolean,
        uploadCallback: UploadCallback
    ) {

////        if (isCountryId == "4") {
////            if (isProfilePage) {
////                if (isCommunication) {
////                    isBucket = AWSKeys.THAI_SCHOOL_PHOTOS
////                    bucketPath = instituteID
////                } else {
////                    isBucket = AWSKeys.THAI_SCHOOL_DOCS
////                    bucketPath = instituteID + "/" + "profile"
////                }
////            } else {
////                if (isCommunication) {
////                    isBucket = AWSKeys.THAI_SCHOOL_CHIMES_COMMUNICATION
////                    bucketPath = currentDate + "/" + instituteID
////                } else {
////                    isBucket = AWSKeys.THAI_SCHOOL_CHIMES_LMS
////                    bucketPath = instituteID + "/" + "lsrw"
////                }
////            }
////        } else {
////            if (isProfilePage) {
////                if (isCommunication) {
////                    isBucket = AWSKeys.SCHOOL_PHOTOS
////                    bucketPath = instituteID
////                } else {
////                    isBucket = AWSKeys.SCHOOL_DOCS
////                    bucketPath = instituteID + "/" + "profile"
////                }
////            } else {
////                if (isCommunication) {
//                    isBucket = AWSKeys.SCHOOL_CHIMES_COMMUNICATION
//                    bucketPath = currentDate + "/" + instituteID
////                } else {
////                    isBucket = AWSKeys.SCHOOL_CHIMES_LMS
////                    bucketPath = instituteID + "/" + "lsrw"
////                }
//        // }
////        }


        var bucketPath: String? = ""
        val currentDate: String? = CurrentDatePicking.currentDate
        var fileExtension: String?

        isBucket = AWSKeys.SCHOOL_CHIMES_COMMUNICATION
        bucketPath = "$currentDate/$instituteID"

        Log.d("isBucket", isBucket)

        var mediaType: MediaType? = null
        fileExtension = if (Constant.isVoiceType == 2) {
            isPickingFileExtension
        } else {
            getFileExtension(File(isFilePathUrl).name)
        }

        try {
            mediaType = getMediaType(fileExtension)
            Log.d("MediaType", mediaType.toString())
        } catch (e: UnsupportedOperationException) {
            Log.e("MediaTypeError", e.message.toString())
        }

        val baseURL = "https://api.schoolchimes.com/nodejs/api/MergedApi/"
        RestClient.changeApiBaseUrl(baseURL)
        val apiService = RestClient.apiInterfaces

        val isFileName = getFileNameFromPath(isFilePathUrl)

        val call = apiService.getPreSignedUrl(isBucket, isFileName, bucketPath, mediaType.toString())
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
                            isPickingFileExtension,
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

    fun getFileNameFromPath(filePath: String): String {
        return if (filePath.startsWith("content://")) {
            "audiorecord.m4a"
        } else {
            File(filePath).name
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun isAwsUpload(
        isPickingFileExtension: String,
        activity: Activity,
        presignedUrl: String?,
        filePath: String,
        isFileUploadUrl: String?,
        uploadCallback: UploadCallback
    ) {
        val imageData = getImageData(filePath, activity)
        val fileExtension = if (Constant.isVoiceType == 2) {
            isPickingFileExtension
        } else {
            getFileExtension(File(filePath).name)
        }

        var mediaType: MediaType? = null
        try {
            mediaType = getMediaType(fileExtension)
        } catch (e: UnsupportedOperationException) {
            Log.e("MediaTypeError", e.message.toString())
        }

        val uploader = S3Uploader()
        uploader.uploadImageToS3(presignedUrl, imageData, mediaType.toString(), object : UploadCallbackResponse {
            override fun onSuccess(message: String?) {
                Log.d("S3Upload", message ?: "Upload success")
                uploadCallback.onUploadSuccess(message, isFileUploadUrl)
            }

            override fun onError(error: Exception?) {
                Log.e("UploadError", error.toString())
                uploadCallback.onUploadError(error?.message)
            }
        })
    }

    private fun getFileExtension(fileName: String): String {
        val lastIndexOfDot = fileName.lastIndexOf('.')
        return if (lastIndexOfDot > 0 && lastIndexOfDot < fileName.length - 1) {
            fileName.substring(lastIndexOfDot + 1).lowercase()
        } else {
            ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getImageData(filePath: String, activity: Activity): ByteArray? {
        return try {
            if (filePath.startsWith("content://")) {
                val uri = Uri.parse(filePath)
                val inputStream = activity.contentResolver.openInputStream(uri)
                inputStream?.readBytes()
            } else {
                val file = File(filePath)
                java.nio.file.Files.readAllBytes(file.toPath())
            }
        } catch (e: Exception) {
            Log.e("FileReadError", "Error reading file data: ${e.message}")
            null
        }
    }

    fun getMediaType(fileExtension: String): MediaType? {
        return when (fileExtension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg".toMediaTypeOrNull()
            "png" -> "image/png".toMediaTypeOrNull()
            "pdf" -> "application/pdf".toMediaTypeOrNull()
            "mp3" -> "audio/mpeg".toMediaTypeOrNull()
            "wav" -> "audio/wav".toMediaTypeOrNull()
            "3gp" -> "audio/3gpp".toMediaTypeOrNull()
            "m4a" -> "audio/mp4".toMediaTypeOrNull()
            else -> throw UnsupportedOperationException("Unsupported file type: $fileExtension")
        }
    }
}





//package com.vs.schoolmessenger.AWS
//
//import android.app.Activity
//import android.os.Build
//import android.util.Log
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import com.vs.schoolmessenger.AWS.S3Uploader.UploadCallbackResponse
//import com.vs.schoolmessenger.Repository.RestClient
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.SharedPreference
//import okhttp3.MediaType
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import retrofit2.Call
//import retrofit2.Response
//import java.io.File
//
//class AwsUploadingPreSigned {
//    var isBucket: String = ""
//
//    fun getPreSignedUrl(
//        isPickingFileExtension: String,
//        isFilePathUrl: String,
//        instituteID: String?,
//        isFileType: String,
//        activity: Activity,
//        isCountryId: String,
//        isCommunication: Boolean,
//        isProfilePage: Boolean,
//        uploadCallback: UploadCallback
//    ) {
//        var bucketPath: String? = ""
//        val currentDate: String? = CurrentDatePicking.currentDate
//        var fileExtension: String? = null
//
////        if (isCountryId == "4") {
////            if (isProfilePage) {
////                if (isCommunication) {
////                    isBucket = AWSKeys.THAI_SCHOOL_PHOTOS
////                    bucketPath = instituteID
////                } else {
////                    isBucket = AWSKeys.THAI_SCHOOL_DOCS
////                    bucketPath = instituteID + "/" + "profile"
////                }
////            } else {
////                if (isCommunication) {
////                    isBucket = AWSKeys.THAI_SCHOOL_CHIMES_COMMUNICATION
////                    bucketPath = currentDate + "/" + instituteID
////                } else {
////                    isBucket = AWSKeys.THAI_SCHOOL_CHIMES_LMS
////                    bucketPath = instituteID + "/" + "lsrw"
////                }
////            }
////        } else {
////            if (isProfilePage) {
////                if (isCommunication) {
////                    isBucket = AWSKeys.SCHOOL_PHOTOS
////                    bucketPath = instituteID
////                } else {
////                    isBucket = AWSKeys.SCHOOL_DOCS
////                    bucketPath = instituteID + "/" + "profile"
////                }
////            } else {
////                if (isCommunication) {
//                    isBucket = AWSKeys.SCHOOL_CHIMES_COMMUNICATION
//                    bucketPath = currentDate + "/" + instituteID
////                } else {
////                    isBucket = AWSKeys.SCHOOL_CHIMES_LMS
////                    bucketPath = instituteID + "/" + "lsrw"
////                }
//        // }
////        }
//
//        Log.d("isBucket", isBucket)
////        Log.d("isFileExtension", isFileExtension)
//        val isFilePth = java.io.File(isFilePathUrl)
//        var mediaType: MediaType? = null
//        if (Constant.isVoiceType == 2) {
//            fileExtension = isPickingFileExtension
//        } else {
//            fileExtension = getFileExtension(isFilePth.name)
//        }
//
//        try {
//            mediaType = getMediaType(fileExtension)
//            println("MediaType: " + mediaType)
//        } catch (e: java.lang.UnsupportedOperationException) {
//            Log.d("isLoadException", e.message.toString())
//        }
//        val baseURL = "https://api.schoolchimes.com/nodejs/api/MergedApi/"
//        RestClient.changeApiBaseUrl(baseURL)
//
//        val apiService = RestClient.apiInterfaces
//        val isFileName = getFileNameFromPath(isFilePathUrl)
//
//        val call = apiService.getPreSignedUrl(
//            isBucket, isFileName, bucketPath, mediaType.toString()
//        )
//        call!!.enqueue(object : retrofit2.Callback<PreSignedUrl?> {
//
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onResponse(
//                call: Call<PreSignedUrl?>, response: Response<PreSignedUrl?>
//            ) {
//                Log.d(
//                    "UploadFile:code-res", response.code().toString() + " - " + response
//                )
//
//                if (response.isSuccessful && response.body() != null) {
//                    val preSignedUrlResponse: PreSignedUrl? = response.body()
//                    Log.d(
//                        "PreSignedData", com.google.gson.Gson().toJson(preSignedUrlResponse)
//                    )
//
//                    if (preSignedUrlResponse!!.status === 1) {
//                        Log.d("isSuccessFullUpload", "isSuccessFullUpload")
//                        val presignedUrl: String =
//                            preSignedUrlResponse!!.data!!.presignedUrl.toString()
//                        val isFileUrl: String? = preSignedUrlResponse.data!!.fileUrl
//                        Log.d("presignedUrl", presignedUrl)
//
//                        // Upload the file and get the upload response
//                        isAwsUpload(
//                            isPickingFileExtension,
//                            activity,
//                            presignedUrl,
//                            isFilePathUrl,
//                            isFileUrl,
//                            uploadCallback
//                        )
//                    } else {
//                        Log.d(
//                            "isSuccessFullUpload",
//                            "isErrorUpload: " + preSignedUrlResponse!!.message
//                        )
//                        var isBaseUrl = SharedPreference.getBaseUrl(activity)
//                        Log.d("isBaseUrl",isBaseUrl.toString())
//                        RestClient.changeApiBaseUrl(isBaseUrl.toString())
//                        uploadCallback.onUploadError(preSignedUrlResponse.message)
//                    }
//                } else {
//                    Toast.makeText(
//                        activity, "Check InterNet", Toast.LENGTH_SHORT
//                    ).show()
//                    val errorMessage = response.message() // Get the error message from the response
//                    Log.e(
//                        "Response Error",
//                        if (errorMessage != null) errorMessage else "Unknown error occurred"
//                    )
//
//                    var isBaseUrl = SharedPreference.getBaseUrl(activity)
//                    Log.d("isBaseUrl",isBaseUrl.toString())
//                    RestClient.changeApiBaseUrl(isBaseUrl.toString())
//                    uploadCallback.onUploadError(errorMessage)
//                }
//            }
//
//            override fun onFailure(call: Call<PreSignedUrl?>?, t: Throwable?) {
//                Log.e("Response Failure", t!!.message!!)
//                Toast.makeText(
//                    activity, "Check InterNet", Toast.LENGTH_SHORT
//                ).show()
//                uploadCallback.onUploadError(t.message)
//            }
//        })
//    }
//
//    fun getFileNameFromPath(filePath: String): String {
//        val file = File(filePath)
//        return file.name
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun isAwsUpload(
//        isPickingFileExtension: String,
//        activity: Activity,
//        presignedUrl: String?,
//        filePath: String,
//        isFileUploadUrl: String?,
//        uploadCallback: UploadCallback
//    ) {
//        val imageData = getImageData(filePath)
//
//        val isFilePth = java.io.File(filePath)
//
//        var fileExtension: String? = null
//
//        if (Constant.isVoiceType == 2) {
//            fileExtension = isPickingFileExtension
//        } else {
//            fileExtension = getFileExtension(isFilePth.name)
//        }
//
//
//        var mediaType: MediaType? = null
//
//
//        try {
//            mediaType = getMediaType(fileExtension)
//            println("MediaType++: " + mediaType)
//        } catch (e: java.lang.UnsupportedOperationException) {
//            Log.d("isException", e.message.toString())
//        }
//
//        val uploader = S3Uploader()
//        uploader.uploadImageToS3(
//            presignedUrl, imageData, mediaType.toString(), object : UploadCallbackResponse {
//
//                override fun onSuccess(message: String?) {
//                    Log.d("S3Upload", message!!)
//                    uploadCallback.onUploadSuccess(message, isFileUploadUrl)
//                }
//
//                override fun onError(error: Exception?) {
//                    Log.d("isErrorException", error.toString())
//                }
//            })
//    }
//
//    private fun getFileExtension(fileName: String): String {
//        val lastIndexOfDot = fileName.lastIndexOf('.')
//        if (lastIndexOfDot > 0 && lastIndexOfDot < fileName.length - 1) {
//            return fileName.substring(lastIndexOfDot + 1).lowercase(java.util.Locale.getDefault())
//        }
//        return "" // Return empty string if no extension found
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun getImageData(filePath: String): ByteArray? {
//        val imageFile = File(filePath)
//        var imageData: ByteArray? = null
//        try {
//            imageData = java.nio.file.Files.readAllBytes(imageFile.toPath())
//        } catch (e: java.io.IOException) {
//            e.printStackTrace()
//        }
//        return imageData
//    }
//
//    fun getMediaType(fileExtension: String): MediaType? {
//        return when (fileExtension.lowercase(java.util.Locale.getDefault())) {
//            "jpg", "jpeg" -> "image/jpeg".toMediaTypeOrNull()
//            "png" -> "image/png".toMediaTypeOrNull()
//            "pdf" -> "application/pdf".toMediaTypeOrNull()
//            "mp3" -> "audio/mpeg".toMediaTypeOrNull()
//            "wav" -> "audio/wav".toMediaTypeOrNull()
//            "3gp" -> "audio/3gpp".toMediaTypeOrNull()
//            "m4a" -> "audio/mp4".toMediaTypeOrNull()
//            else -> throw UnsupportedOperationException("Unsupported file type: $fileExtension")
//        }
//    }
//
//}
