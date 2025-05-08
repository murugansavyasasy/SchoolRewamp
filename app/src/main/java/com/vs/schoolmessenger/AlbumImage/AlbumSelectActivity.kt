package com.vs.schoolmessenger.AlbumImage


import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.databinding.AlbumSelectActivityBinding

class AlbumSelectActivity : AppCompatActivity() {
    private lateinit var fileType: String
    private lateinit var binding: AlbumSelectActivityBinding
    private val selectedUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AlbumSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileType = intent.getStringExtra("fileType") ?: ""
        fileType = "audio"
        val files = loadFiles(this, fileType)
        val adapter = FileGridAdapter(files) { uri, isSelected ->
            if (isSelected) selectedUris.add(uri) else selectedUris.remove(uri)
        }


        binding.albumRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.albumRecyclerView.adapter = adapter

        binding.doneButton.setOnClickListener {
            val resultIntent = intent
            resultIntent.putParcelableArrayListExtra("selectedUris", ArrayList(selectedUris))
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun loadFiles(context: Context, type: String): List<FileItem> {
        val files = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        val (uri, selection, selectionArgs) = when (type) {
            "image" -> Triple(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null)
            "video" -> Triple(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null)
            "audio" -> Triple(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null)
            "document" -> {
                val mimeTypes = arrayOf(
                    "application/pdf", "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                val selection = mimeTypes.joinToString(" OR ") { "${MediaStore.Files.FileColumns.MIME_TYPE} = ?" }
                Triple(MediaStore.Files.getContentUri("external"), selection, mimeTypes)
            }
            else -> return emptyList()
        }

        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol)
                val mimeType = it.getString(mimeCol)
                val fileUri = ContentUris.withAppendedId(uri, id)
                files.add(FileItem(fileUri, name, mimeType))
            }
        }
        return files
    }


}