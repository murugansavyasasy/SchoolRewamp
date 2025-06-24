package com.vs.schoolmessenger.Parent.Attachment.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentClickListener
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentData
import com.vs.schoolmessenger.Parent.Homework.FullScreenViewerActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator2

class AttachmentAdapter(
    private var attachmentList: List<AttachmentData>?,
    private val listener: AttachmentClickListener,
    private val context: Context,
    var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.homework_school_reportitem)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.homework_school_reportitem, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isLoading && holder is DataViewHolder) {
            holder.bind(attachmentList!![position], listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else attachmentList?.size ?: 0
    }

    fun updateList(newList: List<AttachmentData>) {
        this.attachmentList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val LblHWSubjectName: TextView = itemView.findViewById(R.id.LblHWSubjectName)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val rlaSelectText: RelativeLayout = itemView.findViewById(R.id.rlaSelectText)
        private val rytList: RelativeLayout = itemView.findViewById(R.id.rytList)
        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val imgNewImage: ImageView = itemView.findViewById(R.id.imgNewImage)
        private val webView: android.webkit.WebView = itemView.findViewById(R.id.webView)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val indicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)

        fun bind(item: AttachmentData, listener: AttachmentClickListener) {

            LblHWSubjectName.visibility = View.GONE
            imgNewImage.visibility = View.VISIBLE
            rlaSelectText.visibility = View.GONE
            lblTitleImage.text = item.title
            lblContentImage.text = item.description
            lblDateImage.text = Constant.convertDateTimeFormat(item.date)

            webView.setOnTouchListener(object : OnTouchListener {
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        return false
                    }

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Constant.commonFileList.isEmpty()
                        Constant.selectedFileIndex = -1
                        val commonList = item.file_path?.map {
                            CommonFileData(
                                type = it.type,
                                path = it.url,
                            )
                        } ?: emptyList()

                        Constant.commonFileList = commonList
                        Constant.selectedFileIndex = position

                        val intent = Intent(context, FullScreenViewerActivity::class.java)
                        intent.putExtra(Constant.subjectName, item.title)
                        context.startActivity(intent)
                    }

                    return false
                }
            })

            if (item.iframe != "") {
                webView.visibility = View.VISIBLE
                rytList.visibility = View.VISIBLE
                rcyImgPDF.visibility = View.GONE
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
                webView.settings.loadWithOverviewMode = true
                webView.settings.useWideViewPort = true

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(
                        view: android.webkit.WebView, url: String, favicon: Bitmap?
                    ) {
                        loadingBar.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                        loadingBar.visibility = View.GONE
                    }

                    override fun onReceivedError(
                        view: android.webkit.WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        loadingBar.visibility = View.GONE
                        Log.e("WebViewError", "Error loading: ${error?.description}")
                    }
                }

                webView.loadUrl(item.file_path[0].url.toString())
            } else {

                if (item.file_path.size > 1) {
                    indicator.visibility = View.VISIBLE
                } else {
                    indicator.visibility = View.GONE
                }

                webView.visibility = View.GONE
                rytList.visibility = View.VISIBLE
                rcyImgPDF.visibility = View.VISIBLE
                rcyImgPDF.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rcyImgPDF.adapter = AttachmentFilePathAdapter(
                    item.file_path, context, Constant.isShimmerViewDisable
                )
                indicator.attachToRecyclerView(rcyImgPDF)
            }

            itemView.setOnClickListener {
                listener.onItemClick(item, this)
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
                }
            })

            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    this@attachToRecyclerView.createIndicators(adapter.itemCount, 0)
                }
            })
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)?.startShimmer()
        }
    }
}
