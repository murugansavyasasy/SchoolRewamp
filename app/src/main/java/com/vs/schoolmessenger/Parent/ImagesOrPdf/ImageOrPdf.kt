package com.vs.schoolmessenger.Parent.ImagesOrPdf

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.WebView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ImageOrPdfBinding

class ImageOrPdf : BaseActivity<ImageOrPdfBinding>(), View.OnClickListener,
    ImageOrPdfClickListener {

    override fun getViewBinding(): ImageOrPdfBinding {
        return ImageOrPdfBinding.inflate(layoutInflater)
    }

    private lateinit var isImageOrPdfData: List<ImageOrPdfData>
    lateinit var mAdapter: ImageOrPdfAdapter


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE

        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.ImageOrPdf)
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"
        loadData()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }


    private fun loadData() {
        isImageOrPdfData = listOf(

            ImageOrPdfData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isPDF", "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                ""
            ),
            ImageOrPdfData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isImage", "",
                ""
            ),

            ImageOrPdfData(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isImage", "", ""
            )
        )

        mAdapter = ImageOrPdfAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyImagePdf.layoutManager = LinearLayoutManager(this)
        binding.rcyImagePdf.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                ImageOrPdfAdapter(isImageOrPdfData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyImagePdf.adapter = mAdapter
        }

    }

    override fun onItemImageClick(data: ImageOrPdfData) {

    }

    override fun onItemPDFClick(data: ImageOrPdfData) {
        val intent = Intent(this, WebView::class.java)
        intent.putExtra("isTitle", data.isTitle)
        intent.putExtra("isWebLink", data.isLink)
        startActivity(intent)
    }

}