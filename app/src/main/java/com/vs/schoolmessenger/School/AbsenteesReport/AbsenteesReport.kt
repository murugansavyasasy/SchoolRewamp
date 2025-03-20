package com.vs.schoolmessenger.School.AbsenteesReport

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AbsenteesReportBinding

class AbsenteesReport : BaseActivity<AbsenteesReportBinding>(),
    View.OnClickListener {

    private lateinit var adapter: AbsenteesReportAdapter
    private lateinit var adapter1: AbsenteesReportDetailAdapter

    private val absenteesList = mutableListOf<AbsenteesDateData>()
    private val absenteesDetailList = mutableListOf<AbsenteesDetailData>()


    override fun getViewBinding(): AbsenteesReportBinding {
        return AbsenteesReportBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        setupRecyclerView()
        setupRecyclerView1()
        loadHardcodedData()
        loadHardcodedData1()

    }


    private fun setupRecyclerView() {
        adapter = AbsenteesReportAdapter(absenteesList, object : AbsenteesClickListener {
            override fun onItemClick(
                data: AbsenteesDateData,
                holder: AbsenteesReportAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.rlaabsenteesreport.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.rlaabsenteesreport.adapter = adapter
    }


    private fun setupRecyclerView1() {
        adapter1 = AbsenteesReportDetailAdapter(absenteesDetailList, object : AbsenteesDetailClickListener {
            override fun onItemClick(
                data: AbsenteesDetailData,
                holder: AbsenteesReportDetailAdapter.DataViewHolder
            ) {


                // Handle item click
            }
        }, this, false)
        binding.rlaabsenteesreport2.layoutManager =
            LinearLayoutManager(this)
        binding.rlaabsenteesreport2.adapter = adapter1

    }


    private fun loadHardcodedData() {
        absenteesList.apply {
            add(AbsenteesDateData("March", "10", "Monday"))
            add(AbsenteesDateData("March", "11", "Tuesday"))
            add(AbsenteesDateData("March", "12", "Wednesday"))
            add(AbsenteesDateData("March", "13", "Thursday"))
            add(AbsenteesDateData("March", "14", "Friday"))
        }
        adapter.notifyDataSetChanged()
    }

    private fun loadHardcodedData1() {
        absenteesDetailList.apply {
            add(AbsenteesDetailData("10th Grade", "21 Jan 2024"))
            add(AbsenteesDetailData("9th Grade", "20 Jan 2024"))
        }
        adapter1.notifyDataSetChanged()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            // Handle clicks if needed
        }
    }
}
