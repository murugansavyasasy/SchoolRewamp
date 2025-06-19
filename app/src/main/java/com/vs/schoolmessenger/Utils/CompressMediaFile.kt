//package com.vs.schoolmessenger.Utils
//
//import android.content.Context
//import android.net.Uri
//import android.os.Environment
//import android.util.Log
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.io.File
//
//class CompressMediaFile {
//
//    suspend fun compressVideoAndSave(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
//        val realPath = getRealPathFromURI(context, uri) ?: return@withContext null
//        val inputFile = File(realPath)
//
//        // Step 1: Create or clean custom folder
//        val outputDir = File(Environment.getExternalStorageDirectory(), "SchoolChimes/CompressedMedia")
//        if (!outputDir.exists()) outputDir.mkdirs() else outputDir.listFiles()?.forEach { it.delete() }
//
//        // Step 2: Output file
//        val outputFile = File(outputDir, "compressed_${System.currentTimeMillis()}.mp4")
//
//        // Step 3: FFmpeg command
//        val command = "-y -i ${inputFile.absolutePath} -vcodec libx264 -crf 28 -preset fast ${outputFile.absolutePath}"
//
//        val session = FFmpegKit.execute(command)
//        return@withContext if (session.returnCode.isValueSuccess) {
//            Log.d("VideoCompress", "Success: ${outputFile.absolutePath}")
//            outputFile
//        } else {
//            Log.e("VideoCompress", "Failed: ${session.failStackTrace}")
//            null
//        }
//    }
//
//    fun getRealPathFromURI(context: Context, uri: Uri): String? {
//        val projection = arrayOf(android.provider.MediaStore.Video.Media.DATA)
//        val cursor = context.contentResolver.query(uri, projection, null, null, null)
//        cursor?.use {
//            val columnIndex = it.getColumnIndexOrThrow(android.provider.MediaStore.Video.Media.DATA)
//            it.moveToFirst()
//            return it.getString(columnIndex)
//        }
//        return null
//    }
//}