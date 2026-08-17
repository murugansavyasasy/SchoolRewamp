package com.vs.schoolmessenger.Utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.R
import java.net.URLEncoder

class FileViewerAdapter(
    private val context: Context,
    private val fileList: List<CommonFileData>
) : RecyclerView.Adapter<FileViewerAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.homework_view_image_document_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val item = fileList[position]

        holder.documentWebView.visibility = View.GONE
        holder.imageView.visibility = View.GONE
        holder.loadingBar.visibility = View.VISIBLE

        if (item.type == Constant.IMAGE) {
            holder.lblVideoAvailable.visibility = View.GONE
            holder.documentWebView.visibility = View.GONE
            holder.loadingBar.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(item.path)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.loadingBar.visibility = View.GONE
                        e?.printStackTrace()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.loadingBar.visibility = View.GONE
                        holder.imageView.setImageDrawable(resource)
                        return true
                    }
                })
                .into(holder.imageView)
        } else {

            if (item.type == Constant.VIDEO) {

                if (item.path.contains("player.vimeo.com")) {

                    Log.d(
                        "isWithin30Minutes",
                        if (!Constant.isVideoPostedDate.isNullOrBlank())
                            Constant.isWithin30Minutes(Constant.isVideoPostedDate).toString()
                        else
                            "false"
                    )

                    Log.d("isVideoPostedDate", Constant.isVideoPostedDate ?: "null")

                    if (Constant.SELECTED_MENU_ID != Constant.M_QUIZ_EXAM) {

                        if (!Constant.isVideoPostedDate.isNullOrBlank() &&
                            Constant.isWithin30Minutes(Constant.isVideoPostedDate)
                        ) {

                            holder.lblVideoAvailable.visibility = View.VISIBLE
                            holder.documentWebView.visibility = View.GONE
                            holder.loadingBar.visibility = View.GONE
                            holder.imageView.visibility = View.GONE

                        } else {

                            holder.lblVideoAvailable.visibility = View.GONE
                            holder.documentWebView.visibility = View.VISIBLE
                            holder.loadingBar.visibility = View.GONE
                            holder.imageView.visibility = View.GONE

                            var isFile = ""
                            holder.documentWebView.visibility = View.VISIBLE

                            isFile = item.path

                            holder.documentWebView.settings.apply {
                                javaScriptEnabled = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                domStorageEnabled = true
                            }

                            holder.documentWebView.setInitialScale(1)
                            holder.documentWebView.scrollBarStyle =
                                WebView.SCROLLBARS_INSIDE_OVERLAY
                            holder.documentWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

                            holder.documentWebView.webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    holder.loadingBar.visibility = View.GONE
                                }
                            }

                            holder.documentWebView.loadUrl(isFile)
                        }

                    } else {

                        holder.lblVideoAvailable.visibility = View.GONE
                        holder.documentWebView.visibility = View.VISIBLE
                        holder.loadingBar.visibility = View.GONE
                        holder.imageView.visibility = View.GONE

                        var isFile = ""
                        holder.documentWebView.visibility = View.VISIBLE

                        isFile = item.path

                        holder.documentWebView.settings.apply {
                            javaScriptEnabled = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            domStorageEnabled = true
                        }

                        holder.documentWebView.setInitialScale(1)
                        holder.documentWebView.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
                        holder.documentWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

                        holder.documentWebView.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                holder.loadingBar.visibility = View.GONE
                            }
                        }

                        holder.documentWebView.loadUrl(isFile)
                    }

                } else {

                    holder.lblVideoAvailable.visibility = View.GONE
                    holder.documentWebView.visibility = View.VISIBLE
                    holder.loadingBar.visibility = View.GONE
                    holder.imageView.visibility = View.GONE

                    var isFile = ""
                    holder.documentWebView.visibility = View.VISIBLE

                    isFile = item.path

                    holder.documentWebView.settings.apply {
                        javaScriptEnabled = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        domStorageEnabled = true
                    }

                    holder.documentWebView.setInitialScale(1)
                    holder.documentWebView.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
                    holder.documentWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    holder.documentWebView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            holder.loadingBar.visibility = View.GONE
                        }
                    }

                    holder.documentWebView.loadUrl(isFile)
                }

            } else if (item.type == Constant.AUDIO) {

//                if (item.path.contains("player.vimeo.com")) {
//
//
//                    Log.d("isVideoPostedDate", Constant.isVideoPostedDate ?: "null")
//
//                    if (Constant.SELECTED_MENU_ID != Constant.M_QUIZ_EXAM) {
//
//                        if (!Constant.isVideoPostedDate.isNullOrBlank() &&
//                            Constant.isWithin30Minutes(Constant.isVideoPostedDate)
//                        ) {
//
//                            holder.lblVideoAvailable.visibility = View.VISIBLE
//                            holder.documentWebView.visibility = View.GONE
//                            holder.loadingBar.visibility = View.GONE
//                            holder.imageView.visibility = View.GONE
//
//                        } else {
//
//                            holder.lblVideoAvailable.visibility = View.GONE
//                            holder.documentWebView.visibility = View.VISIBLE
//                            holder.loadingBar.visibility = View.GONE
//                            holder.imageView.visibility = View.GONE
//
//                            var isFile = ""
//                            holder.documentWebView.visibility = View.VISIBLE
//
//                            isFile = item.path
//
//                            holder.documentWebView.settings.apply {
//                                javaScriptEnabled = true
//                                setSupportZoom(true)
//                                builtInZoomControls = true
//                                displayZoomControls = false
//                                loadWithOverviewMode = true
//                                useWideViewPort = true
//                                domStorageEnabled = true
//                            }
//
//                            holder.documentWebView.setInitialScale(1)
//                            holder.documentWebView.scrollBarStyle =
//                                WebView.SCROLLBARS_INSIDE_OVERLAY
//                            holder.documentWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
//
//                            holder.documentWebView.webViewClient = object : WebViewClient() {
//                                override fun onPageFinished(view: WebView?, url: String?) {
//                                    holder.loadingBar.visibility = View.GONE
//                                }
//                            }
//
//                            holder.documentWebView.loadUrl(isFile)
//                        }
//
//                    } else {
//
//                        holder.lblVideoAvailable.visibility = View.GONE
//                        holder.documentWebView.visibility = View.VISIBLE
//                        holder.loadingBar.visibility = View.GONE
//                        holder.imageView.visibility = View.GONE
//
//                        var isFile = ""
//                        holder.documentWebView.visibility = View.VISIBLE
//
//                        isFile = item.path
//
//                        holder.documentWebView.settings.apply {
//                            javaScriptEnabled = true
//                            setSupportZoom(true)
//                            builtInZoomControls = true
//                            displayZoomControls = false
//                            loadWithOverviewMode = true
//                            useWideViewPort = true
//                            domStorageEnabled = true
//                        }
//
//                        holder.documentWebView.setInitialScale(1)
//                        holder.documentWebView.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
//                        holder.documentWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
//
//                        holder.documentWebView.webViewClient = object : WebViewClient() {
//                            override fun onPageFinished(view: WebView?, url: String?) {
//                                holder.loadingBar.visibility = View.GONE
//                            }
//                        }
//
//                        holder.documentWebView.loadUrl(isFile)
//                    }

//                } else {


                currentAudioWebView = holder.documentWebView

                holder.lblVideoAvailable.visibility = View.GONE
                holder.documentWebView.visibility = View.VISIBLE
                holder.loadingBar.visibility = View.GONE
                holder.imageView.visibility = View.GONE

                holder.documentWebView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }

                holder.documentWebView.setInitialScale(1)

                holder.documentWebView.scrollBarStyle =
                    WebView.SCROLLBARS_INSIDE_OVERLAY

                holder.documentWebView.setLayerType(
                    View.LAYER_TYPE_HARDWARE,
                    null
                )

                holder.documentWebView.webViewClient =
                    object : WebViewClient() {

                        override fun onPageFinished(
                            view: WebView?,
                            url: String?
                        ) {
                            holder.loadingBar.visibility = View.GONE
                        }
                    }

                holder.documentWebView.loadUrl(item.path)

//                    holder.lblVideoAvailable.visibility = View.GONE
//                    holder.documentWebView.visibility = View.VISIBLE
//                    holder.loadingBar.visibility = View.GONE
//                    holder.imageView.visibility = View.GONE
//
//                    var isFile = ""
//                    holder.documentWebView.visibility = View.VISIBLE
//
//                    isFile = item.path
//
//                    holder.documentWebView.settings.apply {
//                        javaScriptEnabled = true
//                        setSupportZoom(true)
//                        builtInZoomControls = true
//                        displayZoomControls = false
//                        loadWithOverviewMode = true
//                        useWideViewPort = true
//                        domStorageEnabled = true
//                    }
//
//                    holder.documentWebView.setInitialScale(1)
//                    holder.documentWebView.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
//                    holder.documentWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
//
//                    holder.documentWebView.webViewClient = object : WebViewClient() {
//                        override fun onPageFinished(view: WebView?, url: String?) {
//                            holder.loadingBar.visibility = View.GONE
//                        }
//                    }
//
//                    holder.documentWebView.loadUrl(isFile)
//                }

            } else {

                holder.documentWebView.visibility = View.VISIBLE

                val encodedUrl = URLEncoder.encode(item.path, "UTF-8")
                val isFile = "https://docs.google.com/gview?embedded=true&url=$encodedUrl"

                holder.documentWebView.apply {
                    clearCache(true)
                    clearHistory()

                    settings.apply {
                        javaScriptEnabled = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                    }

                    setInitialScale(1)
                    scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                }

                holder.documentWebView.webViewClient = object : WebViewClient() {

                    override fun onPageFinished(view: WebView?, url: String?) {
                        holder.loadingBar.visibility = View.GONE
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        holder.loadingBar.visibility = View.GONE
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.path))
                        context.startActivity(intent)
                    }
                }

                holder.documentWebView.loadUrl(isFile)
            }
        }
    }

    fun stopAudio() {

        currentAudioWebView?.let { webView ->

            webView.post {

                webView.evaluateJavascript(
                    """
                (function() {
                    var media = document.querySelectorAll('audio, video');

                    media.forEach(function(element) {
                        try {
                            element.pause();
                            element.currentTime = 0;
                        } catch (e) {
                            console.log(e);
                        }
                    });

                    return true;
                })();
                """.trimIndent(),
                    null
                )
            }
        }

        currentAudioWebView = null
    }

    override fun getItemCount(): Int = fileList.size

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val documentWebView: WebView = itemView.findViewById(R.id.documentWebView)
        val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        val imageView: PhotoView = itemView.findViewById(R.id.imageView)
        val lblVideoAvailable: TextView = itemView.findViewById(R.id.lblVideoAvailable)
    }
    private var currentAudioWebView: WebView? = null
}