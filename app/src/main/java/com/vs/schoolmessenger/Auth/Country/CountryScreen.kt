package com.vs.schoolmessenger.Auth.Country

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.TermsConditions.TermsAndConditions
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.CountryListScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountryScreen : BaseActivity<CountryListScreenBinding>(), View.OnClickListener {

    override fun getViewBinding(): CountryListScreenBinding {
        return CountryListScreenBinding.inflate(layoutInflater)
    }

    private var isAgree = false
    private val DELAY_BETWEEN_SCROLL_MS = 25L
    private val SCROLL_DX = 5
    private val DIRECTION_RIGHT = 1

    private val isCountryAdapter by lazy {
        CountryListScrollingAdapter(getDummyFeatures())
    }

    override fun setupViews() {
        super.setupViews()
        binding.btnArrowNext.setOnClickListener(this)
        setupToolbar()

        binding.lblTermsConditions.setOnClickListener {
            startActivity(Intent(this, TermsAndConditions::class.java))
        }

        binding.termsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            isAgree = isChecked
        }


//        binding.lnrFirst.setBackgroundResource(R.drawable.thailand_flag)
//        binding.lnrSecound.setBackgroundResource(R.drawable.india_flag)
//        binding.lnrThird.setBackgroundResource(R.drawable.united_states_flag)
//        binding.lnrFourth.setBackgroundResource(R.drawable.australia_flag)
//        binding.lnrFifth.setBackgroundResource(R.drawable.albania_flag)
//        binding.lnrSixth.setBackgroundResource(R.drawable.afghanistan_flag)

        //  isCountryLoading(getDummyFeatures())
    }

    private fun isCountryLoading(isCountryItem: List<CountryItem>) {
        with(binding.recyclerFeatures) {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = isCountryAdapter
        }
        isCountryAdapter.submitList(isCountryItem)

        lifecycleScope.launch { autoScrollFeaturesList() }
    }

    private var isAutoScrollActive = true

    private suspend fun autoScrollFeaturesList() {
        while (isAutoScrollActive) {
            if (binding.recyclerFeatures.canScrollHorizontally(DIRECTION_RIGHT)) {
                binding.recyclerFeatures.smoothScrollBy(SCROLL_DX, 0)
            } else {
                val layoutManager = binding.recyclerFeatures.layoutManager as LinearLayoutManager
                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                val currentList =
                    (binding.recyclerFeatures.adapter as CountryListScrollingAdapter).itemList

                if (firstPosition != RecyclerView.NO_POSITION && currentList.isNotEmpty()) {
                    val reorderedList = currentList.subList(firstPosition, currentList.size) +
                            currentList.subList(0, firstPosition)
                    (binding.recyclerFeatures.adapter as CountryListScrollingAdapter).submitList(
                        reorderedList
                    )
                }
            }
            delay(DELAY_BETWEEN_SCROLL_MS)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnArrowNext -> {
                if (isAgree) {
                    val intent = Intent(this@CountryScreen, Login::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, R.string.AgreeTermsConditions, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getDummyFeatures(): List<CountryItem> {
        return listOf(
            CountryItem(R.drawable.thailand_flag),
            CountryItem(R.drawable.india_flag),
            CountryItem(R.drawable.united_states_flag),
            CountryItem(R.drawable.zimbabwe_flag),
            CountryItem(R.drawable.france_flag),
            CountryItem(R.drawable.argentina_flag),
            CountryItem(R.drawable.finland_flag),
            CountryItem(R.drawable.antigua_and_barbuda_flag),
            CountryItem(R.drawable.australia_flag),
            CountryItem(R.drawable.american_samoa_flag),
            CountryItem(R.drawable.angola_flag),
            CountryItem(R.drawable.albania_flag),
            CountryItem(R.drawable.united_states_flag),
            CountryItem(R.drawable.algeria_flag),
        )
    }
}
