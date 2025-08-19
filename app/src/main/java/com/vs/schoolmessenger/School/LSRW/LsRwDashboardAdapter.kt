package com.vs.schoolmessenger.School.LSRW

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.Model.DashboardItem

class LsRwDashboardAdapter(
    private var itemList: List<DashboardItem>,
    private val context: Context
) : RecyclerView.Adapter<LsRwDashboardAdapter.HeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lsrw_dashboard_viewitem, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val txtCount: TextView = itemView.findViewById(R.id.txtCount)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtSubTitle: TextView = itemView.findViewById(R.id.txtSubTitle)

        fun bind(item: DashboardItem) {
            imgIcon.setImageResource(item.icon)
            txtCount.text = item.count
            txtTitle.text = item.title
            txtSubTitle.text = item.subtitle
        }
    }
}