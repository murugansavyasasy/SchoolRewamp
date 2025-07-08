package com.vs.schoolmessenger.Parent.Timetable

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.LSRW.LSRWAdapter
import com.vs.schoolmessenger.Parent.LSRW.LSRWClickListener
import com.vs.schoolmessenger.Parent.LSRW.LSRWData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LsrwBinding
import com.vs.schoolmessenger.databinding.TimeTableBinding

class TimeTable : BaseActivity<TimeTableBinding>(),
    View.OnClickListener {

    private lateinit var adapter: TimeTableAdapter
    private lateinit var adapter2: TimeTableDayAdapter

    private val timetableList = mutableListOf<TimeTableData>()
    private val timetabledayList = mutableListOf<TimeTableDayData>()
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    override fun getViewBinding(): TimeTableBinding {
        return TimeTableBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        setupRecyclerView()
        loadHardcodedData()
        setupRecyclerView1()
        loadHardcodedData1()

        // Toolbar setup
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text = isChildDetails?.standard_name+ " - " +isChildDetails?.section_name

    }


    private fun setupRecyclerView() {
        adapter = TimeTableAdapter(timetableList, object : TimeTableListener {
            override fun onItemClick(
                data: TimeTableData,
                holder: TimeTableAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupRecyclerView1() {
        adapter2 = TimeTableDayAdapter(timetabledayList, object : TimeTableDayListener {
            override fun onItemClick(
                data: TimeTableDayData,
                holder: TimeTableDayAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.recyclerViewDays.layoutManager =
            LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.recyclerViewDays.adapter = adapter2
    }





    private fun loadHardcodedData() {
        timetableList.apply {
            add(TimeTableData("08: 00 AM", "Tamil","Saran","8AM - 9AM","30 mins"))
            add(TimeTableData("08: 00 AM", "Tamil","Saran","8AM - 9AM","30 mins"))
            add(TimeTableData("08: 00 AM", "Tamil","Saran","8AM - 9AM","30 mins"))
            add(TimeTableData("08: 00 AM", "Tamil","Saran","8AM - 9AM","30 mins"))
            add(TimeTableData("08: 00 AM", "Tamil","Saran","8AM - 9AM","30 mins"))
        }
        adapter.notifyDataSetChanged()
    }

    private fun loadHardcodedData1() {
        timetabledayList.apply {
            add(TimeTableDayData("Mon"))
            add(TimeTableDayData("Tue"))
            add(TimeTableDayData("Wed"))
            add(TimeTableDayData("Thu"))
            add(TimeTableDayData("Fri"))
        }
        adapter2.notifyDataSetChanged()
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}
