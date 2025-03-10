package com.vs.schoolmessenger.School.LessonPlan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.CustomPieChartView
import com.vs.schoolmessenger.Utils.PieChartView

class LessonPlanPicChartAdapter(
    private var itemList: List<LessonPlanChartData>? = emptyList(),
    private var listener: LessonPlanChartClickListener,
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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)

            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.lesson_plan_piechart, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            itemList?.get(position)?.let { data ->
                holder.bind(data, listener, position) // Pass position to the bind method
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
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


        fun bind(data: LessonPlanChartData, listener: LessonPlanChartClickListener, position: Int) {
            lblSubject.text = data.Subject
            lblSection.text = data.Section
            lblStaffName.text = "Staff name : " + data.StaffName
            lblStatus.text = "Completed Status : " + data.Status
            lblComplete.text = data.Completed.toString() + "% " + "Completed"
            lblPending.text = data.Pending.toString() + "% " + "Pending"
            val chartData = listOf(
                Pair(data.Completed.toFloat(), context.getColor(R.color.dark_blue_and_green_)),
                Pair(data.Pending.toFloat(), context.getColor(R.color.red))
            )

            btnView.setOnClickListener {
                listener.onItem(data)
            }

            customPieChart.setProgress(60f)
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}
