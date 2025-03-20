package com.vs.schoolmessenger.School.AbsenteesReport

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AbsenteesReportBinding
import com.vs.schoolmessenger.databinding.AbsenteesStudentlistBinding

class AbsenteesStudents : BaseActivity<AbsenteesStudentlistBinding>(),
    View.OnClickListener {

    private lateinit var adapter: AbsenteesStudentHeaderListAdapter
    private lateinit var adapter1: AbsenteesStudentFooterListAdapter

    private val absenteesheaderlist = mutableListOf<AbsenteesStudentHeaderData>()
    private val absenteesfooterlist = mutableListOf<AbsenteesStudentFooterData>()


    override fun getViewBinding(): AbsenteesStudentlistBinding {
        return AbsenteesStudentlistBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        setupRecyclerView()
        loadHardcodedData()
        setupRecyclerView1()
        loadHardcodedData1()
    }


    private fun setupRecyclerView() {
        adapter = AbsenteesStudentHeaderListAdapter(absenteesheaderlist, object : AbsenteesHeaderClickListener {
            override fun onItemClick(
                data: AbsenteesStudentHeaderData,
                holder: AbsenteesStudentHeaderListAdapter.DataViewHolder
            ) {
///Handle Item Click

            }
        }, this, false)

        binding.studendreport.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.studendreport.adapter = adapter
    }

    private fun setupRecyclerView1() {
        adapter1 = AbsenteesStudentFooterListAdapter(absenteesfooterlist, object : AbsenteesFooterClickListener {
            override fun onItemClick(
                data: AbsenteesStudentFooterData,
                holder: AbsenteesStudentFooterListAdapter.DataViewHolder
            ) {
///Handle Item Click

            }
        }, this, false)
        binding.studentlistreport.layoutManager =
            LinearLayoutManager(this)
        binding.studentlistreport.adapter = adapter1
    }


    private fun loadHardcodedData() {
        absenteesheaderlist.apply {
            add(AbsenteesStudentHeaderData("VII - A"))
            add(AbsenteesStudentHeaderData("VII - B"))
            add(AbsenteesStudentHeaderData("VII - C"))
            add(AbsenteesStudentHeaderData("VII - D"))
            add(AbsenteesStudentHeaderData("VII - E"))
            add(AbsenteesStudentHeaderData("VII - F"))
            add(AbsenteesStudentHeaderData("VII - G"))
            add(AbsenteesStudentHeaderData("VII - H"))
            add(AbsenteesStudentHeaderData("VII - I"))
            add(AbsenteesStudentHeaderData("VII - J"))

        }
        adapter.notifyDataSetChanged()
    }


    private fun loadHardcodedData1() {
        absenteesfooterlist.apply {
            add(AbsenteesStudentFooterData("John Doe","10th A","AD2413"))
            add(AbsenteesStudentFooterData("Steve Smith","9th B","AD2412"))
            add(AbsenteesStudentFooterData("Virat","8th C","AD2411"))
            add(AbsenteesStudentFooterData("Dravid","12th D","AD2417"))
            add(AbsenteesStudentFooterData("Micheal","5th E","AD2418"))
        }
        adapter1.notifyDataSetChanged()
    }


    override fun onClick(p0: View?) {

        when (p0?.id) {
            // Handle clicks if needed
        }
    }
}