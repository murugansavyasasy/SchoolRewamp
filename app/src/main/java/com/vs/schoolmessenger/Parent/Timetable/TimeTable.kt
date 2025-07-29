package com.vs.schoolmessenger.Parent.Timetable

import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.TimeTableBinding
import java.util.Calendar


class TimeTable : BaseActivity<TimeTableBinding>(), View.OnClickListener {

    private lateinit var adapter2: TimeTableDayAdapter
    private val timetabledayList = mutableListOf<TimeTableDayData>()
    private var day_id: Int = 1
    private var timeTableDataList: List<TimeTableListData> = emptyList()
    private var isBottomSheetShown = false

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private val scheduleMap = mutableMapOf<String, List<TimeTableListData>>()

    private lateinit var dayAdapter: TimeTableDayAdapter
    private lateinit var scheduleAdapter: TimeTableAdapter
    private lateinit var bottomSheetLayout: LinearLayout

    private lateinit var recyclerViewDays: RecyclerView
    private lateinit var recyclerViewSchedule: RecyclerView
    private lateinit var dayHeader: TextView
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private val allDays =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun getViewBinding(): TimeTableBinding {
        return TimeTableBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        recyclerViewDays = binding.recyclerViewDays
        dayHeader = binding.bottomsheettimetable.dayHeader
        recyclerViewSchedule = binding.bottomsheettimetable.recyclerViewSchedule

        setupRecyclerViewDays()

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayDayName = when (today) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Monday"
        }

        val todayIndex = allDays.indexOf(todayDayName)
        if (todayIndex != -1) {
            adapter2.setSelectedPosition(todayIndex)
            day_id = todayIndex + 1
            loadTimeTable(day_id)
        }

        if (timeTableDataList.isNotEmpty()) {
            val sortedList = timeTableDataList.toMutableList()
            val upcomingIndex = getUpcomingItemPosition(sortedList)

            if (upcomingIndex != -1) {
                val upcomingItem = sortedList.removeAt(upcomingIndex)
                sortedList.add(0, upcomingItem)
            }

            scheduleAdapter = TimeTableAdapter(
                itemList = sortedList,
                listener = object : TimeTableListener {
                    override fun onItemClick(
                        data: TimeTableListData,
                        holder: TimeTableAdapter.DataViewHolder
                    ) {

                    }
                },
                context = this,
                isLoading = false
            )

            recyclerViewSchedule.layoutManager = LinearLayoutManager(this)
            recyclerViewSchedule.adapter = scheduleAdapter

            recyclerViewSchedule.visibility = View.VISIBLE
            binding.lnrNoRecords.visibility = View.GONE
        } else {
            recyclerViewSchedule.visibility = View.GONE
            binding.lnrNoRecords.visibility = View.VISIBLE
            binding.txtNoData.text = "No data found!"
        }

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, dd MMM yy", Locale.getDefault())
        val shortDate = dateFormat.format(calendar.time)
        binding.tvToday.text = shortDate

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun setupRecyclerViewDays() {
        adapter2 = TimeTableDayAdapter(timetabledayList, object : TimeTableDayListener {
            override fun onItemClick(data: TimeTableDayData) {
                day_id = data.day_id
                loadTimeTable(day_id)
            }
        }, this, false)

        binding.recyclerViewDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewDays.adapter = adapter2

        loadHardcodedDays()

        val calendar = Calendar.getInstance()
        val todayIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
        adapter2.setSelectedPosition(todayIndex)
        binding.recyclerViewDays.scrollToPosition(todayIndex)
        day_id = todayIndex + 1 // Mon = 1 ... Sun = 7
        loadTimeTable(day_id)
    }

    private fun loadHardcodedDays() {
        timetabledayList.apply {
            clear()
            add(TimeTableDayData("Mon", 1)) // index 0
            add(TimeTableDayData("Tue", 2)) // index 1
            add(TimeTableDayData("Wed", 3))
            add(TimeTableDayData("Thu", 4))
            add(TimeTableDayData("Fri", 5))
            add(TimeTableDayData("Sat", 6))
            add(TimeTableDayData("Sun", 7)) // index 6
        }
        adapter2.notifyDataSetChanged()
    }

    private fun getUpcomingItemPosition(timetableList: List<TimeTableListData>): Int {
        val currentTime = Calendar.getInstance().time
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        for ((index, item) in timetableList.withIndex()) {
            try {
                val itemTime = format.parse(item.start_time.trim())
                if (itemTime != null && itemTime.after(currentTime)) {
                    return index
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return -1
    }

    override fun onResume() {
        super.onResume()
        if (!isBottomSheetShown) {
            showBottomSheetForToday()
            isBottomSheetShown = true
        }
    }

    private fun showBottomSheetForToday() {
        val bottomSheet = binding.bottomsheettimetable.root
        if (bottomSheet.visibility != View.VISIBLE) {
            val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            bottomSheet.visibility = View.VISIBLE
            bottomSheet.startAnimation(slideUp)
        }
        val calendar = Calendar.getInstance()
        val todayIndex = calendar.get(Calendar.DAY_OF_WEEK)
        val todayDayId = if (todayIndex == 1) 7 else todayIndex - 1 // Adjust Sunday to 7
        val todayName = getDayNameFromId(todayDayId)
        day_id = todayDayId
        loadTimeTable(day_id)
    }

    private fun loadTimeTable(dayId: Int) {
        Log.d("API_CALLING", "Calling getTimeTable for dayId = $dayId")
        appViewModel?.getTimeTable(
            isAccessToken.orEmpty(),
            dayId,
            activity = this
        )

        appViewModel?.isTimeTabletList?.observe(this) { response ->
            Log.d("API_RESULT", "API response received: $response")
            if (response != null && response.status) {
                timeTableDataList = response.data
                val dayName = getDayNameFromId(day_id)
                dayHeader.text = dayName // Set the day name in the header

                if (timeTableDataList.isNotEmpty()) {
                    recyclerViewSchedule.visibility = View.VISIBLE
                    binding.lnrNoRecords.visibility = View.GONE
                    setupScheduleRecyclerView()
                } else {
                    recyclerViewSchedule.visibility = View.GONE
                    binding.lnrNoRecords.visibility = View.VISIBLE
                    binding.txtNoData.text = "No data found!"
                }
            } else {
                Log.e("API_FAILURE", "Failed to load timetable: ${response?.message}")
            }
        }
    }

    private fun setupScheduleRecyclerView() {
        scheduleAdapter = TimeTableAdapter(
            itemList = timeTableDataList,
            listener = object : TimeTableListener {
                override fun onItemClick(
                    data: TimeTableListData,
                    holder: TimeTableAdapter.DataViewHolder
                ) {
                }
            },
            context = this,
            isLoading = false
        )
        recyclerViewSchedule.layoutManager = LinearLayoutManager(this)
        recyclerViewSchedule.adapter = scheduleAdapter
    }

    private fun getDayNameFromId(id: Int): String {
        return when (id) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Unknown"
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.imgBack) onBackPressed()
    }
}
