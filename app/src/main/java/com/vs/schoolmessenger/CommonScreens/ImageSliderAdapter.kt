package com.vs.schoolmessenger.CommonScreens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.Homework.FullScreenViewerActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ImageSliderAdapter(
    private var isSubjectName: String?,
    private var GetFilePathDetailsData: List<FilePath>?,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int = if (isLoading) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.homework_img_pdf_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.homework_img_pdf_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int = if (isLoading) 20 else GetFilePathDetailsData?.size ?: 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && GetFilePathDetailsData != null) {
            holder.bind(GetFilePathDetailsData!!, position, isSubjectName ?: "")
        }
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val DefaultImage: ImageView = itemView.findViewById(R.id.ImgPDF)
        private val ImgOrDocumentType: ImageView = itemView.findViewById(R.id.imageOrDocumentType)
        private val WebViewThumbnail: WebView = itemView.findViewById(R.id.WVThumbnaildocument)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val fileItem: CardView = itemView.findViewById(R.id.fileItem)

        private var triedRawLoad = false

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            fullList: List<FilePath>,
            position: Int,
            isSubjectName: String,
        ) {
            val data = fullList[position]

            WebViewThumbnail.visibility = View.GONE
            DefaultImage.visibility = View.VISIBLE
            loadingBar.visibility = View.GONE

            when (data.type.uppercase()) {
                Constant.IMAGE -> {
                    Glide.with(context).load(data.url).placeholder(R.drawable.image_placeholder)
                        .into(DefaultImage)
                    ImgOrDocumentType.setBackgroundResource(R.drawable.default_image_icon)
                }

//                Constant.VIDEO -> {
//                    loadVideoThumbnail(data.url)
//                    ImgOrDocumentType.setBackgroundResource(R.drawable.video_icon)
//                }

                Constant.AUDIO -> {
                    Glide.with(context).load(R.drawable.voice).into(DefaultImage)
                    ImgOrDocumentType.setBackgroundResource(R.drawable.voice)
                }

                Constant.PDF, Constant.DOC, Constant.DOCX, Constant.TXT, Constant.PPT, Constant.PPTX, Constant.EXCEL -> {
                    ImgOrDocumentType.setImageResource(getIconForType(data.type))
                    openDocumentInWebView(data.url)
                }
            }

            // On item click: full screen viewer
            fileItem.setOnClickListener {
                Constant.commonFileList =
                    fullList.map { CommonFileData(type = it.type, path = it.url) }
                Constant.selectedFileIndex = position
                val intent = Intent(context, FullScreenViewerActivity::class.java)
                intent.putExtra(Constant.subjectName, isSubjectName)
                context.startActivity(intent)
            }

            WebViewThumbnail.setOnTouchListener(object : OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    if (event.action == MotionEvent.ACTION_MOVE) {
                        return false
                    }

                    if (event.action == MotionEvent.ACTION_UP) {
                        Constant.commonFileList.isEmpty()
                        Constant.selectedFileIndex = -1
                        val commonList = fullList.map {
                            CommonFileData(
                                type = it.type, path = it.url
                            )
                        }

                        Constant.commonFileList = commonList
                        Constant.selectedFileIndex = position
                        val intent = Intent(context, FullScreenViewerActivity::class.java)
                        intent.putExtra(Constant.subjectName, isSubjectName)
                        context.startActivity(intent)
                    }
                    return false
                }
            })
        }

        private fun getIconForType(type: String): Int {
            return when (type.uppercase()) {
                Constant.PDF -> R.drawable.hw_pdf_img
                Constant.DOC, Constant.DOCX -> R.drawable.microsoft_word_img
                Constant.TXT -> R.drawable.txt_file_img
                Constant.PPT, Constant.PPTX -> R.drawable.ppt_icon
                Constant.EXCEL -> R.drawable.excel_icon
                else -> R.drawable.doc_icon
            }
        }

        private fun openDocumentInWebView(urlPath: String) {
            loadingBar.visibility = View.VISIBLE
            DefaultImage.visibility = View.GONE
            WebViewThumbnail.visibility = View.VISIBLE

            val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$urlPath"

            WebViewThumbnail.apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        loadingBar.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        loadingBar.visibility = View.GONE
                    }

                    override fun onReceivedError(
                        view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                    ) {
                        Log.e("WebViewError", "Error loading: ${error?.description}")
                        loadingBar.visibility = View.GONE
                        if (!triedRawLoad) {
                            triedRawLoad = true
                            WebViewThumbnail.loadUrl(urlPath)
                        }
                    }
                }

                loadUrl(googleDocsUrl)
            }
        }

//        private fun loadVideoThumbnail(url: String) {
//            try {
//                val retriever = MediaMetadataRetriever()
//                retriever.setDataSource(url, HashMap())
//                val bitmap = retriever.frameAtTime
//                retriever.release()
//
//                if (bitmap != null) {
//                    DefaultImage.setImageBitmap(bitmap)
//                } else {
//                    DefaultImage.setImageResource(R.drawable.video_icon)
//                }
//            } catch (e: Exception) {
//                Log.e("ThumbnailError", "Failed to load video thumbnail", e)
//                DefaultImage.setImageResource(R.drawable.video_icon)
//            }
//        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
