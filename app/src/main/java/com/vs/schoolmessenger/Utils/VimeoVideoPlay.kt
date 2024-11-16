package com.vs.schoolmessenger.Utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.VimeoVideoPlayBinding
import com.vs.schoolmessenger.util.VimeoVideoDownload
import java.io.File


class VimeoVideoPlay : BaseActivity<VimeoVideoPlayBinding>(), View.OnClickListener,
    VimeoVideoDownload.VimeoDownloadCallback {

    private val VIDEO_FOLDER = "SchoolChimesVideo"  // Avoid leading slashes

    var isDownload = true
    var downloadVideoId = ""
    private var isDownloadFailedShown = false

    override fun getViewBinding(): VimeoVideoPlayBinding {
        return VimeoVideoPlayBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()
        val videoUrl = intent.getStringExtra("VIDEO_URL")
        val videoTitle = intent.getStringExtra("VIDEO_TITLE")

        binding.imgDownload.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        }

        binding.lblVideoTitle.text = videoTitle
        if (videoUrl != null) {
            val embedUrl = "https://player.vimeo.com/video/${videoUrl.split("/").last()}"
            binding.webViewVideo.settings.javaScriptEnabled = true
            binding.webViewVideo.settings.domStorageEnabled = true
            binding.webViewVideo.settings.setSupportZoom(false) // Disable zoom if needed
            binding.webViewVideo.settings.loadWithOverviewMode = true
            binding.webViewVideo.settings.useWideViewPort = true
            binding.webViewVideo.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }
            binding.webViewVideo.loadUrl(embedUrl)
        } else {
            Toast.makeText(this, R.string.VideoURLnotfound, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webViewVideo)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgDownload -> {
                isDownload = true
                downloadVideoId = "1029647739"
                Log.d("downloadVideoId", downloadVideoId)

                // Call the method to get Vimeo download URL
                VimeoVideoDownload.getVimeoDownloadUrl(downloadVideoId, this)
            }

            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }


    override fun onDownloadUrlRetrieved(quality: String, downloadUrl: String) {

        val finalDownloadUrl =
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        if (isDownload) {
            isDownload = false
            if (!isVideoDownloaded()) {
                downloadVideo(this, finalDownloadUrl)
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Video is already downloaded!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onError(errorMessage: String) {

        // Handle error
        Log.e("MainActivity", "Error: $errorMessage")

        (this as Activity).runOnUiThread {
            Toast.makeText(
                this,
                "Error: $errorMessage",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showAlert(context: Context, title: String, message: String) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        alertDialog.show()
    }

    private fun startDownload(
        context: Context,
        downloadUrl: String,
        progressDialog: AlertDialog,
        progressBar: ProgressBar,
        progressText: TextView
    ) {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            VIDEO_FOLDER
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val uri = Uri.parse(downloadUrl)
        val request = DownloadManager.Request(uri).apply {
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$VIDEO_FOLDER/video_for_your_school.mp4"
            )
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Reset the failure flag for a new download
        isDownloadFailedShown = false

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            @SuppressLint("Range")
            override fun run() {
                val query = DownloadManager.Query().apply { setFilterById(downloadId) }
                val cursor = downloadManager.query(query)

                cursor?.let {
                    if (it.moveToFirst()) {
                        val bytesDownloaded =
                            it.getInt(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val totalBytes =
                            it.getInt(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                        if (totalBytes > 0) {
                            val progress = (bytesDownloaded * 100L / totalBytes).toInt()
                            progressBar.progress = progress
                            progressText.text = "$progress%"
                        }

                        val status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                it.close()
                                progressDialog.dismiss()
                                showAlert(
                                    context as Activity,
                                    "Download successful!",
                                    "File stored in: $VIDEO_FOLDER/video_for_your_school.mp4"
                                )
                            }

                            DownloadManager.STATUS_FAILED -> {
                                it.close()
                                progressDialog.dismiss()
                                if (!isDownloadFailedShown) {
                                    isDownloadFailedShown = true
                                    showAlert(
                                        context as Activity,
                                        "Download failed.",
                                        "Please try again later."
                                    )
                                }
                            }
                        }
                    }
                    it.close()
                }

                // Re-run the check after 500ms
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun downloadVideo(context: Context, downloadUrl: String) {
        (context as Activity).runOnUiThread {
            // Inflate the custom layout for the dialog
            val inflater = LayoutInflater.from(context)
            val dialogView: View = inflater.inflate(R.layout.dialog_progress, null)

            val progressBar: ProgressBar = dialogView.findViewById(R.id.progress_bar)
            val progressText: TextView = dialogView.findViewById(R.id.progress_text)

            // Create and show the AlertDialog on the main thread
            val progressDialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            progressDialog.show()

            // Initialize the download after showing the dialog
            startDownload(context, downloadUrl, progressDialog, progressBar, progressText)
        }
    }

    private fun isVideoDownloaded(): Boolean {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            VIDEO_FOLDER
        )
        val videoFile = File(directory, "video_for_your_school.mp4")
        return videoFile.exists()
    }


    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check the result of the permission request
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
                // Call your download function here since permission is granted
                // downloadVideo()
            } else {
                // Permission denied
                Toast.makeText(
                    this,
                    "Permission denied, cannot download files.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


}
