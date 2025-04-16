package com.vs.schoolmessenger.Utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

class FileExtensionFromContentUri {
    fun getFileExtensionFromContentUri(context: Context, uri: Uri): String? {
        var extension: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    val displayName = it.getString(displayNameIndex)
                    println("Display name: $displayName")
                    extension = displayName.substringAfterLast('.', "")
                } else {
                    println("DISPLAY_NAME column not found")
                }
            } else {
                println("Cursor could not move to first")
            }
        } ?: println("Cursor is null")

        return extension
    }

}