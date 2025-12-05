package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanClickListener
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryItem
import com.vs.schoolmessenger.Utils.Constant

class LessonPlanAdapter(
    private var itemList: List<LessonPlanViewSummaryItem>?,
    private val listener: LessonPlanClickListener,
    private val context: Context,
    private val isLoading: Boolean,
    private val requestType: String,
    private val subject_name: String,
    private val items_completed: String,
    private val completed_items: String,
    private val total_items: String
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
                holder.bind(
                    data,
                    context,
                    subject_name,
                    items_completed,
                    completed_items,
                    total_items
                )
            }
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 5 else filteredList.size
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            item: LessonPlanViewSummaryItem,
            context: Context,
            subject_name: String,
            items_completed: String,
            completed_items: String,
            total_items: String
        ) {
            val recyclerView = itemView.findViewById<RecyclerView>(R.id.detailsRecyclerView)
            val lblSubjectId = itemView.findViewById<TextView>(R.id.lblSubjectId)
            val lblTeaching = itemView.findViewById<TextView>(R.id.lblTeaching)
            val lblLevel = itemView.findViewById<TextView>(R.id.lblLevel)
            val status_text1label = itemView.findViewById<ImageView>(R.id.status_text1label)


            item.details.find { it.name.equals(Constant.Activity, ignoreCase = true) }
            item.details.find { it.name.equals(Constant.Topic, ignoreCase = true) }

            lblSubjectId.text = subject_name
            lblTeaching.text =
                context.getString(R.string.chapters_completed) + completed_items + " - " + total_items
            lblLevel.text = item.lesson_plan_status.toString() ?: ""

            val btnedit = itemView.findViewById<LinearLayout>(R.id.btnEditContainer)
            itemView.findViewById<LinearLayout>(R.id.btnDeleteContainer)

            recyclerView.layoutManager = LinearLayoutManager(this@LessonPlanAdapter.context)
            recyclerView.adapter = LessonPlanDetailAdapter(item.details, context)


            when (item.lesson_plan_status) {
                3 -> {
                    status_text1label.setImageResource(R.drawable.completed_icon_3)

                }

                2 -> {
                    status_text1label.setImageResource(R.drawable.progress_icon_2)

                }

                1 -> {
                    status_text1label.setImageResource(R.drawable.sandclockicon)
                    status_text1label.setColorFilter(
                        ContextCompat.getColor(
                            this@LessonPlanAdapter.context,
                            R.color.dark_orange
                        ),   // your color
                        PorterDuff.Mode.SRC_IN
                    )

                }
            }

            btnedit.setOnClickListener {

                val popupView =
                    LayoutInflater.from(context).inflate(R.layout.popup_edit_delete, null)
                val popupWindow = PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                )
                popupWindow.elevation = 10f

                val layoutEdit = popupView.findViewById<LinearLayout>(R.id.layout_edit)
                val layoutDelete = popupView.findViewById<LinearLayout>(R.id.layout_delete)


                layoutDelete.visibility =
                    if (requestType == Constant.myclass) View.VISIBLE else View.GONE

                layoutEdit.setOnClickListener {
                    listener.onEditItem(item)
                    popupWindow.dismiss()
                }

                layoutDelete.setOnClickListener {
                    listener.onDeleteItem(item)
                    popupWindow.dismiss()
                }

                popupWindow.showAsDropDown(btnedit, 0, 10)
            }


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
