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
    private var data: List<LessonPlanData>, // List of lesson plans
    private val listener: LessonPlanClickListener, // Click listener
    private val context: Context,
    private val isLoading: Boolean // Flag for shimmer effect
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
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(data[position], listener)

            // Handle visibility and height of vertical line
            if (position == data.size - 1) {
                // Hide line for last item
                holder.viewVerticalLine.visibility = View.GONE
            } else {
                // Show and adjust height dynamically
                holder.viewVerticalLine.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else data.size // Show 10 shimmer items while loading
    }



    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.header_values)
        private val lblDate: TextView = itemView.findViewById(R.id.date_values)
        private val lblUnit: TextView = itemView.findViewById(R.id.footer_values)
        val viewVerticalLine: View = itemView.findViewById(R.id.viewVerticalLine) // Get reference

        fun bind(data: LessonPlanData, listener: LessonPlanClickListener) {
            lblTitle.text = data.Title
            lblDate.text = "${data.FromDate}  To  ${data.ToDate}"
            lblUnit.text = "Unit: ${data.Remarks}"

            itemView.setOnClickListener {
                listener.onEditItem(data)
            }
        }
    }


    // ViewHolder for shimmer effect
    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }

    // Function to update the data dynamically
    fun updateData(newData: List<LessonPlanData>, isLoading: Boolean) {
        this.data = newData
        notifyDataSetChanged()
    }
}
