package com.vs.schoolmessenger.Parent.ExamMarks.ViewMarksAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.SplitMark
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SplitExamMarkResultsAdapter(
    private var splitList: List<SplitMark>?,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.split_exam_mark_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.split_exam_mark_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            splitList?.get(position)?.let { holder.bind(it) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else splitList?.size ?: 0
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblSplitSubjectName: TextView = itemView.findViewById(R.id.lblSplitSubjectName)
        private val lblSplitmarkoutof100: TextView =
            itemView.findViewById(R.id.lblSplitmarkoutof100)


        fun bind(splitExamMark: SplitMark) {
            lblSplitSubjectName.text = splitExamMark.name

            val markString = "${splitExamMark.mark_obtained} / ${splitExamMark.max_mark}"
            lblSplitmarkoutof100.text = markString
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
