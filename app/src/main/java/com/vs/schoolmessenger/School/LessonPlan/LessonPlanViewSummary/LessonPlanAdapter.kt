package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import android.widget.Filter
import android.widget.Filterable
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanClickListener
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryItem

class LessonPlanAdapter(
    private var itemList: List<LessonPlanViewSummaryItem>?,
    private val listener: LessonPlanClickListener,
    private val context: Context,
    private val isLoading: Boolean,
    private val requestType: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var fullList: List<LessonPlanViewSummaryItem> = itemList ?: listOf()
    private var filteredList: List<LessonPlanViewSummaryItem> = itemList ?: listOf()

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
        if (holder is DataViewHolder && !isLoading) {
            filteredList.getOrNull(position)?.let { data ->
                holder.bind(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else filteredList.size
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: LessonPlanViewSummaryItem) {
            val recyclerView = itemView.findViewById<RecyclerView>(R.id.detailsRecyclerView)
            val bottomstatusrelative_layout =
                itemView.findViewById<RelativeLayout>(R.id.bottomstatusrelative_layout)
            val status_text1label = itemView.findViewById<ImageView>(R.id.status_text1label)
            val status_textlabel = itemView.findViewById<TextView>(R.id.status_textlabel)
            val btnedit = itemView.findViewById<LinearLayout>(R.id.btnEditContainer)
            val btndelete = itemView.findViewById<LinearLayout>(R.id.btnDeleteContainer)

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = LessonPlanDetailAdapter(item.details)

            if (requestType == "allclass") {
                btndelete.visibility = View.GONE
            } else {
                btndelete.visibility = View.VISIBLE
            }

            when (item.lesson_plan_status) {
                3 -> {
                    bottomstatusrelative_layout.setBackgroundResource(R.drawable.bg_green_radoius_20dp)
                    status_text1label.setImageResource(R.drawable.correcticonsvg)
                    status_textlabel.setTextColor(ContextCompat.getColor(context, R.color.green))
                    status_textlabel.text = "Completed"
                }

                2 -> {
                    bottomstatusrelative_layout.setBackgroundResource(R.drawable.bg_blue_radoius_20dp)
                    status_text1label.setImageResource(R.drawable.refreshicon)
                    status_textlabel.setTextColor(ContextCompat.getColor(context, R.color.iconBlue))
                    status_textlabel.text = "In Progress"
                }

                1 -> {
                    bottomstatusrelative_layout.setBackgroundResource(R.drawable.bg_orange_radoius_20dp)
                    status_text1label.setImageResource(R.drawable.sandclockicon)
                    status_textlabel.setTextColor(
                        ContextCompat.getColor(
                            context, R.color.dark_orange
                        )
                    )
                    status_textlabel.text = "Yet to Start"
                }
            }
            btnedit.setOnClickListener { listener.onEditItem(item) }
            btndelete.setOnClickListener { listener.onDeleteItem(item) }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim()?.lowercase() ?: ""
                val resultList = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter { item ->
                        item.details.any { detail ->
                            detail.name.lowercase().contains(query) || detail.value.lowercase()
                                .contains(query)
                        }
                    }
                }
                return FilterResults().apply { values = resultList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<LessonPlanViewSummaryItem> ?: listOf()
                notifyDataSetChanged()
                listener.onSearchResultEmpty(filteredList.isEmpty())
            }
        }
    }

    fun updateData(newData: List<LessonPlanViewSummaryItem>) {
        fullList = newData
        filteredList = newData
        notifyDataSetChanged()
    }
}
