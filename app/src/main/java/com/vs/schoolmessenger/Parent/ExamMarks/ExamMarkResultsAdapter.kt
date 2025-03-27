package com.vs.schoolmessenger.Parent.ExamMarks

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class ExamMarkResultsAdapter(
    private var itemList: List<ExamMarkDataModel>?,
    private var listener: ExamMarkListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectedPosition = RecyclerView.NO_POSITION

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            DataViewHolder.ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.exam_mark_recycledetail, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val subjectname: TextView = itemView.findViewById(R.id.subjectname)
        private val markoutof100: TextView = itemView.findViewById(R.id.markoutof100)
        private val progressBarOutOf100: ProgressBar = itemView.findViewById(R.id.progressBarOutOf100)

        fun bind(
            data: ExamMarkDataModel,
            position: Int,
            listener: ExamMarkListener,
            adapter: ExamMarkResultsAdapter
        ) {
            subjectname.text = data.subjectname
            markoutof100.text = data.markvalue

            val markValue = data.markvalue.toIntOrNull() ?: 0
            val maxValue = 100

            progressBarOutOf100.progress = markValue

            val progressColor = when {
                markValue >= 85/100 -> ContextCompat.getColor(context, R.color.green)
                else -> ContextCompat.getColor(context, R.color.red)
            }
            progressBarOutOf100.progressTintList = ColorStateList.valueOf(progressColor)
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)

            init {
                shimmerLayout.startShimmer()
            }
        }
    }
}
