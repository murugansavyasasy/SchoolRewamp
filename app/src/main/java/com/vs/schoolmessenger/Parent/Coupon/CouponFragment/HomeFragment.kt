package com.vs.schoolmessenger.Parent.Coupon.CouponFragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.Coupon.CouponCredentials.AppCredentials
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.Category
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignItem
import com.vs.schoolmessenger.Parent.Coupon.CouponAdapter.CouponMenuAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponAdapter.CouponSummaryAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.CouponMenuClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.CouponSummaryClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.databinding.FragmentHomeBinding


class HomeFragment : Fragment(), View.OnClickListener, CouponMenuClickListener,
    CouponSummaryClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var appViewModel: App
    private lateinit var menuadapter: CouponMenuAdapter
    private lateinit var summaryadapter: CouponSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.relativeLayout.setOnClickListener {
            onBackPressed()
        }

        AppCredentials.init(requireContext())
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        binding.recyclerview1.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        fetchCouponMenu()
        fetchCouponSummary()

        appViewModel.getcouponmenu?.observe(viewLifecycleOwner) { response ->
            val categoryList = response?.data?.categories
            if (categoryList.isNullOrEmpty()) {
                Log.d("coupon_menu", "Coupon Menu Data Not Available")
            } else {
                isLoadCouponMenuData(categoryList)
            }
        }

        appViewModel.getCouponsSummary?.observe(viewLifecycleOwner) { response ->
            hideProgressBar()
            val campaignsList = response?.data?.campaigns?.data
            if (campaignsList.isNullOrEmpty()) {
                showCouponSummaryErrorUI("No coupon summary data available")
            } else {
                isLoadCouponSummaryData(campaignsList)
            }
        }

        appViewModel.getCouponsCategorySummary?.observe(viewLifecycleOwner) { response ->
            hideProgressBar()
            val campaignsList = response?.data?.campaigns?.data
            if (campaignsList.isNullOrEmpty()) {
                showCategorySummaryErrorUI("No coupon summary data available")
            } else {
                isLoadCouponSummaryData(campaignsList)
            }
        }


        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::summaryadapter.isInitialized) {
                    summaryadapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }



    private fun onBackPressed(){
        val intent = Intent(context, ParentDashboard::class.java)
        context?.startActivity(intent)
    }

    private fun showProgressBar() {
        binding.isProgressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.lblNoRecord.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.isProgressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun showCouponSummaryErrorUI(message: String) {
        binding.lblNoRecord.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showCategorySummaryErrorUI(message: String) {
        binding.lblNoRecord.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun fetchCouponMenu() {
        appViewModel.getcouponmenu(
            AppCredentials.PARTNER_NAME, AppCredentials.API_KEY
        )
    }

    private fun fetchCouponSummary() {
        showProgressBar()
        appViewModel.getCouponsSummary(
            "91${AppCredentials.isMobileNumber}",
            AppCredentials.PARTNER_NAME,
            AppCredentials.API_KEY
        )
    }

    private fun fetchCategoryCouponSummary(categoryId: String) {
        showProgressBar()
        appViewModel.getCouponsCategorySummary(
            categoryId,
            "91${AppCredentials.isMobileNumber}",
            AppCredentials.PARTNER_NAME,
            AppCredentials.API_KEY,
        )
    }

    private fun isLoadCouponMenuData(data: List<Category>) {
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.recyclerview1.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE

        val allCategory = Category(
            id = null, categoryName = "All", categoryImage = ""
        )

        val updatedList = listOf(allCategory) + data

        menuadapter = CouponMenuAdapter(updatedList, this, requireContext(), false)
        binding.recyclerview1.adapter = menuadapter
        menuadapter.selectPosition(0)
        fetchCouponSummary()
    }

    private fun isLoadCouponSummaryData(data: List<CampaignItem>) {
        binding.nomessage.visibility = View.GONE
        binding.lblNoRecord.visibility = View.GONE
        binding.recyclerview1.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        summaryadapter = CouponSummaryAdapter(data, this, this, false)
        binding.recyclerView.adapter = summaryadapter
    }

    override fun onCategoryClick(category: Category?) {
        val categoryId = category?.id?.toString()
        Log.d("CategoryClicked", category?.categoryName ?: "null")
        if (!categoryId.isNullOrEmpty()) {
            fetchCategoryCouponSummary(categoryId)
            binding.textView.text = category?.categoryName + " Coupons"
        } else {
            fetchCouponSummary()
            binding.textView.text = "All Coupons"
        }
    }

    override fun onSummaryClick(campaignItem: CampaignItem?) {
//        Log.d("SummaryClicked", campaignItem?.campaignName ?: "null")
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {

            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = "No matching coupon found"
            binding.recyclerView.visibility = View.GONE
        } else {

            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

