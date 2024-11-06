package com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.AlbumImage.Constants
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ReportBugBinding
import java.io.File

class ReportTheBug : BaseActivity<ReportBugBinding>(), View.OnClickListener {

    override fun getViewBinding(): ReportBugBinding {
        return ReportBugBinding.inflate(layoutInflater)
    }

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
                val readImagePermission =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
                    else Manifest.permission.READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(
                        this, readImagePermission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(readImagePermission),
                        READ_EXTERNAL_STORAGE_PERMISSION_CODE
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


    private fun openGallery() {
        val intent1 = Intent(this, AlbumSelectActivity::class.java)
        intent1.putExtra("Gallery", "Images")
        intent1.putExtra(Constants.INTENT_EXTRA_LIMIT, 4)
        startActivityForResult(intent1, REQUEST_IMAGE_GALLERY_CAPTURE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val file: File = File(imageFilePath)
            val imageUri = Uri.fromFile(file)

        } else if (requestCode == REQUEST_IMAGE_GALLERY_CAPTURE) {
            if (data != null) {
                isImageSelected.clear()
                isImageSelected = data.getStringArrayListExtra("images")!!
                isLoadTheReportImage(isImageSelected)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun isLoadTheReportImage(isImageSelected: ArrayList<String>) {
        val courseAdapter = ImagePreviewAdapter(isImageSelected,
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