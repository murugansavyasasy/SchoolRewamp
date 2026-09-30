package com.vs.schoolmessenger.CommonScreens

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.FileViewerAdapter
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator2
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class FilesViewActivity : BaseActivity<HomeworkViewImageDocumentBinding>(),
    View.OnClickListener {

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private lateinit var adapter: FileViewerAdapter
    private var currentPosition = 0

    private var pendingDownloadUrl: String? = null

    private val activeDownloads = ConcurrentHashMap<Long, String>()

    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
            val displayPath = activeDownloads.remove(id) ?: return

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var success = false
            dm.query(DownloadManager.Query().setFilterById(id))?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    )
                    success = status == DownloadManager.STATUS_SUCCESSFUL
                    Log.d(
                        "Download",
                        "status=$status size=${cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))} " +
                                "type=${cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE))} " +
                                "uri=${cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))}"
                    )
                }
            }

            if (activeDownloads.isEmpty()) binding.lnrDownloadStatus.visibility = View.GONE
            if (isFinishing || isDestroyed) return

            if (success) {
                Constant.showValidationAlertPopup(
                    getString(R.string.successfully_downloaded),
                    "File saved to $displayPath",
                    this@FilesViewActivity
                )
            } else {
                Toast.makeText(
                    this@FilesViewActivity,
                    getString(R.string.Download_failed_2),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    var isFilesList: MutableList<CommonFileData> = mutableListOf()

    companion object {
        private const val REQ_STORAGE_PERMISSION = 101
    }

    override fun getViewBinding(): HomeworkViewImageDocumentBinding =
        HomeworkViewImageDocumentBinding.inflate(layoutInflater)

    override fun setupViews() {
        super.setupViews()
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        val mainView = binding.main
        val toolbarLayout = findViewById<View>(R.id.ImageLayout)
        findViewById<View>(R.id.rytHeader)

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )

            binding.statusBarBackground.updateLayoutParams {
                height = systemBars.top
            }
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { _, insets ->
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }

        val subjectName = intent.getStringExtra(Constant.subjectName) ?: ""
        binding.lblSubject.visibility = View.GONE
        binding.lblSubject.text = subjectName
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        ContextCompat.registerReceiver(
            this,
            downloadCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )

        binding.imgBack.setOnClickListener(this)
        binding.imgMoreOptions.setOnClickListener(this)
        binding.lnrNext.setOnClickListener(this)
        binding.lnrPrevious.setOnClickListener(this)
        isFilesList.clear()

        if (Constant.commonFileList.isNotEmpty()) {
            Log.d("commonFileList", Constant.commonFileList.toString())
            val first = Constant.commonFileList[0]
            if (first.type != FileType.VIDEO.toString()
                && !first.path.startsWith("content://")
                && !first.path.contains("amazonaws.")
                && !first.path.contains("file:///storage")
            ) {
                Constant.commonFileList.removeAt(0)
            }

            if (first.path.contains("amazonaws.") || first.type == FileType.VIDEO.toString()) {
                binding.imgMoreOptions.visibility = View.VISIBLE
            } else {
                binding.imgMoreOptions.visibility = View.GONE
            }
        }

        for (i in Constant.commonFileList.indices) {
            if (Constant.commonFileList[i].path.contains("amazonaws.") ||
                Constant.commonFileList[i].path.contains("player.vimeo.com") ||
                Constant.commonFileList[i].type == Constant.IMAGE ||
                Constant.commonFileList[i].type == Constant.VIDEO
            ) {
                isFilesList.add(
                    CommonFileData(
                        type = Constant.commonFileList[i].type,
                        path = Constant.commonFileList[i].path
                    )
                )
            }
        }
        Log.d("Constant.selectedFiles", isFilesList.size.toString())

        adapter = FileViewerAdapter(this, isFilesList)

        val onlyImages = isFilesList.all { it.type == FileType.IMAGE.toString() }

        val layoutManager = if (onlyImages) {
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        } else {
            object : LinearLayoutManager(this, RecyclerView.HORIZONTAL, false) {
                override fun canScrollHorizontally(): Boolean = false
            }
        }

        binding.rcyFile.layoutManager = layoutManager
        binding.rcyFile.adapter = adapter

        binding.rcyFile.setOnTouchListener { _, _ -> !onlyImages }

        if (!onlyImages && isFilesList.size > 1) {
            binding.lnrNext.visibility = View.VISIBLE
            binding.lnrPrevious.visibility = View.VISIBLE
        } else {
            binding.lnrNext.visibility = View.GONE
            binding.lnrPrevious.visibility = View.GONE
        }
        if (onlyImages && isFilesList.size > 1) {
            binding.indicator.attachToRecyclerView(binding.rcyFile)
        }
        Log.d("currentPosition", Constant.selectedFileIndex.toString())
        currentPosition = Constant.selectedFileIndex
        scrollToPosition(currentPosition)
        updateNavButtons()

        binding.imgBack.setOnClickListener {
            adapter.stopAudio()
            Constant.commonFileList.clear()
            onBackPressed()
        }
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

                if (firstVisible != RecyclerView.NO_POSITION) {
                    currentPosition = firstVisible
                    updateNavButtons()
                }
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
        val currentUrl = isFilesList.getOrNull(position)?.path ?: "Unknown"
        Log.d("CurrentURL", "Currently displayed file: $currentUrl")
    }

    private fun updateNavButtons() {
        binding.lnrPrevious.visibility = if (currentPosition > 0) View.VISIBLE else View.GONE
        binding.lnrNext.visibility =
            if (currentPosition < isFilesList.size - 1) View.VISIBLE else View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgMoreOptions -> showFileOptions(isFilesList[currentPosition].path)

            R.id.lnrNext -> if (currentPosition < isFilesList.size - 1) {
                adapter.stopAudio()
                currentPosition++
                scrollToPosition(currentPosition)
                updateNavButtons()
            }

            R.id.lnrPrevious -> if (currentPosition > 0) {

                adapter.stopAudio()
                currentPosition--
                scrollToPosition(currentPosition)
                updateNavButtons()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Constant.commonFileList.clear()
        adapter.stopAudio()
    }

    private fun showFileOptions(url: String) {
        val popupMenu = PopupMenu(this, binding.imgMoreOptions)
        popupMenu.menuInflater.inflate(R.menu.menu_share_download, popupMenu.menu)
        forcePopupMenuIcons(popupMenu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_share -> {
                    if (url.contains("vimeo.com/video/")) fetchAndShareVimeoVideoFromUrl(url)
                    else shareFileFromUrl(url)
                    true
                }

                R.id.action_download -> {
                    if (url.contains("vimeo.com/video/")) {
                        fetchAndDownloadVimeoVideo(url)
                    } else if (checkStoragePermission()) {
                        downloadFile(url)
                    } else {
                        pendingDownloadUrl = url
                        requestStoragePermission()
                    }
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
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

    private fun fetchAndDownloadVimeoVideo(vimeoUrl: String) {
        val videoId = extractVimeoVideoId(vimeoUrl)
        if (videoId.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.invalid_vimeo_url), Toast.LENGTH_SHORT).show()
            binding.lnrDownloadStatus.visibility = View.GONE
            return
        }

        if (!checkStoragePermission()) {
            pendingDownloadUrl = vimeoUrl
            requestStoragePermission()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getVideoDetails(
                    videoId,
                    "Bearer ${Constant.isVimeoToken}"
                )

                if (response.isSuccessful) {
                    val videoData = response.body()
                    val mp4Link = videoData?.download?.find { it.type == "video/mp4" }?.link

                    if (!mp4Link.isNullOrEmpty()) {
                        downloadFile(mp4Link)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@FilesViewActivity,
                                getString(R.string.no_downloadable_mp4_found),
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lnrDownloadStatus.visibility = View.GONE
                        }
                    }
                } else {
                    Log.e("VimeoAPI", "${response.code()} ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        binding.lnrDownloadStatus.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("VimeoAPI", "Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        adapter.stopAudio()
        super.onStop()
    }

    override fun onDestroy() {
        adapter.stopAudio()
        try {
            unregisterReceiver(downloadCompleteReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w("Download", "Receiver was not registered")
        }
        super.onDestroy()
    }


    private fun downloadFile(url: String) {
        try {
            var fileName = Uri.decode(url.substringAfterLast("/").substringBefore("?"))
            val ext = fileName.substringAfterLast('.', "").lowercase()

            val subFolder = when (ext) {
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

            val mime = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileName.substringAfterLast('.').lowercase())

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setMimeType(mime ?: "application/octet-stream")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "SchoolChimes/Attachments/$subFolder/$fileName"
                )
            request.allowScanningByMediaScanner()

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)
            activeDownloads[downloadId] = "Downloads/SchoolChimes/Attachments/$subFolder/$fileName"
            Log.d("Download", "Enqueued id=$downloadId file=$fileName")

            runOnUiThread {
                binding.lnrDownloadStatus.visibility = View.VISIBLE
//                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("Download", "Download error: ${e.message}", e)
            runOnUiThread {
                binding.lnrDownloadStatus.visibility = View.GONE
                Toast.makeText(this, getString(R.string.Download_failed_2), Toast.LENGTH_SHORT)
                    .show()
            }
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
                            input.copyTo(output)
                        }
                    }
                }
                val uri = FileProvider.getUriForFile(
                    this@FilesViewActivity,
                    "$packageName.fileprovider",
                    file
                )
                val mimeType =
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())
                        ?: contentType
                withContext(Dispatchers.Main) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share File"))
                }

            } catch (e: Exception) {
                Log.e("ShareFile", "Error sharing: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FilesViewActivity,
                        getString(R.string.failed_to_share_file),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.lnrDownloadStatus.visibility = View.GONE
                }
            }
        }
    }



    private fun checkStoragePermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_STORAGE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_STORAGE_PERMISSION) {
            val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
            val url = pendingDownloadUrl
            pendingDownloadUrl = null

            if (granted && url != null) {
                if (url.contains("vimeo.com/video/")) fetchAndDownloadVimeoVideo(url)
                else downloadFile(url)
            } else if (!granted) {
                Toast.makeText(this, getString(R.string.Download_failed_2), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    data class VimeoDownload(
        val quality: String?,
        val type: String?,
        val width: Int?,
        val link: String?
    )

    data class VimeoVideoResponse(val name: String?, val download: List<VimeoDownload>?)

    interface VimeoApiService {
        @GET("videos/{video_id}")
        suspend fun getVideoDetails(
            @Path("video_id") videoId: String,
            @Header("Authorization") auth: String
        ): Response<VimeoVideoResponse>
    }

    object RetrofitClient {
        val apiService: VimeoApiService by lazy {
            Retrofit.Builder().baseUrl("https://api.vimeo.com/")
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(VimeoApiService::class.java)
        }
    }

    fun extractVimeoVideoId(url: String): String? {
        val regex = Regex("vimeo.com/video/(\\d+)")
        return regex.find(url)?.groupValues?.get(1)
    }

    private fun fetchAndShareVimeoVideoFromUrl(vimeoUrl: String) {
        val videoId = extractVimeoVideoId(vimeoUrl)
        if (videoId.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.invalid_vimeo_url), Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getVideoDetails(
                    videoId,
                    "Bearer ${Constant.isVimeoToken}"
                )
                if (response.isSuccessful) {
                    val videoData = response.body()
                    val mp4Link = videoData?.download?.find { it.type == "video/mp4" }?.link
                    if (!mp4Link.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            shareFileFromUrl(mp4Link)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@FilesViewActivity,
                                getString(R.string.no_downloadable_mp4_found),
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lnrDownloadStatus.visibility = View.GONE
                        }
                    }
                } else {
                    Log.e("VimeoAPI", "${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("VimeoAPI", "Error: ${e.message}", e)
            }
        }
    }
}