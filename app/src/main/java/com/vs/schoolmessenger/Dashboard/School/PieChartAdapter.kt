package com.vs.schoolmessenger.Dashboard.School

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.PieChartView

class PieChartAdapter (private val context: Context, private val item: List<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
//        private const val VIEW_TYPE_PIE_CHART = 0
        private const val VIEW_TYPE_REGULAR_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
//        return if (position == 0) VIEW_TYPE_PIE_CHART else VIEW_TYPE_REGULAR_ITEM
        return  VIEW_TYPE_REGULAR_ITEM
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == VIEW_TYPE_PIE_CHART) {
//            val view = LayoutInflater.from(context).inflate(R.layout.piechart_view, parent, false)
//            PieChartViewHolder(view)
//        } else {
//            val view = LayoutInflater.from(context).inflate(R.layout.autoscrolling_message, parent, false)
//            BannerViewHolder(view)
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.autoscrolling_message, parent, false)
        return BannerViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is PieChartViewHolder) {
//            // Customize PieChartView at 0th position
//            holder.bind()
//        } else
            if (holder is BannerViewHolder) {
            val url = item[position % item.size]
            holder.bind(url)

        }
    }

    override fun getItemCount(): Int = item.size

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblContent: TextView = itemView.findViewById(R.id.lblContent)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)

        fun bind(url: Int) {
//            Glide.with(context).load(url).centerCrop().into(imgBanner)
        }
    }

//    inner class PieChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val pieChart: PieChartView = itemView.findViewById(R.id.customPieChart)
//
//        fun bind() {
//            // Set u  p pie chart data and appearance here
//            val chartData = listOf(
//                Pair(20f, context.getColor(R.color.yellow)),
//                Pair(50f, context.getColor(R.color.light_green_bg1)),
//                Pair(30f, context.getColor(R.color.light_orange0))
//            )
//            pieChart.setData(chartData)
//        }
//    }
}