package com.vs.schoolmessenger.Parent.ExamMarks.ViewMarksAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.Group
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamGroupActivity(
    private var itemList: List<Group>?,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private lateinit var groupExamMarkResultsAdapter: GroupExamMarkResultsAdapter


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.activity_exam_group)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_exam_group, parent, false)
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
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupSubjectname: TextView = itemView.findViewById(R.id.groupSubjectname)
        private val mark: TextView = itemView.findViewById(R.id.mark)
        private val rcGroupSplitMark: RecyclerView = itemView.findViewById(R.id.rcGroupSplitMark)

        fun bind(groupExamMark: Group) {
            groupSubjectname.text = groupExamMark.name
            mark.text = groupExamMark.mark

            if (groupExamMark.sub_groups.size <= 1) {
                rcGroupSplitMark.visibility = View.GONE
            } else {
                rcGroupSplitMark.visibility = View.VISIBLE
                rcGroupSplitMark.layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
                groupExamMarkResultsAdapter =
                    GroupExamMarkResultsAdapter(groupExamMark.sub_groups, itemView.context, false)
                rcGroupSplitMark.isNestedScrollingEnabled = false
                rcGroupSplitMark.adapter = groupExamMarkResultsAdapter
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
