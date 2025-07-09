package com.vs.schoolmessenger.Parent.Timetable

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateListData
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

    private val timetabledayList = mutableListOf<TimeTableDayData>()
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private var day_id: Int? = 1


    private lateinit var timeTableDataList: List<TimeTableListData>


    override fun getViewBinding(): TimeTableBinding {
        return TimeTableBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        setupRecyclerViewDays()
        loadHardcodedDays()

        // Toolbar setup
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text = isChildDetails?.standard_name+ " - " +isChildDetails?.section_name

        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        loadTimeTable()

        appViewModel!!.isTimeTabletList?.observe(this) { response ->
            if (response != null && response.status) {
                timeTableDataList = response.data
                if(timeTableDataList.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.lnrNoRecords.visibility = View.GONE
                    setupRecyclerView()
                }
            }
            else{
                binding.recyclerView.visibility = View.GONE
                binding.lnrNoRecords.visibility = View.VISIBLE
                binding.txtNoData.text = "No data found!"
            }
        }
    }

    private fun loadTimeTable() {
        showShimmer()
        appViewModel?.getTimeTable(
            isAccessToken.orEmpty(),day_id!!, activity = this
        )
    }

    private fun showShimmer() {
        adapter = TimeTableAdapter(null, object : TimeTableListener {
            override fun onItemClick(
                data: TimeTableListData,
                holder: TimeTableAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, Constant.isShimmerViewShow)

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }


    private fun setupRecyclerView() {
        adapter = TimeTableAdapter(timeTableDataList, object : TimeTableListener {
            override fun onItemClick(
                data: TimeTableListData,
                holder: TimeTableAdapter.DataViewHolder
            ) {
                // Handle item click

            }
        }, this, Constant.isShimmerViewDisable)

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupRecyclerViewDays() {
        adapter2 = TimeTableDayAdapter(timetabledayList, object : TimeTableDayListener {
            override fun onItemClick(
                data: TimeTableDayData
            ) {
                // Handle item click
                Log.d("selected_day_id",data.day_id.toString())
                day_id = data.day_id
                loadTimeTable()
            }
        }, this, false)

        binding.recyclerViewDays.layoutManager =
            LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.recyclerViewDays.adapter = adapter2

        adapter2.setSelectedPosition(0)

    }


    private fun loadHardcodedDays() {
        timetabledayList.apply {
            add(TimeTableDayData("Mon",1))
            add(TimeTableDayData("Tue",2))
            add(TimeTableDayData("Wed",3))
            add(TimeTableDayData("Thu",4))
            add(TimeTableDayData("Fri",5))
            add(TimeTableDayData("Sat",6))
            add(TimeTableDayData("Sun",7))
        }
        adapter2.notifyDataSetChanged()
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}
