package com.vs.schoolmessenger.AlbumImage

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AlbumSelectActivityBinding

class AlbumSelectActivity : AppCompatActivity() {

    private lateinit var binding: AlbumSelectActivityBinding
    private lateinit var adapter: FileGridAdapter
    private lateinit var documentPickerLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    companion object {
        private const val REQUEST_CODE_MANAGE_ALL_FILES = 100
        private val SUPPORTED_EXTENSIONS =
            listOf("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt")
        private const val TAG = "DocumentScan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AlbumSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarLayout.rytFilePicking.visibility = View.VISIBLE

        setupPermissionLauncher()
        setupDocumentPicker()

        adapter = FileGridAdapter(limit = 5) { selectedUris ->
            selectedUris.forEach {
                println("Selected: $it")
            }
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter

        checkAndRequestPermissions()


        val fileType = intent.getStringExtra(Constant.isFileType) ?: Constant.IMAGE
        if (fileType.uppercase() == Constant.DOCUMENT) {
            checkAndRequestPermissions()
        } else {
            when (fileType.uppercase()) {
                Constant.IMAGE -> adapter.submitList(loadImages())
                Constant.VIDEO -> adapter.submitList(loadVideos())
                Constant.AUDIO -> adapter.submitList(loadAudio())
                else -> adapter.submitList(emptyList())
            }
        }

        binding.toolbarLayout.btnDone.setOnClickListener {
            val selectedUris = adapter.getSelectedItems()
            val intent = Intent().apply {
                putParcelableArrayListExtra(Constant.isSelectedFiles, ArrayList(selectedUris))
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun setupPermissionLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val allGranted = permissions.entries.all { it.value }
                if (allGranted || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && android.os.Environment.isExternalStorageManager())) {
                    loadDocumentsOrOpenPicker()
                }
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

    private fun checkAndRequestPermissions() {

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (android.os.Environment.isExternalStorageManager()) {
                    loadDocumentsOrOpenPicker()
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, REQUEST_CODE_MANAGE_ALL_FILES)
                }
            }

            else -> {
                permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
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
            "doc" to listOf(
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ),
            "ppt" to listOf(
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            ),
            "xls" to listOf(
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ),
            "txt" to listOf("text/plain")
        )
        return extensions.any { ext ->
            map[ext]?.contains(mimeType) == true
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
        "doc" -> listOf(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        "ppt" -> listOf(
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        )
        "xls" -> listOf(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
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
