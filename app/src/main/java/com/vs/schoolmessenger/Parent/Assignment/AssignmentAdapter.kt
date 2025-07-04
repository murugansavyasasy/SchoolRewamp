package com.vs.schoolmessenger.Parent.Assignment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FullScreenViewerActivity
import me.relex.circleindicator.CircleIndicator2

class AssignmentAdapter(
    var itemList: MutableList<AssignmentData>,
    private val listener: AssignmentClickListener,
    private val context: Context,
    private val isLoading: Boolean
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
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.assignment_report_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position, this, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
        private val lblSubject: TextView = itemView.findViewById(R.id.lblSubject)
        private val lblCategotry: TextView = itemView.findViewById(R.id.lblCategotry)
        private val lblSubmissionDue: TextView = itemView.findViewById(R.id.lblSubmissionDue)
        private val lblSubmitted: TextView = itemView.findViewById(R.id.lblSubmitted)
        private val lblNotSubmitted: TextView = itemView.findViewById(R.id.lblNotSubmitted)
        private val lblSendby: TextView = itemView.findViewById(R.id.lblSendby)
        private val rytList: RelativeLayout = itemView.findViewById(R.id.rytList)
        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)
        private val webView: WebView = itemView.findViewById(R.id.webView)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val indicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)
        private val imgDelete: ImageView = itemView.findViewById(R.id.imgDelete)

        @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
        fun bind(
            data: AssignmentData,
            position: Int,
            adapter: AssignmentAdapter,
            listener: AssignmentClickListener
        ) {
            lblTitle.text = data.title
            lblDescription.text = data.description
            lblSubject.text = data.subject
            lblCategotry.text = data.category
            lblSubmissionDue.text = data.end_date
            lblSubmitted.text = "Submitted : ${data.submitted_count}"
            lblNotSubmitted.text = "NotSubmitted : ${data.total_count}"
            lblSendby.text = data.created_date

            if (data.file_path.isNotEmpty()) {
                rytList.visibility = View.VISIBLE
                if (data.file_path[0].type != Constant.VIDEO) {
                    rcyAssignment.visibility = View.VISIBLE
                    webView.visibility = View.GONE
                } else {
                    webView.visibility = View.VISIBLE
                    rcyAssignment.visibility = View.GONE
                    loadWebView(data.file_path[0].url ?: "")
                }
            } else {
                rytList.visibility = View.GONE
            }

            val fileAdapter = ImageSliderAdapter(
                data.subject ?: "",
                data.file_path,
                context,
                Constant.isShimmerViewDisable
            )
            rcyAssignment.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rcyAssignment.adapter = fileAdapter

            if (data.file_path.size > 1) {
                indicator.visibility = View.VISIBLE
                indicator.attachToRecyclerView(rcyAssignment)
            } else {
                indicator.visibility = View.GONE
            }

            webView.setBackgroundColor(Color.BLACK)
            webView.setOnTouchListener { _, event ->
                webView.onPause()
                if (event.action == MotionEvent.ACTION_UP) {
                    Constant.commonFileList.clear()
                    Constant.selectedFileIndex = -1
                    Constant.commonFileList = data.file_path.map {
                        CommonFileData(it.type, it.url)
                    }.toMutableList()
                    Constant.selectedFileIndex = position
                    val intent = Intent(context, FullScreenViewerActivity::class.java)
                    intent.putExtra(Constant.subjectName, data.subject)
                    context.startActivity(intent)
                }
                false
            }

            lblSubmitted.setOnClickListener { listener.onSubmittedClick(data) }
            lblNotSubmitted.setOnClickListener { listener.onNotSubmittedClick(data) }

            imgDelete.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    AlertDialog.Builder(context)
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete this assignment?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            adapter.itemList.removeAt(pos)
                            adapter.notifyItemRemoved(pos)
                            listener.onDeleteClick(data)
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }

        private fun loadWebView(url: String) {
            progressBar.visibility = View.VISIBLE
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                allowFileAccess = true
                allowContentAccess = true
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
            }

            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    progressBar.visibility = View.GONE
                    Log.e("WebViewError", "Error: ${error?.description}")
                }
            }

            webView.loadUrl(url)
        }

        private fun CircleIndicator2.attachToRecyclerView(recyclerView: RecyclerView) {
            val adapter = recyclerView.adapter ?: return
            createIndicators(adapter.itemCount, 0)
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                    this@attachToRecyclerView.animatePageSelected(firstVisible)
                }
            })

            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    createIndicators(adapter.itemCount, 0)
                }
            })
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }
}
