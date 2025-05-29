package com.vs.schoolmessenger.AlbumImage

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.databinding.AlbumSelectActivityBinding

class AlbumSelectActivity : AppCompatActivity() {

    private lateinit var binding: AlbumSelectActivityBinding
    private lateinit var adapter: FileGridAdapter
    private lateinit var documentPickerLauncher: ActivityResultLauncher<Array<String>>

    companion object {
        private const val REQUEST_CODE_MANAGE_ALL_FILES = 100
        private const val REQUEST_CODE_READ_STORAGE = 101
        private val SUPPORTED_EXTENSIONS = listOf("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt")
        private const val TAG = "DocumentScan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AlbumSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarLayout.rytFilePicking.visibility = View.VISIBLE

        adapter = FileGridAdapter(limit = 5) { selectedUris ->
            // Print selected URIs
            selectedUris.forEach {
                println("Selected: $it")
            }
        }

        setupDocumentPicker()

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter

        val fileType = intent.getStringExtra("type") ?: "IMAGE"
        when (fileType.uppercase()) {
            "IMAGE" -> adapter.submitList(loadImages())
            "VIDEO" -> adapter.submitList(loadVideos())
            "AUDIO" -> adapter.submitList(loadAudio())
            "DOCUMENT" -> {
                if (hasStoragePermission()) {
                    loadDocumentsOrOpenPicker()
                } else {
                    requestStoragePermission()
                }
            }
            else -> adapter.submitList(emptyList())
        }
    }

    private fun setupDocumentPicker() {
        documentPickerLauncher =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                if (uris != null) {
                    adapter.submitList(uris)
                }
            }
    }

    private fun openDocumentPicker() {
        documentPickerLauncher.launch(
            arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain"
            )
        )
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, REQUEST_CODE_MANAGE_ALL_FILES)
            } catch (e: Exception) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, REQUEST_CODE_MANAGE_ALL_FILES)
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                loadDocumentsOrOpenPicker()
            } else {
                Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Read storage permission denied")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MANAGE_ALL_FILES) {
            if (hasStoragePermission()) {
                loadDocumentsOrOpenPicker()
            } else {
                Toast.makeText(this, "Permission denied to manage all files", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Manage all files permission denied")
            }
        }
    }

    private fun loadDocumentsOrOpenPicker() {
        val docs = loadDocumentsFromMediaStore()
        if (docs.isNotEmpty()) {
            adapter.submitList(docs)
        } else {
            openDocumentPicker()
        }
    }

    private fun loadDocumentsFromMediaStore(): List<Uri> {
        val documentUris = mutableListOf<Uri>()
        val collection = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val selection = buildSelectionForMimeTypes(SUPPORTED_EXTENSIONS)
        val selectionArgs = buildMimeTypesArgs(SUPPORTED_EXTENSIONS)

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        val cursor = contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mimeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val mimeType = it.getString(mimeCol)
                val contentUri = Uri.withAppendedPath(collection, id.toString())

                // Optional: filter again by extension to be safe
                if (mimeTypeMatchesExtension(mimeType, SUPPORTED_EXTENSIONS)) {
                    documentUris.add(contentUri)
                }
            }
        }

        return documentUris
    }

    private fun mimeTypeMatchesExtension(mimeType: String?, extensions: List<String>): Boolean {
        if (mimeType == null) return false
        val map = mapOf(
            "pdf" to listOf("application/pdf"),
            "doc" to listOf("application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            "ppt" to listOf("application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            "xls" to listOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            "txt" to listOf("text/plain")
        )
        return extensions.any { ext ->
            map[ext]?.contains(mimeType) ?: false
        }
    }

    private fun buildSelectionForMimeTypes(extensions: List<String>): String {
        val mimeTypes = extensions.flatMap { extToMimeTypes(it) }
        return "${MediaStore.Files.FileColumns.MIME_TYPE} IN (${mimeTypes.joinToString(",") { "?" }})"
    }

    private fun buildMimeTypesArgs(extensions: List<String>): Array<String> {
        return extensions.flatMap { extToMimeTypes(it) }.toTypedArray()
    }

    private fun extToMimeTypes(extension: String): List<String> = when (extension) {
        "pdf" -> listOf("application/pdf")
        "doc" -> listOf("application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        "ppt" -> listOf("application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation")
        "xls" -> listOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        "txt" -> listOf("text/plain")
        else -> emptyList()
    }

    private fun loadImages(): List<Uri> {
        val imageUris = mutableListOf<Uri>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                imageUris.add(uri)
            }
        }
        return imageUris
    }

    private fun loadVideos(): List<Uri> {
        val videoUris = mutableListOf<Uri>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.Media._ID)
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                videoUris.add(uri)
            }
        }
        return videoUris
    }

    private fun loadAudio(): List<Uri> {
        val audioUris = mutableListOf<Uri>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                audioUris.add(uri)
            }
        }
        return audioUris
    }
}
