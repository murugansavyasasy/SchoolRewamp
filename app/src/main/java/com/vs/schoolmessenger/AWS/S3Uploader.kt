package com.vs.schoolmessenger.AWS

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

class S3Uploader {
    interface UploadCallbackResponse {
        fun onSuccess(message: String?)
        fun onError(error: Exception?)
    }

    fun uploadImageToS3(
        presignedUrl: String?,
        imageData: ByteArray?,
        contentType: String,
        callback: UploadCallbackResponse
    ) {
        Thread(Runnable {
            try {
                Log.d("contentTypeeee", contentType)
                val url = URL(presignedUrl)

                // Open a connection
                val connection = url.openConnection() as HttpURLConnection
                connection.setDoOutput(true)
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", contentType)

                // Write the image data to the output stream
                val outputStream = connection.getOutputStream()
                outputStream.write(imageData)
                outputStream.close()

                // Check the response code
                val responseCode = connection.getResponseCode()
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    callback.onSuccess("File uploaded successfully!")
                } else {
                    callback.onError(Exception("Upload failed with status code: " + responseCode))
                }

                connection.disconnect()
            } catch (e: Exception) {
                callback.onError(e)
            }
        }).start()
    }
}
