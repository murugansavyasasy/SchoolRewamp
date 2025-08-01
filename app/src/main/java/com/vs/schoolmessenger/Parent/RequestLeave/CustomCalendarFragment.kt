package com.vs.schoolmessenger.Parent.RequestLeave

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class CustomCalendarFragment : Fragment() {

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var currentMonthText: TextView

    private var selectedDate: LocalDate? = null
    private var minDate: LocalDate? = null
    private var maxDate: LocalDate? = null
    private var calendarTag: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private var today: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private var calendar: YearMonth = YearMonth.now()

    private var calendarDateListener: CalendarDateListener? = null

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
        ): CustomCalendarFragment {
            val fragment = CustomCalendarFragment()
            val args = Bundle()
            args.putString(ARG_MIN_DATE, minDate)
            args.putString(ARG_MAX_DATE, maxDate)
            args.putString(ARG_SELECTED_DATE, selectedDate)
            args.putString(ARG_TAG, tag)
            fragment.arguments = args
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            minDate = it.getString(ARG_MIN_DATE)?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
            maxDate = it.getString(ARG_MAX_DATE)?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
            selectedDate = it.getString(ARG_SELECTED_DATE)?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
            calendarTag = it.getString(ARG_TAG)
        }

        selectedDate?.let {
            calendar = YearMonth.of(it.year, it.month)
        }

        calendarDateListener = activity as? CalendarDateListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_calendar, container, false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentMonthText = view.findViewById(R.id.currentMonthText)
        val prevButton = view.findViewById<ImageView>(R.id.prevMonthButton)
        val nextButton = view.findViewById<ImageView>(R.id.nextMonthButton)
        val recyclerView = view.findViewById<RecyclerView>(R.id.dateRecyclerView)
        val holidayLabel = view.findViewById<TextView>(R.id.holidaylabel)
        holidayLabel.visibility = View.GONE

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = CalendarAdapter(
            onDateClicked = { date ->
                selectedDate = date
                calendarAdapter.setSelectedDate(date)
                calendarDateListener?.onDateSelected(date.toString(), calendarTag ?: "")
            },
            minDate = minDate,
            maxDate = maxDate
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar() {
        currentMonthText.text =
            "${calendar.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${calendar.year}"
        val dates = generateDates(calendar)
        calendarAdapter.submitList(dates, selectedDate, today)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateDates(yearMonth: YearMonth): List<LocalDate?> {
        val days = mutableListOf<LocalDate?>()
        val firstOfMonth = yearMonth.atDay(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Sunday = 0
        repeat(dayOfWeek) { days.add(null) }

        for (day in 1..yearMonth.lengthOfMonth()) {
            days.add(yearMonth.atDay(day))
        }
        return days
    }

    interface CalendarDateListener {
        fun onDateSelected(date: String, tag: String)
    }
}

//both
//package com.vs.schoolmessenger.Parent.RequestLeave
//
//import android.os.Build
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.annotation.RequiresApi
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import java.time.LocalDate
//import java.time.YearMonth
//import java.time.format.DateTimeFormatter
//import java.time.format.TextStyle
//import java.util.*
//
//class CustomCalendarFragment : Fragment() {
//
//    private lateinit var calendarAdapter: CalendarAdapter
//    private lateinit var currentMonthText: TextView
//
//    private var selectedDate: LocalDate? = null
//    @RequiresApi(Build.VERSION_CODES.O)
//    private var today: LocalDate = LocalDate.now()
//    @RequiresApi(Build.VERSION_CODES.O)
//    private var calendar: YearMonth = YearMonth.now()
//
//    private var minDate: LocalDate? = null
//    private var maxDate: LocalDate? = null
//    private var calendarTag: String? = null
//
//    private var calendarDateListener: CalendarDateListener? = null
//
//    companion object {
//        fun newInstance(minDate: String, maxDate: String, tag: String): CustomCalendarFragment {
//            val fragment = CustomCalendarFragment()
//            val args = Bundle()
//            args.putString("minDate", minDate)
//            args.putString("maxDate", maxDate)
//            args.putString("tag", tag)
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        arguments?.let {
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//            minDate = it.getString("minDate")?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
//            maxDate = it.getString("maxDate")?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
//            calendarTag = it.getString("tag")
//        }
//
//        calendarDateListener = activity as? CalendarDateListener
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View = inflater.inflate(R.layout.fragment_calendar, container, false)
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        currentMonthText = view.findViewById(R.id.currentMonthText)
//        val prevButton = view.findViewById<ImageView>(R.id.prevMonthButton)
//        val nextButton = view.findViewById<ImageView>(R.id.nextMonthButton)
//        val recyclerView = view.findViewById<RecyclerView>(R.id.dateRecyclerView)
//        val holidayLabel = view.findViewById<TextView>(R.id.holidaylabel)
//        holidayLabel.visibility=View.GONE
//
//        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
//        calendarAdapter = CalendarAdapter(
//            onDateClicked = { date ->
//                selectedDate = date
//                calendarAdapter.setSelectedDate(date)
//                calendarDateListener?.onDateSelected(date.toString(), calendarTag ?: "")
//            },
//            minDate = minDate,
//            maxDate = maxDate
//        )
//        recyclerView.adapter = calendarAdapter
//
//        prevButton.setOnClickListener {
//            calendar = calendar.minusMonths(1)
//            updateCalendar()
//        }
//
//        nextButton.setOnClickListener {
//            calendar = calendar.plusMonths(1)
//            updateCalendar()
//        }
//
//        selectedDate = today
//        updateCalendar()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun updateCalendar() {
//        currentMonthText.text = "${calendar.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${calendar.year}"
//        val dates = generateDates(calendar)
//        calendarAdapter.submitList(dates, selectedDate, today)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun generateDates(yearMonth: YearMonth): List<LocalDate?> {
//        val days = mutableListOf<LocalDate?>()
//        val firstOfMonth = yearMonth.atDay(1)
//        val dayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Sunday = 0
//        repeat(dayOfWeek) { days.add(null) }
//
//        for (day in 1..yearMonth.lengthOfMonth()) {
//            days.add(yearMonth.atDay(day))
//        }
//        return days
//    }
//
//    interface CalendarDateListener {
//        fun onDateSelected(date: String, tag: String)
//    }
//}

//wordking code
//package com.vs.schoolmessenger.Parent.RequestLeave
//
//import android.os.Build
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import java.time.LocalDate
//import java.time.YearMonth
//import java.time.format.DateTimeFormatter
//import java.time.format.TextStyle
//import java.util.Locale
//
//class CustomCalendarFragment : Fragment() {
//
//    private lateinit var calendarAdapter: CalendarAdapter
//    private lateinit var selectedDate: LocalDate
//    private lateinit var currentMonthText: TextView
//    private var calendarTag: String? = null
//
//
//    private var minDate: LocalDate? = null
//    private var maxDate: LocalDate? = null
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private var today = LocalDate.now()
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private var calendar = YearMonth.now()
//
//    companion object {
//        fun newInstance(minDate: String, maxDate: String, tag: String): CustomCalendarFragment {
//            val fragment = CustomCalendarFragment()
//            val args = Bundle().apply {
//                putString("minDate", minDate)
//                putString("maxDate", maxDate)
//                putString("tag", tag)
//            }
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        return inflater.inflate(R.layout.fragment_calendar, container, false)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        currentMonthText = view.findViewById(R.id.currentMonthText)
//        val prevButton = view.findViewById<ImageView>(R.id.prevMonthButton)
//        val nextButton = view.findViewById<ImageView>(R.id.nextMonthButton)
//        val recyclerView = view.findViewById<RecyclerView>(R.id.dateRecyclerView)
//        val holidaylabel = view.findViewById<TextView>(R.id.holidaylabel)
//        holidaylabel.visibility=View.GONE
//
//        // Parse arguments for minDate and maxDate
//        arguments?.getString("minDate")?.let {
//            minDate = LocalDate.parse(it) // Format: yyyy-MM-dd
//        }
//        arguments?.getString("maxDate")?.let {
//            maxDate = LocalDate.parse(it)
//        }
//
//        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
//        calendarAdapter = CalendarAdapter(
//            onDateClicked = { date ->
//                if ((minDate == null || !date.isBefore(minDate)) &&
//                    (maxDate == null || !date.isAfter(maxDate))) {
//                    selectedDate = date
//                    (activity as? CalendarDateListener)?.onDateSelected(date.toString())
//                    calendarAdapter.setSelectedDate(date)
//                } else {
//                    Toast.makeText(requireContext(), "Date out of allowed range", Toast.LENGTH_SHORT).show()
//                }
//            }
//        )
//        recyclerView.adapter = calendarAdapter
//
//        prevButton.setOnClickListener {
//            calendar = calendar.minusMonths(1)
//            updateCalendar()
//        }
//
//        nextButton.setOnClickListener {
//            calendar = calendar.plusMonths(1)
//            updateCalendar()
//        }
//
//        selectedDate = today
//        updateCalendar()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun updateCalendar() {
//        currentMonthText.text =
//            calendar.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + calendar.year
//        val dates = generateDates(calendar)
////        calendarAdapter.submitList(dates, selectedDate, today)
//        calendarAdapter.submitList(dates, selectedDate, today, minDate, maxDate)
//
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun generateDates(yearMonth: YearMonth): List<LocalDate?> {
//        val dates = mutableListOf<LocalDate?>()
//        val firstDayOfMonth = yearMonth.atDay(1)
//        val dayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
//
//        repeat(dayOfWeek) { dates.add(null) }
//        for (day in 1..yearMonth.lengthOfMonth()) {
//            dates.add(yearMonth.atDay(day))
//        }
//
//        return dates
//    }
//
//    fun hideHolidayLabel() {
//        view?.findViewById<View>(R.id.holidaylabel)?.visibility = View.GONE
//    }
//
//    interface CalendarDateListener {
//        fun onDateSelected(date: String)
//    }
//}


//working code
//package com.vs.schoolmessenger.Parent.RequestLeave
//
//import android.os.Build
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.annotation.RequiresApi
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import java.time.LocalDate
//import java.time.YearMonth
//import java.time.format.TextStyle
//import java.util.Locale
//
//class CustomCalendarFragment : Fragment() {
//
//    private lateinit var calendarAdapter: CalendarAdapter
//    private lateinit var selectedDate: LocalDate
//    private lateinit var currentMonthText: TextView
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private var today = LocalDate.now()
//    @RequiresApi(Build.VERSION_CODES.O)
//    private var calendar = YearMonth.now()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        return inflater.inflate(R.layout.fragment_calendar, container, false)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        currentMonthText = view.findViewById(R.id.currentMonthText)
//        val prevButton = view.findViewById<ImageView>(R.id.prevMonthButton)
//        val nextButton = view.findViewById<ImageView>(R.id.nextMonthButton)
//        val recyclerView = view.findViewById<RecyclerView>(R.id.dateRecyclerView)
//        val holidaylabel = view.findViewById<TextView>(R.id.holidaylabel)
//        holidaylabel.visibility=View.GONE
//
//        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
//        calendarAdapter = CalendarAdapter(
//            onDateClicked = { date ->
//                selectedDate = date
//                (activity as? CalendarDateListener)?.onDateSelected(date.toString())
//                calendarAdapter.setSelectedDate(date)
//            }
//        )
//
//        recyclerView.adapter = calendarAdapter
//
//        prevButton.setOnClickListener {
//            calendar = calendar.minusMonths(1)
//            updateCalendar()
//        }
//
//        nextButton.setOnClickListener {
//            calendar = calendar.plusMonths(1)
//            updateCalendar()
//        }
//
//        selectedDate = today
//        updateCalendar()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun updateCalendar() {
//        currentMonthText.text = calendar.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + calendar.year
//        val dates = generateDates(calendar)
//        calendarAdapter.submitList(dates, selectedDate, today)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun generateDates(yearMonth: YearMonth): List<LocalDate?> {
//        val dates = mutableListOf<LocalDate?>()
//        val firstDayOfMonth = yearMonth.atDay(1)
//        val dayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday=0, Saturday=6
//
//        repeat(dayOfWeek) { dates.add(null) }
//        for (day in 1..yearMonth.lengthOfMonth()) {
//            dates.add(yearMonth.atDay(day))
//        }
//
//        return dates
//    }
//
//    fun hideHolidayLabel() {
//        view?.findViewById<View>(R.id.holidaylabel)?.visibility = View.GONE
//    }
//
//
//    interface CalendarDateListener {
//        fun onDateSelected(date: String)
//    }
//}
