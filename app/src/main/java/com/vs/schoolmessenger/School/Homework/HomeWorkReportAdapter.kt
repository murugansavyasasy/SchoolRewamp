package com.vs.schoolmessenger.School.Homework

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
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FullScreenViewerActivity
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator2

class HomeWorkReportAdapter(
    private var itemList: List<HomeWorkReport>?,
    private var listener: HomeWorkReportClickListener,
    private var context: Context,
    private var isLoading: Boolean
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
        if (holder is DataViewHolder) {
            itemList?.let {
                holder.bind(it[position], position, listener, context)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val rlaImageReport: RelativeLayout = itemView.findViewById(R.id.rlaImageReport)
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTimeImage: TextView = itemView.findViewById(R.id.lblTimeImage)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val rytList: RelativeLayout = itemView.findViewById(R.id.rytList)
        private val webView: android.webkit.WebView = itemView.findViewById(R.id.webView)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val indicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)
        private val tvSeeMoreImage: TextView = itemView.findViewById(R.id.tvSeeMoreImage)
        private val LblHWSubjectName: TextView = itemView.findViewById(R.id.LblHWSubjectName)
        private val rlaSelectText: RelativeLayout = itemView.findViewById(R.id.rlaSelectText)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: HomeWorkReport,
            position: Int,
            listener: HomeWorkReportClickListener,
            adapterContext: Context
        ) {
            lblTimeImage.visibility=View.GONE
            rlaImageReport.visibility = View.VISIBLE
            LblHWSubjectName.text = data.subject_name
            lblTitleImage.text = data.title
            lblContentImage.text = data.description

            if (data.file_path.isNotEmpty()) {
                rytList.visibility = View.VISIBLE
                if (data.file_path[0].type != Constant.VIDEO) {
                    rcyImgPDF.visibility = View.VISIBLE
                    webView.visibility = View.GONE
                } else {
                    webView.visibility = View.VISIBLE
                    rcyImgPDF.visibility = View.GONE
                    val videoUrl = data.file_path[0].url ?: ""
                    loadSimpleUrl(webView, videoUrl)
                }
            } else {
                rytList.visibility = View.GONE
            }

            val adapter = ImageSliderAdapter(
                data.subject_name ?: "",
                data.file_path,
                adapterContext,
                Constant.isShimmerViewDisable,
            )

            rcyImgPDF.layoutManager =
                LinearLayoutManager(adapterContext, LinearLayoutManager.HORIZONTAL, false)
            rcyImgPDF.adapter = adapter

            if (data.file_path.isEmpty()) {
                indicator.visibility = View.GONE
            } else {
                if (data.file_path.size > 1) {
                    indicator.visibility = View.VISIBLE
                } else {
                    indicator.visibility = View.GONE
                }
                indicator.attachToRecyclerView(rcyImgPDF)
            }

            rlaSelectText.setOnClickListener {
                listener.onClickListener(data)
            }

            webView.setOnTouchListener(object : OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        return false
                    }

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Constant.commonFileList.isEmpty()
                        Constant.selectedFileIndex = -1
                        val commonList = data.file_path?.map {
                            CommonFileData(
                                type = it.type,
                                path = it.url
                            )
                        }?.toMutableList() ?: mutableListOf()

                        Constant.commonFileList = commonList

                        Constant.selectedFileIndex = position

                        val intent = Intent(context, FullScreenViewerActivity::class.java)
                        intent.putExtra(Constant.subjectName, data.subject_name)
                        context.startActivity(intent)
                    }

                    return false
                }
            })
        }

        @SuppressLint("SetJavaScriptEnabled")
        fun loadSimpleUrl(webView: android.webkit.WebView, url: String) {
            progressBar.visibility = View.VISIBLE
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = true
            webView.settings.allowFileAccess = true
            webView.settings.allowContentAccess = true

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(
                    view: android.webkit.WebView?,
                    url: String?,
                    favicon: Bitmap?
                ) {
                    progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: android.webkit.WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    progressBar.visibility = View.GONE
                    Log.e("WebViewError", "Error loading: ${error?.description}")
                }
            }

            webView.loadUrl(url)
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
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
