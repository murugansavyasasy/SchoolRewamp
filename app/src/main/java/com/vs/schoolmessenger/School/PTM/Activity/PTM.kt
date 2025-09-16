package com.vs.schoolmessenger.School.PTM.Activity

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.UpComingSlotAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.SlotCategory
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PtmStaffBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PTM : BaseActivity<PtmStaffBinding>(),
    View.OnClickListener, StaffSlotClickListener {

    override fun getViewBinding(): PtmStaffBinding {
        return PtmStaffBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isAllSlot = true
    var isSlotCategory: List<SlotCategory>? = null
    var isSelectedDate = ""
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

        binding.lblDatePicking.setOnClickListener(this)
        binding.layoutDatePicking.setOnClickListener(this)
        binding.imgDelete.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.layoutCreateSlot.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name

        loadData()

        appViewModel!!.isPtmSlotResponse?.observe(this) { response ->
            if (response != null && response.status) {
                isSlotCategory = response.data
                isLoadData(isSlotCategory)
            }
        }
    }

    private fun normalizeDate(date: String?): String? {
        if (date.isNullOrBlank()) return null
        val inputPatterns = listOf("dd-MM-yyyy", "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        for (pattern in inputPatterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                val parsed = sdf.parse(date)
                if (parsed != null) {
                    val out = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    return out.format(parsed)
                }
            } catch (_: Exception) { }
        }
        return null
    }

    fun isLoadData(isSlotCategory: List<SlotCategory>?) {
        if (isSlotCategory.isNullOrEmpty()) return

        val todayList = ArrayList<SlotDetail>()
        val upcomingList = ArrayList<SlotDetail>()
        val completedList = ArrayList<SlotDetail>()

        for (category in isSlotCategory) {
            if (isAllSlot) {
                todayList.addAll(category.today.flatMap { it.details })
                upcomingList.addAll(category.upcoming.flatMap { it.details })
                completedList.addAll(category.completed.flatMap { it.details })
            } else {
                val selectedNorm = normalizeDate(isSelectedDate)
                Log.d("PTM", "Selected (raw): $isSelectedDate, Normalized: $selectedNorm")

                for (category in isSlotCategory) {
                    for (group in category.today) {
                        for (detail in group.details) {
                            Log.d(
                                "PTM",
                                "API Date raw: ${detail.date}, normalized: ${normalizeDate(detail.date)}"
                            )
                        }
                    }
                }

                for (group in category.today) {
                    todayList.addAll(group.details.filter { normalizeDate(it.date) == selectedNorm })
                }
                for (group in category.upcoming) {
                    upcomingList.addAll(group.details.filter { normalizeDate(it.date) == selectedNorm })
                }
                for (group in category.completed) {
                    completedList.addAll(group.details.filter { normalizeDate(it.date) == selectedNorm })
                }
            }
        }

        isLoadDataAdapter(todayList, binding.rcyToday)
        isLoadDataAdapter(upcomingList, binding.rcyUpcoming)
        isLoadDataAdapter(completedList, binding.rcyComplete)

        binding.tvNoData.visibility =
            if (todayList.isEmpty() && upcomingList.isEmpty() && completedList.isEmpty())
                View.VISIBLE else View.GONE

        Log.d(
            "PTM",
            "Today: ${todayList.size}, Upcoming: ${upcomingList.size}, Complete: ${completedList.size}"
        )

        binding.lblToday.visibility = if (todayList.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyToday.visibility = binding.lblToday.visibility

        binding.lblUpComing.visibility = if (upcomingList.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyUpcoming.visibility = binding.lblUpComing.visibility

        binding.lblComplete.visibility = if (completedList.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rcyComplete.visibility = binding.lblComplete.visibility
    }

    private fun isLoadDataAdapter(list: ArrayList<SlotDetail>, recyclerView: RecyclerView) {
        val adapter = UpComingSlotAdapter(list, this, this, Constant.isShimmerViewDisable)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadData() {
        val shimmerAdapter = UpComingSlotAdapter(null, this, this, Constant.isShimmerViewShow)

        binding.rcyToday.layoutManager = LinearLayoutManager(this)
        binding.rcyToday.adapter = shimmerAdapter

        binding.rcyUpcoming.layoutManager = LinearLayoutManager(this)
        binding.rcyUpcoming.adapter = shimmerAdapter

        binding.rcyComplete.layoutManager = LinearLayoutManager(this)
        binding.rcyComplete.adapter = shimmerAdapter
        appViewModel!!.isSlotForStaff(isAccessToken!!, "ALL")
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.layoutDatePicking -> {
                Constant.showDatePickerNormal(this) { selectedDate ->
                    Log.d("PTM", "Selected Date (picker): $selectedDate")

                    val formattedDate = try {
                        val input = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val date = input.parse(selectedDate)
                        output.format(date!!)
                    } catch (e: Exception) {
                        selectedDate
                    }

                    binding.imgDelete.visibility = View.VISIBLE
                    binding.lblDatePicking.text = formattedDate
                    isSelectedDate = selectedDate
                    isAllSlot = false
                    isLoadData(isSlotCategory)
                }
            }

            R.id.imgDelete -> {
                binding.lblDatePicking.text = "All"
                binding.imgDelete.visibility = View.GONE
                isAllSlot = true
                isLoadData(isSlotCategory)
            }

            R.id.imgBack -> onBackPressed()

            R.id.layoutCreateSlot -> {
                val intent = Intent(this, CreateSlots::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
    }

    override fun onClickListener(data: SlotDetail) {
        val intent = Intent(this, StaffSlotDetails::class.java)
        val slotList = ArrayList(data.slots)
        intent.putExtra("isSlot", slotList)
        intent.putExtra("isSlotDetails", data)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}
