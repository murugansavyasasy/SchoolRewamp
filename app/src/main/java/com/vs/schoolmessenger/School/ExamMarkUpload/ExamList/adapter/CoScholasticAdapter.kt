package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getCoScholasticData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class CoScholasticAdapter(
    private var coScholastic: List<getCoScholasticData>?,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (coScholastic == null) TYPE_SHIMMER else TYPE_DATA
    }

    override fun getItemCount(): Int {
        return coScholastic?.size ?: 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.co_scholastic_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.co_scholastic_item, parent, false)
            SubjectViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ShimmerViewHolder)
            holder.startShimmer()
        else if (holder is SubjectViewHolder)
            coScholastic?.get(position)?.let { holder.bind(it, position) }
    }

    // Called by ExamListAdapter when API result arrives
    fun updateData(newList: List<getCoScholasticData>?) {
        coScholastic = newList
        notifyDataSetChanged()
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() = ShimmerUtil.startShimmer(itemView)
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val co_scholastic_name: TextView = itemView.findViewById(R.id.co_scholastic_name)

        fun bind(item: getCoScholasticData, position: Int) {

            co_scholastic_name.text = item.name

        }
    }
}