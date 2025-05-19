package com.vs.schoolmessenger.School.Homework

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator

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
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener, this)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()

        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        // Image
        private val rlaImageReport: RelativeLayout = itemView.findViewById(R.id.rlaImageReport)
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)

        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val indicator: CircleIndicator = itemView.findViewById(R.id.indicator)
        private val tvSeeMoreImage: TextView = itemView.findViewById(R.id.tvSeeMoreImage)
        private val LblHWSubjectName: TextView = itemView.findViewById(R.id.LblHWSubjectName)
        private val rlaSelectText: RelativeLayout = itemView.findViewById(R.id.rlaSelectText)
        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: HomeWorkReport,
            position: Int,
            listener: HomeWorkReportClickListener,
            adapter: HomeWorkReportAdapter
        ) {
            rlaImageReport.visibility = View.VISIBLE
            LblHWSubjectName.text = data.subject_name
            lblTitleImage.text = data.title
            lblContentImage.text = data.description
            if (data.file_path.size > 0) {
                rcyImgPDF.visibility = View.VISIBLE
                indicator.visibility = View.VISIBLE
            } else {
                rcyImgPDF.visibility = View.GONE
                indicator.visibility = View.GONE
            }
            Log.d("data.file_path", data.file_path.size.toString())
            val layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val adapter = ImageSliderAdapter(context, data.file_path)
            rcyImgPDF.layoutManager = layoutManager
            rcyImgPDF.adapter = adapter

            val dotsLayout =
                itemView.findViewById<LinearLayout?>(R.id.dotIndicatorLayout)
            dotsLayout?.let {
                updateDotIndicator(it, data.file_path.size, 0)
                rcyImgPDF.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(
                        recyclerView: RecyclerView, dx: Int, dy: Int
                    ) {
                        val visiblePosition =
                            layoutManager.findFirstVisibleItemPosition()
                        updateDotIndicator(it, data.file_path.size, visiblePosition)
                    }
                })
            }

            rlaSelectText.setOnClickListener {
                listener.onClickListener(data)
            }
        }

        fun updateDotIndicator(dotsLayout: LinearLayout, count: Int, selectedPosition: Int) {
            dotsLayout.removeAllViews()
            for (i in 0 until count) {
                val dot = ImageView(dotsLayout.context)
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 0, 8, 0)
                dot.layoutParams = params
                dot.setImageResource(if (i == selectedPosition) R.drawable.active_dot else R.drawable.inactive_dot)
                dotsLayout.addView(dot)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}