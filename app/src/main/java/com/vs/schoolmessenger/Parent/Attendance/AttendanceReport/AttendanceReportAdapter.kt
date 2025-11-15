    package com.vs.schoolmessenger.Parent.Attendance.AttendanceReport

    import android.annotation.SuppressLint
    import android.content.Context
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Filter
    import android.widget.Filterable
    import android.widget.FrameLayout
    import android.widget.LinearLayout
    import android.widget.RelativeLayout
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.vs.schoolmessenger.R
    import com.vs.schoolmessenger.Utils.Constant
    import com.vs.schoolmessenger.Utils.ShimmerUtil
    import java.text.ParseException
    import java.text.SimpleDateFormat
    import java.util.Locale

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
                ShimmerViewHolder(shimmerView)
            } else {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.attendance_report_student, parent, false)
                DataViewHolder(view, context)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is DataViewHolder) {
                holder.bind(filteredList[position], position, this)
            } else if (holder is ShimmerViewHolder) {
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

            private val lnrDateCircle: RelativeLayout = itemView.findViewById(R.id.lnrDateCircle)
            private val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)
            private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
            private val tvFullDate: TextView = itemView.findViewById(R.id.tvFullDate)
            private val tvDayName: TextView = itemView.findViewById(R.id.tvDayName)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)


            @SuppressLint("UseCompatLoadingForDrawables")
            fun bind(
                data: AttendanceReportStudentData,
                position: Int,
                adapter: AttendanceReportAdapter
            ) {

                if (data.type == "A/A") {
                    tvStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_status_badge))
                    tvFullDate.text = Constant.convertDateTimeFormat2(data.date)
                    tvStatus.text = "Absent"
                    tvDayName.text = data.day

                    val inputDate = data.date
                    val inputFormat = SimpleDateFormat(Constant.ddMMyyyy, Locale.getDefault())
                    val outputMonthFormat = SimpleDateFormat(Constant.MMM_, Locale.getDefault())
                    val outputDayFormat = SimpleDateFormat(Constant.dd, Locale.getDefault())

                    try {
                        val dateObj = inputFormat.parse(inputDate)
                        tvMonth.text = outputMonthFormat.format(dateObj)
                        tvDay.text = outputDayFormat.format(dateObj)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        tvMonth.text = ""
                        tvDay.text = ""
                    }
                } else {
                    lnrDateCircle.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_date_circle_light_green))
                    tvStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_status_badge_green))

                    tvFullDate.text = Constant.convertDateTimeFormat2(data.date)
                    tvStatus.text = "Present"
                    tvDayName.text = data.day

                    val inputDate = data.date
                    val inputFormat = SimpleDateFormat(Constant.ddMMyyyy, Locale.getDefault())
                    val outputMonthFormat = SimpleDateFormat(Constant.MMM_, Locale.getDefault())
                    val outputDayFormat = SimpleDateFormat(Constant.dd, Locale.getDefault())

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


            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }