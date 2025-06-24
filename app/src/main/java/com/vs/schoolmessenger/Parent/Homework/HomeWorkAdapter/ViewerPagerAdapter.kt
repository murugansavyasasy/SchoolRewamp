package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.bumptech.glide.request.RequestListener
import com.github.chrisbanes.photoview.PhotoView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

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
            holder.imageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(item.path)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.loadingBar.visibility = View.GONE
                        e?.printStackTrace()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.loadingBar.visibility = View.GONE
                        holder.imageView.setImageDrawable(resource)
                        return true
                    }
                })
                .into(holder.imageView)

            // enableZoomOnImage(holder.imageView)
        } else {
            var isFile = ""
            holder.documentWebView.visibility = View.VISIBLE

            if (item.type == Constant.VIDEO) {
                isFile = item.path
            } else {
                isFile = "https://docs.google.com/gview?embedded=true&url=${item.path}"
            }
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
    }

    override fun getItemCount(): Int = fileList.size

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val documentWebView: WebView = itemView.findViewById(R.id.documentWebView)
        val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        val imageView: PhotoView = itemView.findViewById(R.id.imageView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableZoomOnImage(imageView: ImageView) {
        val matrix = Matrix()
        val savedMatrix = Matrix()

        val startPoint = PointF()
        val midPoint = PointF()
        var oldDist = 1f
        var mode = NONE

        imageView.setOnTouchListener { v, event ->
            val view = v as ImageView
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    matrix.set(view.imageMatrix)
                    savedMatrix.set(matrix)
                    startPoint.set(event.x, event.y)
                    mode = DRAG
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = spacing(event)
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix)
                        midPoint(midPoint, event)
                        mode = ZOOM
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mode == DRAG) {
                        matrix.set(savedMatrix)
                        matrix.postTranslate(event.x - startPoint.x, event.y - startPoint.y)
                    } else if (mode == ZOOM) {
                        val newDist = spacing(event)
                        if (newDist > 10f) {
                            matrix.set(savedMatrix)
                            val scale = newDist / oldDist
                            matrix.postScale(scale, scale, midPoint.x, midPoint.y)
                        }
                    }
                }
            }
            view.imageMatrix = matrix
            true
        }
    }

    companion object {
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2

        fun spacing(event: MotionEvent): Float {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return kotlin.math.sqrt(x * x + y * y)
        }

        fun midPoint(point: PointF, event: MotionEvent) {
            val x = event.getX(0) + event.getX(1)
            val y = event.getY(0) + event.getY(1)
            point.set(x / 2, y / 2)
        }
    }
}
