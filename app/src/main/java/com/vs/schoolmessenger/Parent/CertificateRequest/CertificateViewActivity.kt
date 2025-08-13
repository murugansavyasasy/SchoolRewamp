package com.vs.schoolmessenger.Parent.CertificateRequest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CertificateViewActivityBinding
import com.vs.schoolmessenger.databinding.GatePassBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


class CertificateViewActivity : BaseActivity<CertificateViewActivityBinding>(), View.OnClickListener{

    override fun getViewBinding(): CertificateViewActivityBinding {
        return CertificateViewActivityBinding.inflate(layoutInflater)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()
        binding.imgBack.setOnClickListener(this)
        binding.imgMoreOptions.setOnClickListener(this)

        binding.imgTimimg.setColorFilter(ContextCompat.getColor(this, R.color.dark_orange), PorterDuff.Mode.SRC_IN)
        binding.imgRequeston.setColorFilter(ContextCompat.getColor(this, R.color.PrimaryColor), PorterDuff.Mode.SRC_IN)
        binding.imgCertificate.setColorFilter(ContextCompat.getColor(this, R.color.PrimaryColor), PorterDuff.Mode.SRC_IN)
        binding.imgCertificateType.setColorFilter(ContextCompat.getColor(this, R.color.PrimaryColor), PorterDuff.Mode.SRC_IN)
        binding.imgReason.setColorFilter(ContextCompat.getColor(this, R.color.PrimaryColor), PorterDuff.Mode.SRC_IN)

        binding.lblRequestedOnDate.text= Constant.isCertificateData?.requested_on ?:""
        if (Constant.isCertificateData!!.url!="" && Constant.isCertificateData!!.issued_on!=""){
            binding.rytCertificate.visibility=View.VISIBLE
            binding.wvCertificatePdf.visibility=View.VISIBLE
            binding.lblCertificateDate.visibility=View.VISIBLE
            binding.imgMoreOptions.visibility=View.VISIBLE
            binding.rytWaitingProcess.visibility=View.GONE
            binding.lblCertificateDate.text= Constant.isCertificateData?.issued_on ?:""

            binding.loadingBar.visibility = View.VISIBLE
            binding.wvCertificatePdf.apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.domStorageEnabled = true


                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(
                        view: WebView?,
                        url: String?,
                        favicon: Bitmap?
                    ) {
                        binding.loadingBar.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        binding.loadingBar.visibility = View.GONE
                    }
                }

                webChromeClient = WebChromeClient()

                loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=${Constant.isCertificateData!!.url}")

            }

        }
        else{
            binding.wvCertificatePdf.visibility=View.GONE
            binding.rytCertificate.visibility=View.GONE
            binding.imgMoreOptions.visibility=View.GONE
            binding.loadingBar.visibility = View.GONE
            binding.rytWaitingProcess.visibility=View.VISIBLE
            binding.lblCertificateDate.visibility=View.GONE
        }
        binding.lblCerticateTypeValue.text= Constant.isCertificateData?.type ?:""
        binding.lblReasonValue.text= Constant.isCertificateData?.reason ?:""
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.imgMoreOptions -> showFileOptions(Constant.isCertificateData?.url ?:"")

        }

    }

    private fun showFileOptions(url: String) {
        val popupMenu = PopupMenu(this, binding.imgMoreOptions)
        popupMenu.menuInflater.inflate(R.menu.menu_share_download, popupMenu.menu)
        forcePopupMenuIcons(popupMenu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_share -> {
                    shareFileFromUrl(url)
                    true
                }

                R.id.action_download -> {
                    binding.lnrDownloadStatus.visibility = View.VISIBLE
                    if (checkStoragePermission()) downloadFile(url)
                    else requestStoragePermission()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permission, 101)
    }

    private fun downloadFile(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.VISIBLE
                }

                var fileName = url.substringAfterLast("/").substringBefore("?")
                val fileExtension =
                    fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()

                val subFolder = when (fileExtension) {
                    "mp4", "mov", "mkv", "avi", "flv", "wmv", "webm", "mpeg", "mpg", "3gp", "m4v" -> "Videos"
                    "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "csv", "txt", "rtf", "odt", "ods", "odp", "html", "xml", "json", "log" -> "Documents"
                    "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "tiff", "svg", "ico" -> "Images"
                    "mp3", "wav", "aac", "ogg", "flac", "m4a", "wma", "amr", "opus" -> "Audio"
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
                val subFolderPath = "Attachments/$subFolder"

                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "$baseFolderName/$subFolderPath")
                if (!targetDir.exists()) targetDir.mkdirs()

                val file = File(targetDir, fileName)

                if (!file.exists()) {
                    val connection = URL(url).openConnection()
                    connection.getInputStream().use { input ->
                        FileOutputStream(file).use { output -> input.copyTo(output) }
                    }

                    MediaScannerConnection.scanFile(
                        this@CertificateViewActivity,
                        arrayOf(file.absolutePath),
                        null,
                        null
                    )
                }

                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                    Constant.showValidationAlertPopup(
                        "Successfully Download...✅",
                        "File saved to Downloads/$baseFolderName/$subFolderPath/$fileName", this@CertificateViewActivity
                    )
                }

            } catch (e: Exception) {
                Log.e("Download", "Download error: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                    Toast.makeText(
                        this@CertificateViewActivity,
                        "Download failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
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
                    this@CertificateViewActivity,
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
                        this@CertificateViewActivity,
                        "Failed to share file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                }
            }
        }
    }

    private fun forcePopupMenuIcons(menu: PopupMenu) {
        try {
            val fields = menu.javaClass.declaredFields
            for (field in fields) {
                if (field.name == "mPopup") {
                    field.isAccessible = true
                    val helper = field.get(menu)
                    val classPopup = Class.forName(helper.javaClass.name)
                    val setIcons = classPopup.getMethod("setForceShowIcon", Boolean::class.java)
                    setIcons.invoke(helper, true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}