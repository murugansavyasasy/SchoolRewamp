package com.vs.schoolmessenger.School.SchoolStrength.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.SchoolStrength.Model.SchoolData
import com.vs.schoolmessenger.School.SchoolStrength.Model.Standard
import com.vs.schoolmessenger.School.SchoolStrength.Model.SummaryItem
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SummaryStaticsAdapter(
    private var itemList: List<SummaryItem>,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.summarystatics_report)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.summarystatics_report, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(itemList[position])
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else itemList.size
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val student_count: TextView = itemView.findViewById(R.id.student_count)
        private val growth_value: TextView = itemView.findViewById(R.id.growth_value)
        private val progressbar: ProgressBar = itemView.findViewById(R.id.progressbar)

        @SuppressLint("SetTextI18n")
        fun bind(data: SummaryItem) {
            student_count.text = data.title
            growth_value.text = data.value
            val valueInt = data.value.toIntOrNull() ?: 0
            progressbar.progress = if (valueInt > 0) 100 else 0
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
