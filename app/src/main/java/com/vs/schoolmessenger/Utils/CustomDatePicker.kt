package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.Adapter.DateAdapter
import com.vs.schoolmessenger.School.Communication.DataClass.DateItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CustomDatePicker(
    private val context: Context,
    private val preSelectedDates: List<String>,
    private var dateAdapter: DateAdapter?, // Nullable
    private val onDatesConfirmed: (List<String>) -> Unit
) {

    private val calendar = Calendar.getInstance()

    fun show(anchorView: View) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.dialog_multi_date_picker, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Views
        val prevMonthButton = popupView.findViewById<ImageView>(R.id.prevMonthButton)
        val nextMonthButton = popupView.findViewById<ImageView>(R.id.nextMonthButton)
        val currentMonthText = popupView.findViewById<TextView>(R.id.currentMonthText)
        val confirmButton = popupView.findViewById<TextView>(R.id.confirmButton)
        val dateRecyclerView = popupView.findViewById<RecyclerView>(R.id.dateRecyclerView)

        // Setup adapter
        dateAdapter = DateAdapter(context) {}
        dateRecyclerView.layoutManager = GridLayoutManager(context, 7)
        dateRecyclerView.adapter = dateAdapter

        // Load and apply preselected dates
        loadDates(currentMonthText)
        dateAdapter!!.setSelectedDates(preSelectedDates)

        // Button listeners
        prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            loadDates(currentMonthText)
            dateAdapter!!.setSelectedDates(dateAdapter!!.getSelectedDates())
        }

        nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            loadDates(currentMonthText)
            dateAdapter!!.setSelectedDates(dateAdapter!!.getSelectedDates())
        }

        confirmButton.setOnClickListener {
            onDatesConfirmed(dateAdapter!!.getSelectedDates())
            popupWindow.dismiss()
        }

        val parentView = anchorView.rootView as ViewGroup
        val dimView = View(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#80000000"))
            isClickable = true // Intercept clicks
        }
        parentView.addView(dimView)

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener {
            parentView.removeView(dimView)
        }

        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
    }

    private fun loadDates(currentMonthText: TextView) {
        val dateFormat = SimpleDateFormat(Constant.MMMM_yyyy, Locale.getDefault())
        currentMonthText.text = dateFormat.format(calendar.time)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endDate = today.clone() as Calendar
        endDate.add(Calendar.DAY_OF_MONTH, 6)

        val dates = mutableListOf<DateItem>()
        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)


        val displayedMonth = calendar.get(Calendar.MONTH)
        val displayedYear = calendar.get(Calendar.YEAR)



        for (i in 1 until firstDayOfWeek) {
            dates.add(DateItem(null, false))
        }

        for (i in 1..daysInMonth) {
            val currentDate = calendar.clone() as Calendar
            currentDate.set(Calendar.DAY_OF_MONTH, i)
            currentDate.set(Calendar.HOUR_OF_DAY, 0)
            currentDate.set(Calendar.MINUTE, 0)
            currentDate.set(Calendar.SECOND, 0)
            currentDate.set(Calendar.MILLISECOND, 0)

            val isSelectable = !currentDate.before(today) && !currentDate.after(endDate)
            dates.add(DateItem(i, isSelectable, displayedMonth, displayedYear))
        }


        dateAdapter!!.submitDates(dates)
    }
}
