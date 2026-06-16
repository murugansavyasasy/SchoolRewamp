package com.vs.schoolmessenger.Parent.Coupon.CouponFragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Parent.Coupon.CouponAdapter.TicketCouponAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponCredentials.AppCredentials
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.TicketCouponClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummary
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.FragmentTicketBinding


class TicketFragment : Fragment(), View.OnClickListener, TicketCouponClickListener {

    private var _binding: FragmentTicketBinding? = null
    private val binding get() = _binding!!
    private lateinit var appViewModel: App
    private lateinit var ticketcouponadapter: TicketCouponAdapter
    private var previouslySelectedView: View? = null
    private var currentStatus: String =
        Constant.all__  // Added to track current tab status for race condition prevention

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketBinding.inflate(inflater, container, false)

        binding.coupontablayout.alltext.setOnClickListener(this)
        binding.coupontablayout.activetext.setOnClickListener(this)
        binding.coupontablayout.expiredtext.setOnClickListener(this)
        binding.coupontablayout.redeemedtext.setOnClickListener(this)
        binding.back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        previouslySelectedView = binding.coupontablayout.alltext

        AppCredentials.init(requireContext())
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        binding.imgSearchToolBar.setOnClickListener {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (binding.linearlayout.visibility == View.VISIBLE) {
                binding.linearlayout.visibility = View.GONE
                binding.editSearch.setText("")
                imm.hideSoftInputFromWindow(binding.editSearch.windowToken, 0)
            } else {
                binding.linearlayout.visibility = View.VISIBLE
                binding.editSearch.setText("")
                binding.editSearch.requestFocus()
                imm.showSoftInput(binding.editSearch, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        fetchticketsummary(Constant.all__)

        appViewModel.getmycouponsSummary?.observe(viewLifecycleOwner) { response ->
            hideProgressBar()
            val couponList = response?.data?.coupon_list?.data?.filterNotNull() ?: emptyList()
            if (response?.data?.totalpages == 0) {
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.imgSearchToolBar.visibility = View.GONE
                binding.linearlayout.visibility = View.GONE
                binding.root.hideKeyboard()
                binding.txtNoData.text = getString(R.string.no_coupon_summary_data_available)
                binding.recyclerView.visibility = View.GONE
            } else {
                if (couponList.isNotEmpty()) {
                    val sampleStatus = couponList.first().coupon_status
                    if (sampleStatus != currentStatus && currentStatus != Constant.all__) {
                        return@observe
                    }
                }
                isLoadCouponSummaryData(couponList)
            }
        }

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::ticketcouponadapter.isInitialized) {
                    ticketcouponadapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun fetchticketsummary(couponstatus: String) {
        currentStatus = couponstatus
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.lblNoRecord.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        showProgressBar()
        appViewModel.getmycouponsSummary(
            couponstatus,
            "91${AppCredentials.isMobileNumber}",
            AppCredentials.PARTNER_NAME,
            AppCredentials.API_KEY
        )
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    private fun isLoadCouponSummaryData(data: List<TicketSummary>) {
        // Hide “no data” message
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.lblNoRecord.visibility = View.GONE
        binding.imgSearchToolBar.visibility = View.VISIBLE


        binding.recyclerView.visibility = View.VISIBLE
        ticketcouponadapter = TicketCouponAdapter(data, this, requireContext(), false)
        binding.recyclerView.adapter = ticketcouponadapter
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
            binding.editSearch.setText("")
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.lblNoRecord.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            binding.root.hideKeyboard()
            if (::ticketcouponadapter.isInitialized) {
                ticketcouponadapter.filter.filter("")
            }

            // Show progress bar when changing tabs (new fetch begins)
            when (v.id) {
                R.id.alltext -> fetchticketsummary(Constant.all__)
                R.id.activetext -> fetchticketsummary(Constant.activated)
                R.id.expiredtext -> fetchticketsummary(Constant.expired)  // Fixed: Correct mapping for expired tab
                R.id.redeemedtext -> fetchticketsummary(Constant.claimed)  // Fixed: Correct mapping for redeemed tab
            }
        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_coupon_found)
            binding.recyclerView.visibility = View.GONE
        } else {

            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onticketCouponSummaryClick(ticketSummary: TicketSummary?) {
        // TODO: Handle item click
    }
}
