package com.vs.schoolmessenger.School.AbsenteesReport

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class AbsenteesStudentHeaderListAdapter (

    private var itemList: List<AbsenteesStudentHeaderData>?,
    private var listener: AbsenteesHeaderClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {

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
                .inflate(R.layout.absentees_student_headerlist, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this) // Pass adapter reference
        }
    }



    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val section_values: TextView = itemView.findViewById(R.id.section_values)
        private val cardview: RelativeLayout = itemView.findViewById(R.id.cardview)

        fun bind(
            data: AbsenteesStudentHeaderData,
            position: Int,
            listener: AbsenteesHeaderClickListener,
            adapter: AbsenteesStudentHeaderListAdapter
        ) {
            section_values.text = data.section_values


            if (adapter.selectedPosition == position) {
                cardview.setBackgroundColor(ContextCompat.getColor(context,R.color.custom_blue))
                section_values.setTextColor(ContextCompat.getColor(context,R.color.black))
            } else {
                cardview.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                section_values.setTextColor(ContextCompat.getColor(context,R.color.grey))
            }

            itemView.setOnClickListener {
                adapter.setSelectedPosition(position)  // Update selected position
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
}
