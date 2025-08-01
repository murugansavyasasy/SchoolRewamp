

////Working code for opening both schools
package com.vs.schoolmessenger.Parent.RequestLeave

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import java.time.LocalDate

class CalendarAdapter(
    private val onDateClicked: (LocalDate) -> Unit,
    private val minDate: LocalDate?,
    private val maxDate: LocalDate?
) : RecyclerView.Adapter<CalendarAdapter.DateViewHolder>() {

    private var days: List<LocalDate?> = emptyList()
    private var selectedDate: LocalDate? = null
    @RequiresApi(Build.VERSION_CODES.O)
    private var today: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    fun submitList(newDays: List<LocalDate?>, selected: LocalDate?, current: LocalDate) {
        days = newDays
        selectedDate = selected
        today = current
        notifyDataSetChanged()
    }

    fun setSelectedDate(date: LocalDate) {
        selectedDate = date
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_day_item, parent, false)
        return DateViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = days[position]
        holder.bind(date)
    }

    override fun getItemCount(): Int = days.size

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateBox: TextView = itemView.findViewById(R.id.dateBox)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(date: LocalDate?) {
            if (date == null) {
                dateBox.text = ""
                dateBox.setBackgroundResource(0)
                dateBox.setTextColor(Color.TRANSPARENT)
                dateBox.isClickable = false
                return
            }

            dateBox.text = date.dayOfMonth.toString()
            dateBox.setTextColor(Color.BLACK)
            dateBox.isClickable = true

            // Handle disabled state
            val isBeforeMin = minDate != null && date.isBefore(minDate)
            val isAfterMax = maxDate != null && date.isAfter(maxDate)
            val isDisabled = isBeforeMin || isAfterMax

            if (isDisabled) {
                dateBox.setTextColor(Color.LTGRAY)
                dateBox.setBackgroundResource(0)
                dateBox.isClickable = false
            } else {
                when {
                    selectedDate == date -> {
                        dateBox.setBackgroundResource(R.drawable.bg_circle_selecto)
                        dateBox.setTextColor(Color.WHITE)
                    }
                    date == today -> {
                        dateBox.setBackgroundResource(R.drawable.circle_today)
                        dateBox.setTextColor(Color.BLUE)
                    }
                    else -> {
                        dateBox.setBackgroundResource(0)
                        dateBox.setTextColor(Color.BLACK)
                    }
                }

                dateBox.setOnClickListener {
                    onDateClicked(date)
                }
            }
        }
    }
}


//package com.vs.schoolmessenger.Parent.RequestLeave
//
//import android.graphics.Color
//import android.os.Build
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.annotation.RequiresApi
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import java.time.LocalDate
//
//class CalendarAdapter(
//    private val onDateClicked: (LocalDate) -> Unit
//) : RecyclerView.Adapter<CalendarAdapter.DateViewHolder>() {
//
//    private val dates = mutableListOf<LocalDate?>()
//    private var selectedDate: LocalDate? = null
//    private var today: LocalDate? = null
//    private var minDate: LocalDate? = null
//    private var maxDate: LocalDate? = null
//
//    fun submitList(
//        newDates: List<LocalDate?>,
//        selected: LocalDate,
//        current: LocalDate,
//        min: LocalDate? = null,
//        max: LocalDate? = null
//    ) {
//        dates.clear()
//        dates.addAll(newDates)
//        selectedDate = selected
//        today = current
//        minDate = min
//        maxDate = max
//        notifyDataSetChanged()
//    }
//
//    fun setSelectedDate(date: LocalDate) {
//        selectedDate = date
//        notifyDataSetChanged()
//    }
//
//    override fun getItemCount(): Int = dates.size
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_date_box, parent, false)
//        return DateViewHolder(view)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
//        val date = dates[position]
//
//        if (date == null) {
//            holder.bindEmpty()
//        } else {
//            val isDisabled = (minDate != null && date.isBefore(minDate)) ||
//                    (maxDate != null && date.isAfter(maxDate))
//            val isToday = date == today
//            val isSelected = date == selectedDate
//
//            holder.bind(date, isSelected, isToday, isDisabled)
//
//            holder.itemView.setOnClickListener {
//                if (!isDisabled) {
//                    onDateClicked(date)
//                }
//            }
//        }
//    }
//
//    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        private val textDate = view.findViewById<TextView>(R.id.dateBox)
//
//        fun bindEmpty() {
//            textDate.text = ""
//            textDate.setBackgroundColor(Color.TRANSPARENT)
//        }
//
//        @RequiresApi(Build.VERSION_CODES.O)
//        fun bind(date: LocalDate, isSelected: Boolean, isToday: Boolean, isDisabled: Boolean) {
//            textDate.text = date.dayOfMonth.toString()
//
//            when {
//                isSelected -> {
//                    textDate.setBackgroundResource(R.drawable.bg_circle_selecto)
//                    textDate.setTextColor(Color.WHITE)
//                }
//                isToday -> {
//                    textDate.setBackgroundResource(R.drawable.circle_today)
//                    textDate.setTextColor(Color.BLACK)
//                }
//                isDisabled -> {
//                    textDate.setBackgroundResource(0)
//                    textDate.setTextColor(Color.GRAY)
//                }
//                else -> {
//                    textDate.setBackgroundResource(0)
//                    textDate.setTextColor(Color.BLACK)
//                }
//            }
//        }
//    }
//}

//working code
//package com.vs.schoolmessenger.Parent.RequestLeave
//
//import android.graphics.Color
//import android.graphics.drawable.GradientDrawable
//import android.os.Build
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.annotation.RequiresApi
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import java.time.LocalDate
//
//class CalendarAdapter(
//    private val onDateClicked: (LocalDate) -> Unit
//) : RecyclerView.Adapter<CalendarAdapter.DateViewHolder>() {
//
//    private var dates: List<LocalDate?> = emptyList()
//    private var selectedDate: LocalDate? = null
//    private var today: LocalDate? = null
//
//    fun submitList(newDates: List<LocalDate?>, selected: LocalDate, todayDate: LocalDate) {
//        dates = newDates
//        selectedDate = selected
//        today = todayDate
//        notifyDataSetChanged()
//    }
//
//    fun setSelectedDate(date: LocalDate) {
//        selectedDate = date
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_date_box, parent, false)
//        return DateViewHolder(view)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
//        val date = dates[position]
//        holder.bind(date, selectedDate, today, onDateClicked)
//    }
//
//    override fun getItemCount(): Int = dates.size
//
//    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val dateText: TextView = itemView.findViewById(R.id.dateBox)
//
//        @RequiresApi(Build.VERSION_CODES.O)
//        fun bind(
//            date: LocalDate?,
//            selectedDate: LocalDate?,
//            today: LocalDate?,
//            onDateClicked: (LocalDate) -> Unit
//        ) {
//            if (date == null) {
//                dateText.text = ""
//                dateText.background = null
//                return
//            }
//
//            dateText.text = date.dayOfMonth.toString()
//            dateText.setOnClickListener {
//                onDateClicked(date)
//            }
//
//            val context = dateText.context
//
//            val backgroundDrawable = GradientDrawable()
//            backgroundDrawable.shape = GradientDrawable.OVAL
//            backgroundDrawable.setSize(60, 60)
//
//            when {
//                date == selectedDate -> {
//                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.teal_700))
//                    dateText.setTextColor(Color.WHITE)
//                    dateText.background = backgroundDrawable
//                }
//                date == today -> {
//                    backgroundDrawable.setStroke(3, ContextCompat.getColor(context, R.color.red))
//                    backgroundDrawable.setColor(Color.TRANSPARENT)
//                    dateText.setTextColor(ContextCompat.getColor(context, R.color.red))
//                    dateText.background = backgroundDrawable
//                }
//                else -> {
//                    dateText.setTextColor(Color.BLACK)
//                    dateText.background = null
//                }
//            }
//        }
//    }
//}
