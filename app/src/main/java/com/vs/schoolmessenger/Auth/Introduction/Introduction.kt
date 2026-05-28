package com.vs.schoolmessenger.Auth.Introduction

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.Country.CountryScreen
import com.vs.schoolmessenger.Auth.Introduction.Model.GetFeatureData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.NewFeaturesBinding

class Introduction : BaseActivity<NewFeaturesBinding>(), View.OnClickListener {

    override fun getViewBinding(): NewFeaturesBinding {
        return NewFeaturesBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private lateinit var adapter: OnboardingAdapter
    private lateinit var layoutDots: LinearLayout
    private var currentIndex = 0

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryIntroduction(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel!!.init()
        appViewModel!!.getNewFeature!!.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    if (response.data.isNotEmpty()) {
                        setupOnboardingRecycler(response.data)
                    } else {
                        //if suppose api status is failed we directly go country list
                        RedirectCountryList()
                    }
                } else {
                    //if suppose api status is failed we directly go country list

                    RedirectCountryList()
                }
            } else {
                //if suppose api status is failed we directly go country list

                RedirectCountryList()
            }
        }

        getNewfeatures()
    }

    private fun getNewfeatures() {
        Constant.showLoading(this)
        appViewModel!!.isGetFeature()
    }


    private fun setupOnboardingRecycler(features: List<GetFeatureData>) {
        val recycler = binding.recyclerOnboarding
        layoutDots = binding.layoutDots
        adapter = OnboardingAdapter(features, this)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        PagerSnapHelper().attachToRecyclerView(recycler)

        setupDots(features.size)
        updateDots(0)

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos =
                        (rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (pos != currentIndex) {
                        currentIndex = pos
                        updateDots(pos)
                        updateButtonText(pos)
                    }
                }
            }
        })

        binding.btnSkip.setOnClickListener {
            RedirectCountryList()
        }

        binding.rytNext.setOnClickListener {
            if (currentIndex == adapter.itemCount - 1) {
                // Last page → Go to next screen
                RedirectCountryList()
            } else {
                //  Move to next page one by one
                val nextIndex = currentIndex + 1
                binding.recyclerOnboarding.smoothScrollToPosition(nextIndex)
                updateButtonText(nextIndex)
                updateDots(nextIndex)
                currentIndex = nextIndex
            }
        }

    }

    private fun updateButtonText(position: Int) {
        val isLastPage = position == adapter.itemCount - 1
        binding.btnNext.text =
            if (isLastPage) getString(R.string.let_s_go) else getString(R.string.next)
        binding.btnSkip.visibility = if (isLastPage) View.GONE else View.VISIBLE

    }
    private fun setupDots(count: Int) {
        layoutDots.removeAllViews()
        val size = 25
        for (i in 0 until count) {
            val dot = View(this)
            val params = LinearLayout.LayoutParams(size, size)
            params.marginEnd = 12
            dot.layoutParams = params
            dot.background = ContextCompat.getDrawable(this, R.drawable.dot_inactive)
            layoutDots.addView(dot)
        }
    }

    private fun updateDots(index: Int) {
        for (i in 0 until layoutDots.childCount) {
            val dot = layoutDots.getChildAt(i)
            dot.background = ContextCompat.getDrawable(
                this,
                if (i == index) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }

    fun RedirectCountryList() {
        SharedPreference.putIntroductionSkip(this@Introduction, true)
        startActivity(Intent(this@Introduction, CountryScreen::class.java))
        finish()
    }

    override fun onClick(v: View?) {}
}