package com.vs.schoolmessenger.Parent.Coupon.CouponFragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Parent.Coupon.CouponAdapter.CouponMenuAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponAdapter.CouponSummaryAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponAdapter.TicketCouponAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponCredentials.AppCredentials
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.CouponMenuClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.CouponSummaryClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.TicketCouponClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignItem
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummary
import com.vs.schoolmessenger.Parent.Coupon.CouponView.CouponDashboardActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.databinding.FragmentHomeBinding
import com.vs.schoolmessenger.databinding.FragmentTicketBinding


class TicketFragment : Fragment(), View.OnClickListener, TicketCouponClickListener {

    private var _binding: FragmentTicketBinding? = null
    private val binding get() = _binding!!
    private lateinit var appViewModel: App
    private lateinit var ticketcouponadapter: TicketCouponAdapter
    private var previouslySelectedView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketBinding.inflate(inflater, container, false)

        binding.coupontablayout.alltext.setOnClickListener(this)
        binding.coupontablayout.activetext.setOnClickListener(this)
        binding.coupontablayout.expiredtext.setOnClickListener(this)
        binding.coupontablayout.redeemedtext.setOnClickListener(this)
        binding.relativeLayout.setOnClickListener {
            onBackPressed()
        }

        previouslySelectedView = binding.coupontablayout.alltext

        AppCredentials.init(requireContext())
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        fetchticketsummary("all")

        appViewModel.getmycouponsSummary?.observe(viewLifecycleOwner) { response ->
            hideProgressBar()
            val couponList = response?.data?.coupon_list?.data?.filterNotNull()
            if (couponList.isNullOrEmpty()) {
                showMyCouponSummaryErrorUI("No coupon summary data available")
            } else {
                isLoadCouponSummaryData(couponList)
            }
        }

        return binding.root
    }

    private fun fetchticketsummary(couponstatus: String) {
        showProgressBar()
        appViewModel.getmycouponsSummary(
            couponstatus,
            "91${AppCredentials.isMobileNumber}",
            AppCredentials.PARTNER_NAME,
            AppCredentials.API_KEY
        )
    }

    private fun isLoadCouponSummaryData(data: List<TicketSummary>) {
        binding.lblNoRecord.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        ticketcouponadapter = TicketCouponAdapter(data, this, requireContext(), false)
        binding.recyclerView.adapter = ticketcouponadapter
    }

    private fun showMyCouponSummaryErrorUI(message: String) {
        binding.lblNoRecord.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.isProgressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.lblNoRecord.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.isProgressBar.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        if (v != null) {
            previouslySelectedView?.setBackgroundResource(0)
            v.setBackgroundResource(R.drawable.green_radious)
            previouslySelectedView = v

            // Show progress bar when changing tabs (new fetch begins)
            when (v.id) {
                R.id.alltext -> fetchticketsummary("all")
                R.id.activetext -> fetchticketsummary("activated")
                R.id.expiredtext -> fetchticketsummary("claimed")
                R.id.redeemedtext -> fetchticketsummary("expired")
            }
        }
    }

    private fun onBackPressed() {
        val intent = Intent(context, CouponDashboardActivity::class.java)
        context?.startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onticketCouponSummaryClick(ticketSummary: TicketSummary?) {
        // TODO: Handle item click
    }
}

