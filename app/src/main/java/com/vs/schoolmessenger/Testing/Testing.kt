package com.vs.schoolmessenger.Testing

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.CalendarAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.CalendarDate
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.HomeWorkParentData
import com.vs.schoolmessenger.databinding.ParentHomeworkActivityBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Testing : BaseActivity<ParentHomeworkActivityBinding>(), View.OnClickListener {

    override fun getViewBinding(): ParentHomeworkActivityBinding {
        return ParentHomeworkActivityBinding.inflate(layoutInflater)
    }

    private lateinit var dateList: List<CalendarDate>
    private lateinit var calendarAdapter: CalendarAdapter


    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

        binding.recyclerViewCalendar.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        dateList = generateCalendarDates()

        val todayDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

        calendarAdapter = CalendarAdapter(dateList, todayDate) {
            Toast.makeText(this, "Selected: ${it.fullDate}", Toast.LENGTH_SHORT).show()
        }


        binding.recyclerViewCalendar.adapter = calendarAdapter

        binding.recyclerViewCalendar.post {
            val centerOffset = binding.recyclerViewCalendar.width / 2 - 35
            (binding.recyclerViewCalendar.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                10,
                centerOffset
            )
        }

        listOf(
            HomeWorkParentData("Science", "HW: Newton's Laws", 40),
            HomeWorkParentData("Math", "HW: Algebra Basics", 100),
            HomeWorkParentData("Geography", "HW: Climate Zones", 100),
            HomeWorkParentData("Math", "HW: Trigonometry", 80),
            HomeWorkParentData("Science", "HW: Chemical Reactions", 50),
            HomeWorkParentData("History", "HW: World War II", 100),
            HomeWorkParentData("Math", "HW: Area & Perimeter", 75),
            HomeWorkParentData("English", "HW: Grammar Practice", 85),
            HomeWorkParentData("Science", "HW: Photosynthesis", 100),
            HomeWorkParentData("Math", "HW: Pie Charts", 55)
        )


//        val adapter = HomeworkParentAdapter(homeworkList)
//        binding.recyclerView.layoutManager =
//            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
//        binding.recyclerView.adapter = adapter
//        binding.recyclerView.setHasFixedSize(true)
    }

    override fun onClick(v: View?) {

    }

    fun generateCalendarDates(): List<CalendarDate> {
        val list = mutableListOf<CalendarDate>()
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DATE, -10)

        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("dd", Locale.getDefault())
        val fullFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0..20) {
            val date = calendar.time
            list.add(
                CalendarDate(
                    dayFormatter.format(date),
                    dateFormatter.format(date),
                    fullFormatter.format(date), ""
                )
            )
            calendar.add(Calendar.DATE, 1)
        }
        return list
    }
}

