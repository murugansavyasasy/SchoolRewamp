package com.vs.schoolmessenger.Dashboard.School

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class PieChartAdapter(private val context: Context, private val item: List<Int>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_REGULAR_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_REGULAR_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.autoscrolling_message, parent, false)
        return BannerViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BannerViewHolder) {
            val url = item[position % item.size]
            holder.bind(url)

        }
    }

    override fun getItemCount(): Int = item.size

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblContent: TextView = itemView.findViewById(R.id.lblContent)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)

        fun bind(url: Int) {
        }
    }

}