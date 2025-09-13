package com.vs.schoolmessenger.Parent.RequestLeave

import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.GatePassBinding


class OutPass : BaseActivity<GatePassBinding>(), View.OnClickListener {

    override fun getViewBinding(): GatePassBinding {
        return GatePassBinding.inflate(layoutInflater)
    }

    private var isChildDetails: ChildDetails? = null
    private var appViewModel: App? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val childDetails = SharedPreference.getChildDetails(this)
        binding.imgBack.setOnClickListener(this)
        binding.imgBack.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )
        binding.btnOk.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )
        binding.btnOk.visibility=View.GONE


        Glide.with(this)
            .load(childDetails!!.profile)
            .placeholder(R.drawable.user_vector_icon)
            .error(R.drawable.user_vector_icon)
            .into(binding.profileImage1)

        binding.tvName.text = Constant.isLeaveData!!.student_name
        binding.isLeaveApplyOn.text =
            Constant.convertToReadableDateformat(Constant.isLeaveData!!.applied_on)
        binding.tvStandard.text =
            Constant.isLeaveData!!.class_name + " - " + Constant.isLeaveData!!.section_name
        binding.lblFromDate.text = Constant.convertToReadableDate(Constant.isLeaveData!!.leave_from)
        binding.lblToDate.text = Constant.convertToReadableDate(Constant.isLeaveData!!.leave_to)
        if (Constant.isLeaveData!!.no_of_days == Constant.one) {
            binding.lblDays.text = "${Constant.isLeaveData!!.no_of_days} ${getString(R.string.Day)}"
        } else {
            binding.lblDays.text = "${Constant.isLeaveData!!.no_of_days} ${getString(R.string.days)}"
        }
        binding.lblApprovalBy.text = Constant.isLeaveData!!.approved_by
        binding.lblReason.text = Constant.isLeaveData!!.reason
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }

    }
}