package com.vs.schoolmessenger.School.ExamMarkUpload.ClassList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.ClassList.Model.ClassSectionData
import com.vs.schoolmessenger.Utils.ShimmerUtil


class ClassListAdapter(
    private var itemList: List<ClassSectionData>?,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.item_class_section)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_class_section, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 8 else itemList?.size ?: 0
    }

    fun updateData(newList: List<ClassSectionData>) {
        isLoading = false
        itemList = newList
        notifyDataSetChanged()
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvCount: TextView = itemView.findViewById(R.id.tvCount)

        fun bind(item: ClassSectionData) {
            tvTitle.text = "${item.grade} - ${context.getString(R.string.Section)} ${item.section}"
            tvCount.text = "${item.studentCount} ${context.getString(R.string.Students)}"


        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
