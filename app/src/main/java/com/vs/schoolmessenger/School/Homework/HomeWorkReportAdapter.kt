package com.vs.schoolmessenger.School.Homework

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport
import com.vs.schoolmessenger.Utils.Constant
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
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
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
            rlaImageReport.visibility = View.VISIBLE
            LblHWSubjectName.text = data.subject_name
            lblTitleImage.text = data.title
            lblContentImage.text = data.description

            rcyImgPDF.visibility = if (data.file_path.isNullOrEmpty()) View.GONE else View.VISIBLE

            val adapter = ImageSliderAdapter(
                data.subject_name ?: "",
                data.file_path,
                adapterContext,
                Constant.isShimmerViewDisable,
            )
            rcyImgPDF.layoutManager = LinearLayoutManager(adapterContext, LinearLayoutManager.HORIZONTAL, false)
            rcyImgPDF.adapter = adapter

            // Setup indicator
            if (data.file_path.isNullOrEmpty()) {
                indicator.visibility = View.GONE
            } else {
                indicator.visibility = View.VISIBLE
                indicator.attachToRecyclerView(rcyImgPDF)
            }

            rlaSelectText.setOnClickListener {
                listener.onClickListener(data)
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
