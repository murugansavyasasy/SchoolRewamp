package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.FilePath
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class EventFilePathAdapter (

    private var GetFilePathDetailsData: List<FilePath>?,
    private var context: Context,
    private var isLoading: Boolean
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.homework_img_pdf_item)
            UnifiedVoiceAdapter.ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.homework_img_pdf_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else GetFilePathDetailsData?.size ?: 0

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {

            holder.bind(GetFilePathDetailsData!![position], position, this)
        }
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val DefaultImage: ImageView = itemView.findViewById(R.id.ImgPDF)
        private val ImgOrDocumentType: ImageView = itemView.findViewById(R.id.imageOrDocumentType)
        private val WebViewThumbnail: WebView = itemView.findViewById(R.id.WVThumbnaildocument)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)

        private val fileItem: CardView = itemView.findViewById(R.id.fileItem)
        @SuppressLint("ClickableViewAccessibility")
        fun bind(


            data: FilePath?,
            position: Int,
            adapter: EventFilePathAdapter,
        ) {

            Log.d("GetFileDetails", data.toString())
            if (data?.url.isNullOrEmpty()) {
                Log.e("FilePathAdapter", "Invalid URL at position $position")
                return
            }
            when (data?.type?.uppercase()) {
                Constant.IMAGE -> {
                    Glide.with(context)
                        .load(data.url)
                        .placeholder(R.drawable.image_placeholder)
                        .into(DefaultImage)

                    ImgOrDocumentType.setBackgroundResource(R.drawable.default_image_icon)
                    WebViewThumbnail.visibility = View.GONE
                    DefaultImage.visibility = View.VISIBLE
                }

                Constant.PDF -> {
                    ImgOrDocumentType.setBackgroundResource(R.drawable.hw_pdf_img)
                    openDocumentInWebView(data.url)
                }

                Constant.DOC, Constant.DOCX -> {
                    ImgOrDocumentType.setBackgroundResource(R.drawable.microsoft_word_img)
                    openDocumentInWebView(data.url)
                }

                Constant.TXT -> {
                    ImgOrDocumentType.setBackgroundResource(R.drawable.txt_file_img)
                    openDocumentInWebView(data.url)
                }

                Constant.PPT, Constant.PPTX -> {
                    ImgOrDocumentType.setBackgroundResource(R.drawable.ppt_icon)
                    openDocumentInWebView(data.url)
                }

                Constant.EXCEL -> {
                    ImgOrDocumentType.setBackgroundResource(R.drawable.excel_icon)
                    openDocumentInWebView(data.url)
                }
            }

            fileItem.setOnClickListener {
                Constant.commonFileList.isEmpty()
                Constant.selectedFileIndex=-1
                val commonList = adapter.GetFilePathDetailsData?.map {
                    CommonFileData(
                        type = it.type,
                        path = it.url
                    )
                }?.toMutableList() ?: mutableListOf()

                Constant.commonFileList = commonList

                Constant.selectedFileIndex = position

                val intent = Intent(context, FilesViewActivity::class.java)
                context.startActivity(intent)
            }




            WebViewThumbnail.setOnTouchListener(object : OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        return false
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Constant.commonFileList.isEmpty()
                        Constant.selectedFileIndex=-1
                        val commonList = adapter.GetFilePathDetailsData?.map {
                            CommonFileData(
                                type = it.type,
                                path = it.url
                            )
                        }?.toMutableList() ?: mutableListOf()

                        Constant.commonFileList = commonList

                        Constant.selectedFileIndex = position

                        val intent = Intent(context, FilesViewActivity::class.java)
                        context.startActivity(intent)
                    }

                    return false
                }
            })
        }

        private fun openDocumentInWebView(urlPath: String) {
            loadingBar.visibility = View.VISIBLE

            val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$urlPath"

            DefaultImage.visibility = View.GONE
            WebViewThumbnail.visibility = View.VISIBLE
            WebViewThumbnail.setOnTouchListener(null)
            WebViewThumbnail.settings.javaScriptEnabled = true
            WebViewThumbnail.settings.domStorageEnabled = true
            WebViewThumbnail.settings.loadWithOverviewMode = true
            WebViewThumbnail.settings.useWideViewPort = true

            WebViewThumbnail.webViewClient = object : WebViewClient() {
                override fun onPageStarted(
                    view: WebView?, url: String?, favicon: android.graphics.Bitmap?
                ) {
                    loadingBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    loadingBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                ) {
                    loadingBar.visibility = View.GONE
                    Log.e("WebViewError", "Error loading: ${error?.description}")
                }
            }

            WebViewThumbnail.loadUrl(googleDocsUrl)
        }



        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)

            init {
                shimmerLayout.startShimmer() // Start shimmer effect
            }
        }
    }
}