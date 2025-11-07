package com.vs.schoolmessenger.Parent.Coupon.CouponFragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Parent.Coupon.CouponAdapter.CouponMenuAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponAdapter.CouponSummaryAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponCredentials.AppCredentials
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.CouponMenuClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.CouponSummaryClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.Category
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FragmentHomeBinding


class HomeFragment : Fragment(), View.OnClickListener, CouponMenuClickListener,
    CouponSummaryClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var appViewModel: App
    private lateinit var menuadapter: CouponMenuAdapter
    private lateinit var summaryadapter: CouponSummaryAdapter
    private var isAccessToken: String? = null



    private var earnedPoints: Int = 0
    private var pointspercoupon: Int = 0
    private var spentPoints: Int = 0
    private var remainingPoints: Int = 0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.relativeLayout.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        val isChildDetails = SharedPreference.getChildDetails(requireContext())
        isAccessToken = isChildDetails?.access_token

        AppCredentials.init(requireContext())
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()
        binding.recyclerview1.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        fetchCouponMenu()
        fetchCouponSummary()
        fetchPauketPoints()
        binding.backtext.text=Constant.isSelectedMenuName
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
                showCouponSummaryErrorUI(getString(R.string.no_coupon_summary_data_available))
            } else {
                isLoadCouponSummaryData(campaignsList)
            }
        }

        appViewModel.getCouponsCategorySummary?.observe(viewLifecycleOwner) { response ->
            hideProgressBar()
            val campaignsList = response?.data?.campaigns?.data
            if (campaignsList.isNullOrEmpty()) {
                showCategorySummaryErrorUI(getString(R.string.no_coupon_summary_data_available))
            } else {
                isLoadCouponSummaryData(campaignsList)
            }
        }


        appViewModel.isGetPauketPoints?.observe(viewLifecycleOwner) { response ->
            remainingPoints = response?.data?.firstOrNull()?.remaining ?: 0
            spentPoints = response?.data?.firstOrNull()?.spent ?: 0
            earnedPoints = response?.data?.firstOrNull()?.earned ?: 0
            pointspercoupon = response?.data?.firstOrNull()?.per_coupon ?: 0

            binding.totalcoins.text = "$earnedPoints"
            binding.usedcoins.text = "${getString(R.string.Used)} : $spentPoints"
            binding.availablecoins.text = "${getString(R.string.Available)} : $remainingPoints"
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



    private fun showProgressBar() {
        binding.isProgressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.lblNoRecord.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.isProgressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun updateNoDataView(hasData: Boolean) {
        if (hasData) {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        } else {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        }
    }
    private fun showCouponSummaryErrorUI(message: String) {
        updateNoDataView(false)
        binding.lblNoRecord.visibility = View.VISIBLE
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


    private fun fetchPauketPoints() {
        val mobileNumberLong = AppCredentials.isMobileNumber.toLong()
        appViewModel.isGetPauketPoints(isAccessToken ?: "", mobileNumberLong, 1)
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
            id = null, categoryName = Constant.All_, categoryImage = ""
        )

        val updatedList = listOf(allCategory) + data

        menuadapter = CouponMenuAdapter(updatedList, this, requireContext(), false)
        binding.recyclerview1.adapter = menuadapter
        menuadapter.selectPosition(0)
        fetchCouponSummary()
    }

    private fun isLoadCouponSummaryData(data: List<CampaignItem>) {
        updateNoDataView(data.isNotEmpty())
        binding.lblNoRecord.visibility = View.GONE
        binding.recyclerview1.visibility = View.VISIBLE
        summaryadapter = CouponSummaryAdapter(
            data,
            this,
            this,
            false,
            earnedPoints,
            spentPoints,
            remainingPoints,
            pointspercoupon
        )

        binding.recyclerView.adapter = summaryadapter
    }

    override fun onCategoryClick(category: Category?) {
        val categoryId = category?.id?.toString()
        Log.d("CategoryClicked", category?.categoryName ?: "null")
        if (!categoryId.isNullOrEmpty()) {
            fetchCategoryCouponSummary(categoryId)
            binding.textView.text = "${category?.categoryName} ${getString(R.string.Coupons)}"
        } else {
            fetchCouponSummary()
            binding.textView.text = getString(R.string.all_coupons)
        }
        //  Clear search box
        binding.editSearch.text?.clear()
        if (::summaryadapter.isInitialized) {
            summaryadapter.filter.filter("")
        }

        updateNoDataView(true)
    }

    override fun onSummaryClick(campaignItem: CampaignItem?) {
//        Log.d("SummaryClicked", campaignItem?.campaignName ?: "null")
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

    override fun onClick(v: View?) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

