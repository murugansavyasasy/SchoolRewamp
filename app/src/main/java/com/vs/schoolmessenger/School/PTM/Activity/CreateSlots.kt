package com.vs.schoolmessenger.School.PTM.Activity

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CreateSlotsBinding

class CreateSlots : BaseActivity<CreateSlotsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): CreateSlotsBinding {
        return CreateSlotsBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name
    }

    override fun onClick(v: View?) {

    }
}