package com.vs.schoolmessenger.School.LessonPlan

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.LessonPlanBinding

class LessonPlan : BaseActivity<LessonPlanBinding>(),
    View.OnClickListener, LessonPlanClickListener {

    private lateinit var mAdapter: LessonPlanAdapter
    private lateinit var isLessonPlanData: List<LessonPlanData>

    override fun getViewBinding(): LessonPlanBinding {
        return LessonPlanBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)



        isLessonPlanData = listOf(
            LessonPlanData(
                "Tamil",
                "12:21",
                "11:21",
                "Tamil",
                "Nothing",
                2
            ),
            LessonPlanData(
                "Tamil",
                "12:21",
                "11:21",
                "Tamil",
                "Nothing",
                3
            ),
            LessonPlanData(
                "Tamil",
                "12:21",
                "11:21",
                "Tamil",
                "Nothing", 1
            ),
            LessonPlanData(
                "Tamil",
                "12:21",
                "11:21",
                "Tamil",
                "Nothing", 2
            ),
            LessonPlanData(
                "Tamil",
                "12:21",
                "11:21",
                "Tamil",
                "Nothing", 1
            ),
            LessonPlanData(
                "Tamil",
                "12:21",
                "11:21",
                "Tamil",
                "Nothing", 3
            ),
            LessonPlanData(
                "Tamil",
                "12:21",
                "11:21",
                "Tamil",
                "Nothing", 2
            ),
            LessonPlanData(
                "Tamil",
                "12:21",
                "11:21",
                "Tamil",
                "Nothing", 1
            ),
            LessonPlanData(
                "Tamil",
                "12:21",
                "11:21",
                "Tamil",
                "Nothing", 3
            )
        )

        mAdapter = LessonPlanAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyLessonPlan.layoutManager = LinearLayoutManager(this)
        binding.rcyLessonPlan.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                LessonPlanAdapter(isLessonPlanData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyLessonPlan.adapter = mAdapter
        }


    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    override fun onItem(data: LessonPlanData) {

    }
}