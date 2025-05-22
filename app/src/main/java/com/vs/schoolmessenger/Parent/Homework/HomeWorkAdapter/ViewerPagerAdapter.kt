package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R

class FileViewerAdapter(
    private val context: Context,
    private val fileList: ArrayList<GetFilePathDetails>
) : RecyclerView.Adapter<FileViewerAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.homework_view_image_document_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        val fileUrl = file.path
        val fileType = file.type.uppercase()

        holder.documentWebView.visibility = View.GONE
        holder.imageView.visibility = View.GONE
        holder.loadingBar.visibility = View.VISIBLE

        if (fileType == "IMAGE") {
            holder.imageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(fileUrl)
                .listener(object :
                    com.bumptech.glide.request.RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.loadingBar.visibility = View.GONE
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
                        return false
                    }
                })
                .into(holder.imageView)

        } else {
            holder.documentWebView.visibility = View.VISIBLE
            val googleViewerUrl = "https://docs.google.com/gview?embedded=true&url=$fileUrl"
            holder.documentWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    holder.loadingBar.visibility = View.GONE
                }
            }

            holder.documentWebView.settings.apply {
                javaScriptEnabled = true
                setSupportZoom(true)
                allowFileAccess = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }

            holder.documentWebView.loadUrl(googleViewerUrl)
            Log.d("Document Loaded URL", googleViewerUrl)
        }
    }
    override fun getItemCount(): Int = fileList.size
    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val documentWebView: WebView = itemView.findViewById(R.id.documentWebView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
    }
}

