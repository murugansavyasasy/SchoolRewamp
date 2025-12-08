package com.vs.schoolmessenger.AlbumImage

import android.Manifest
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AlbumSelectActivityBinding

class AlbumSelectActivity : BaseActivity<AlbumSelectActivityBinding>() {

    private lateinit var adapter: FileGridAdapter
    private lateinit var documentPickerLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private var fileType: String = Constant.IMAGE
    private var isWithOutHotCodeImage = false

    private var openedSettings = false   // avoids repeated dialog loop

    companion object {
        private val SUPPORTED_EXTENSIONS =
            listOf("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt")
    }

    override fun getViewBinding(): AlbumSelectActivityBinding {
        return AlbumSelectActivityBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.lytTitleAndName.visibility = View.GONE
        binding.toolbarLayout.monthSelectorLayout.visibility = View.GONE
        binding.toolbarLayout.rlaStudentName.visibility = View.GONE
        binding.toolbarLayout.rlaSpinner.visibility = View.GONE
        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        binding.toolbarLayout.blocktoolbar.visibility = View.GONE

        binding.toolbarLayout.rytFilePicking.visibility = View.VISIBLE

        fileType = intent.getStringExtra(Constant.isFileType) ?: Constant.IMAGE
        isWithOutHotCodeImage = intent.getBooleanExtra("isWithOutHotCodeImage", false)

        setupPermissionLauncher()
        setupDocumentPicker()

        binding.toolbarLayout.tvSelectionCount.text =
            "${getString(R.string.Selected_Files)} : 0 / ${Constant.isFileLimit}"
        binding.toolbarLayout.tvSelectedFiles.visibility = View.VISIBLE
//here we are checking for default first image in recycler view
        if (!isWithOutHotCodeImage) {
            binding.toolbarLayout.tvSelectedFiles.text =
                "Total Selected Files : ${Constant.selectedFiles.size - 1}"
        } else {
            binding.toolbarLayout.tvSelectedFiles.text =
                "Total Selected Files : ${Constant.selectedFiles.size}"
        }

        adapter = FileGridAdapter(Constant.isFileLimit, onSelectionChanged = { selectedUris ->
            binding.toolbarLayout.tvSelectionCount.text =
                "Selected Files : ${selectedUris.size} / ${Constant.isFileLimit}"
            binding.toolbarLayout.btnDone.visibility =
                if (selectedUris.isEmpty()) View.GONE else View.VISIBLE
        }, onItemClicked = { uri ->
            Log.d("AlbumSelectActivity", "Clicked file: $uri")
        })

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter

        requestPermission()

        binding.toolbarLayout.imgBack.setOnClickListener {
            isWithOutHotCodeImage = false
            onBackPressed()
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

    // ---------------------------------------------------------
    // PERMISSION HANDLING
    // ---------------------------------------------------------

    override fun onResume() {
        super.onResume()

        if (!openedSettings) return

        Log.d("PermissionFlow", "Returned from settings")

        if (permissionsGranted()) {
            loadFiles()
        } else {
            showPermissionRequiredDialog()
        }

        openedSettings = false
    }

    private fun requestPermission() {
        if (permissionsGranted()) {
            loadFiles()
            return
        }

        val permissions = getRequiredPermissions()
        permissionLauncher.launch(permissions)
    }

    private fun permissionsGranted(): Boolean {
        return if (fileType.uppercase() == Constant.DOCUMENT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                        checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            } else {
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (fileType.uppercase() == Constant.DOCUMENT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                emptyArray() // MANAGE_EXTERNAL_STORAGE handled manually
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun setupPermissionLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->

                val allGranted = result.values.all { it }

                if (allGranted || permissionsGranted()) {
                    loadFiles()
                } else {
                    val permanentlyDenied =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            !shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }

                    if (permanentlyDenied) {
                        showPermissionRequiredDialog()
                    }
                }
            }
    }

    private fun showPermissionRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.storage_permission_is_required_to_load_files_please_enable_it_in_settings))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                openedSettings = true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.Cancel)) { _, _ -> finish() }
            .show()
    }

    // ---------------------------------------------------------
    // MEDIA/DOCUMENT LOADING
    // ---------------------------------------------------------

    private fun loadFiles() {
        if (fileType.uppercase() == Constant.DOCUMENT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !Environment.isExternalStorageManager()
            ) {
                openAllFileAccessSettings()
                return
            }
            loadDocumentsOrPicker()
        } else {
            loadMediaFiles()
        }
    }

    private fun openAllFileAccessSettings() {
        openedSettings = true
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    private fun loadMediaFiles() {
        val list = when (fileType.uppercase()) {
            Constant.IMAGE -> loadImages()
            Constant.VIDEO -> loadVideos()
            Constant.AUDIO -> loadAudio()
            else -> emptyList()
        }
        adapter.submitList(list)
    }

    private fun setupDocumentPicker() {
        documentPickerLauncher =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                if (uris != null) adapter.submitList(uris)
            }
    }

    private fun loadDocumentsOrPicker() {
        val docs = loadDocumentsFromMediaStore()
        if (docs.isNotEmpty()) {
            adapter.submitList(docs)
        } else {
            openDocumentPicker()
        }
    }

    private fun openDocumentPicker() {
        documentPickerLauncher.launch(
            arrayOf(
                "application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain"
            )
        )
    }

    // ---------------------------------------------------------
    // LOADING DOCUMENTS
    // ---------------------------------------------------------

    private fun loadDocumentsFromMediaStore(): List<Uri> {
        val list = mutableListOf<Uri>()
        val collection = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val mimeTypes = arrayOf(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
        )

        val selection = mimeTypes.joinToString(prefix = "mime_type IN (", postfix = ")") { "?" }

        val cursor = contentResolver.query(
            collection, projection,
            selection, mimeTypes, "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val uri = Uri.withAppendedPath(collection, id.toString())
                list.add(uri)
            }
        }

        return list
    }

    // ---------------------------------------------------------
    // LOAD IMAGES, VIDEOS, AUDIO
    // ---------------------------------------------------------

    private fun loadImages(): List<Uri> {
        val list = mutableListOf<Uri>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(
            collection,
            arrayOf(MediaStore.Images.Media._ID),
            null, null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                list.add(ContentUris.withAppendedId(collection, it.getLong(idCol)))
            }
        }
        return list
    }

    private fun loadVideos(): List<Uri> {
        val list = mutableListOf<Uri>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(
            collection,
            arrayOf(MediaStore.Video.Media._ID),
            null, null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            while (it.moveToNext()) {
                list.add(ContentUris.withAppendedId(collection, it.getLong(idCol)))
            }
        }
        return list
    }

    private fun loadAudio(): List<Uri> {
        val list = mutableListOf<Uri>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(
            collection,
            arrayOf(MediaStore.Audio.Media._ID),
            null, null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            while (it.moveToNext()) {
                list.add(ContentUris.withAppendedId(collection, it.getLong(idCol)))
            }
        }
        return list
    }
}
