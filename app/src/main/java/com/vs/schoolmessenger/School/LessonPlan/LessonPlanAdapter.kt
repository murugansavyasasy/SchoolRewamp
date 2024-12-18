package com.vs.schoolmessenger.School.LessonPlan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class LessonPlanAdapter(
    private var itemList: List<LessonPlanData>? = emptyList(),
    private var listener: LessonPlanClickListener,
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
                .inflate(R.layout.lesson_plan_item, parent, false)
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
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblFromDate: TextView = itemView.findViewById(R.id.lblFromDate)
        private val lblToTime: TextView = itemView.findViewById(R.id.lblToTime)
        private val lblUnitValue: TextView = itemView.findViewById(R.id.lblUnitValue)
        private val lblRemarks: TextView = itemView.findViewById(R.id.lblRemarks)
        private val lblYetToStart: TextView = itemView.findViewById(R.id.lblYetToStart)
        private val lblInProgress: TextView = itemView.findViewById(R.id.lblInProgress)
        private val lblCompleted: TextView = itemView.findViewById(R.id.lblCompleted)
        private val rlaHeader: RelativeLayout = itemView.findViewById(R.id.rlaHeader)
        private val imgYetStart: ImageView = itemView.findViewById(R.id.imgYetStart)
        private val imgInProgress: ImageView = itemView.findViewById(R.id.imgInProgress)
        private val imgCompleted: ImageView = itemView.findViewById(R.id.imgCompleted)
        private val vwStageOne: View = itemView.findViewById(R.id.vwStageOne)
        private val vwStageTwo: View = itemView.findViewById(R.id.vwStageTwo)

        fun bind(data: LessonPlanData, listener: LessonPlanClickListener, position: Int) {
            lblTitle.text = data.Title
            lblFromDate.text = data.FromTime
            lblToTime.text = data.ToTime
            lblUnitValue.text = data.Unit
            lblRemarks.text = data.Remarks

            // Set the background color based on the position
            val backgroundResource = when (position % 4) { // Cycle through 4 different backgrounds
                0 -> R.drawable.bg_green_gradient
                1 -> R.drawable.bg_blue_gradient
                2 -> R.drawable.bg_orange_gradient
                else -> R.drawable.bg_purple_gradient
            }
            rlaHeader.setBackgroundResource(backgroundResource)

            // Handle status and stage updates
            when (data.Status) {
                1 -> {
                    vwStageOne.setBackgroundColor(context.resources.getColor(R.color.grey))
                    vwStageTwo.setBackgroundColor(context.resources.getColor(R.color.grey))
                    imgYetStart.setImageResource(R.drawable.yet_to_start_green)
                    imgInProgress.setImageResource(R.drawable.inprogress_grey)
                    imgCompleted.setImageResource(R.drawable.completed_grey)
                    lblYetToStart.setTextColor(context.resources.getColor(R.color.green))
                    lblInProgress.setTextColor(context.resources.getColor(R.color.grey))
                    lblCompleted.setTextColor(context.resources.getColor(R.color.grey))
                }

                2 -> {
                    vwStageOne.setBackgroundColor(context.resources.getColor(R.color.green))
                    vwStageTwo.setBackgroundColor(context.resources.getColor(R.color.grey))
                    imgYetStart.setImageResource(R.drawable.yet_to_start_green)
                    imgInProgress.setImageResource(R.drawable.inprogress_green)
                    imgCompleted.setImageResource(R.drawable.completed_grey)
                    lblYetToStart.setTextColor(context.resources.getColor(R.color.green))
                    lblInProgress.setTextColor(context.resources.getColor(R.color.green))
                    lblCompleted.setTextColor(context.resources.getColor(R.color.grey))
                }

                3 -> {
                    vwStageOne.setBackgroundColor(context.resources.getColor(R.color.green))
                    vwStageTwo.setBackgroundColor(context.resources.getColor(R.color.green))
                    imgYetStart.setImageResource(R.drawable.yet_to_start_green)
                    imgInProgress.setImageResource(R.drawable.inprogress_green)
                    imgCompleted.setImageResource(R.drawable.completed_green)
                    lblYetToStart.setTextColor(context.resources.getColor(R.color.green))
                    lblInProgress.setTextColor(context.resources.getColor(R.color.green))
                    lblCompleted.setTextColor(context.resources.getColor(R.color.green))
                }
            }
        }
    }

    fun updateData(newList: List<LessonPlanData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}
