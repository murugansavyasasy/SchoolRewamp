package com.vs.schoolmessenger.Utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
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

class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>(),
    View.OnClickListener {
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private lateinit var adapter: FileViewerAdapter
    private var currentPosition = 0

    override fun getViewBinding(): HomeworkViewImageDocumentBinding =
        HomeworkViewImageDocumentBinding.inflate(layoutInflater)

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

            if (first.path.contains("amazonaws.") || first.type == FileType.VIDEO.toString()) {
                binding.imgMoreOptions.visibility = View.VISIBLE
            } else {
                binding.imgMoreOptions.visibility = View.GONE
            }
        }

        adapter = FileViewerAdapter(this, Constant.commonFileList)
        val noScrollLayoutManager = object : LinearLayoutManager(this, HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean = false
            override fun canScrollVertically(): Boolean = false
        }

        binding.rcyFile.layoutManager = noScrollLayoutManager
        binding.rcyFile.adapter = adapter

        binding.lnrNext.visibility =
            if (Constant.commonFileList.size <= 1) View.GONE else View.VISIBLE
        binding.lnrPrevious.visibility =
            if (Constant.commonFileList.size <= 1) View.GONE else View.VISIBLE

        if (Constant.commonFileList.size > 1) binding.indicator.attachToRecyclerView(binding.rcyFile)
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
            R.id.imgMoreOptions -> showFileOptions(Constant.commonFileList[currentPosition].path)
            R.id.lnrNext -> if (currentPosition < Constant.commonFileList.size - 1) {
                currentPosition++
                scrollToPosition(currentPosition)
                updateNavButtons()
            }

            R.id.lnrPrevious -> if (currentPosition > 0) {
                currentPosition--
                scrollToPosition(currentPosition)
                updateNavButtons()
            }
        }
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
                    binding.lnrDownloadStatus.visibility = View.VISIBLE
                    if (url.contains("vimeo.com/video/")) fetchAndDownloadVimeoVideo(url)
                    else if (checkStoragePermission()) downloadFile(url)
                    else requestStoragePermission()
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
            Toast.makeText(this, "Invalid Vimeo URL", Toast.LENGTH_SHORT).show()
            binding.lnrDownloadStatus.visibility = View.GONE
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
                        downloadFile(mp4Link)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@FullScreenViewerActivity,
                                "No downloadable .mp4 found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        withContext(Dispatchers.Main) {
                            binding.lnrDownloadStatus.visibility = View.GONE
                        }
                    }
                } else {
                    Log.e("VimeoAPI", "${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("VimeoAPI", "Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                }
            }
        }
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
                        this@FullScreenViewerActivity,
                        arrayOf(file.absolutePath),
                        null,
                        null
                    )
                }

                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                    Toast.makeText(
                        this@FullScreenViewerActivity,
                        "File saved to Downloads/$baseFolderName/$subFolderPath/$fileName",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("Download", "Download error: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.lnrDownloadStatus.visibility = View.GONE
                    Toast.makeText(
                        this@FullScreenViewerActivity,
                        "Download failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                            input.copyTo(
                                output
                            )
                        }
                    }
                }
                val uri = FileProvider.getUriForFile(
                    this@FullScreenViewerActivity,
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
                        this@FullScreenViewerActivity,
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

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permission, 101)
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
            Toast.makeText(this, "Invalid Vimeo URL", Toast.LENGTH_SHORT).show()
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
                                this@FullScreenViewerActivity,
                                "No downloadable .mp4 found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        withContext(Dispatchers.Main) {
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