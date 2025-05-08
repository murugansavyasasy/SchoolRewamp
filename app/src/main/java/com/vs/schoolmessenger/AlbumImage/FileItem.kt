package com.vs.schoolmessenger.AlbumImage

import android.net.Uri

data class FileItem(
    val uri: Uri,
    val name: String,
    val mimeType: String?
)
