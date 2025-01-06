package com.vs.schoolmessenger.Parent.EventsHolidays

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.EventParentBinding

class Event : BaseActivity<EventParentBinding>() {

    override fun getViewBinding(): EventParentBinding {
        return EventParentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}