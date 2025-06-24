package com.vs.schoolmessenger.School.LessonPlan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LessonPlan.Model.AllClassData
import com.vs.schoolmessenger.Utils.CustomPieChartView


class LessonPlanPicChartAdapter(
    private var itemList: List<AllClassData>? = emptyList(),
    private val listener: LessonPlanChartClickListener,
    private val context: Context,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.lesson_plan_piechart, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { data ->
                holder.bind(data, listener)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblSubject: TextView = itemView.findViewById(R.id.lblSubject)
        private val lblSection: TextView = itemView.findViewById(R.id.lblSection)
        private val lblStaffName: TextView = itemView.findViewById(R.id.lblStaffName)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val lblComplete: TextView = itemView.findViewById(R.id.lblComplete)
        private val lblPending: TextView = itemView.findViewById(R.id.lblPending)
        private val customPieChart: CustomPieChartView = itemView.findViewById(R.id.customPieChart)
        private val btnView: TextView = itemView.findViewById(R.id.btnView)

        fun bind(data: AllClassData, listener: LessonPlanChartClickListener) {
            lblSubject.text = data.subject_name
            lblSection.text = data.section_name
            lblStaffName.text = data.staff_name
            lblStatus.text = "Items Completed : ${data.items_completed}"

            val percentage = data.percentage_value.toFloatOrNull() ?: 0f
            customPieChart.setProgress(percentage)

            btnView.setOnClickListener {
                listener.onItem(data)
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)
        init {
            shimmerLayout.startShimmer()
        }
    }
}
