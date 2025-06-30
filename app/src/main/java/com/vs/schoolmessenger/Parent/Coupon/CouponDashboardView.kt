package com.vs.schoolmessenger.Parent.Coupon

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.CouponDashboardBinding

class CouponDashboardView : BaseActivity<CouponDashboardBinding>(), View.OnClickListener {

    override fun getViewBinding(): CouponDashboardBinding {
        return CouponDashboardBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

    }



    override fun onClick(p0: View?) {

    }

}