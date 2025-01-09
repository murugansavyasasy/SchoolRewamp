package com.vs.schoolmessenger.CommonScreens

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ImageViewActivityBinding

class ImageViewActivity : BaseActivity<ImageViewActivityBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): ImageViewActivityBinding {
        return ImageViewActivityBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)


        binding.toolbarLayout.lblParentToolBar.text = "Images"
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"

        val imageUrls = listOf(
            "https://picsum.photos/600/400?random=1", // Random Image 1
            "https://picsum.photos/600/400?random=2", // Random Image 2
            "https://picsum.photos/600/400?random=3", // Random Image 3
            "https://picsum.photos/600/400?random=4", // Random Image 4
            "https://picsum.photos/600/400?random=5"  // Random Image 5
        )

        // Set up the adapter
        val viewPagerAdapter   = ImageSliderAdapter(this, imageUrls)
        binding.viewpager.adapter = viewPagerAdapter
        binding.indicator.setViewPager(binding.viewpager)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }

}