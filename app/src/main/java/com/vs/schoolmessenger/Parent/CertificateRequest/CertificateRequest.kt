package com.vs.schoolmessenger.Parent.CertificateRequest

import android.R
import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.LSRW.LSRWAdapter
import com.vs.schoolmessenger.Parent.LSRW.LSRWClickListener
import com.vs.schoolmessenger.Parent.LSRW.LSRWData
import com.vs.schoolmessenger.databinding.CertificateRequestParentBinding


class CertificateRequest  : BaseActivity<CertificateRequestParentBinding>(), View.OnClickListener {
    private lateinit var adapter: CertificateRequestAdapter
    private val cflist = mutableListOf<CertificateRequestData>()
    override fun getViewBinding(): CertificateRequestParentBinding {
        return CertificateRequestParentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
//        setUpGradientParent()
        binding.lblTeacher.setOnClickListener(this)
        binding.lblParent.setOnClickListener(this)
        setupRecyclerView()
        loadHardcodedData()
    }


    private fun setupRecyclerView() {
        adapter = CertificateRequestAdapter(cflist, object : CertificateListener {
            override fun onItemClick(
                data: CertificateRequestData,
                holder: CertificateRequestAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }


    private fun loadHardcodedData() {
        cflist.apply {
            add(CertificateRequestData(": Attendance Certificate", ": Listen to an audio about saving the environment and answer questions",": 21 Jan 2024"))
            add(CertificateRequestData(": Attendance Certificate", ": Listen to an audio about saving the environment and answer questions",": 21 Jan 2024"))
            add(CertificateRequestData(": Attendance Certificate", ": Listen to an audio about saving the environment and answer questions",": 21 Jan 2024"))
            add(CertificateRequestData(": Attendance Certificate", ": Listen to an audio about saving the environment and answer questions",": 21 Jan 2024"))
            add(CertificateRequestData(": Attendance Certificate", ": Listen to an audio about saving the environment and answer questions",": 21 Jan 2024"))
        }
        adapter.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            com.vs.schoolmessenger.R.id.lblTeacher -> {
                binding.lblTeacher.setBackgroundResource(com.vs.schoolmessenger.R.drawable.bg_radiantgreen)
                binding.lblParent.setTextColor(Color.GRAY)
                binding.lblTeacher.setTextColor(Color.BLACK)
                binding.lblParent.setBackgroundResource(0)
                binding.recyclerView.visibility = View.GONE
                binding.rellay1.visibility = View.VISIBLE
            }

            com.vs.schoolmessenger.R.id.lblParent -> {
                binding.lblParent.setBackgroundResource(com.vs.schoolmessenger.R.drawable.bg_radiantgreen)
                binding.lblParent.setTextColor(Color.BLACK)
                binding.lblTeacher.setTextColor(Color.GRAY)
                binding.lblTeacher.setBackgroundResource(0)
                binding.recyclerView.visibility = View.VISIBLE
                binding.rellay1.visibility = View.GONE

            }
        }
    }

}