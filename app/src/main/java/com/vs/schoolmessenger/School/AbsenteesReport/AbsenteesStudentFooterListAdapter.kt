package com.vs.schoolmessenger.School.AbsenteesReport

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesStudentHeaderListAdapter.DataViewHolder

class AbsenteesStudentFooterListAdapter (

    private var itemList: List<AbsenteesStudentFooterData>?,
    private var listener: AbsenteesFooterClickListener,
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
                .inflate(R.layout.absentees_student_footerlist, parent, false)
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




    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val student_name: TextView = itemView.findViewById(R.id.student_name)
        private val section_value: TextView = itemView.findViewById(R.id.section_value)
        private val register_number: TextView = itemView.findViewById(R.id.register_number)


        fun bind(
            data: AbsenteesStudentFooterData,
            position: Int,
            listener: AbsenteesFooterClickListener,
            adapter: AbsenteesStudentFooterListAdapter
        ) {
            student_name.text = data.student_name
            section_value.text = data.student_grade
            register_number.text = data.student_number
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
