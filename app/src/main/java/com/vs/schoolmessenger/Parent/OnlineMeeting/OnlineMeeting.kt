package com.vs.schoolmessenger.Parent.OnlineMeeting

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateListener
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestAdapter
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.OnlineMeeting.OnlineMeetingClickListener
import com.vs.schoolmessenger.School.OnlineMeeting.OnlineMeetingHistoryAdapter
import com.vs.schoolmessenger.School.OnlineMeeting.OnlineMeetingHistoryData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.CertificateRequestParentBinding
import com.vs.schoolmessenger.databinding.OnlineMeetingBinding
import com.vs.schoolmessenger.databinding.OnlineMeetingParentBinding
import java.text.SimpleDateFormat
import java.util.Locale

class OnlineMeeting : BaseActivity<OnlineMeetingParentBinding>() , View.OnClickListener {
    private lateinit var adapter: OnlineMeetingAdapter
    private val onlinemeetinglist = mutableListOf<OnlineMeetingData>()

    override fun getViewBinding(): OnlineMeetingParentBinding {
        return OnlineMeetingParentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        setupRecyclerView()
        loadHardcodedData()
    }

    private fun setupRecyclerView() {
        adapter = OnlineMeetingAdapter(onlinemeetinglist, object : OnlineMeetingListener {
            override fun onItemClick(
                data: OnlineMeetingData,
                holder: OnlineMeetingAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.rcyOnlineMeeting.layoutManager = LinearLayoutManager(this)
        binding.rcyOnlineMeeting.adapter = adapter
    }

    private fun loadHardcodedData() {
        onlinemeetinglist.apply {
            add(OnlineMeetingData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ))
            add(OnlineMeetingData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ))
            add(OnlineMeetingData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ))
            add(OnlineMeetingData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ))
            add(OnlineMeetingData(
                "Sathish",
                "12:12 AM",
                "12/12/2024",
                "Google Meet",
                "https://meet.google.com/mnd-tpqm-gbb"
            ))
        }
        adapter.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {

        }
    }
}
