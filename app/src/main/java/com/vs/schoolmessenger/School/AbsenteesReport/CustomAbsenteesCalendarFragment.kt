package com.vs.schoolmessenger.School.AbsenteesReport

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesCalendarListener
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CustomAbsenteesCalendarFragment : Fragment() {
    private lateinit var calendarAdapter: CalendarAbsenteesAdapter
    private lateinit var currentMonthText: TextView
    private var selectedDate: LocalDate? = null
    private var minDate: LocalDate? = null
    private var maxDate: LocalDate? = null
    private var calendarTag: String? = null
    private var today: LocalDate = LocalDate.now()
    private var calendar: YearMonth = YearMonth.now()
    private var calendarListener: AbsenteesCalendarListener? = null  // Updated type

    companion object {
        private const val ARG_MIN_DATE = "minDate"
        private const val ARG_MAX_DATE = "maxDate"
        private const val ARG_SELECTED_DATE = "selectedDate"
        private const val ARG_TAG = "tag"
        fun newInstance(
            minDate: String,
            maxDate: String,
            selectedDate: String?,
            tag: String
        ): CustomAbsenteesCalendarFragment {
            val fragment = CustomAbsenteesCalendarFragment()
            val args = Bundle()
            args.putString(ARG_MIN_DATE, minDate)
            args.putString(ARG_MAX_DATE, maxDate)
            args.putString(ARG_SELECTED_DATE, selectedDate)
            args.putString(ARG_TAG, tag)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            minDate =
                it.getString(ARG_MIN_DATE)?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
            maxDate =
                it.getString(ARG_MAX_DATE)?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
            selectedDate = it.getString(ARG_SELECTED_DATE)
                ?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
            calendarTag = it.getString(ARG_TAG)
        }
        selectedDate?.let {
            calendar = YearMonth.of(it.year, it.month)
        }
        calendarListener = activity as? AbsenteesCalendarListener  // Updated cast
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_calendar_2, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentMonthText = view.findViewById(R.id.currentMonthText)
        val prevButton = view.findViewById<ImageView>(R.id.prevMonthButton)
        val nextButton = view.findViewById<ImageView>(R.id.nextMonthButton)
        val recyclerView = view.findViewById<RecyclerView>(R.id.dateRecyclerView)
        val holidayLabel = view.findViewById<TextView>(R.id.holidaylabel)
        holidayLabel.visibility = View.GONE
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = CalendarAbsenteesAdapter(
            onDateClicked = { date ->
                selectedDate = date
                calendarAdapter.setSelectedDate(date)
                calendarListener?.onDateSelected(date.toString(), calendarTag ?: "")  // Updated call
            },
            minDate = minDate,
            maxDate = maxDate,
            isAbsenteesReport = calendarTag == "absentees_calendar"
        )
        recyclerView.adapter = calendarAdapter
        prevButton.setOnClickListener {
            calendar = calendar.minusMonths(1)
            updateCalendar()
        }
        nextButton.setOnClickListener {
            calendar = calendar.plusMonths(1)
            updateCalendar()
        }
        updateCalendar()
    }

    fun setAbsentDates(dates: List<LocalDate>) {
        calendarAdapter.setAbsentDates(dates.toSet())
    }

    private fun updateCalendar() {
        currentMonthText.text =
            "${calendar.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${calendar.year}"
        val dates = generateDates(calendar)
        calendarAdapter.submitList(dates, selectedDate, today)
        // Notify activity of month change (initial load and button clicks)
        calendarListener?.onMonthChanged(calendar.monthValue, calendar.year)
    }

    private fun generateDates(yearMonth: YearMonth): List<LocalDate?> {
        val days = mutableListOf<LocalDate?>()
        val firstOfMonth = yearMonth.atDay(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value % 7
        repeat(dayOfWeek) { days.add(null) }
        for (day in 1..yearMonth.lengthOfMonth()) {
            days.add(yearMonth.atDay(day))
        }
        return days
    }
}
