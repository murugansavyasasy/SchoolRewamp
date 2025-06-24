package com.vs.schoolmessenger.Parent.Noticeboard.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Noticeboard.Notice
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoardClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator2

class NoticeBoardAdapter(
    private var itemList: List<Notice>?,
    private var listener: NoticeBoardClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<Notice> = itemList ?: listOf()
    private var filteredList: List<Notice> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.noticeboard_report_item)
            ShimmerViewHolder(shimmerView)
        }
        else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.noticeboard_report_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position, this)
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
                                it.description.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<Notice> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }

        }
    }



    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val rcyImgPdf: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val indicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)
        private var mnoticeboardImgPDFAdapter: FilePathAdapter? = null

        @SuppressLint("ClickableViewAccessibility")
        fun bind(noticeData: Notice, position: Int, adapter: NoticeBoardAdapter) {
            lblTitleImage.text = noticeData.title
            lblContentImage.text = noticeData.description
            lblDateImage.text = Constant.convertDateAndTimeFormat(noticeData.created_on)

            if (noticeData.file_path.isNotEmpty()) {
                rcyImgPdf.visibility = View.VISIBLE
                if (noticeData.file_path.size > 1) {
                    indicator.visibility = View.VISIBLE
                } else {
                    indicator.visibility = View.GONE
                }

                rcyImgPdf.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                mnoticeboardImgPDFAdapter = FilePathAdapter(
                    noticeData.file_path,
                    context,
                    Constant.isShimmerViewDisable
                )
                rcyImgPdf.adapter = mnoticeboardImgPDFAdapter
                indicator.attachToRecyclerView(rcyImgPdf)
            } else {
                rcyImgPdf.visibility = View.GONE
                indicator.visibility = View.GONE
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
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}


