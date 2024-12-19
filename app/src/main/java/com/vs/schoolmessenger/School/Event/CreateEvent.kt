package com.vs.schoolmessenger.School.Event
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.CreateEventBinding

class CreateEvent : BaseActivity<CreateEventBinding>(),
    View.OnClickListener, EventClickListener {

    override fun getViewBinding(): CreateEventBinding {
        return CreateEventBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: EventHistoryAdapter
    private lateinit var isEventHistoryData: List<EventHistoryData>

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)

        Glide.with(this)
            .load("https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391795845263.png")
            .into(binding.imgPick1)

        Glide.with(this)
            .load("https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391801142838.png")
            .into(binding.imgPick2)

        Constant.editTextCounter(this,binding.txtDesc,500,binding.lbTextCount)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.btnCreate -> {
                isBackRoundChange(binding.btnCreate)
                binding.rytHistory.visibility = View.GONE
                binding.eventCreate.visibility = View.VISIBLE

            }

            R.id.btnHistory -> {
                isBackRoundChange(binding.btnHistory)
                binding.rytHistory.visibility = View.VISIBLE
                binding.eventCreate.visibility = View.GONE
                loadData()
            }
        }
    }


    private fun loadData() {
        isEventHistoryData = listOf(
            EventHistoryData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            EventHistoryData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            EventHistoryData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            EventHistoryData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            EventHistoryData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            EventHistoryData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            EventHistoryData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            EventHistoryData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            EventHistoryData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            )
        )

        mAdapter = EventHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.recycleStudents.layoutManager = LinearLayoutManager(this)
        binding.recycleStudents.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                EventHistoryAdapter(isEventHistoryData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.recycleStudents.adapter = mAdapter
        }

    }

    private fun isBackRoundChange(isClickingId: TextView) {

        if (isClickingId == binding.btnCreate) {
            binding.btnHistory.background = null
            binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
        }

        if (isClickingId == binding.btnHistory) {
            binding.btnCreate.background = null
            binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))

        }

        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.bg_blue)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))

    }

    override fun onItemClick(data: EventHistoryData) {

    }
}