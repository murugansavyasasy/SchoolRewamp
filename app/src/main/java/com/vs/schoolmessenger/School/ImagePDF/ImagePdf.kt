package com.vs.schoolmessenger.School.ImagePDF

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.ImagePickingData
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ImagePdfBinding

class ImagePdf : BaseActivity<ImagePdfBinding>(),
    View.OnClickListener, OnImageClickListener {

    override fun getViewBinding(): ImagePdfBinding {
        return ImagePdfBinding.inflate(layoutInflater)
    }

    private lateinit var imageList: MutableList<ImagePickingData>

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)

        imageList = mutableListOf(
            ImagePickingData(R.drawable.add_image),
            ImagePickingData(R.drawable.student_image),
            ImagePickingData(R.drawable.circle_image),
            ImagePickingData(R.drawable.image_file),
            ImagePickingData(R.drawable.pause_icon)
        )

        // Set up RecyclerView with a GridLayoutManager
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = ImagePickingAdapter(imageList, this, this)



    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
    override fun onImageClick(position: Int) {
        if (position == 0) {
            showBottomDialog()
        }
    }


    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.filepick_bottom_sheet)
        val rlaGallery = dialog.findViewById<RelativeLayout>(R.id.rlaGallery)
        val rlaCamera = dialog.findViewById<RelativeLayout>(R.id.rlaCamera)
        val rlaVideo = dialog.findViewById<RelativeLayout>(R.id.rlaVideo)

        rlaGallery.setOnClickListener {
            dialog.dismiss()
        }
        rlaCamera.setOnClickListener {
            dialog.dismiss()
        }
        rlaVideo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ) // Size
            setGravity(Gravity.BOTTOM) // Display at the bottom
            setWindowAnimations(R.style.PopupAnimation) // Apply the animation
        }
        dialog.show()
    }

}