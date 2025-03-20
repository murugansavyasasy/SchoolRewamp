package com.vs.schoolmessenger.School.DailyCollection

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesDateData
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesDetailClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesDetailData
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReportAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReportDetailAdapter
import com.vs.schoolmessenger.databinding.DailyCollectionBinding

class DailyCollection : BaseActivity<DailyCollectionBinding>(),
    View.OnClickListener {


    private lateinit var adapter: DcfAdapter1
    private lateinit var adapter2: DcfAdapter2

    private val dcflist1 = mutableListOf<DcfData1>()
    private val dcflist2 = mutableListOf<DcfData2>()


    override fun getViewBinding(): DailyCollectionBinding {
        return DailyCollectionBinding.inflate(layoutInflater)
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
        adapter = DcfAdapter1(dcflist1, object : Dcf1ClickListener {
            override fun onItemClick(data: DcfData1, holder: DcfAdapter1.DataViewHolder) {

            }

        }, this, false)

        binding.totalsummary1.layoutManager =
            LinearLayoutManager(this)

        binding.totalsummary1.adapter = adapter
    }


    private fun setupRecyclerView1() {
        adapter2 = DcfAdapter2(dcflist2, object :
            Dcf2ClickListener {
            override fun onItemClick(
                data: DcfData2,
                holder: DcfAdapter2.DataViewHolder
            ) {


                // Handle item click
            }
        }, this, false)
        binding.totalsummary2.layoutManager =
            LinearLayoutManager(this)
        binding.totalsummary2.adapter = adapter2

    }


    private fun loadHardcodedData() {
        dcflist1.apply {
            add(DcfData1("Tution", "300"))
            add(DcfData1("Library", "700"))
            add(DcfData1("Labroratory", "400"))
            add(DcfData1("Sports", "1000"))
        }
        adapter.notifyDataSetChanged()
    }

    private fun loadHardcodedData1() {
        dcflist2.apply {
            add(DcfData2("Tution", "300"))
            add(DcfData2("Library", "700"))
            add(DcfData2("Labroratory", "400"))
            add(DcfData2("Sports", "1000"))
        }
        adapter2.notifyDataSetChanged()
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}