package com.vs.schoolmessenger.AlbumImage

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.databinding.ActivityAlbumSelectBinding
import com.vs.schoolmessenger.databinding.AlbumSelectActivityBinding

class AlbumSelectActivity : AppCompatActivity() {

    private lateinit var binding: AlbumSelectActivityBinding
    private lateinit var adapter: FileGridAdapter

    private lateinit var documentPickerLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AlbumSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val docs = loadDocumentsFromMediaStore()
                    if (docs.isNotEmpty()) {
                        adapter.submitList(docs)
                    } else {
                        Log.d("isDocumentEmpty","isDocumentEmpty")
                        openDocumentPicker()
                    }
                } else {
                    openDocumentPicker()
                }
            }
            else -> adapter.submitList(emptyList())
        }
    }

    private fun setupDocumentPicker() {
        documentPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            // Handle the selected documents here
            if (uris != null) {
                adapter.submitList(uris)
            }
        }
    }
    private fun openDocumentPicker() {
        documentPickerLauncher.launch(arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint"
        ))
    }

    private fun loadDocumentsFromMediaStore(): List<Uri> {
        val documentUris = mutableListOf<Uri>()
        val collection = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val selection = ("${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ?, ?)")
        val selectionArgs = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint"
        )

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
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val contentUri = Uri.withAppendedPath(collection, id.toString())
                documentUris.add(contentUri)
            }
        }

        return documentUris
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



//    private fun loadImages(context: Context): List<Uri> {
//        return loadMediaUris(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//    }
//
//    private fun loadVideos(context: Context): List<Uri> {
//        return loadMediaUris(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//    }
//
//    private fun loadAudio(context: Context): List<Uri> {
//        return loadMediaUris(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
//    }
//
//    private fun loadMediaUris(context: Context, uri: Uri): List<Uri> {
//        val mediaUris = mutableListOf<Uri>()
//        val projection = arrayOf(MediaStore.MediaColumns._ID)
//        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"
//
//        val query = context.contentResolver.query(uri, projection, null, null, sortOrder)
//        query?.use { cursor ->
//            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
//            while (cursor.moveToNext()) {
//                val id = cursor.getLong(idColumn)
//                val contentUri = ContentUris.withAppendedId(uri, id)
//                mediaUris.add(contentUri)
//            }
//        }
//        return mediaUris
//    }
//
//    private fun loadDocuments(context: Context): List<Uri> {
//        val documentUris = mutableListOf<Uri>()
//        val collection = MediaStore.Files.getContentUri("external")
//
//        val projection = arrayOf(
//            MediaStore.Files.FileColumns._ID,
//            MediaStore.Files.FileColumns.MIME_TYPE
//        )
//
//        val selection = ("${MediaStore.Files.FileColumns.MIME_TYPE}=? OR " +
//                "${MediaStore.Files.FileColumns.MIME_TYPE}=? OR " +
//                "${MediaStore.Files.FileColumns.MIME_TYPE}=? OR " +
//                "${MediaStore.Files.FileColumns.MIME_TYPE}=?")
//
//        val selectionArgs = arrayOf(
//            "application/pdf",
//            "application/msword",
//            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
//            "application/vnd.ms-powerpoint"
//        )
//
//        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
//
//        val query = context.contentResolver.query(
//            collection,
//            projection,
//            selection,
//            selectionArgs,
//            sortOrder
//        )
//
//        query?.use { cursor ->
//            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
//            while (cursor.moveToNext()) {
//                val id = cursor.getLong(idColumn)
//                val contentUri = ContentUris.withAppendedId(collection, id)
//                documentUris.add(contentUri)
//            }
//        }
//        return documentUris
//    }
}