package com.vs.schoolmessenger.School.OnlineMeeting

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.OnlineMeetingBinding
import java.text.SimpleDateFormat
import java.util.Locale

class OnlineMeeting : BaseActivity<OnlineMeetingBinding>(),
    View.OnClickListener, OnlineMeetingClickListener {

    lateinit var mAdapter: OnlineMeetingHistoryAdapter
    private lateinit var isOnlineMeetingHistoryData: List<OnlineMeetingHistoryData>

    override fun getViewBinding(): OnlineMeetingBinding {
        return OnlineMeetingBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnCreate -> {
                binding.noticeboardCreate.visibility = View.VISIBLE
                binding.rcyOnlineMeeting.visibility = View.GONE
                binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnHistory.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
                binding.btnCreate.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_blue)
                binding.btnHistory.background = null
            }

            R.id.btnHistory -> {
                binding.noticeboardCreate.visibility = View.GONE
                binding.rcyOnlineMeeting.visibility = View.VISIBLE
                binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.btnHistory.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_blue)
                binding.btnCreate.background = null
                isLoadData()
            }
        }
    }


    private fun isLoadData() {
        isOnlineMeetingHistoryData = listOf(
            OnlineMeetingHistoryData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ),
            OnlineMeetingHistoryData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ),
            OnlineMeetingHistoryData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ),
            OnlineMeetingHistoryData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ),
            OnlineMeetingHistoryData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ),
            OnlineMeetingHistoryData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ),
            OnlineMeetingHistoryData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ),
            OnlineMeetingHistoryData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ),
            OnlineMeetingHistoryData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            )
        )


        mAdapter = OnlineMeetingHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyOnlineMeeting.layoutManager = LinearLayoutManager(this)
        binding.rcyOnlineMeeting.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                OnlineMeetingHistoryAdapter(
                    isOnlineMeetingHistoryData,
                    this,
                    this,
                    Constant.isShimmerViewDisable
                )
            binding.rcyOnlineMeeting.adapter = mAdapter
        }
    }

    override fun onItemReminderClick(data: OnlineMeetingHistoryData) {
        openCalendarWithEvent(data)
    }

    override fun onItemLinkClick(data: OnlineMeetingHistoryData) {
        isOpenLink()
    }

    private fun parseDateTime(date: String, time: String): Long {
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val dateTime = "$date $time"
        return format.parse(dateTime)?.time ?: System.currentTimeMillis()
    }

    private fun openCalendarWithEvent(event: OnlineMeetingHistoryData) {
        val beginTime = parseDateTime(event.date, event.time)
        val endTime = beginTime + 60 * 60 * 1000 // Example: 1-hour duration
        val intent = Intent(Intent.ACTION_EDIT)
        intent.setType("vnd.android.cursor.item/event")
        intent.putExtra("beginTime", beginTime)
        intent.putExtra("allDay", false)
        intent.putExtra("rrule", "FREQ=DAILY")
        intent.putExtra("description", event.link)
        intent.putExtra("endTime", endTime)
        intent.putExtra("title", event.title)
        startActivity(intent)
    }

    private fun isOpenLink() {

    }
}