package com.vs.schoolmessenger.Dashboard.Settings.Faq

import android.os.Build
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Settings.Faq.Model.FaqItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FaqBinding

class Faq : BaseActivity<FaqBinding>(), View.OnClickListener {
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var faqadapter: FaqAdapter? = null
    private var currentPosition = 0
    private var isChildDetails: ChildDetails? = null
    private var isStaffDetails: StaffDetails? = null
    var userDetails: UserDetails? = null

    override fun getViewBinding(): FaqBinding {
        return FaqBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        isChildDetails = SharedPreference.getChildDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        userDetails = SharedPreference.getUserDetails(this)

        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.lblFAQ)

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        if (Constant.isParentChoose) {
            isAccessToken = isChildDetails?.access_token
        } else {
            if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                isAccessToken = isStaffDetails!!.access_token
            } else {
                isAccessToken = userDetails!!.staff_details[0].access_token
            }
        }


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()


        loadfaqdata()
        appViewModel?.isfrequentlyasked?.observe(this) { response ->
            if (response != null && response.status) {
                binding.rcyfaq.visibility = View.VISIBLE
                binding.lytList.visibility = View.GONE
                getFaqData(response.data)

            } else {
                binding.rcyfaq.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message
            }
        }
    }

    private fun loadfaqdata() {
        appViewModel?.isfrequentlyasked(isAccessToken!!,this)

    }


    private fun getFaqData(data: List<FaqItem>?) {
        if (data.isNullOrEmpty()) {
            binding.rcyfaq.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        } else {
            binding.rcyfaq.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
            binding.rcyfaq.layoutManager = LinearLayoutManager(this)
            faqadapter = FaqAdapter(data, this, false)
            binding.rcyfaq.adapter = faqadapter
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }

}