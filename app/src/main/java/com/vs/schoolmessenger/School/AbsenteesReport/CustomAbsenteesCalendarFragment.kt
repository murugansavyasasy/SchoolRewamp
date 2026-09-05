package com.vs.schoolmessenger.School.AbsenteesReport

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesCalendarListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
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
    private var calendarListener: AbsenteesCalendarListener? = null

    // Time picker state variables
    private var isFromTime: Boolean = true
    private var fromHour24: Int? = null
    private var fromMinute: Int? = null
    private var toHour24: Int? = null
    private var toMinute: Int? = null
    private var selectedDates: MutableSet<String> = mutableSetOf()

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
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

            minDate = it.getString(ARG_MIN_DATE)?.let { dateStr ->
                LocalDate.parse(normalizeDigits(dateStr), formatter)
            }
            maxDate = it.getString(ARG_MAX_DATE)?.let { dateStr ->
                LocalDate.parse(normalizeDigits(dateStr), formatter)
            }
            selectedDate = it.getString(ARG_SELECTED_DATE)?.let { dateStr ->
                LocalDate.parse(normalizeDigits(dateStr), formatter)
            }
            calendarTag = it.getString(ARG_TAG)
        }
        selectedDate?.let {
            calendar = YearMonth.of(it.year, it.month)
        }
        calendarListener = activity as? AbsenteesCalendarListener
    }

    // ✅ Normalize Arabic-Indic and Eastern Arabic-Indic digits to ASCII
    private fun normalizeDigits(input: String): String {
        val arabicIndic = "٠١٢٣٤٥٦٧٨٩"
        val easternArabic = "۰۱۲۳۴۵۶۷۸۹"
        val ascii = "0123456789"

        return input.map { char ->
            when {
                arabicIndic.contains(char) -> ascii[arabicIndic.indexOf(char)]
                easternArabic.contains(char) -> ascii[easternArabic.indexOf(char)]
                else -> char
            }
        }.joinToString("")
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
                calendarListener?.onDateSelected(
                    date.toString(),
                    calendarTag ?: ""
                )
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
            "${calendar.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${calendar.year}"
        selectedDate = null
        calendarAdapter.setSelectedDate(null)

        val dates = generateDates(calendar)
        calendarAdapter.submitList(dates, selectedDate, today)

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

    // =================================================================
    // TIME PICKER DIALOG WITH ENGLISH LOCALE - FULLY SELF-CONTAINED
    // =================================================================

    private fun showTimePickerDialog(
        context: Context,
        listener: TimeSelectedListener,
        preSelectedHour: Int?,
        preSelectedMinute: Int?
    ) {
        val calendar = Calendar.getInstance()

        val hour = preSelectedHour ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = preSelectedMinute ?: calendar.get(Calendar.MINUTE)

        var isTimeSelected = false

        // ✅ Save original locale and force English
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)

        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.ENGLISH)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        val timePicker = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->

                isTimeSelected = true

                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                }

                val today = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                val isTodaySelected = selectedDates.contains(today)

                if (isFromTime) {

                    if (isTodaySelected) {
                        val minAllowedCal = Calendar.getInstance()
                        minAllowedCal.add(Calendar.MINUTE, 10)

                        if (selectedCal.before(minAllowedCal)) {
                            Toast.makeText(
                                context,
                                "From time must be at least 10 minutes from now",
                                Toast.LENGTH_LONG
                            ).show()

                            // ✅ Restore locale before returning
                            restoreLocale(context, originalLocale)
                            return@TimePickerDialog
                        }
                    }

                    fromHour24 = selectedHour
                    fromMinute = selectedMinute

                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        add(Calendar.MINUTE, 40)
                    }

                    toHour24 = cal.get(Calendar.HOUR_OF_DAY)
                    toMinute = cal.get(Calendar.MINUTE)

                    // Update your UI bindings here (adapt to your actual views)
                    // binding.lblStartTime.text = formatTime12h(fromHour24!!, fromMinute!!)
                    // binding.lblEndTime.text = formatTime12h(toHour24!!, toMinute!!)

                } else {

                    if (fromHour24 != null && fromMinute != null) {

                        val minToCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, fromHour24!!)
                            set(Calendar.MINUTE, fromMinute!!)
                            set(Calendar.SECOND, 0)
                            add(Calendar.MINUTE, 40)
                        }

                        if (selectedCal.before(minToCal)) {
                            Toast.makeText(
                                context,
                                "End time must be at least 40 minutes after start time",
                                Toast.LENGTH_LONG
                            ).show()

                            // ✅ Restore locale before returning
                            restoreLocale(context, originalLocale)
                            return@TimePickerDialog
                        }
                    }

                    toHour24 = selectedHour
                    toMinute = selectedMinute

                    // Update your UI bindings here
                    // binding.lblEndTime.text = formatTime12h(toHour24!!, toMinute!!)
                }

                // ✅ Restore locale after successful selection
                restoreLocale(context, originalLocale)

                // Notify listener
                listener.onTimeSelected(selectedHour, selectedMinute, isFromTime)

            }, hour, minute, false
        )

        // ✅ Restore locale when dialog is cancelled
        timePicker.setOnCancelListener {
            if (!isFromTime && !isTimeSelected &&
                fromHour24 != null && fromMinute != null
            ) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, fromHour24!!)
                    set(Calendar.MINUTE, fromMinute!!)
                    add(Calendar.MINUTE, 40)
                }

                toHour24 = cal.get(Calendar.HOUR_OF_DAY)
                toMinute = cal.get(Calendar.MINUTE)

                // Update your UI bindings here
                // binding.lblEndTime.text = formatTime12h(toHour24!!, toMinute!!)
            }

            // ✅ Always restore locale on cancel
            restoreLocale(context, originalLocale)
        }

        // ✅ Also restore locale on dismiss (covers back button, outside tap)
        timePicker.setOnDismissListener {
            restoreLocale(context, originalLocale)
        }

        timePicker.show()
    }

    // ✅ Helper: Restore original locale
    private fun restoreLocale(context: Context, originalLocale: Locale) {
        Locale.setDefault(originalLocale)
        val restoreConfig = Configuration(context.resources.configuration)
        restoreConfig.setLocale(originalLocale)
        context.resources.updateConfiguration(restoreConfig, context.resources.displayMetrics)
    }

    // ✅ Helper: Format time in 12h English format
    private fun formatTime12h(hour24: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        return sdf.format(calendar.time)
    }

    // ✅ Listener interface
    interface TimeSelectedListener {
        fun onTimeSelected(hour: Int, minute: Int, isFromTime: Boolean)
    }
}









//package com.vs.schoolmessenger.School.AbsenteesReport
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesCalendarListener
//import java.time.LocalDate
//import java.time.YearMonth
//import java.time.format.DateTimeFormatter
//import java.time.format.TextStyle
//import java.util.Locale
//
//class CustomAbsenteesCalendarFragment : Fragment() {
//    private lateinit var calendarAdapter: CalendarAbsenteesAdapter
//    private lateinit var currentMonthText: TextView
//    private var selectedDate: LocalDate? = null
//    private var minDate: LocalDate? = null
//    private var maxDate: LocalDate? = null
//    private var calendarTag: String? = null
//    private var today: LocalDate = LocalDate.now()
//    private var calendar: YearMonth = YearMonth.now()
//    private var calendarListener: AbsenteesCalendarListener? = null  // Updated type
//
//    companion object {
//        private const val ARG_MIN_DATE = "minDate"
//        private const val ARG_MAX_DATE = "maxDate"
//        private const val ARG_SELECTED_DATE = "selectedDate"
//        private const val ARG_TAG = "tag"
//        fun newInstance(
//            minDate: String,
//            maxDate: String,
//            selectedDate: String?,
//            tag: String
//        ): CustomAbsenteesCalendarFragment {
//            val fragment = CustomAbsenteesCalendarFragment()
//            val args = Bundle()
//            args.putString(ARG_MIN_DATE, minDate)
//            args.putString(ARG_MAX_DATE, maxDate)
//            args.putString(ARG_SELECTED_DATE, selectedDate)
//            args.putString(ARG_TAG, tag)
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//            minDate =
//                it.getString(ARG_MIN_DATE)?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
//            maxDate =
//                it.getString(ARG_MAX_DATE)?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
//            selectedDate = it.getString(ARG_SELECTED_DATE)
//                ?.let { dateStr -> LocalDate.parse(dateStr, formatter) }
//            calendarTag = it.getString(ARG_TAG)
//        }
//        selectedDate?.let {
//            calendar = YearMonth.of(it.year, it.month)
//        }
//        calendarListener = activity as? AbsenteesCalendarListener  // Updated cast
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View = inflater.inflate(R.layout.fragment_calendar_2, container, false)
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        currentMonthText = view.findViewById(R.id.currentMonthText)
//        val prevButton = view.findViewById<ImageView>(R.id.prevMonthButton)
//        val nextButton = view.findViewById<ImageView>(R.id.nextMonthButton)
//        val recyclerView = view.findViewById<RecyclerView>(R.id.dateRecyclerView)
//        val holidayLabel = view.findViewById<TextView>(R.id.holidaylabel)
//        holidayLabel.visibility = View.GONE
//        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
//        calendarAdapter = CalendarAbsenteesAdapter(
//            onDateClicked = { date ->
//                selectedDate = date
//                calendarAdapter.setSelectedDate(date)
//                calendarListener?.onDateSelected(
//                    date.toString(),
//                    calendarTag ?: ""
//                )  // Updated call
//            },
//            minDate = minDate,
//            maxDate = maxDate,
//            isAbsenteesReport = calendarTag == "absentees_calendar"
//        )
//        recyclerView.adapter = calendarAdapter
//        prevButton.setOnClickListener {
//            calendar = calendar.minusMonths(1)
//            updateCalendar()
//        }
//        nextButton.setOnClickListener {
//            calendar = calendar.plusMonths(1)
//            updateCalendar()
//        }
//        updateCalendar()
//    }
//
//    fun setAbsentDates(dates: List<LocalDate>) {
//        calendarAdapter.setAbsentDates(dates.toSet())
//    }
//
//    private fun updateCalendar() {
//        currentMonthText.text =
//            "${calendar.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${calendar.year}"
//
//        // Clear selection when month changes
//        selectedDate = null
//        calendarAdapter.setSelectedDate(null)
//
//        val dates = generateDates(calendar)
//        calendarAdapter.submitList(dates, selectedDate, today)
//
//        calendarListener?.onMonthChanged(calendar.monthValue, calendar.year)
//    }
//
//    private fun generateDates(yearMonth: YearMonth): List<LocalDate?> {
//        val days = mutableListOf<LocalDate?>()
//        val firstOfMonth = yearMonth.atDay(1)
//        val dayOfWeek = firstOfMonth.dayOfWeek.value % 7
//        repeat(dayOfWeek) { days.add(null) }
//        for (day in 1..yearMonth.lengthOfMonth()) {
//            days.add(yearMonth.atDay(day))
//        }
//        return days
//    }
//}
