package com.vs.schoolmessenger.Parent.Attendance

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import java.text.SimpleDateFormat
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filter.FilterResults
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import java.text.ParseException
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.NoticeBoardAdapter
import com.vs.schoolmessenger.Parent.Noticeboard.Notice
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttendanceReportAdapter(
    private var itemList: List<AttendanceReportStudentData>?,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<AttendanceReportStudentData> = itemList ?: listOf()
    private var filteredList: List<AttendanceReportStudentData> = itemList ?: listOf()


    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }



    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.attendace_report_student)
            com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.attendace_report_student, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position, this)
        }  else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.date.lowercase().contains(query) ||
                                it.day.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<AttendanceReportStudentData> ?: listOf()
                notifyDataSetChanged()
            }
        }
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)
        private val lblAdmissionValue: TextView = itemView.findViewById(R.id.lblAdmissionValue)
        private val lblAttendanceStatus: TextView = itemView.findViewById(R.id.lblAttendanceStatus)
        private val rlaAttendance: RelativeLayout = itemView.findViewById(R.id.rlaAttendance)
        private val lblMonth: TextView = itemView.findViewById(R.id.lblMonth)
        private val lnrDate: RelativeLayout = itemView.findViewById(R.id.lnrDate)
        private val lblAdmission: TextView = itemView.findViewById(R.id.lblAdmission)
        private val lblNameOfTheDate: TextView = itemView.findViewById(R.id.lblNameOfTheDate)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: AttendanceReportStudentData, position: Int, adapter: AttendanceReportAdapter) {

            if (data.type == "Absent") {
                lblAttendanceStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.rounded_top_right_bottom_end_red))
                rlaAttendance.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_outline_red))
                lnrDate.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_light_red_radious))
                lblMonth.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_red_radious_top_left_different))
                lblAdmission.text = data.date
                lblAttendanceStatus.text = data.type
                lblNameOfTheDate.text = data.day

                val inputDate = data.date
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val outputMonthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                val outputDayFormat = SimpleDateFormat("dd", Locale.getDefault())

                try {
                    val dateObj = inputFormat.parse(inputDate)
                    lblMonth.text = outputMonthFormat.format(dateObj)
                    lblDate.text = outputDayFormat.format(dateObj)
                } catch (e: ParseException) {
                    e.printStackTrace()
                    lblMonth.text = ""
                    lblDate.text = ""
                }

            }else {
                    lblAttendanceStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.rounded_top_right_bottom_end_green))
                    rlaAttendance.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_outline_green))
                    lnrDate.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_light_green_radious))
                    lblMonth.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_green_radious))
                    lblAdmission.text = data.date
                    lblAttendanceStatus.text = data.type
                    lblNameOfTheDate.text = data.day
                }


        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}