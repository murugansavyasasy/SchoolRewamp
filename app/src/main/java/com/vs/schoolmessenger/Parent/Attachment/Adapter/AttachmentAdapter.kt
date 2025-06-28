package com.vs.schoolmessenger.Parent.Attachment.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import android.widget.Filter
import android.widget.Filterable
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
import com.vs.schoolmessenger.Utils.FullScreenViewerActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator2

class AttachmentAdapter(
    private var attachmentList: List<AttachmentData>?,
    private val listener: AttachmentClickListener,
    private val context: Context,
    var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<AttachmentData> = attachmentList ?: listOf()
    private var filteredList: List<AttachmentData> = attachmentList ?: listOf()

    init {
        fullList = attachmentList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.homework_school_reportitem)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.homework_school_reportitem, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.title.lowercase().contains(query) ||
                                it.description.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<AttachmentData> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isLoading && holder is DataViewHolder) {
            holder.bind(filteredList[position], listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else filteredList.size
    }

    fun updateList(newList: List<AttachmentData>) {
        this.attachmentList = newList
        this.fullList = newList
        this.filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private var isTextExpanded = false
        private val LblHWSubjectName: TextView = itemView.findViewById(R.id.LblHWSubjectName)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTimeImage: TextView = itemView.findViewById(R.id.lblTimeImage)
        private val tvView: TextView = itemView.findViewById(R.id.tvView)
        private val tvSeeMoreImage: TextView = itemView.findViewById(R.id.tvSeeMoreImage)
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
            lblTimeImage.text =item.time
            isSeeMoreVisibility(lblContentImage, tvSeeMoreImage)
            tvSeeMoreImage.setOnClickListener {
                isSeeMoreExpanded(tvSeeMoreImage, lblContentImage)
            }


            webView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {

                    Constant.commonFileList = item.file_path?.map { file ->
                        CommonFileData(type = file.type, path = file.url)
                    }?.toMutableList() ?: mutableListOf()

                    Constant.selectedFileIndex = position

                    val intent = Intent(context, FullScreenViewerActivity::class.java)
                    intent.putExtra(Constant.subjectName, item.title)
                    context.startActivity(intent)
                }
                false
            }

            if (item.iframe.isNotEmpty()) {
                webView.visibility = View.VISIBLE
                rytList.visibility = View.VISIBLE
                rcyImgPDF.visibility = View.GONE

                webView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }


                webView.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: android.webkit.WebView, url: String, favicon: Bitmap?) {
                        loadingBar.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: android.webkit.WebView, url: String) {
                        loadingBar.visibility = View.GONE
                    }

                    override fun onReceivedError(
                        view: android.webkit.WebView?, request: WebResourceRequest?, error: WebResourceError?
                    ) {
                        loadingBar.visibility = View.GONE
                        Log.e("WebViewError", "Error loading: ${error?.description}")
                    }
                }

                webView.loadUrl(item.file_path.firstOrNull()?.url ?: "")

            } else {
                indicator.visibility = if (item.file_path.size > 1) View.VISIBLE else View.GONE

                webView.visibility = View.GONE
                rytList.visibility = View.VISIBLE
                rcyImgPDF.visibility = View.VISIBLE
                rcyImgPDF.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rcyImgPDF.adapter = AttachmentFilePathAdapter(
                    item.file_path, context, Constant.isShimmerViewDisable
                )
                indicator.attachToRecyclerView(rcyImgPDF)
            }

            itemView.setOnClickListener {
                listener.onItemClick(item, this)
            }
        }

        private fun CircleIndicator2.attachToRecyclerView(recyclerView: RecyclerView) {
            val adapter = recyclerView.adapter ?: return
            this.createIndicators(adapter.itemCount, 0)

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
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

        private fun isSeeMoreVisibility(lblContent: TextView, tvSeeMore: TextView) {
            lblContent.post {
                if (lblContent.lineCount > 3) {
                    tvSeeMore.visibility = View.VISIBLE
                    lblContent.maxLines = 3
                    lblContent.ellipsize = TextUtils.TruncateAt.END
                }
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)?.startShimmer()
        }
    }
}