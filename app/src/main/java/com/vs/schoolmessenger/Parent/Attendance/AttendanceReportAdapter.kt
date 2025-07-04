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
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import java.text.ParseException
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.NoticeBoardAdapter
import com.vs.schoolmessenger.Parent.Noticeboard.Notice
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoardClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttendanceReportAdapter(
    private var itemList: List<AttendanceReportStudentData>?,
    private var listener: AttendanceReportClickListener,
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
                ShimmerUtil.wrapWithShimmer(parent, R.layout.attendance_report_student)
            com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.attendance_report_student, parent, false)
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
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lnrDateCircle: LinearLayout = itemView.findViewById(R.id.lnrDateCircle)
        private val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val tvFullDate: TextView = itemView.findViewById(R.id.tvFullDate)
        private val tvDayName: TextView = itemView.findViewById(R.id.tvDayName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)


        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: AttendanceReportStudentData, position: Int, adapter: AttendanceReportAdapter) {

            if (data.type == "Absent") {
                lnrDateCircle.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_date_circle))
                tvStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_status_badge))
                tvFullDate.text = Constant.convertDateTimeFormat(data.date)
                tvStatus.text = data.type
                tvDayName.text = data.day

                val inputDate = data.date
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val outputMonthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                val outputDayFormat = SimpleDateFormat("dd", Locale.getDefault())

                try {
                    val dateObj = inputFormat.parse(inputDate)
                    tvMonth.text = outputMonthFormat.format(dateObj)
                    tvDay.text = outputDayFormat.format(dateObj)
                } catch (e: ParseException) {
                    e.printStackTrace()
                    tvMonth.text = ""
                    tvDay.text = ""
                }

            }
            else {
                lnrDateCircle.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_date_circle_light_green))
                tvStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_status_badge_green))
                }


        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}