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

class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>(), View.OnClickListener {
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

        adapter = FileViewerAdapter(this, Constant.commonFileList)
        val noScrollLayoutManager = object : LinearLayoutManager(this, HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean = false
            override fun canScrollVertically(): Boolean = false
        }

        binding.rcyFile.layoutManager = noScrollLayoutManager
        binding.rcyFile.adapter = adapter

        binding.lnrNext.visibility = if (Constant.commonFileList.size <= 1) View.GONE else View.VISIBLE
        binding.lnrPrevious.visibility = if (Constant.commonFileList.size <= 1) View.GONE else View.VISIBLE
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
        binding.imgMoreOptions.visibility = View.VISIBLE
    }

    private fun updateNavButtons() {
        binding.lnrPrevious.visibility = if (currentPosition > 0) View.VISIBLE else View.GONE
        binding.lnrNext.visibility = if (currentPosition < Constant.commonFileList.size - 1) View.VISIBLE else View.GONE
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
                            val url = Constant.commonFileList[currentPosition].path
                            if (url.contains("vimeo.com/video/")) {
                                fetchAndShareVimeoVideoFromUrl(url)
                            } else {
                                shareFileFromUrl(url)
                            }
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
                        FileOutputStream(file).use { output -> input.copyTo(output) }
                    }
                }

                val uri = FileProvider.getUriForFile(this@FullScreenViewerActivity, "$packageName.fileprovider", file)
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(file.extension.lowercase()) ?: contentType

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
                    Toast.makeText(this@FullScreenViewerActivity, "Failed to share file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 101)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
        }
    }

    private fun downloadFile(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileName = url.substringAfterLast("/").substringBefore("?")
                val folderName = "SchoolChimes"
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, folderName)
                if (!appDir.exists()) appDir.mkdirs()

                val file = File(appDir, fileName)
                if (!file.exists()) {
                    val connection = URL(url).openConnection()
                    connection.getInputStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Trigger media scan for gallery/file browser visibility
                    MediaScannerConnection.scanFile(this@FullScreenViewerActivity, arrayOf(file.absolutePath), null, null)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FullScreenViewerActivity,
                        "File downloaded to Downloads/$folderName/$fileName",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("Download", "Download error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FullScreenViewerActivity, "Download failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


//    private fun downloadFile(fileUrl: String) {
//        Log.d("fileUrl",fileUrl)
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val url = URL(fileUrl)
//                val connection = url.openConnection()
//                connection.connect()
//
//                val fileName = fileUrl.substringAfterLast("/").substringBefore("?")
//                val folderName = "SchoolChimes"
//                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                val appDir = File(downloadsDir, folderName)
//                if (!appDir.exists()) appDir.mkdirs()
//
//                val file = File(appDir, fileName)
//                if (file.exists()) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@FullScreenViewerActivity, "File already downloaded", Toast.LENGTH_SHORT).show()
//                    }
//                    return@launch
//                }
//
//                connection.getInputStream().use { input ->
//                    FileOutputStream(file).use { output -> input.copyTo(output) }
//                }
//
//                MediaScannerConnection.scanFile(this@FullScreenViewerActivity, arrayOf(file.absolutePath), null, null)
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@FullScreenViewerActivity, "File downloaded to ${file.absolutePath}", Toast.LENGTH_LONG).show()
//                }
//            } catch (e: Exception) {
//                Log.e("DownloadFile", "Error: ${e.message}", e)
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@FullScreenViewerActivity, "Download failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

    data class VimeoDownload(val quality: String?, val type: String?, val width: Int?, val link: String?)
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
            Retrofit.Builder()
                .baseUrl("https://api.vimeo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
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
                    "Bearer " + Constant.isVimeoToken // Replace with actual token
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
                            Toast.makeText(this@FullScreenViewerActivity, "No downloadable .mp4 found", Toast.LENGTH_SHORT).show()
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