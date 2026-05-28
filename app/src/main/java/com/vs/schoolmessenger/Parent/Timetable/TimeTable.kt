package com.vs.schoolmessenger.Parent.Timetable

import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.TimeTableBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TimeTable : BaseActivity<TimeTableBinding>(), View.OnClickListener {

    private lateinit var adapter2: TimeTableDayAdapter
    private val timetabledayList = mutableListOf<TimeTableDayData>()
    private var day_id: Int = 1
    private var timeTableDataList: List<TimeTableListData> = emptyList()
    private var isBottomSheetShown = false
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    private lateinit var scheduleAdapter: TimeTableAdapter
    private lateinit var recyclerViewDays: RecyclerView
    private lateinit var recyclerViewSchedule: RecyclerView
    private lateinit var dayHeader: TextView

    private var selectedDayId: Int = -1

    private val allDays =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun getViewBinding(): TimeTableBinding {
        return TimeTableBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        recyclerViewDays = binding.recyclerViewDays
        dayHeader = binding.bottomsheettimetable.dayHeader
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        recyclerViewSchedule = binding.bottomsheettimetable.recyclerViewSchedule
        setupRecyclerViewDays()


        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat(Constant.EEE_comma_dd_MMM_yy, Locale.getDefault())
        val shortDate = dateFormat.format(calendar.time)
        binding.tvToday.text = shortDate
        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        appViewModel?.isTimeTabletList?.observe(this) { response ->
            Constant.hideLoading(this)
            Log.d("API_RESULT", "API response received: $response")
            val dayName = getDayNameFromId(day_id)
            dayHeader.text = dayName
            if (response != null && response.status && response.data.isNotEmpty()) {
                timeTableDataList = response.data
                recyclerViewSchedule.visibility = View.VISIBLE
                binding.bottomsheettimetable.lnrNoRecords.visibility = View.GONE
                binding.bottomsheettimetable.imgNoData.visibility = View.GONE
                binding.bottomsheettimetable.txtNoData.visibility = View.GONE
                setupScheduleRecyclerView()
                val mobileNumber = SharedPreference.getMobileNumber(this)
                val jsonObject = JsonObject().apply {
                    addProperty(APIKeyNames.mobile_number, mobileNumber)
                    addProperty(APIKeyNames.activity, Constant.add_points_view_time_table)
                    addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                    addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                }
                appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)


            } else {
                timeTableDataList = emptyList()
                recyclerViewSchedule.visibility = View.GONE
                binding.bottomsheettimetable.lnrNoRecords.visibility = View.VISIBLE
                binding.bottomsheettimetable.imgNoData.visibility = View.VISIBLE
                binding.bottomsheettimetable.txtNoData.visibility = View.VISIBLE
                binding.bottomsheettimetable.txtNoData.text =
                    response?.message ?: getString(R.string.no_timetable_available)
            }
        }
    }


    private fun setupRecyclerViewDays() {
        adapter2 = TimeTableDayAdapter(timetabledayList, object : TimeTableDayListener {
            override fun onItemClick(data: TimeTableDayData) {
                Constant.showLoading(this@TimeTable)
                if (data.day_id == selectedDayId) {
                    return
                }
                selectedDayId = data.day_id
                day_id = data.day_id
                loadTimeTable(day_id)
                val position = timetabledayList.indexOfFirst { it.day_id == day_id }
                val layoutManager = binding.recyclerViewDays.layoutManager as LinearLayoutManager
                val viewAtPosition = layoutManager.findViewByPosition(position)
                val itemWidth = viewAtPosition?.width ?: 0
                val screenWidth = resources.displayMetrics.widthPixels
                val offset = (screenWidth / 2) - (itemWidth / 2)
                layoutManager.scrollToPositionWithOffset(position, offset)
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
        day_id = todayIndex + 1
        loadTimeTable(day_id)
    }

    private fun loadHardcodedDays() {
        timetabledayList.apply {
            clear()
            add(TimeTableDayData(Constant.Mon, 1))
            add(TimeTableDayData(Constant.Tue, 2))
            add(TimeTableDayData(Constant.Wed, 3))
            add(TimeTableDayData(Constant.Thu, 4))
            add(TimeTableDayData(Constant.Fri, 5))
            add(TimeTableDayData(Constant.Sat, 6))
            add(TimeTableDayData(Constant.Sun, 7))
        }
        adapter2.notifyDataSetChanged()
    }

    private fun getUpcomingItemPosition(timetableList: List<TimeTableListData>): Int {
        val currentTime = Calendar.getInstance().time
        val format = SimpleDateFormat(Constant.HH_mm, Locale.getDefault())
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
        val todayDayId = if (todayIndex == 1) 7 else todayIndex - 1
        val dayName = getDayNameFromId(todayDayId)
        if (day_id != todayDayId) {
            day_id = todayDayId
            loadTimeTable(day_id)
        } else {
            dayHeader.text = dayName
        }
    }

    private fun loadTimeTable(dayId: Int) {
        Log.d("API_CALLING", "Calling getTimeTable for dayId = $dayId")
        appViewModel?.getTimeTable(
            isAccessToken.orEmpty(),
            dayId,
            activity = this
        )
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
            1 -> Constant.Monday
            2 -> Constant.Tuesday
            3 -> Constant.Wednesday
            4 -> Constant.Thursday
            5 -> Constant.Friday
            6 -> Constant.Saturday
            7 -> Constant.Sunday
            else -> Constant.Unknown
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.imgBack) onBackPressed()
    }
}