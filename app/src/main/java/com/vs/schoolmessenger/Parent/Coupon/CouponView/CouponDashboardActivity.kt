package com.vs.schoolmessenger.Parent.Coupon.CouponView

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Coupon.CouponFragment.HomeFragment
import com.vs.schoolmessenger.Parent.Coupon.CouponFragment.TicketFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.FragmentCouponDashboardBinding

class CouponDashboardActivity : BaseActivity<FragmentCouponDashboardBinding>(),
    View.OnClickListener {


    override fun getViewBinding(): FragmentCouponDashboardBinding {
        return FragmentCouponDashboardBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            binding.statusBarBackground.layoutParams =
                (binding.statusBarBackground.layoutParams as ConstraintLayout.LayoutParams).apply {
                    height = statusBars.top
                }
            binding.statusBarBackground.requestLayout()

            insets
        }


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