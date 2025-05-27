package com.vs.schoolmessenger.School.AbsenteesReport

import android.content.Context
import android.util.Log
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
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudents.Student
import com.vs.schoolmessenger.Utils.Constant

class AbsenteesStudentHeaderListAdapter(
    private var itemList: List<Student>?,
    private var listener: AbsenteesHeaderClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectedPosition = 0

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
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && itemList != null) {
            holder.bind(itemList!![position], position, listener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    fun updateData(newList: List<Student>) {
        this.itemList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val section_values: TextView = itemView.findViewById(R.id.section_values)
        private val cardview: RelativeLayout = itemView.findViewById(R.id.cardview)
        private val badge_count: TextView = itemView.findViewById(R.id.badge_count)

        fun bind(
            data: Student,
            position: Int,
            listener: AbsenteesHeaderClickListener,
            adapter: AbsenteesStudentHeaderListAdapter
        ) {
            Log.d("BindViewHolder", "Binding student at position $position: ${data.student_name}")

            Constant.isAbsenteesReportDataSending?.let { report ->
                val sectionNamesCombined = report.section_wise?.joinToString(", ") { it.name } ?: ""
                val combinedText = "${report.name ?: ""} - $sectionNamesCombined"

                section_values.text = combinedText
                badge_count.text = report.total_absentees ?: "0"

                Log.d("BindViewHolder", "Class: ${report.name}, Date: ${report.date}, Sections: $sectionNamesCombined, Absentees: ${report.total_absentees}")
            }

            if (adapter.selectedPosition == position) {
                cardview.setBackgroundColor(ContextCompat.getColor(context, R.color.custom_blue))
                section_values.setTextColor(ContextCompat.getColor(context, R.color.black))
            } else {
                cardview.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                section_values.setTextColor(ContextCompat.getColor(context, R.color.grey))
            }

            itemView.setOnClickListener {
                Log.d("BindViewHolder", "Item clicked at position $position")
                adapter.setSelectedPosition(position)
                listener.onHeaderItemClicked(position, data)
            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)

            init {
                shimmerLayout.startShimmer()
                Log.d("ShimmerViewHolder", "Shimmer started")
            }
        }
    }
}
