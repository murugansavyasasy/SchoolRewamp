package com.vs.schoolmessenger.School.LessonPlan

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.LessonPlanBinding

class LessonPlan : BaseActivity<LessonPlanBinding>(),
    View.OnClickListener, LessonPlanChartClickListener {

    private lateinit var mAdapter: LessonPlanPicChartAdapter
    private lateinit var isLessonPlanChartData: List<LessonPlanChartData>

    override fun getViewBinding(): LessonPlanBinding {
        return LessonPlanBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.btnAllClasses.setOnClickListener(this)
        binding.btnMyClasses.setOnClickListener(this)

        isLoadData()

    }

    private fun isLoadData() {
        isLessonPlanChartData = listOf(
            LessonPlanChartData(
                "Tamil",
                "X - A",
                "Sathish",
                "Pending",
                30,
                70
            ),
            LessonPlanChartData(
                "Tamil",
                "X - A",
                "Sathish",
                "Pending",
                10,
                90
            ),
            LessonPlanChartData(
                "Tamil",
                "X - A",
                "Sathish",
                "Pending",
                80,
                20
            ),
            LessonPlanChartData(
                "Tamil",
                "X - A",
                "Sathish",
                "Pending",
                90,
                10
            ),
            LessonPlanChartData(
                "Tamil",
                "X - A",
                "Sathish",
                "Pending",
                60,
                40
            ),
            LessonPlanChartData(
                "Tamil",
                "X - A",
                "Sathish",
                "Pending",
                70,
                30
            ),
            LessonPlanChartData(
                "Tamil",
                "X - A",
                "Sathish",
                "Pending",
                90,
                10
            ),
            LessonPlanChartData(
                "Tamil",
                "X - A",
                "Sathish",
                "Pending",
                40,
                60
            ),
            LessonPlanChartData(
                "Tamil",
                "X - A",
                "Sathish",
                "Pending",
                50,
                50
            )
        )


        mAdapter = LessonPlanPicChartAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyLessonPlan.layoutManager = LinearLayoutManager(this)
        binding.rcyLessonPlan.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                LessonPlanPicChartAdapter(
                    isLessonPlanChartData,
                    this,
                    this,
                    Constant.isShimmerViewDisable
                )
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyLessonPlan.adapter = mAdapter
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnAllClasses -> {
                isLoadData()
                binding.btnMyClasses.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.btnAllClasses.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnAllClasses.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_blue)
                binding.btnMyClasses.background = null
            }

            R.id.btnMyClasses -> {
                isLoadData()
                binding.btnMyClasses.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnAllClasses.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.btnMyClasses.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_blue)
                binding.btnAllClasses.background = null

            }
        }
    }


    override fun onItem(data: LessonPlanChartData) {
        val intent = Intent(this@LessonPlan, LessonPlanViewDetails::class.java)
        startActivity(intent)
    }
}