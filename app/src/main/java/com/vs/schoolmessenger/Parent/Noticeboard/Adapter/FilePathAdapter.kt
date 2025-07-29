package com.vs.schoolmessenger.Parent.Noticeboard.Adapter

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
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.imageview.ShapeableImageView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.Parent.Noticeboard.FilePath
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class FilePathAdapter (

    private var visibleList: List<FilePath>,
    private var fullList: List<FilePath>,
    private var context: Context,
    private var isLoading: Boolean
):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var visibleCount = 3

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.attachement_rewamp_recycler)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.attachement_rewamp_recycler, parent, false)
            DataViewHolder(view, context)
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) {
            20
        } else {
            visibleList.size
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(visibleList[position], position, fullList, context)
        }
    }






    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val DefaultImage: ShapeableImageView = itemView.findViewById(R.id.ImgPDF)
//        private val ImgOrDocumentType:ImageView=itemView.findViewById(R.id.imageOrDocumentType)
//        private val WebViewThumbnail:WebView=itemView.findViewById(R.id.WVThumbnaildocument)

//        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)

        private val fileItem: RelativeLayout = itemView.findViewById(R.id.fileItem)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: FilePath?,
            position: Int,
            fullList: List<FilePath>,
            context: Context
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

//                    ImgOrDocumentType.setBackgroundResource(R.drawable.default_image_icon)
//                    WebViewThumbnail.visibility = View.GONE
                    DefaultImage.visibility = View.VISIBLE
                }

                Constant.PDF -> {
                    DefaultImage.setBackgroundResource(R.drawable.hw_pdf_img)
                    openDocumentInWebView(data.url)
                }

                Constant.DOC, Constant.DOCX -> {
                    DefaultImage.setBackgroundResource(R.drawable.microsoft_word_img)
                    openDocumentInWebView(data.url)
                }

                Constant.TXT -> {
                    DefaultImage.setBackgroundResource(R.drawable.txt_file_img)
                    openDocumentInWebView(data.url)
                }

                Constant.PPT, Constant.PPTX -> {
                    DefaultImage.setBackgroundResource(R.drawable.ppt_icon)
                    openDocumentInWebView(data.url)
                }

                Constant.EXCEL -> {
                    DefaultImage.setBackgroundResource(R.drawable.excel_icon)
                    openDocumentInWebView(data.url)
                }
            }

            fileItem.setOnClickListener {
                val commonList = fullList.map {
                    CommonFileData(type = it.type, path = it.url)
                }.toMutableList()

                Constant.commonFileList = commonList
                Constant.selectedFileIndex = fullList.indexOf(data)

                val intent = Intent(context, FilesViewActivity::class.java)
                context.startActivity(intent)
            }




//            WebViewThumbnail.setOnTouchListener(object : OnTouchListener {
//                override fun onTouch(v: View?, event: MotionEvent): Boolean {
//                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        return false
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        Constant.commonFileList.isEmpty()
//                        Constant.selectedFileIndex=-1
//                        val commonList = adapter.GetFilePathDetailsData?.map {
//                            CommonFileData(
//                                type = it.type,
//                                path = it.url,
//                            )
//                        }?.toMutableList() ?: mutableListOf()
//
//                        Constant.commonFileList = commonList
//
//                        Constant.selectedFileIndex = position
//
//                        val intent = Intent(context, FilesViewActivity::class.java)
//                        context.startActivity(intent)
//                    }
//
//                    return false
//                }
//            })
        }

        private fun openDocumentInWebView(urlPath: String) {
//            loadingBar.visibility = View.VISIBLE

            val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$urlPath"

            DefaultImage.visibility = View.GONE
//            WebViewThumbnail.visibility = View.VISIBLE
//            WebViewThumbnail.setOnTouchListener(null)
//            WebViewThumbnail.settings.javaScriptEnabled = true
//            WebViewThumbnail.settings.domStorageEnabled = true
//            WebViewThumbnail.settings.loadWithOverviewMode = true
//            WebViewThumbnail.settings.useWideViewPort = true

//            WebViewThumbnail.webViewClient = object : WebViewClient() {
//                override fun onPageStarted(
//                    view: WebView?, url: String?, favicon: android.graphics.Bitmap?
//                ) {
//                    loadingBar.visibility = View.VISIBLE
//                }
//
//                override fun onPageFinished(view: WebView?, url: String?) {
//                    loadingBar.visibility = View.GONE
//                }
//
//                override fun onReceivedError(
//                    view: WebView?, request: WebResourceRequest?, error: WebResourceError?
//                ) {
//                    loadingBar.visibility = View.GONE
//                    Log.e("WebViewError", "Error loading: ${error?.description}")
//                }
//            }

//            WebViewThumbnail.loadUrl(googleDocsUrl)
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