package com.vs.schoolmessenger.Parent.Noticeboard.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

import com.vs.schoolmessenger.Parent.Noticeboard.Notice
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoardClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class NoticeBoardAdapter(
    private var itemList: List<Notice>?,
    private var listener: NoticeBoardClickListener,
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
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.noticeboard_report_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)

        private val RcyImgPdf: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        var mnoticeboardImgPDFAdapter: FilePathAdapter? = null

        private fun getRecyclerView(): RecyclerView {
            return RcyImgPdf
        }

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            noticeData: Notice,
            position: Int,
            adapter: NoticeBoardAdapter
        ) {

            val noticeboardImgPdf = getRecyclerView()
            lblTitleImage.text = noticeData.title
            lblContentImage.text = noticeData.content
            lblDateImage.text = noticeData.created_on


            if (noticeData.file_path.size > 0) {
                RcyImgPdf.visibility = View.VISIBLE
            } else {
                RcyImgPdf.visibility = View.GONE
            }

            mnoticeboardImgPDFAdapter =
                FilePathAdapter(null,context,Constant.isShimmerViewShow)
            noticeboardImgPdf.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            noticeboardImgPdf.adapter = mnoticeboardImgPDFAdapter


            mnoticeboardImgPDFAdapter =
                FilePathAdapter(
                    noticeData.file_path,
                    context,
                    Constant.isShimmerViewDisable

                )
            noticeboardImgPdf.adapter = mnoticeboardImgPDFAdapter


        }


        }

}

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }

