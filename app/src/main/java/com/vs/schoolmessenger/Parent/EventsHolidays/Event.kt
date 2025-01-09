package com.vs.schoolmessenger.Parent.EventsHolidays

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.WebView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.EventParentBinding

class Event : BaseActivity<EventParentBinding>(), View.OnClickListener, EventClickListener {

    override fun getViewBinding(): EventParentBinding {
        return EventParentBinding.inflate(layoutInflater)
    }

    private lateinit var isEventData: List<EventData>
    private lateinit var isHolidayData: List<HolidayData>
    lateinit var mAdapter: EventAdapter
    lateinit var isHolidayAdapter: HolidayAdapter


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Event)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.lnrParent.visibility = View.VISIBLE
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"

        binding.toolbarLayout.lblLeftSideBar.text = resources.getText(R.string.Event)
        binding.toolbarLayout.lblRightSideBar.text = resources.getText(R.string.HoliDay)

        loadData()

        binding.toolbarLayout.lblRightSideBar.setOnClickListener {
            isBackRoundChange(binding.toolbarLayout.lblRightSideBar)
            loadData()
        }
        binding.toolbarLayout.lblLeftSideBar.setOnClickListener {
            isBackRoundChange(binding.toolbarLayout.lblLeftSideBar)
            loadHolidayData()
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }


    private fun loadData() {

        isEventData = listOf(

            EventData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isPDF", "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                ""
            ),
            EventData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isImage", "",
                ""
            ),

            EventData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isImage", "", ""
            )
        )

        mAdapter = EventAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyEvent.layoutManager = LinearLayoutManager(this)
        binding.rcyEvent.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                EventAdapter(isEventData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyEvent.adapter = mAdapter
        }

    }

    private fun loadHolidayData() {

        isHolidayData = listOf(
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration",

                ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),

            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"

            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            ),
            HolidayData(
                "15 Nov 2024",
                "Annual Day Celebration"
            )
        )

        isHolidayAdapter = HolidayAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcyEvent.layoutManager = LinearLayoutManager(this)
        binding.rcyEvent.adapter = isHolidayAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            isHolidayAdapter =
                HolidayAdapter(isHolidayData, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyEvent.adapter = isHolidayAdapter
        }

    }


    private fun isBackRoundChange(isClickingId: TextView) {

        if (isClickingId == binding.toolbarLayout.lblRightSideBar) {
            binding.toolbarLayout.lblLeftSideBar.background = null
            binding.toolbarLayout.lblLeftSideBar.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.dark_blue
                )
            )
        }

        if (isClickingId == binding.toolbarLayout.lblLeftSideBar) {
            binding.toolbarLayout.lblRightSideBar.background = null
            binding.toolbarLayout.lblRightSideBar.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.dark_blue
                )
            )
        }

        isClickingId.background =
            ContextCompat.getDrawable(this, R.drawable.bg_gradient_parent_clickbar)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))

    }

    override fun onItemImageClick(data: EventData) {

    }

    override fun onItemPDFClick(data: EventData) {
        val intent = Intent(this, WebView::class.java)
        intent.putExtra("isTitle", data.isTitle)
        intent.putExtra("isWebLink", data.isLink)
        startActivity(intent)
    }
}