package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.vs.schoolmessenger.AWS.UploadCallback
import java.io.File

object AwsFileUploader {

    interface AwsUploadResultCallback {
        fun onAllFilesUploaded(uploadedFiles: List<AwsUploadedFiles>)
        fun onUploadFailed(error: String)
    }

    fun uploadFilesToAws(
        context: Context,
        selectedFiles: MutableList<FileItem>,
        schoolId: String,
        isFileType: String,
        selectedSchoolMenu: Int,
        getPresignedUrlCallback: (filePath: String, callback: UploadCallback) -> Unit,
        resultCallback: AwsUploadResultCallback
    ) {
        // Clear any previously recorded uploaded files.
        Constant.isAwsUploadedFiles.clear()

        // Show the progress dialog.
        ProgressDialogHelper.show(context)

        // Create an output directory for compressed images.
        val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput")
        outputDir.mkdirs()

        // Prepare lists and counters.
        val newSelectedFiles = mutableListOf<FileItem>()
        var uploadedFiles = 0
        val isSelectedFileListSize = selectedFiles.size

        // Compress the images (or files) provided.
        Constant.compressImageFilesOnly(
            context = context,
            files = selectedFiles,
            outputDir = outputDir.absolutePath,
            format = Bitmap.CompressFormat.JPEG,
            quality = 80,
            maxWidth = 1280,
            maxHeight = 1280,
            onEachProcessed = { original, outputPath, success ->
                if (success && outputPath != null) {
                    val compressedFile = File(outputPath)
                    val originalSizeKB = try {
                        if (original.path.startsWith("content://")) {
                            context.contentResolver.openFileDescriptor(Uri.parse(original.path), "r")?.statSize ?: 0
                        } else {
                            File(original.path).length()
                        }
                    } catch (e: Exception) {
                        0L
                    }

                    Log.d(
                        "Compressor",
                        "Compressed: $outputPath (${compressedFile.length() / 1024}KB), Original: ${originalSizeKB / 1024}KB"
                    )

                    newSelectedFiles.add(FileItem(path = outputPath, type = original.type))
                } else {
                    Log.e("Compressor", "Failed: ${original.path}")
                }
            },
            onComplete = {
                // Replace original list with compressed files.
                selectedFiles.clear()
                selectedFiles.addAll(newSelectedFiles)

                // Upload each file one by one.
                for (i in selectedFiles.indices) {
                    getPresignedUrlCallback.invoke(selectedFiles[i].path,
                        object : UploadCallback {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onUploadSuccess(response: String?, isFileUploaded: String?) {
                                uploadedFiles++
                                val percent = (uploadedFiles * 100) / isSelectedFileListSize
                                ProgressDialogHelper.updateProgress(percent)

                                Constant.isAwsUploadedFiles.add(
                                    AwsUploadedFiles(
                                        isFileUrl = isFileUploaded ?: "",
                                        isFileType = selectedFiles[i].type.name
                                    )
                                )

                                // When all files are processed, dismiss progress and return result.
                                if (uploadedFiles == isSelectedFileListSize) {
                                    ProgressDialogHelper.dismiss()
                                    resultCallback.onAllFilesUploaded(Constant.isAwsUploadedFiles)
                                }
                            }

                            override fun onUploadError(error: String?) {
                                uploadedFiles++
                                val percent = (uploadedFiles * 100) / isSelectedFileListSize
                                ProgressDialogHelper.updateProgress(percent)

                                if (uploadedFiles == isSelectedFileListSize) {
                                    ProgressDialogHelper.dismiss()
                                    resultCallback.onUploadFailed(error ?: "Unknown error")
                                }
                            }
                        })
                }
            }
        )
    }
}



//package com.vs.schoolmessenger.Utils
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Build
//import android.os.Environment
//import android.util.Log
//import androidx.annotation.RequiresApi
//import com.vs.schoolmessenger.AWS.UploadCallback
//import java.io.File
//
//object AwsFileUploader {
//
//    interface AwsUploadResultCallback {
//        fun onAllFilesUploaded(uploadedFiles: List<AwsUploadedFiles>)
//        fun onUploadFailed(error: String)
//    }
//
//    fun uploadFilesToAws(
//        context: Context,
//        selectedFiles: MutableList<FileItem>,
//        schoolId: String,
//        isFileType: String,
//        selectedSchoolMenu: Int,
//        getPresignedUrlCallback: (filePath: String, callback: UploadCallback) -> Unit,
//        resultCallback: AwsUploadResultCallback
//    ) {
////        // Remove plus icon
////        if (selectedSchoolMenu == Constant.M_ATTACHMENTS || selectedSchoolMenu == Constant.M_HOMEWORK ||
////            selectedSchoolMenu == Constant.M_SCHOOL_CLASS_EVENTS || selectedSchoolMenu == Constant.M_ASSIGNMENT
////        ) {
////            if (selectedFiles.isNotEmpty()) selectedFiles.removeAt(0)
////        }
//
//        Constant.isAwsUploadedFiles.clear()
//        val isSelectedFileListSize = selectedFiles.size
//     //   val iterator = selectedFiles.iterator()
//
//       // ProgressDialogHelper.show(context)
//
////        while (iterator.hasNext()) {
////            val fileItem = iterator.next()
////            if (fileItem.path.contains("amazonaws.")) {
////                Constant.isAwsUploadedFiles.add(
////                    AwsUploadedFiles(
////                        isFileUrl = fileItem.path,
////                        isFileType = fileItem.type.name
////                    )
////                )
////                iterator.remove()
////            }
////        }
//        val outputDir =
//            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput")
//        outputDir.mkdirs()
//        val newSelectedFiles = mutableListOf<FileItem>()
//        var uploadedFiles = 0
//
//        Constant.compressImageFilesOnly(
//            context = context,
//            files = selectedFiles,
//            outputDir = outputDir.absolutePath,
//            format = Bitmap.CompressFormat.JPEG,
//            quality = 80,
//            maxWidth = 1280,
//            maxHeight = 1280,
//            onEachProcessed = { original, outputPath, success ->
//                if (success && outputPath != null) {
//                    val compressedFile = File(outputPath)
//                    val originalSizeKB = try {
//                        if (original.path.startsWith("content://")) {
//                            context.contentResolver.openFileDescriptor(
//                                Uri.parse(original.path), "r"
//                            )?.statSize ?: 0
//                        } else {
//                            File(original.path).length()
//                        }
//                    } catch (e: Exception) {
//                        0L
//                    }
//
//                    Log.d(
//                        "Compressor",
//                        "Compressed: $outputPath (${compressedFile.length() / 1024}KB), Original: ${originalSizeKB / 1024}KB"
//                    )
//
//                    newSelectedFiles.add(FileItem(path = outputPath, type = original.type))
//                } else {
//                    Log.e("Compressor", "Failed: ${original.path}")
//                }
//            },
//            onComplete = {
//                selectedFiles.clear()
//                selectedFiles.addAll(newSelectedFiles)
//
//                for (i in selectedFiles.indices) {
//                    getPresignedUrlCallback.invoke(selectedFiles[i].path,
//                        object : UploadCallback {
//                            @RequiresApi(Build.VERSION_CODES.O)
//                            override fun onUploadSuccess(response: String?, isFileUploaded: String?) {
//                                uploadedFiles++
//                                val percent = (uploadedFiles * 100) / isSelectedFileListSize
//                                ProgressDialogHelper.updateProgress(percent)
//
//                                Constant.isAwsUploadedFiles.add(
//                                    AwsUploadedFiles(
//                                        isFileUrl = isFileUploaded!!,
//                                        isFileType = selectedFiles[i].type.name
//                                    )
//                                )
//
//                                if (Constant.isAwsUploadedFiles.size == isSelectedFileListSize) {
//                                    ProgressDialogHelper.dismiss()
//                                    resultCallback.onAllFilesUploaded(Constant.isAwsUploadedFiles)
//                                }
//                            }
//
//                            override fun onUploadError(error: String?) {
//                                uploadedFiles++
//                                val percent = (uploadedFiles * 100) / isSelectedFileListSize
//                                ProgressDialogHelper.updateProgress(percent)
//
//                                if (uploadedFiles == isSelectedFileListSize) {
//                                    ProgressDialogHelper.dismiss()
//                                    resultCallback.onUploadFailed(error ?: "Unknown error")
//                                }
//                            }
//                        })
//                }
//            }
//        )
//    }
//}
