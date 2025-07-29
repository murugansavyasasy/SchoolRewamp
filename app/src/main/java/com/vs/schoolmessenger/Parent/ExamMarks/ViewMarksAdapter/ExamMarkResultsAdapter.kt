package com.vs.schoolmessenger.Parent.ExamMarks.ViewMarksAdapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.SubjectMark
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamMarkResultsAdapter(
    private var itemList: List<SubjectMark>?,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private lateinit var splitExamMarkResultAdapter: SplitExamMarkResultsAdapter


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.exam_mark_recycledetail)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.exam_mark_recycledetail, parent, false)
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
        private val subjectname: TextView = itemView.findViewById(R.id.subjectname)
        private val markOutOf100: TextView = itemView.findViewById(R.id.markoutof100)
        private val rcSplitMark: RecyclerView = itemView.findViewById(R.id.rcSplitMark)
        private val colourPercent: View = itemView.findViewById(R.id.colourPercent)

        fun bind(examMark: SubjectMark) {
            subjectname.text = examMark.name
            val markString = "${examMark.mark_obtained} / ${examMark.max_mark}"
            markOutOf100.text = markString

            val percentageStr = examMark.percentage ?: "0%"
            val cleanedPercentageStr = percentageStr.replace("%", "").trim()
            val percentage = cleanedPercentageStr.toFloatOrNull() ?: 0f

            val bgDrawable = colourPercent.background.mutate() as GradientDrawable
            when {
                percentage <= 50 -> bgDrawable.setColor(Color.RED)
                percentage <= 75 -> bgDrawable.setColor(Color.parseColor("#FFA500")) // Orange
                else -> bgDrawable.setColor(Color.GREEN)
            }
            colourPercent.background = bgDrawable


            if (examMark.split.size<=1) {
                rcSplitMark.visibility = View.GONE
            }
            else{
                rcSplitMark.visibility=View.VISIBLE
                rcSplitMark.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
                splitExamMarkResultAdapter = SplitExamMarkResultsAdapter(examMark.split, itemView.context, false)
                rcSplitMark.isNestedScrollingEnabled = false
                rcSplitMark.adapter = splitExamMarkResultAdapter
            }
        }
    }
        inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
