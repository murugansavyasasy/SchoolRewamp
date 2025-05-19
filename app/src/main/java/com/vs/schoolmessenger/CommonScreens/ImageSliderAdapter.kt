package com.vs.schoolmessenger.CommonScreens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.webkit.WebView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath
import com.vs.schoolmessenger.Utils.Constant
import java.net.URLEncoder

class ImageSliderAdapter(
    private val context: Context,
    private val imageUrls: List<FilePath>,
) : RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    interface OnFileClickListener {
        fun onItemPDFClick(filePath: FilePath)
    }

    var isFileUrl = ""


    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.sliderImage)
        val progressBar: ProgressBar = view.findViewById(R.id.loadingBar)
        val webPdf: WebView = view.findViewById(R.id.webPdf)
        val loadingBar: ProgressBar = view.findViewById(R.id.loadingBar)
        val imgTypeOfFiles: ImageView = view.findViewById(R.id.imgTypeOfFiles)
        val webFileLoad: RelativeLayout = view.findViewById(R.id.webFileLoad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_slider_view_list, parent, false)
        return ImageViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val filePath = imageUrls[position].url
        val fileType = imageUrls[position].type

        when (fileType) {
            Constant.PPT, Constant.PPTX -> holder.imgTypeOfFiles.setBackgroundResource(R.drawable.ppt_icon)
            Constant.PDF -> holder.imgTypeOfFiles.setBackgroundResource(R.drawable.hw_pdf_img)
            Constant.EXCEL -> holder.imgTypeOfFiles.setBackgroundResource(R.drawable.excel_icon)
            Constant.DOC, Constant.DOCX -> holder.imgTypeOfFiles.setBackgroundResource(R.drawable.microsoft_word_img)
            Constant.TXT -> holder.imgTypeOfFiles.setBackgroundResource(R.drawable.txt_file_img)
        }

        holder.progressBar.visibility = View.VISIBLE

        if (fileType == Constant.IMAGE) {
            holder.imageView.visibility = View.VISIBLE
            holder.webFileLoad.visibility = View.GONE

            Glide.with(context)
                .load(filePath)
                .listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.progressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(holder.imageView)
        } else if (
            fileType == Constant.PDF ||
            fileType == Constant.DOC ||
            fileType == Constant.DOCX ||
            fileType == Constant.PPT ||
            fileType == Constant.PPTX ||
            fileType == Constant.TXT ||
            fileType == Constant.EXCEL
        ) {
            holder.imageView.visibility = View.GONE
            holder.webFileLoad.visibility = View.VISIBLE
            // WebView settings
            holder.webPdf.settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
            }

            holder.webPdf.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    holder.progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    holder.progressBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    holder.progressBar.visibility = View.GONE
                    Log.e("WebViewError", "Error loading file: ${error?.description}")
                }
            }

            holder.webPdf.webChromeClient = WebChromeClient()
            if (filePath.startsWith("http")) {
                val encodedUrl = URLEncoder.encode(filePath, "UTF-8")
                val viewerUrl =
                    "https://drive.google.com/viewerng/viewer?embedded=true&url=$encodedUrl"
                holder.webPdf.loadUrl(viewerUrl)
                isFileUrl = viewerUrl
            } else {
                holder.webPdf.loadDataWithBaseURL(
                    null,
                    "<html><body><h3>Cannot preview local file</h3></body></html>",
                    "text/html",
                    "UTF-8",
                    null
                )
            }

            holder.webPdf.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val intent = Intent(context, ImageViewActivity::class.java)
                    intent.putExtra(Constant.isFileUrl, imageUrls[position].url)
                    intent.putExtra(Constant.isFileType, imageUrls[position].type)
                    context.startActivity(intent)
                }
                false
            }
        }

        holder.imageView.setOnClickListener {
            val intent = Intent(context, ImageViewActivity::class.java)
            intent.putExtra(Constant.isFileUrl, imageUrls[position].url)
            intent.putExtra(Constant.isFileType, Constant.isImage)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = imageUrls.size
}
