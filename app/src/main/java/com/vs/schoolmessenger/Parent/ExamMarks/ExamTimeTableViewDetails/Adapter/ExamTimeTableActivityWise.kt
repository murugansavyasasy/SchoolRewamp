package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableViewDetails.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamTimeTableActivityWise(
    private var itemList: List<ExamTimetableActivity>?,
    private val context: Context,
    private val listener: ExamMarkListener,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(
                parent,
                R.layout.exam_time_table_activity_item
            )
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.exam_time_table_activity_item, parent, false)
            DataViewHolder(view, context, listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList?.get(position))
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun updateData(newList: List<ExamTimetableActivity>) {
        itemList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: ExamMarkListener
    ) : RecyclerView.ViewHolder(itemView) {


        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val lblSession: TextView = itemView.findViewById(R.id.lblSession)
        private val lblVenue: TextView = itemView.findViewById(R.id.lblVenue)
        private val lblSyllabus: TextView = itemView.findViewById(R.id.lblSyllabus)
        private val rlaNoDataFound: RelativeLayout = itemView.findViewById(R.id.rlaNoDataFound)
        private val lnrScheduleDetails: LinearLayout = itemView.findViewById(R.id.lnrScheduleDetails)
        private val subject_name: TextView = itemView.findViewById(R.id.subject_name)
        private val total_marks: TextView = itemView.findViewById(R.id.total_marks)

        private val rcRubrics: RecyclerView =
            itemView.findViewById(R.id.rcRubrics)

        fun bind(data: ExamTimetableActivity?) {

            if (data == null) return
            subject_name.text=data.activityName
            total_marks.text="${context.getString(R.string.max_marks)} - ${data.max_mark}"


            val rubrics = data.rubrics ?: emptyList()

            if (rubrics.isEmpty()) {
                rcRubrics.visibility = View.GONE
                lnrScheduleDetails.visibility = View.VISIBLE
                rlaNoDataFound.visibility = View.VISIBLE

                val scheduleDetails=data.schedulingDetails
                lblDate.text=scheduleDetails?.date?:""
                lblTime.text="${scheduleDetails?.startTime?:""} - ${scheduleDetails?.endTime?:""}"
                lblSession.text=scheduleDetails?.session?:""
                lblVenue.text=scheduleDetails?.venue?:""
                lblSyllabus.text=scheduleDetails?.syllabus?:""

            } else {
                lnrScheduleDetails.visibility = View.GONE
                rlaNoDataFound.visibility = View.GONE
                rcRubrics.visibility = View.VISIBLE
                rcRubrics.layoutManager = LinearLayoutManager(context)
                rcRubrics.isNestedScrollingEnabled = false
                rcRubrics.adapter = ExamTimeTableRubricsWise(
                    rubrics,
                    context,
                    listener,
                    false

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