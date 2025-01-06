package com.vs.schoolmessenger.Parent.Timetable

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.TimeTableBinding

class TimeTable : BaseActivity<TimeTableBinding>() {

    override fun getViewBinding(): TimeTableBinding {
        return TimeTableBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}