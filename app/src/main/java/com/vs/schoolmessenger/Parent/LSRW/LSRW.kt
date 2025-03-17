package com.vs.schoolmessenger.Parent.LSRW

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesDateData
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesDetailClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesDetailData
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReportAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReportDetailAdapter
import com.vs.schoolmessenger.databinding.AbsenteesReportBinding
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.LsrwBinding

class LSRW : BaseActivity<LsrwBinding>() ,
    View.OnClickListener {

        private lateinit var adapter: LSRWAdapter
        private val lsrwList = mutableListOf<LSRWData>()

        override fun getViewBinding(): LsrwBinding {
            return LsrwBinding.inflate(layoutInflater)
        }

        override fun setupViews() {
            super.setupViews()
            setupToolbar()
            setupRecyclerView()
            loadHardcodedData()
        }


        private fun setupRecyclerView() {
            adapter = LSRWAdapter(lsrwList, object : LSRWClickListener {
                override fun onItemClick(
                    data: LSRWData,
                    holder: LSRWAdapter.DataViewHolder
                ) {
                    // Handle item click
                }
            }, this, false)

            binding.rcLsrw.layoutManager =
                LinearLayoutManager(this)
            binding.rcLsrw.adapter = adapter
        }





        private fun loadHardcodedData() {
            lsrwList.apply {
                add(LSRWData("Listening Comprehension - The Environment", "Listen to an audio about saving the environment and answer questions"))
                add(LSRWData("Listening Comprehension - The Environment", "Listen to an audio about saving the environment and answer questions"))
                add(LSRWData("Listening Comprehension - The Environment", "Listen to an audio about saving the environment and answer questions"))
                add(LSRWData("Listening Comprehension - The Environment", "Listen to an audio about saving the environment and answer questions"))
                add(LSRWData("Listening Comprehension - The Environment", "Listen to an audio about saving the environment and answer questions"))
            }
            adapter.notifyDataSetChanged()
        }


        override fun onClick(p0: View?) {
            when (p0?.id) {
                // Handle clicks if needed
            }
        }
    }
