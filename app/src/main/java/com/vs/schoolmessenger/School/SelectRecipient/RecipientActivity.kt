package com.vs.schoolmessenger.School.SelectRecipient

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.SelectRecipientBinding

class RecipientActivity : BaseActivity<SelectRecipientBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): SelectRecipientBinding {
        return SelectRecipientBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Entire School"), true)
        tabLayout.addTab(tabLayout.newTab().setText("Group"))
        tabLayout.addTab(tabLayout.newTab().setText("Standard"))
        tabLayout.addTab(tabLayout.newTab().setText("Section/Specific Student"))

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}