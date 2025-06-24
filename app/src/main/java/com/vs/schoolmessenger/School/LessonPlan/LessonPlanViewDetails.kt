package com.vs.schoolmessenger.School.LessonPlan


import android.view.View

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.databinding.LessonplanViewDetailsBinding

class LessonPlanViewDetails : BaseActivity<LessonplanViewDetailsBinding>(),
    View.OnClickListener, LessonPlanClickListener, OnDateSelectedListener {

    override fun getViewBinding(): LessonplanViewDetailsBinding {
        return LessonplanViewDetailsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.imgBack) {
            onBackPressed()
        }
    }

    override fun onEditItem(data: LessonPlanData) {
        showEditLessonPlanDialog()
    }

    override fun onDeleteItem(data: LessonPlanData) {


    }

    private fun showEditLessonPlanDialog() {


    }

    override fun onDateSelected(date: String) {


    }
}
