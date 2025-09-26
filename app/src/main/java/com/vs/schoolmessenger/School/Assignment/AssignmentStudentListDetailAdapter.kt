package com.vs.schoolmessenger.School.Assignment

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Assignment.Model.SubmissionDetail
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AssignmentStudentListDetailAdapter(
    private var itemList: List<SubmissionDetail>?,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(
                parent, R.layout.assignment_adapter_student_detailreport
            )
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.assignment_adapter_student_detailreport, parent, false)
            DataViewHolder(view, context)
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

    fun updateList(newData: List<SubmissionDetail>) {
        itemList = newData
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(
        itemView: View, private val context: Context
    ) : RecyclerView.ViewHolder(itemView) {
        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)
        private val sectionlabel: TextView = itemView.findViewById(R.id.sectionlabel)
        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)
        private val video_player: ImageView = itemView.findViewById(R.id.video_player)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)


        fun bind(
            data: SubmissionDetail, position: Int, adapter: AssignmentStudentListDetailAdapter
        ) {
            lblStudentName.text = data.description
            sectionlabel.text = data.submitted_on

            val hasIframe = !data.iframe.isNullOrEmpty()
            val hasFiles = !data.file_path.isNullOrEmpty()

            video_player.visibility = if (hasIframe) View.VISIBLE else View.GONE
            rcyAssignment.visibility = if (hasIframe) View.GONE else View.VISIBLE
            rytList2.visibility = if (hasFiles) View.VISIBLE else View.GONE
            total_numbers.visibility = View.GONE

            if (hasFiles) {
                val fileList = data.file_path!!
                val totalFiles = fileList.size
                val visibleList = if (totalFiles > 2) fileList.subList(0, 2) else fileList

                if (totalFiles > 2) {
                    total_numbers.text = "+${totalFiles - 2}"
                    total_numbers.visibility = View.VISIBLE
                }

                rcyAssignment.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rcyAssignment.adapter =
                    AssignmentFilePathAdapter(
                        visibleList,
                        fileList,
                        context,
                        Constant.isShimmerViewDisable
                    )
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
