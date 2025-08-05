package com.vs.schoolmessenger.School.Assignment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Assignment.Model.AssignmentStudentListClickListener
import com.vs.schoolmessenger.School.Assignment.Model.StudentSubmission
import com.vs.schoolmessenger.Utils.ShimmerUtil


class AssignmentStudentListAdapter(
    private var itemList: List<StudentSubmission>?,
    private var listener: AssignmentStudentListClickListener,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.assignment_student_list)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.assignment_student_list, parent, false)
            DataViewHolder(view, context, listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let {
                holder.bind(it, position, this)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else itemList?.size ?: 0
    }

    fun updateList(newData: List<StudentSubmission>) {
        itemList = newData
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: AssignmentStudentListClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)
        private val sectionlabel: TextView = itemView.findViewById(R.id.sectionlabel)
        private val standardlabel: TextView = itemView.findViewById(R.id.standardlabel)
        private val statuslabel: TextView = itemView.findViewById(R.id.statuslabel)
        private val rlarelativelayout: RelativeLayout = itemView.findViewById(R.id.rlarelativelayout)

        fun bind(data: StudentSubmission, position: Int, adapter: AssignmentStudentListAdapter) {
            lblStudentName.text = data.student_name
            sectionlabel.text = data.standard
            standardlabel.text = data.section
            statuslabel.text = data.submit_status

            rlarelativelayout.setOnClickListener {
                val intent = Intent(context, AssignmentStudentListDetail::class.java)
                intent.putParcelableArrayListExtra("submission_list", ArrayList(data.submissions_details))
                context.startActivity(intent)
            }

        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
