package com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Adapter

import android.annotation.SuppressLint
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filter.FilterResults
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.Holiday
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.HolidayClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HolidayAdapter(
    private var itemList: List<Holiday>?,
    private var context: Context,
    private var isLoading: Boolean,
    private var listener: HolidayClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<Holiday> = itemList ?: listOf()
    private var filteredList: List<Holiday> = itemList ?: listOf()
    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.holiday_item)
            ShimmerViewHolder(shimmerView)
        }else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.holiday_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList!![position], position, listener, this)
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
                        it.name.lowercase().contains(query)

                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<Holiday> ?: listOf()
                listener.onSearchHolidayResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblMonth: TextView = itemView.findViewById(R.id.lblMonth)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblNameOfTheDate: TextView = itemView.findViewById(R.id.lblNameOfTheDate)
        private val lblNameOfTheHoliDay: TextView = itemView.findViewById(R.id.lblNameOfTheHoliDay)

        @SuppressLint("SimpleDateFormat")
        fun bind(
            data: Holiday,
            position: Int,
            listener: HolidayClickListener,
            adapter: HolidayAdapter
        ) {
            lblNameOfTheHoliDay.text = data.name

            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date: Date? = inputFormat.parse(data.date)

                if (date != null) {
                    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
                    val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                    val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())

                    lblDate.text = dayFormat.format(date)
                    lblMonth.text = monthFormat.format(date).uppercase()
                    lblNameOfTheDate.text = dayNameFormat.format(date)
                } else {
                    lblDate.text = "--"
                    lblMonth.text = "--"
                    lblNameOfTheDate.text = "--"
                }
            } catch (e: Exception) {
                lblDate.text = "--"
                lblMonth.text = "--"
                lblNameOfTheDate.text = "--"
            }
        }
    }
}

class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun startShimmer() {
        ShimmerUtil.startShimmer(itemView)
    }
}
