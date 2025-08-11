package com.vs.schoolmessenger.Parent.EventsHolidays

import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Adapter.HolidayAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.Holiday
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private var holidayList: List<Holiday> = emptyList()
    var onDateSelected: ((String) -> Unit)? = null


    companion object {
        private const val ARG_HOLIDAY_LIST = "holiday_list"
        fun newInstance(holidays: List<Holiday>): CalendarFragment {
            val fragment = CalendarFragment()
            val args = Bundle()
            args.putSerializable(ARG_HOLIDAY_LIST, ArrayList(holidays))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            holidayList = it.getSerializable(ARG_HOLIDAY_LIST) as? ArrayList<Holiday> ?: emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        setupCalendar()

        binding.dateRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            private val dividerPaint = Paint().apply {
                color = Color.parseColor("#DDDDDD")
                strokeWidth = 1f
            }

            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                for (i in 0 until parent.childCount) {
                    val view = parent.getChildAt(i)
                    c.drawLine(
                        view.right.toFloat(),
                        view.top.toFloat(),
                        view.right.toFloat(),
                        view.bottom.toFloat(),
                        dividerPaint
                    )
                    c.drawLine(
                        view.left.toFloat(),
                        view.bottom.toFloat(),
                        view.right.toFloat(),
                        view.bottom.toFloat(),
                        dividerPaint
                    )
                }
            }
        })

        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupCalendar() {
        binding.dateRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        val dateAdapter = CustomDateAdapter(
            context = requireContext(), onDateClick = { selectedDates ->

            }, holidays = holidayList, isSelectionEnabled = false
        )


        binding.dateRecyclerView.adapter = dateAdapter

        updateCalendar()


        binding.prevMonthButton.isEnabled = true
        binding.nextMonthButton.isEnabled = true



        binding.prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        binding.nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isSunday(dateString: String, pattern: String = "yyyy-MM-dd"): Boolean {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val date = LocalDate.parse(dateString, formatter)
        return date.dayOfWeek == DayOfWeek.SUNDAY
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val currentMonthYear = monthFormat.format(calendar.time)
        binding.currentMonthText.text = currentMonthYear

        val dates = mutableListOf<CustomDateItem>()

        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)

        val currentMonth = calendar.get(Calendar.MONTH) + 1  // 1-based
        val currentYear = calendar.get(Calendar.YEAR)

        for (i in 1 until firstDayOfWeek) {
            dates.add(
                CustomDateItem(
                    day = null,
                    month = currentMonth,
                    year = currentYear,
                    isSelectable = false
                )
            )
        }


        for (i in 1..daysInMonth) {
            val currentDate = calendar.clone() as Calendar
            currentDate.set(Calendar.DAY_OF_MONTH, i)

            val dateStr = fullDateFormat.format(currentDate.time)
            val isSunday = isSunday(dateStr)
            val isHoliday = holidayList.any { it.date == dateStr }

            dates.add(
                CustomDateItem(
                    day = i,
                    month = currentMonth,
                    year = currentYear,
                    isSelectable = true,
                    isHoliday = isHoliday,
                    isSunday = isSunday
                )
            )
        }

        (binding.dateRecyclerView.adapter as? CustomDateAdapter)?.setDates(dates)

        val visibleHolidays = holidayList.filter {
            val parsedDate = fullDateFormat.parse(it.date)
            val cal = Calendar.getInstance().apply { time = parsedDate!! }
            cal.get(Calendar.MONTH) + 1 == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }


        if (visibleHolidays.isNotEmpty()) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            val holidayModels = visibleHolidays.map {
                val formattedDate = try {
                    val parsedDate = inputFormat.parse(it.date)
                    outputFormat.format(parsedDate!!)
                } catch (e: Exception) {
                    it.date
                }

                Holiday(it.name, it.year,formattedDate)
            }

            Log.d("holidayModels",holidayModels.toString())

            binding.holidayRecyclerView.visibility = View.VISIBLE
            binding.holidaylabel.visibility = View.VISIBLE
            binding.holidayRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.holidayRecyclerView.adapter = HolidayAdapter(holidayModels)
            binding.holidaylabel.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.black)
            )
            binding.holidaylabel.text = "Holidays for $currentMonthYear"
        }
        else {
            binding.holidayRecyclerView.visibility = View.GONE
            binding.holidaylabel.visibility = View.VISIBLE
            binding.holidaylabel.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            )
            binding.holidaylabel.text = "No holidays in $currentMonthYear"
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
