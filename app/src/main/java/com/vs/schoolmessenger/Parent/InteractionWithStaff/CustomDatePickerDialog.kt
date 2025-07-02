import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.InteractionWithStaff.DateAdapter
import com.vs.schoolmessenger.Parent.InteractionWithStaff.DateModel
import com.vs.schoolmessenger.Parent.InteractionWithStaff.YearAdapter
import com.vs.schoolmessenger.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CustomDatePickerDialog(
    private val onDateSelected: (LocalDate) -> Unit
) : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DateAdapter
    private lateinit var tvMonthYear: TextView
    private lateinit var tvFullDate: TextView
    private lateinit var tvYear: TextView
    private lateinit var btnPrev: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var weekHeader: LinearLayout
    private lateinit var btnOk: TextView
    private lateinit var btnCancel: TextView
    private lateinit var yearPickerRecycler: RecyclerView
    private var isYearPickerVisible = false

    private val dateList = mutableListOf<DateModel>()
    @RequiresApi(Build.VERSION_CODES.O)
    private var currentMonthDate = LocalDate.now().withDayOfMonth(1)
    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_custom_date_picker, container, false)
        initViews(view)
        setupListeners()
        buildCalendar()
        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewDates)
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        tvFullDate = view.findViewById(R.id.tvFullDate)
        tvYear = view.findViewById(R.id.tvYear)
        weekHeader = view.findViewById(R.id.weekHeader)
        btnPrev = view.findViewById(R.id.btnPrevMonth)
        btnNext = view.findViewById(R.id.btnNextMonth)
        btnOk = view.findViewById(R.id.btnOk)
        btnCancel = view.findViewById(R.id.btnCancel)
        yearPickerRecycler = view.findViewById(R.id.recyclerViewYears)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        yearPickerRecycler.layoutManager = LinearLayoutManager(requireContext())
        yearPickerRecycler.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        btnPrev.setOnClickListener {
            currentMonthDate = currentMonthDate.minusMonths(1)
            buildCalendar()
        }

        btnNext.setOnClickListener {
            currentMonthDate = currentMonthDate.plusMonths(1)
            buildCalendar()
        }

        btnOk.setOnClickListener {
            onDateSelected(selectedDate)
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        tvYear.setOnClickListener {
            toggleYearPicker(true) // Always show year picker on tap
            val years = (1900..2100).toList()
            val adapter = YearAdapter(years, currentMonthDate.year) { year ->
                currentMonthDate = currentMonthDate.withYear(year)
                selectedDate = LocalDate.of(
                    year,
                    currentMonthDate.month,
                    LocalDate.now().dayOfMonth.coerceAtMost(currentMonthDate.lengthOfMonth())
                )
                buildCalendar()
                toggleYearPicker(false)
            }

            yearPickerRecycler.adapter = adapter
            val index = years.indexOf(currentMonthDate.year)
            if (index != -1) {
                yearPickerRecycler.scrollToPosition(index)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleYearPicker(show: Boolean? = null) {
        isYearPickerVisible = show ?: !isYearPickerVisible
        yearPickerRecycler.visibility = if (isYearPickerVisible) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isYearPickerVisible) View.GONE else View.VISIBLE
        weekHeader.visibility = recyclerView.visibility
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildCalendar() {
        dateList.clear()
        val startOfMonth = currentMonthDate
        val endOfMonth = currentMonthDate.withDayOfMonth(currentMonthDate.lengthOfMonth())
        val dayOffset = (startOfMonth.dayOfWeek.value % 7)

        for (i in 1..dayOffset) {
            dateList.add(DateModel(null))
        }

        var date = startOfMonth
        while (!date.isAfter(endOfMonth)) {
            dateList.add(DateModel(date, isSelected = date == selectedDate))
            date = date.plusDays(1)
        }

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        tvMonthYear.text = currentMonthDate.format(formatter)
        updateHeader()

        adapter = DateAdapter(dateList) { clickedDate ->
            selectedDate = clickedDate.date!!
            buildCalendar()
        }
        recyclerView.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateHeader() {
        val fullFormatter = DateTimeFormatter.ofPattern("MMM d")
        tvFullDate.text = selectedDate.format(fullFormatter)
        tvYear.text = selectedDate.year.toString()
    }
}
