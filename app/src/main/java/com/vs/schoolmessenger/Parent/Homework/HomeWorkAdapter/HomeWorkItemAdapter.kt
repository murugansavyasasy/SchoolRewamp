package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.masoudss.lib.utils.Utils
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FullScreenViewerActivity
import com.vs.schoolmessenger.Utils.fetchVimeoThumbnail
import me.relex.circleindicator.CircleIndicator2

class HomeWorkItemAdapter(
    private var GetHomeworkData: GetDateWiseHomeworkData?,
    private var HomeDetails: List<GetHomeworkDetails>?,
    private var context: Context,
    private var isLoading: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            DataViewHolder.ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.homework_school_reportitem, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(GetHomeworkData, HomeDetails!![position], position, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else HomeDetails?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private var isTextExpanded = false
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTimeImage: TextView = itemView.findViewById(R.id.lblTimeImage)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val tvSeeMoreImage: TextView = itemView.findViewById(R.id.tvSeeMoreImage)
        private val RcyImgPdf: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val DotIndicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)
        private val lblSubjectName: TextView = itemView.findViewById(R.id.LblHWSubjectName)
        private val rlaSelectText: RelativeLayout = itemView.findViewById(R.id.rlaSelectText)
        private val rytList: RelativeLayout = itemView.findViewById(R.id.rytList)
        private val webView: android.webkit.WebView = itemView.findViewById(R.id.webView)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val imgNewImage: ImageView = itemView.findViewById(R.id.imgNewImage)

        private var mHomeworkImgPDFAdapter: HomeworkImgPDFAdapter? = null

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: GetDateWiseHomeworkData?,
            homeworkData: GetHomeworkDetails,
            position: Int,
            adapter: HomeWorkItemAdapter,
        ) {
            lblTitleImage.text = homeworkData.title
            lblContentImage.text = homeworkData.description
//            lblDateImage.text = Constant.convertDateTimeFormat(data!!.date)
            lblSubjectName.text = homeworkData.subject_name
            rlaSelectText.visibility = View.GONE
            val dateTime =data!!.date
            val parts = dateTime.split(" ")
            val date = parts.getOrNull(0) ?: ""
//            val time = parts.getOrNull(1) + " " + (parts.getOrNull(2) ?: "")
            lblDateImage.text = Constant.convertDateTimeFormat(date)
            lblTimeImage.visibility=View.GONE
            imgNewImage.visibility = View.GONE


            webView.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    webView.onPause()
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        return false
                    }

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Constant.commonFileList.isEmpty()
                        Constant.selectedFileIndex = -1
                        val commonList = homeworkData.file_path?.map {
                            CommonFileData(
                                type = it.type,
                                path = it.url
                            )
                        }?.toMutableList() ?: mutableListOf()

                        Constant.commonFileList = commonList

                        Constant.selectedFileIndex = position

                        val intent = Intent(context, FullScreenViewerActivity::class.java)
                        intent.putExtra(Constant.subjectName, homeworkData.subject_name)
                        context.startActivity(intent)
                    }

                    return false
                }
            })

            if (homeworkData.file_path.isNotEmpty()) {
                if (homeworkData.file_path[0].type.toString() == Constant.VIDEO) {
                    webView.visibility = View.VISIBLE
                    RcyImgPdf.visibility = View.GONE
                    rytList.visibility = View.GONE
                    DotIndicator.visibility = View.GONE

                    webView.settings.javaScriptEnabled = true
                    webView.settings.domStorageEnabled = true
                    webView.settings.loadWithOverviewMode = true
                    webView.settings.useWideViewPort = true
                    webView.settings.mediaPlaybackRequiresUserGesture = true

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageStarted(
                            view: android.webkit.WebView,
                            url: String,
                            favicon: Bitmap?
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

                    webView.loadUrl(homeworkData.file_path[0].url.toString())
                } else {
                    webView.visibility = View.GONE
                    RcyImgPdf.visibility = View.VISIBLE
                    rytList.visibility = View.VISIBLE
                    DotIndicator.visibility = View.VISIBLE
                    mHomeworkImgPDFAdapter =
                        HomeworkImgPDFAdapter("", null, context, Constant.isShimmerViewShow)
                    RcyImgPdf.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    RcyImgPdf.adapter = mHomeworkImgPDFAdapter

                    mHomeworkImgPDFAdapter = HomeworkImgPDFAdapter(
                        homeworkData.subject_name,
                        homeworkData.file_path,
                        context,
                        Constant.isShimmerViewDisable,
                    )
                    RcyImgPdf.adapter = mHomeworkImgPDFAdapter
                }
            }


            isSeeMoreVisibility(lblContentImage, tvSeeMoreImage)
            tvSeeMoreImage.setOnClickListener {
                isSeeMoreExpanded(tvSeeMoreImage, lblContentImage)
            }

            if (homeworkData.file_path.isNotEmpty()) {
                RcyImgPdf.visibility = View.VISIBLE
                rytList.visibility = View.VISIBLE
                DotIndicator.visibility = View.VISIBLE
            } else {
                RcyImgPdf.visibility = View.GONE
                DotIndicator.visibility = View.GONE
                rytList.visibility = View.GONE
            }
            setupDotIndicator(DotIndicator, homeworkData.file_path.size)
        }

        private fun isSeeMoreExpanded(tvSeeMore: TextView, lblContent: TextView) {
            if (isTextExpanded) {
                isTextExpanded = false
                lblContent.maxLines = 3
                lblContent.ellipsize = TextUtils.TruncateAt.END
                tvSeeMore.text = itemView.context.getString(R.string.SeeMore)
            } else {
                isTextExpanded = true
                lblContent.maxLines = Integer.MAX_VALUE
                lblContent.ellipsize = null
                tvSeeMore.text = itemView.context.getString(R.string.SeeLess)
            }
        }

        fun extractVimeoVideoId(vimeoUrl: String): String? {
            val regex = Regex("vimeo\\.com/video/(\\d+)")
            val match = regex.find(vimeoUrl)
            return match?.groupValues?.get(1)
        }


        private fun isSeeMoreVisibility(lblContent: TextView, tvSeeMore: TextView) {
            lblContent.post {
                if (lblContent.lineCount > 3) {
                    tvSeeMore.visibility = View.VISIBLE
                    lblContent.maxLines = 3
                    lblContent.ellipsize = TextUtils.TruncateAt.END
                }
            }
        }

        private fun setupDotIndicator(indicator: CircleIndicator2, itemCount: Int) {
            if (itemCount <= 1) {
                indicator.visibility = View.GONE
                return
            }
            indicator.createIndicators(itemCount, 0)
            RcyImgPdf.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val currentPosition = layoutManager.findFirstVisibleItemPosition()
                    indicator.animatePageSelected(currentPosition)
                }
            })
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)
            init {
                shimmerLayout.startShimmer()
            }
        }
    }
}