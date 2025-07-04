package com.vs.schoolmessenger.Parent.Coupon.CouponFragment

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.FragmentCouponDashboardBinding


class CouponDashboardActivity : BaseActivity<FragmentCouponDashboardBinding>(), View.OnClickListener {


    override fun getViewBinding(): FragmentCouponDashboardBinding {
        return FragmentCouponDashboardBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding = FragmentCouponDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnHome.setOnClickListener(this)
        binding.ticketBackground.setOnClickListener(this)
        loadFragment(HomeFragment())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnHome -> {
                binding.homeBackground.setBackgroundResource(R.drawable.bg_selected)
                binding.ticketBackground.setBackgroundColor(Color.TRANSPARENT)
                binding.btnHome.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.homeimage)
                )
                binding.btnTicket.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ticketimage)
                )
                loadFragment(HomeFragment())
            }

            R.id.ticketBackground -> {
                binding.ticketBackground.setBackgroundResource(R.drawable.bg_selected)
                binding.homeBackground.setBackgroundColor(Color.TRANSPARENT)

                binding.btnHome.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.home_gray)
                )
                binding.btnTicket.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ticket_blue)
                )
                loadFragment(TicketFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
