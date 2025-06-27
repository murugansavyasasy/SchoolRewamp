package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter

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
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventDataClass
import com.vs.schoolmessenger.Utils.FullScreenViewerActivity
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.FilePathAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator2

class EventAdapter (
    private var itemList: List<EventDataClass>?,
    private var listener: EventClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<EventDataClass> = itemList ?: listOf()
    private var filteredList: List<EventDataClass> = itemList ?: listOf()
    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }


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
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position, listener, this)
        }  else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
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
                                it.description.lowercase().contains(query) ||
                                it.venue.lowercase().contains(query)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<EventDataClass> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val LblHWSubjectName: TextView = itemView.findViewById(R.id.LblHWSubjectName)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblEventTimeImage: TextView = itemView.findViewById(R.id.lblEventTimeImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTimeImage: TextView = itemView.findViewById(R.id.lblTimeImage)
        private val rlaSelectText: RelativeLayout = itemView.findViewById(R.id.rlaSelectText)
        private val rytList: RelativeLayout = itemView.findViewById(R.id.rytList)
        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val imgNewImage: ImageView = itemView.findViewById(R.id.imgNewImage)
        private val webView: android.webkit.WebView = itemView.findViewById(R.id.webView)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val indicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: EventDataClass,
            position: Int,
            listener: EventClickListener,
            adapter: EventAdapter
        ) {

            LblHWSubjectName.visibility = View.VISIBLE
            lblEventTimeImage.visibility=View.VISIBLE
            LblHWSubjectName.text="📍 "+data.venue
            imgNewImage.visibility = View.VISIBLE
            LblHWSubjectName.visibility = View.GONE
            imgNewImage.visibility = View.GONE
            rlaSelectText.visibility = View.GONE
            lblTitleImage.text = data.title
            lblContentImage.text = data.description
            lblDateImage.text = Constant.convertDateTimeFormat(data.date)
            lblTimeImage.text = data.time
            lblEventTimeImage.text="🕒 Event starts at: "+data.time


            webView.setOnTouchListener(object : OnTouchListener {
                @SuppressLint("ClickableViewAccessibility")
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
                        intent.putExtra(Constant.subjectName, data.title)
                        context.startActivity(intent)
                    }

                    return false
                }
            })

            if (data.iframe != "") {
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

                webView.loadUrl(data.file_path[0].url.toString())
            } else {
                if (data.file_path.isEmpty()) {
                    rytList.visibility = View.GONE
                    rcyImgPDF.visibility = View.GONE
                } else {
                    rytList.visibility = View.VISIBLE
                    rcyImgPDF.visibility = View.VISIBLE
                }

                if (data.file_path.size > 1) {
                    indicator.visibility = View.VISIBLE
                } else {
                    indicator.visibility = View.GONE
                }

                webView.visibility = View.GONE
                rcyImgPDF.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rcyImgPDF.adapter = EventFilePathAdapter(
                    data.file_path, context, Constant.isShimmerViewDisable
                )
                indicator.attachToRecyclerView(rcyImgPDF)
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
}

class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun startShimmer() {
        ShimmerUtil.startShimmer(itemView)
    }
}