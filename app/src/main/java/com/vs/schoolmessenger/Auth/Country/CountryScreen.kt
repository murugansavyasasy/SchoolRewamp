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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CountryScreen : BaseActivity<CountryListScreenBinding>(), View.OnClickListener {

    override fun getViewBinding(): CountryListScreenBinding {
        return CountryListScreenBinding.inflate(layoutInflater)
    }

    private var isAgree = false
    private val DELAY_BETWEEN_SCROLL_MS = 25L
    private val SCROLL_DX = 5
    private val DIRECTION_RIGHT = 1

    private val featuresAdapter by lazy {
        CountryListScrollingAdapter()
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

        setupFeatureTiles(getDummyFeatures())
    }

    private fun setupFeatureTiles(featuresList: List<CountryItem>) {
        with(binding.recyclerFeatures) {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = featuresAdapter
        }
        featuresAdapter.submitList(featuresList)

        lifecycleScope.launch { autoScrollFeaturesList() }
    }

    private suspend fun autoScrollFeaturesList() {
        while (true) {
            if (binding.recyclerFeatures.canScrollHorizontally(DIRECTION_RIGHT)) {
                binding.recyclerFeatures.smoothScrollBy(SCROLL_DX, 0)
            } else {
                val layoutManager = binding.recyclerFeatures.layoutManager as LinearLayoutManager
                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                if (firstPosition != RecyclerView.NO_POSITION) {
                    val currentList = featuresAdapter.currentList
                    val reorderedList = currentList.subList(firstPosition, currentList.size) +
                            currentList.subList(0, firstPosition)
                    featuresAdapter.submitList(reorderedList)
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
            CountryItem(R.drawable.call_schedule_icon, "Feature 1"),
            CountryItem(R.drawable.text_icon_black, "Feature 2"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.play_icon_voice, "Feature 3"),
            CountryItem(R.drawable.text_icon, "Feature 4")
        )
    }
}
