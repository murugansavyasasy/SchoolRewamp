//package com.vs.schoolmessenger.AWS
//
//
//class AwsUploadingPreSigned {
//    var isBucket: kotlin.String = ""
//
//    fun getPreSignedUrl(
//        isFilePathUrl: kotlin.String,
//        instituteID: kotlin.String?,
//        uploadCallback: com.vs.schoolmessenger.AWS.UploadCallback
//    ) {
//        var bucketPath = ""
//        val currentDate = CurrentDatePicking.getCurrentDate()
//
//        isBucket = AWSKeys.BUCKET_NAME
//        bucketPath = currentDate + "/" + instituteID
//
//        android.util.Log.d("isBucket", isBucket)
//        val isFilePth = java.io.File(isFilePathUrl)
//        android.util.Log.d("isFilePth.getName()", isFilePth.getName().toString())
//        val fileExtension = getFileExtension(isFilePth.getName())
//        var mediaType: okhttp3.MediaType? = null
//
//        try {
//            mediaType = getMediaType(fileExtension)
//            kotlin.io.println("MediaType: " + mediaType)
//        } catch (e: java.lang.UnsupportedOperationException) {
//            java.lang.System.err.println(e.message)
//        }
//
//        val parts: kotlin.Array<kotlin.String?> =
//            mediaType.toString().split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        var isFileType: kotlin.String? = ""
//        if (parts.size == 2) {
//            val type = parts[0] // "image"
//            val subtype = parts[1] // "jpeg"
//            isFileType = type
//        }
//
//
//        val baseUrl: kotlin.String = RestClient.Companion.getBaseUrl()
//        android.util.Log.d("baseUrl", baseUrl.toString())
//        RestClient.Companion.changeApiBaseUrl("https://api.schoolchimes.com/nodejs/api/MergedApi/")
//
//        val isFileName = getFileNameFromPath(isFilePathUrl)
//
//        val retrofit: Retrofit = RestClient.Companion.getClient()
//        val retrofitBaseUrl = retrofit.baseUrl().toString()
//        android.util.Log.d("RetrofitBaseURL", "Base URL from Retrofit: " + retrofitBaseUrl)
//
//        val apiService: ApiInterfaces =
//            RestClient.Companion.getClient().create(ApiInterfaces::class.java)
//        val call: retrofit2.Call<com.google.gson.JsonArray?> =
//            apiService.getPreSignedUrl(isBucket, isFileName, bucketPath, isFileType.toString())
//
//        call.enqueue(object : retrofit2.Callback<com.google.gson.JsonArray?> {
//            override fun onResponse(
//                call: retrofit2.Call<com.google.gson.JsonArray?>?,
//                response: retrofit2.Response<com.google.gson.JsonArray?>?
//            ) {
//                android.util.Log.d(
//                    "attendance:code-res",
//                    response!!.code().toString() + " - " + response
//                )
//                try {
//                    val jsonArray = org.json.JSONArray(response.body().toString())
//                    for (i in 0..<jsonArray.length()) {
//                        val jsonObject = jsonArray.getJSONObject(i)
//                        val status = jsonObject.getInt("status")
//                        val message = jsonObject.getString("message")
//                        val ispresignedurl = jsonObject.getString("presignedurl")
//                        val isfileurl = jsonObject.getString("fileurl")
//                        isAwsUpload(ispresignedurl, isFilePathUrl, isfileurl, uploadCallback)
//                    }
//                } catch (e: java.lang.Exception) {
//                    val errorMessage =
//                        response.message() // Get the error message from the response
//                    android.util.Log.e(
//                        "Response Error",
//                        if (errorMessage != null) errorMessage else "Unknown error occurred"
//                    )
//                    uploadCallback.onUploadError(errorMessage)
//                }
//            }
//
//            override fun onFailure(
//                call: retrofit2.Call<com.google.gson.JsonArray?>?,
//                t: kotlin.Throwable?
//            ) {
//                android.util.Log.e("Response Failure", t!!.message!!)
//                //                Toast.makeText(activity, activity.getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
//                uploadCallback.onUploadError(t.message)
//            }
//        })
//    }
//
//    fun getFileNameFromPath(filePath: kotlin.String): kotlin.String {
//        val file = java.io.File(filePath)
//        return file.getName()
//    }
//
//    private fun isAwsUpload(
//        presignedUrl: kotlin.String?,
//        filePath: kotlin.String,
//        isFileUploadUrl: kotlin.String?,
//        uploadCallback: com.vs.schoolmessenger.AWS.UploadCallback
//    ) {
//        val imageData = getImageData(filePath) // Replace with the actual byte array of your image
//        val isFilePth = java.io.File(filePath)
//        val fileExtension = getFileExtension(isFilePth.getName())
//        var mediaType: okhttp3.MediaType? = null
//        try {
//            mediaType = getMediaType(fileExtension)
//            kotlin.io.println("MediaType: " + mediaType)
//        } catch (e: java.lang.UnsupportedOperationException) {
//            java.lang.System.err.println(e.message)
//        }
//
//        //        String[] parts = String.valueOf(mediaType).split("/");
////        String isFileType = "";
////        if (parts.length == 2) {
////            String type = parts[0];   // "image"
////            String subtype = parts[1]; // "jpeg"
////            isFileType = type;
////        }
//        val uploader = S3Uploader()
//        uploader.uploadImageToS3(
//            presignedUrl,
//            imageData,
//            mediaType.toString(),
//            object : S3Uploader.UploadCallback {
//                override fun onSuccess(message: kotlin.String) {
//                    android.util.Log.d("S3Upload", message)
//                    uploadCallback.onUploadSuccess(message, isFileUploadUrl)
//                }
//
//                override fun onError(error: java.lang.Exception) {
//                    android.util.Log.e("S3Upload", "Error: " + error.message, error)
//                }
//            })
//    }
//
//    private fun getFileExtension(fileName: kotlin.String): kotlin.String {
//        val lastIndexOfDot = fileName.lastIndexOf('.')
//        if (lastIndexOfDot > 0 && lastIndexOfDot < fileName.length - 1) {
//            return fileName.substring(lastIndexOfDot + 1).lowercase(java.util.Locale.getDefault())
//        }
//        return "" // Return empty string if no extension found
//    }
//
//    private fun getImageData(filePath: kotlin.String): kotlin.ByteArray? {
//        val imageFile = java.io.File(filePath)
//        var imageData: kotlin.ByteArray? = null
//        try {
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                imageData = java.nio.file.Files.readAllBytes(imageFile.toPath())
//            }
//        } catch (e: java.io.IOException) {
//            e.printStackTrace()
//        }
//        return imageData
//    }
//
//    fun getMediaType(fileExtension: kotlin.String): okhttp3.MediaType? {
//        when (fileExtension.lowercase(java.util.Locale.getDefault())) {
//            "jpg", "jpeg" -> return parse.parse("image/jpeg")
//
//            "png" -> return parse.parse("image/png")
//
//            "pdf" -> return parse.parse("application/pdf")
//            "mp3" -> return parse.parse("audio/mpeg")
//
//            "wav" -> return parse.parse("audio/wav")
//            else -> throw java.lang.UnsupportedOperationException("Unsupported file type: " + fileExtension)
//        }
//    }
//}
