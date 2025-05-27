package com.vs.schoolmessenger.School.AbsenteesReport

import android.content.Context
import android.content.Intent
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
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise
import com.vs.schoolmessenger.School.AbsenteesReport.Model.SectionWise
import com.vs.schoolmessenger.Utils.Constant
import kotlin.String


class AbsenteesReportDetailAdapter(
    private var itemList: List<ClassWise>?,
    private var listener: AbsenteesDetailClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private val selectedDate: String,
    private var class_name: String? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


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
                .inflate(R.layout.absentees_detail_list, parent, false)
            DataViewHolder(view, context)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener, this, selectedDate)
        }
    }




    override fun getItemCount(): Int {
        return if (isLoading) 20
        else itemList?.size ?: 0
    }



    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val grade_view: TextView = itemView.findViewById(R.id.grade_view)
        private val date_view: TextView = itemView.findViewById(R.id.date_view)
        private val relative_layout: RelativeLayout = itemView.findViewById(R.id.relative_layout)
        private val badge_count: TextView = itemView.findViewById(R.id.badge_count)

        fun bind(
            data: ClassWise,
            position: Int,
            listener: AbsenteesDetailClickListener,
            adapter: AbsenteesReportDetailAdapter,
            selectedDate: String
        ) {
            grade_view.text = data.name
            badge_count.text = data.total_absentees
            date_view.text = selectedDate

            relative_layout.setOnClickListener {
                val intent = Intent(context, AbsenteesStudents::class.java).apply {
                }
                isSaveAbsenteesReportDetails(data,selectedDate)
                context.startActivity(intent)
            }
        }

        private fun isSaveAbsenteesReportDetails(data: ClassWise, selectedDate: String) {
            val saveAbsenteesReportData = ClassWise(
                id = data.id,
                name = data.name,
                section_wise = data.section_wise,
                total_absentees = data.total_absentees,
                date = selectedDate
            )
            Constant.isAbsenteesReportDataSending = saveAbsenteesReportData
        }


        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)
            init {
                shimmerLayout.startShimmer()
            }
        }
    }
}
