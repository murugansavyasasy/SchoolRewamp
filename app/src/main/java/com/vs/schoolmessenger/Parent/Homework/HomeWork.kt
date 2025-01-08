package com.vs.schoolmessenger.Parent.Homework

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Communication.TextData
import com.vs.schoolmessenger.Parent.Communication.VoiceAdapter
import com.vs.schoolmessenger.Parent.Communication.VoiceData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.HomeWorkParentBinding

class HomeWork : BaseActivity<HomeWorkParentBinding>(), View.OnClickListener,HomeWorkDateClickListener {

    override fun getViewBinding(): HomeWorkParentBinding {
        return HomeWorkParentBinding.inflate(layoutInflater)
    }

    private lateinit var isHomeWork: ArrayList<HomeWorkDateData>
    var mAdapter: HomeWorkAdapter? = null

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.HomeWork)
        binding.toolbarLayout.rytSearch.visibility=View.VISIBLE
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"
        loadVoiceData()

    }


    private fun loadVoiceData() {

        isHomeWork = arrayListOf(
            HomeWorkDateData(
                "Sun, April 1, 2025"
            ),
            HomeWorkDateData(
                "Sun, April 1, 2025"
            ),
            HomeWorkDateData(
                "Sun, April 1, 2025"
            ),
            HomeWorkDateData(
                "Sun, April 1, 2025"
            ),
            HomeWorkDateData(
                "Sun, April 1, 2025"
            ),
            HomeWorkDateData(
                "Sun, April 1, 2025"
            ),
            HomeWorkDateData(
                "Sun, April 1, 2025"
            ),
            HomeWorkDateData(
                "Sun, April 1, 2025"
            ),
            HomeWorkDateData(
                "Sun, April 1, 2025"
            )
        )

        mAdapter = HomeWorkAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHomework.layoutManager = LinearLayoutManager(this)
        binding.rcyHomework.isNestedScrollingEnabled = false;
        binding.rcyHomework.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                HomeWorkAdapter(isHomeWork, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyHomework.adapter = mAdapter
        }
    }


    override fun onClick(p0: View?) {

    }

    override fun onItemClick(data: TextData, holder: HomeWorkAdapter.DataViewHolder) {

    }
}