package com.vs.schoolmessenger.Parent.Homework

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.FileViewerAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>(), View.OnClickListener {

    private lateinit var adapter: FileViewerAdapter
    private var currentPosition = 0

    override fun getViewBinding(): HomeworkViewImageDocumentBinding {
        return HomeworkViewImageDocumentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        val subjectName = intent.getStringExtra(Constant.subjectName) ?: ""
        binding.lblSubject.text = subjectName

        binding.imgBack.setOnClickListener(this)
        binding.lytDownload.setOnClickListener(this)
        binding.lnrNext.setOnClickListener(this)
        binding.lnrPrevious.setOnClickListener(this)

        adapter = FileViewerAdapter(this, Constant.commonFileList)
        val noScrollLayoutManager = object : LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean = false
            override fun canScrollVertically(): Boolean = false
        }
        binding.rcyFile.layoutManager = noScrollLayoutManager
        binding.rcyFile.adapter = adapter
        binding.rcyFile.setOnTouchListener { _, _ -> true }
        currentPosition = Constant.selectedFileIndex
        scrollToPosition(currentPosition)

        updateNavButtons()
    }


    private fun scrollToPosition(position: Int) {
        binding.rcyFile.scrollToPosition(position)
        adapter.notifyItemChanged(position)
    }

    private fun updateNavButtons() {
        binding.lnrPrevious.isEnabled = currentPosition > 0
        binding.btnPrevious.alpha = if (currentPosition > 0) 1.0f else 0.5f

        binding.lnrNext.isEnabled = currentPosition < Constant.commonFileList.size - 1
        binding.btnNext.alpha = if (currentPosition < Constant.commonFileList.size - 1) 1.0f else 0.5f
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.lytDownload -> {
                if (checkStoragePermission()) {
                    downloadFile(Constant.commonFileList[currentPosition].path)
                } else {
                    requestStoragePermission()
                }
            }

            R.id.lnrNext -> {
                if (currentPosition < Constant.commonFileList.size - 1) {
                    currentPosition++
                    scrollToPosition(currentPosition)
                    updateNavButtons()
                }
            }

            R.id.lnrPrevious -> {
                if (currentPosition > 0) {
                    currentPosition--
                    scrollToPosition(currentPosition)
                    updateNavButtons()
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                101
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                101
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun downloadFile(fileUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection()
                connection.connect()

                val fileName = fileUrl.substringAfterLast("/")
                val folderName = "SchoolChimes"
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, folderName)

                if (!appDir.exists()) appDir.mkdirs()

                val file = File(appDir, fileName)
                if (file.exists()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@FullScreenViewerActivity,
                            "This file is already downloaded.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val input = connection.getInputStream()
                val output = FileOutputStream(file)
                input.copyTo(output)
                input.close()
                output.close()

                MediaScannerConnection.scanFile(
                    this@FullScreenViewerActivity,
                    arrayOf(file.absolutePath),
                    null,
                    null
                )

                withContext(Dispatchers.Main) {
                    Constant.showTopAlertPopup(
                        "File saved to ${file.absolutePath}",
                        this@FullScreenViewerActivity
                    )
                }

            } catch (e: Exception) {
                Log.e("DownloadError", "Download failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FullScreenViewerActivity,
                        "Download failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
