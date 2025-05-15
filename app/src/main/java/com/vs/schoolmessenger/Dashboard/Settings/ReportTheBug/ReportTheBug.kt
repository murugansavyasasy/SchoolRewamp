package com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ReportBugBinding
import java.io.File

class ReportTheBug : BaseActivity<ReportBugBinding>(), View.OnClickListener {

    override fun getViewBinding(): ReportBugBinding {
        return ReportBugBinding.inflate(layoutInflater)
    }


    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val paths = result.data?.getStringArrayListExtra("selected_files")
                paths?.forEach {
                    Log.d("MainActivity", "Selected file: $it")
                }
            }
        }

//    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == RESULT_OK) {
//            val paths = result.data?.getStringArrayListExtra("selected_images")
//            paths?.forEach {
//                Log.d("MainActivity", "Selected image: $it")
//            }
//        }
//    }

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_GALLERY_CAPTURE = 2
    private var imageFilePath: String? = null
    private val READ_EXTERNAL_STORAGE_PERMISSION_CODE = 102
    var isImageSelected = ArrayList<String>()

    override fun setupViews() {
        super.setupViews()
        binding.rlaPickImage.setOnClickListener(this)
        binding.lblSendBug.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)

        setupToolbar()
        loadMenu()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaPickImage -> {
                // Check the SDK version and request the appropriate permission
                val readImagePermission =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES  // Android 13 (API 33) and later
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE  // Pre-Android 13
                    }

                // Check if the permission is granted
                if (ContextCompat.checkSelfPermission(
                        this,
                        readImagePermission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission granted, proceed to show the bottom dialog or picker
                    showBottomDialog()
                } else {
                    // Request permission if not granted
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(readImagePermission),
                        READ_EXTERNAL_STORAGE_PERMISSION_CODE // Define your constant for the permission code
                    )
                }
            }

            R.id.lblSendBug -> {
                if (binding.edtReportBug.text.toString() != "") {

                } else {
                    Toast.makeText(this, R.string.EnterTheBug, Toast.LENGTH_SHORT).show()

                }
            }

            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            READ_EXTERNAL_STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed to show the bottom dialog or picker
                    showBottomDialog()
                } else {
                    // Permission denied
                    if (shouldShowRequestPermissionRationale(permissions[0])) {
                        // If the user denied the permission but didn't check "Don't ask again"
                        Toast.makeText(
                            this,
                            "Permission denied. Please allow access to images.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // If the user checked "Don't ask again"
                        showPermissionDeniedDialog()
                    }
                }
            }
        }
    }

    // Show a dialog explaining why the permission is needed and guide the user to app settings
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app requires permission to access your images. Please enable it in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.filepick_bottom_sheet)
        val rlaGallery = dialog.findViewById<RelativeLayout>(R.id.rlaGallery)
        val rlaCamera = dialog.findViewById<RelativeLayout>(R.id.rlaCamera)
        val rlaDocument = dialog.findViewById<RelativeLayout>(R.id.rlaVideo)

        rlaGallery.setOnClickListener {
            openAlbumSelectActivity("DOCUMENT")
            dialog.dismiss()
        }

//        rlaCamera.setOnClickListener {
//            openCamera()
//            dialog.dismiss()
//        }
//        rlaDocument.setOnClickListener {
//            onPdfButtonClick()
//            dialog.dismiss()
//        }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ) // Size
            setGravity(Gravity.BOTTOM) // Display at the bottom
            setWindowAnimations(R.style.PopupAnimation) // Apply the animation
        }
        dialog.show()

    }

    // Method to launch AlbumSelectActivity with selected media type
    private fun openAlbumSelectActivity(mediaType: String) {
        val intent = Intent(this, AlbumSelectActivity::class.java).apply {
            putExtra("type", mediaType)  // Pass media type to the next activity
        }
        startActivityForResult(intent, MEDIA_REQUEST_CODE)
    }

    // Handle the selected files from AlbumSelectActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MEDIA_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val selectedFiles = it.getStringArrayListExtra("selected_files")
                selectedFiles?.let { files ->
                    // Do something with the selected files (e.g., display file paths)
                    files.forEach { filePath ->
                        Log.d("MainActivity", "Selected file: $filePath")
                    }
                }
            }
        }
    }

    companion object {
        const val MEDIA_REQUEST_CODE = 1001  // Request code for media selection
    }

    private fun isLoadTheReportImage(isImageSelected: ArrayList<String>) {
        val courseAdapter = ImagePreviewAdapter(
            isImageSelected,
            this,
            object : ImagePreviewRemoveListener {
                override fun add(isAddingId: Int?) {

                }

                override fun remove(isRemovingId: Int) {

                }
            })

        binding.imgPreview.adapter = courseAdapter
        Constant.setGridViewHeight(binding.imgPreview, isImageSelected.size)
    }


    private fun loadMenu() {

        val isMenuItem: MutableList<String> = ArrayList()

        isMenuItem.add("Select the menu")
        isMenuItem.add("Attendance")
        isMenuItem.add("Assignment")
        isMenuItem.add("Image")
        isMenuItem.add("Video")
        isMenuItem.add("Notice Board")
        isMenuItem.add("Message From Management")
        isMenuItem.add("Staff Attendance")
        isMenuItem.add("Leave Apply")
        isMenuItem.add("Voice Message")
        isMenuItem.add("Text Message")


        val isMenuLoading = ArrayAdapter(this, R.layout.spinner_textview, isMenuItem)
        isMenuLoading.setDropDownViewResource(R.layout.dropdown_spinner)

        binding.isMenuSpinner.adapter = isMenuLoading
        binding.isMenuSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }
}