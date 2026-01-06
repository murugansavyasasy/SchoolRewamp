package com.vs.schoolmessenger.Parent.FeeDetails

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeeReceiptViewActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class FeeReceiptViewActivity : BaseActivity<FeeReceiptViewActivityBinding>(), View.OnClickListener {

    override fun getViewBinding(): FeeReceiptViewActivityBinding {
        return FeeReceiptViewActivityBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    private var pdfUrl: String? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )


        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.imgDownload.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.downloadicon
            )
        )
        binding.imgDownload.visibility = View.VISIBLE
        binding.lytDownloadPdf.setOnClickListener(this)
        binding.lytShare.setOnClickListener(this)
        binding.toolbarLayout.rytSearch.visibility = View.GONE

        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.fee_receipt)
        binding.toolbarLayout.lblStudentSection.text =
            "${isChildDetails?.standard_name} - ${isChildDetails?.section_name}"

        pdfUrl = intent.getStringExtra("pdf_url")
            ?: ""

        val googleDocsUrl = "${Constant.google_g_view_embedded}$pdfUrl"

        Constant.showLoading(this)

        binding.feeReceiptWebview.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            alpha = 0f

            webViewClient = object : android.webkit.WebViewClient() {
                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.animate()?.alpha(1f)?.setDuration(300)?.start()
                    Constant.hideLoading(this@FeeReceiptViewActivity)
                }

                override fun onReceivedError(
                    view: android.webkit.WebView?,
                    request: android.webkit.WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    Constant.hideLoading(this@FeeReceiptViewActivity)
                    Toast.makeText(
                        this@FeeReceiptViewActivity,
                        context.getString(R.string.failed_to_load_receipt_please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            loadUrl(googleDocsUrl)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.lytDownloadPdf -> {
                if (checkStoragePermission()) downloadFile(pdfUrl!!)
                else requestStoragePermission()
            }

            R.id.lytShare -> {
                if (checkStoragePermission()) shareFileFromUrl(pdfUrl!!)
                else requestStoragePermission()
            }
        }
    }

    private fun downloadFile(url: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.VISIBLE
                }

                var fileName = url.substringAfterLast("/").substringBefore("?")
                val fileExtension = fileName.substringAfterLast('.', "").lowercase()

                val subFolder = when (fileExtension) {
                    "mp4", "mov", "mkv", "avi", "flv", "wmv", "webm", "mpeg", "mpg", "3gp", "m4v" -> "Videos"
                    "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx" -> "Documents"
                    "jpg", "jpeg", "png", "gif", "bmp", "webp" -> "Images"
                    "mp3", "wav", "aac", "ogg", "flac", "m4a" -> "Audio"
                    else -> "Others"
                }
                if (!fileName.contains(".")) {
                    fileName += when (subFolder) {
                        "Videos" -> ".mp4"
                        "Documents" -> ".pdf"
                        "Images" -> ".jpg"
                        else -> ".bin"
                    }
                }

                val baseFolderName = "SchoolChimes"
                val subFolderPath = "FeeReceipt/$subFolder"

                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                val targetDir = File(downloadsDir, "$baseFolderName/$subFolderPath")
                if (!targetDir.exists()) targetDir.mkdirs()

                val file = File(targetDir, fileName)

                if (file.exists()) {
                    Log.d("Download", "Existing file found. Deleting old file.")
                    file.delete()
                }
                Log.d("Download", "Downloading new file.")
                val connection = URL(url).openConnection()
                connection.getInputStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                MediaScannerConnection.scanFile(
                    this@FeeReceiptViewActivity,
                    arrayOf(file.absolutePath),
                    null,
                    null
                )

                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                    Constant.showValidationAlertPopup(
                        getString(R.string.successfully_downloaded),
                        "File saved to Downloads/$baseFolderName/$subFolderPath/$fileName",
                        this@FeeReceiptViewActivity
                    )
                }

            } catch (e: Exception) {
                Log.e("Download", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                    Toast.makeText(
                        this@FeeReceiptViewActivity,
                        getString(R.string.Download_failed_2),
                        Toast.LENGTH_SHORT
                    ).show()
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
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permission, 101)
    }

    private fun shareFileFromUrl(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                val contentType = connection.contentType ?: "application/octet-stream"
                var fileName = url.substringAfterLast("/").substringBefore("?")
                if (!fileName.contains(".")) {
                    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType)
                    fileName += ".${ext ?: "bin"}"
                }
                val file = File(cacheDir, fileName)
                if (!file.exists()) {
                    connection.inputStream.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(
                                output
                            )
                        }
                    }
                }
                val uri = FileProvider.getUriForFile(
                    this@FeeReceiptViewActivity,
                    "$packageName.fileprovider",
                    file
                )
                val mimeType =
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())
                        ?: contentType
                withContext(Dispatchers.Main) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        setType(mimeType)
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share File"))
                }

            } catch (e: Exception) {
                Log.e("ShareFile", "Error sharing: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FeeReceiptViewActivity,
                        getString(R.string.failed_to_share_file),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                }
            }
        }
    }
}
