package com.vs.schoolmessenger.Utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator2
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>(),
    View.OnClickListener {
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
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
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        binding.imgBack.setOnClickListener(this)
        binding.imgMoreOptions.setOnClickListener(this)
        binding.lnrNext.setOnClickListener(this)
        binding.lnrPrevious.setOnClickListener(this)

        if (Constant.commonFileList.isNotEmpty()) {
            val first = Constant.commonFileList[0]
            if (first.type != FileType.VIDEO.toString() && !first.path.startsWith("content://") && !first.path.contains(
                    "amazonaws."
                )
            ) {
                Constant.commonFileList.removeAt(0)
            }
        }

        adapter = FileViewerAdapter(this, Constant.commonFileList)
        val noScrollLayoutManager = object : LinearLayoutManager(this, HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean = false
            override fun canScrollVertically(): Boolean = false
        }

        binding.rcyFile.layoutManager = noScrollLayoutManager
        binding.rcyFile.adapter = adapter

        if (Constant.commonFileList.size <= 1) {
            binding.lnrNext.visibility = View.GONE
            binding.lnrPrevious.visibility = View.GONE
        } else {
            binding.lnrNext.visibility = View.VISIBLE
            binding.lnrPrevious.visibility = View.VISIBLE
            binding.indicator.attachToRecyclerView(binding.rcyFile)
        }

        binding.rcyFile.setOnTouchListener { _, _ -> true }
        currentPosition = Constant.selectedFileIndex
        scrollToPosition(currentPosition)
        updateNavButtons()
    }

    fun CircleIndicator2.attachToRecyclerView(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return
        this.createIndicators(adapter.itemCount, 0)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                this@attachToRecyclerView.animatePageSelected(firstVisible)
            }
        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                this@attachToRecyclerView.createIndicators(adapter.itemCount, 0)
            }
        })
    }

    private fun scrollToPosition(position: Int) {
        binding.rcyFile.scrollToPosition(position)
        adapter.notifyItemChanged(position)

        val currentUrl = Constant.commonFileList.getOrNull(position)?.path ?: "Unknown"
        Log.d("CurrentURL", "Currently displayed file: $currentUrl")
        if (currentUrl.contains("amazonaws.") || currentUrl.contains("vimeo.")) {
            binding.imgMoreOptions.visibility = View.VISIBLE
        } else {
            binding.imgMoreOptions.visibility = View.GONE
        }
    }

    private fun updateNavButtons() {
        binding.lnrPrevious.visibility = if (currentPosition > 0) View.VISIBLE else View.GONE
        binding.lnrNext.visibility =
            if (currentPosition < Constant.commonFileList.size - 1) View.VISIBLE else View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.imgMoreOptions -> {

                val popupMenu = PopupMenu(this, binding.imgMoreOptions)
                popupMenu.menuInflater.inflate(R.menu.menu_share_download, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_share -> {
                            shareFileFromUrl(Constant.commonFileList[currentPosition].path)
                            true
                        }

                        R.id.action_download -> {
                            if (checkStoragePermission()) {
                                downloadFile(Constant.commonFileList[currentPosition].path)
                            } else {
                                requestStoragePermission()
                            }
                            true
                        }

                        else -> false
                    }
                }

                popupMenu.show()

                try {
                    val fields = popupMenu.javaClass.declaredFields
                    for (field in fields) {
                        if ("mPopup" == field.name) {
                            field.isAccessible = true
                            val menuPopupHelper = field.get(popupMenu)
                            val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                            val setForceIcons =
                                classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                            setForceIcons.invoke(menuPopupHelper, true)
                            break
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 101
            )
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101
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
                        Constant.hideLoading(this@FullScreenViewerActivity)
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
                    this@FullScreenViewerActivity, arrayOf(file.absolutePath), null, null
                )

                withContext(Dispatchers.Main) {
                    Constant.hideLoading(this@FullScreenViewerActivity)
                    Constant.showTopAlertPopup(
                        "File saved to ${file.absolutePath}", this@FullScreenViewerActivity
                    )
                }

            } catch (e: Exception) {
                Log.e("DownloadError", "Download failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FullScreenViewerActivity, "Download failed", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun shareFileFromUrl(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileName = url.substringAfterLast("/")
                val file = File(cacheDir, fileName)

                // Download file if not already cached
                if (!file.exists()) {
                    val input = URL(url).openStream()
                    val output = FileOutputStream(file)
                    input.copyTo(output)
                    input.close()
                    output.close()
                }

                val uri = FileProvider.getUriForFile(
                    this@FullScreenViewerActivity, "${packageName}.fileprovider", file
                )

                val mimeType = when (file.extension.lowercase()) {
                    "jpg", "jpeg" -> "image/jpeg"
                    "png" -> "image/png"
                    "gif" -> "image/gif"
                    "webp" -> "image/webp"
                    "bmp" -> "image/bmp"

                    "mp4" -> "video/mp4"
                    "3gp" -> "video/3gpp"
                    "mkv" -> "video/x-matroska"
                    "avi" -> "video/x-msvideo"
                    "mov" -> "video/quicktime"

                    "mp3" -> "audio/mpeg"
                    "wav" -> "audio/wav"
                    "m4a" -> "audio/mp4"
                    "ogg" -> "audio/ogg"
                    "aac" -> "audio/aac"

                    "pdf" -> "application/pdf"
                    "doc" -> "application/msword"
                    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    "ppt" -> "application/vnd.ms-powerpoint"
                    "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                    "xls" -> "application/vnd.ms-excel"
                    "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    "txt" -> "text/plain"
                    "csv" -> "text/csv"
                    "rtf" -> "application/rtf"
                    "html", "htm" -> "text/html"

                    "zip" -> "application/zip"
                    "rar" -> "application/vnd.rar"
                    "7z" -> "application/x-7z-compressed"
                    "tar" -> "application/x-tar"
                    "gz" -> "application/gzip"

                    "apk" -> "application/vnd.android.package-archive"
                    "json" -> "application/json"
                    "xml" -> "application/xml"

                    else -> "*/*"
                }


                Log.d("ShareFile", "Sharing $fileName as $mimeType, Uri: $uri")

                withContext(Dispatchers.Main) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share File"))
                }
            } catch (e: Exception) {
                Log.e("ShareFile", "Failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FullScreenViewerActivity, "Failed to share file", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


//    private fun shareImageFile(url: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val imageName = url.substringAfterLast("/")
//                val imageFile = File(cacheDir, imageName)
//
//                if (!imageFile.exists()) {
//                    val input = URL(url).openStream()
//                    val output = FileOutputStream(imageFile)
//                    input.copyTo(output)
//                    input.close()
//                    output.close()
//                }
//
//                val uri = FileProvider.getUriForFile(
//                    this@FullScreenViewerActivity, "${packageName}.fileprovider", imageFile
//                )
//
//                Log.d("ShareImageDebug", "Sharing URI: $uri, File Exists: ${imageFile.exists()}")
//
//                withContext(Dispatchers.Main) {
//                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                        type = "image/*"
//                        putExtra(Intent.EXTRA_STREAM, uri)
//                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    }
//                    startActivity(Intent.createChooser(shareIntent, "Share Image"))
//                }
//            } catch (e: Exception) {
//                Log.e("ShareError", "Failed to share image: ${e.message}", e)
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@FullScreenViewerActivity,
//                        "Failed to share image: ${e.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }
}