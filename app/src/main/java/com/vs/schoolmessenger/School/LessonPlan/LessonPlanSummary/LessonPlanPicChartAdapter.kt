package com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel.AllClassData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.CustomPieChartView
import com.vs.schoolmessenger.Utils.ShimmerUtil

class LessonPlanPicChartAdapter(
    private var itemList: List<AllClassData>? = emptyList(),
    private val listener: LessonPlanChartClickListener,
    private val context: Context,
    private val isLoading: Boolean,
    private val requestType: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {


    private var fullList: List<AllClassData> = itemList ?: listOf()
    private var filteredList: List<AllClassData> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.lesson_plan_piechart)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.lesson_plan_piechart, parent, false)
            DataViewHolder(view, context, requestType)

        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position, listener)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.subject_name.lowercase().contains(query) ||
                                it.class_name.lowercase().contains(query) ||
                                it.section_name.lowercase().contains(query) ||
                                it.staff_name.lowercase().contains(query)

                    }
                }
                return FilterResults().apply { values = result }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<AllClassData> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 5 else filteredList?.size ?: 0
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val requestType: String
    ) : RecyclerView.ViewHolder(itemView) {

        private val lblSubject: TextView = itemView.findViewById(R.id.lblSubject)
        private val lblSection: TextView = itemView.findViewById(R.id.lblSection)
        private val lblStaffName: TextView = itemView.findViewById(R.id.lblStaffName)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val lblComplete: TextView = itemView.findViewById(R.id.lblComplete)
        private val lblPending: TextView = itemView.findViewById(R.id.lblPending)
        private val lblView: TextView = itemView.findViewById(R.id.lblView)
        private val customPieChart: CustomPieChartView = itemView.findViewById(R.id.customPieChart)
        private val imgPunchHistory: ImageView = itemView.findViewById(R.id.imgPunchHistory)
        private val rootHeader: CardView = itemView.findViewById(R.id.rootHeader)
        private val totalrelative_layout: RelativeLayout =
            itemView.findViewById(R.id.totalrelative_layout)

        fun bind(data: AllClassData, position: Int, listener: LessonPlanChartClickListener) {
            lblSubject.text = data.subject_name.orEmpty()
            lblSection.text = "${data.class_name} - ${data.section_name}"
            lblStaffName.text = data.staff_name.orEmpty()
            lblStatus.text = "${context.getString(R.string.Items_Completed)} ${data.items_completed}"

            val itemscompleted = data.items_completed

            val percentage = data.percentage_value
            customPieChart.setProgress(data.percentage_value)



            val context = rootHeader.context
            val color = ContextCompat.getColor(context, R.color.pale_white_3)
            (rootHeader as CardView).setCardBackgroundColor(color)



            lblView.setBackgroundColor(ContextCompat.getColor(context, R.color.pale_white_3))


            imgPunchHistory.visibility = if (itemscompleted == Constant._0_0) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

            totalrelative_layout.setOnClickListener {
                if (itemscompleted == Constant._0_0) {
                    Log.d("Listener Status", "The percentage value is zero")
                } else {
                    listener.onItem(data, requestType)
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