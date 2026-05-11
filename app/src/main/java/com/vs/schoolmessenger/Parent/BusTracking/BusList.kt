package com.vs.schoolmessenger.Parent.BusTracking



import android.view.View

import androidx.lifecycle.ViewModelProvider

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App

import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.BusListActivityBinding


class BusList : BaseActivity<BusListActivityBinding>(), View.OnClickListener{

    override fun getViewBinding(): BusListActivityBinding {
        return BusListActivityBinding.inflate(layoutInflater)
    }


    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    var userDetails: UserDetails? = null


    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)


        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblStudentSection.text =
            childDetails?.standard_name + " - " + childDetails?.section_name

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

    }

    override fun onClick(p0: View?) {
    }


}
