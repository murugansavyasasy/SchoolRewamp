package com.vs.schoolmessenger.Parent.LSRW

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.LsrwSummaryBinding


class LSRWSummary : BaseActivity<LsrwSummaryBinding>() ,
    View.OnClickListener {

    private lateinit var adapter: LSRWSummaryAdapter
    private val lsrwsummarylist = mutableListOf<LSRWSummaryData>()

    override fun getViewBinding(): LsrwSummaryBinding {
        return LsrwSummaryBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        setupRecyclerView()
        loadHardcodedData()
    }


    private fun setupRecyclerView() {
        adapter = LSRWSummaryAdapter(lsrwsummarylist, object : LSRWSummaryClickListener {
            override fun onItemClick(
                data: LSRWSummaryData,
                holder: LSRWSummaryAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.rcLsrw.layoutManager =
            LinearLayoutManager(this)
        binding.rcLsrw.adapter = adapter
    }





    private fun loadHardcodedData() {
        lsrwsummarylist.apply {
            add(LSRWSummaryData("Image", "Text"))
            add(LSRWSummaryData("Image", "Text"))
            add(LSRWSummaryData("Image", "Text"))
            add(LSRWSummaryData("Image", "Text"))
            add(LSRWSummaryData("Image", "Text"))
        }
        adapter.notifyDataSetChanged()
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            // Handle clicks if needed
        }
    }
}
