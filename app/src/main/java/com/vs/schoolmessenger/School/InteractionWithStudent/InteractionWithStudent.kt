package com.vs.schoolmessenger.School.InteractionWithStudent

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.IntrectionWithStudentBinding

class InteractionWithStudent : BaseActivity<IntrectionWithStudentBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): IntrectionWithStudentBinding {
        return IntrectionWithStudentBinding.inflate(layoutInflater)
    }

    private lateinit var isInteractionStudentData: List<InteractionStudentData>
    lateinit var mAdapter: InteractionWithStudentAdapter
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.lblParentToolBar.text = "Interact with student"
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }

        loadData()
    }


    private fun loadData() {

        isInteractionStudentData = listOf(
            InteractionStudentData(
                "Sathish",
                "Tamil"
            ),
            InteractionStudentData(
                "Sathish",
                "Tamil"
            ),

            InteractionStudentData(
                "Sathish",
                "Tamil"
            ),
            InteractionStudentData(
                "Sathish",
                "Tamil"
            ),
            InteractionStudentData(
                "Sathish",
                "Tamil"
            ),
            InteractionStudentData(
                "Sathish",
                "Tamil"
            )
        )

        mAdapter = InteractionWithStudentAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcyStudentList.layoutManager = GridLayoutManager(this, 2) // Set 2 columns
        binding.rcyStudentList.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter = InteractionWithStudentAdapter(
                isInteractionStudentData,
                this,
                Constant.isShimmerViewDisable
            )
            // Ensure the layout manager is still GridLayoutManager
            binding.rcyStudentList.layoutManager = GridLayoutManager(this, 2)
            binding.rcyStudentList.adapter = mAdapter
        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}