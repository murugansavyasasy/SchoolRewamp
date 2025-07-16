package com.vs.schoolmessenger.Parent.ExamMarks

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.Group
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.SubjectMark
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamGroupActivity (
    private var itemList: List<Group>?,
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
        private val subjectname: TextView = itemView.findViewById(R.id.subjectname)
        private val markOutOf100: TextView = itemView.findViewById(R.id.markoutof100)
        private val downarrowicon: ImageView = itemView.findViewById(R.id.downarrowicon)
        private val totalmarklabel: TextView = itemView.findViewById(R.id.totalmarklabel)
        private val linear_layout1: LinearLayout = itemView.findViewById(R.id.linear_layout1)
        private val progressBarOutOf100: ProgressBar =
            itemView.findViewById(R.id.progressBarOutOf100)

        fun bind(examMark: Group) {
            subjectname.text = examMark.name

            markOutOf100.text = examMark.mark

            totalmarklabel.text = examMark.subgroups.joinToString(", ") { it.name } + "-" + examMark.subgroups.joinToString(", ") { it.mark }




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
