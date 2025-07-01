package com.vs.schoolmessenger.Parent.Coupon.CouponView.CouponDashboard

import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.Category
import com.vs.schoolmessenger.Parent.Coupon.CouponView.Adapter.CouponMenuAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponController.CouponViewModel
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.CouponDashboardBinding


class CouponMainClassActivity : BaseActivity<CouponDashboardBinding>(),
    View.OnClickListener, CouponMenuAdapter.OnCategoryClickListener {

    override fun getViewBinding(): CouponDashboardBinding {
        return CouponDashboardBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: CouponViewModel
    private lateinit var adapter: CouponMenuAdapter

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        viewModel = ViewModelProvider(this)[CouponViewModel::class.java]
        adapter = CouponMenuAdapter(this, this)
        binding.recyclerview1.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerview1.adapter = adapter
        observeViewModel()
        viewModel.fetchCategories()
    }

    private fun observeViewModel() {
        viewModel.categoriesLiveData.observe(this) { list ->
            adapter.setData(list)
        }

        viewModel.isLoading.observe(this) { loading ->

        }
    }

    override fun onCategoryClick(category: Category?) {
        Toast.makeText(this, "Clicked: ${category?.categoryName}", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {


    }
}
