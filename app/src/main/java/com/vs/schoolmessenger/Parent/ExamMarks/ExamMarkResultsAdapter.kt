package com.vs.schoolmessenger.Parent.ExamMarks

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filter.FilterResults
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.ExamMarkData
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.SubjectMark
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter.InteractionWithStaffAdapter
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Listener.InteractionWithStaffListener
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.Staff
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamMarkResultsAdapter(
    private var itemList: List<SubjectMark>?,
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
        private val downarrowicon: ImageView = itemView.findViewById(R.id.downarrowicon)
        private val totalmarklabel: TextView = itemView.findViewById(R.id.totalmarklabel)
        private val linear_layout1: LinearLayout = itemView.findViewById(R.id.linear_layout1)
        private val progressBarOutOf100: ProgressBar =
            itemView.findViewById(R.id.progressBarOutOf100)

        fun bind(examMark: SubjectMark) {
            subjectname.text = examMark.name

            val markString = "${examMark.mark_obtained} / ${examMark.max_mark}"
            totalmarklabel.text = markString
            markOutOf100.text = markString

                try {
                val split = markString.split("/").map { it.trim() }

                if (split.size == 2) {
                    val obtained = split[0].toFloatOrNull() ?: 0f
                    val total = split[1].toFloatOrNull() ?: 100f
                    val percent = ((obtained / total) * 100).toInt()

                   progressBarOutOf100.progress = percent
                }
            } catch (e: Exception) {
                Log.e("ProgressError", "Error parsing markString: $markString", e)
            }

            downarrowicon.setOnClickListener {
                if (totalmarklabel.visibility == View.VISIBLE) {
                    totalmarklabel.visibility = View.GONE
                } else {
                    totalmarklabel.visibility = View.VISIBLE
                }
            }
            linear_layout1.setOnClickListener {
                if (totalmarklabel.visibility == View.VISIBLE) {
                    totalmarklabel.visibility = View.GONE
                } else {
                    totalmarklabel.visibility = View.VISIBLE
                }
            }

        }
    }
        inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
