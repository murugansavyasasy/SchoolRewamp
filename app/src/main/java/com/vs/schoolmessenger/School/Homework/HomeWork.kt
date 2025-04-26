package com.vs.schoolmessenger.School.Homework

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.ImagePickingData
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.WebView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.VimeoVideoPlay
import com.vs.schoolmessenger.databinding.HomeWorkBinding




class HomeWork : BaseActivity<HomeWorkBinding>(),
    View.OnClickListener, OnImageClickListener, OnDateSelectedListener,
    HomeWorkReportClickListener {

    override fun getViewBinding(): HomeWorkBinding {
        return HomeWorkBinding.inflate(layoutInflater)
    }
    private val PICK_IMAGES_REQUEST = 1
    private val maxImages = 5
    private val selectedImagePaths = mutableListOf<String>()
    private val selectedImageFormats = mutableListOf<String>()

    private lateinit var imageList: MutableList<ImagePickingData>
    lateinit var mAdapter: HomeWorkReportAdapter
    private lateinit var isHomeWorkReport: List<HomeWorkReport>

    private val itemsSection = listOf(
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
    )
    private val itemsStandard = listOf(
        "V",
        "VI",
        "VII",
        "VIII",
        "IX",
        "X",
        "XI",
        "XII"
    )

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.rlaSection.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.lblDatePick.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)


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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rlaStandard -> {
                showDropdownMenuSort(
                    binding.lblStandard,
                    this,
                    itemsStandard
                ) { selectedOption ->
                    binding.lblStandard.text = selectedOption
                }
            }

            R.id.rlaSection -> {
                showDropdownMenuSort(
                    binding.lblSection,
                    this,
                    itemsSection
                ) { selectedOption ->
                    binding.lblSection.text = selectedOption
                }
            }

            R.id.lblDatePick -> {
                showDatePickerDialog(this, this)
            }

            R.id.btnCreate -> {
                isBackRoundChange(binding.btnCreate)
                binding.rlaHomeWorkReport.visibility = View.GONE
                binding.rlaHomework.visibility = View.VISIBLE

            }
//report button when it is cliked the entire report compnents will come and HomeWork components will be gone
            R.id.btnHistory -> {
                isBackRoundChange(binding.btnHistory)
                binding.rlaHomeWorkReport.visibility = View.VISIBLE
                binding.rlaHomework.visibility = View.GONE
                loadData()
            }

        }
    }

    override fun onImageClick(position: Int) {
        if (position == 0) {
            showBottomDialog()
        }
    }
// -----------------------
    private fun canAddMoreFiles(): Boolean {
        return imageList.size < 5
    }

    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.filepick_bottom_sheet)
        val rlaGallery = dialog.findViewById<RelativeLayout>(R.id.rlaGallery)
        val rlaCamera = dialog.findViewById<RelativeLayout>(R.id.rlaCamera)
        val rlaDocument = dialog.findViewById<RelativeLayout>(R.id.rlaVideo)

        rlaGallery.setOnClickListener {
            //gallery
            if (canAddMoreFiles()) {
                pickImagesFromGallery()
            } else {
                Toast.makeText(this, "You can upload a maximum of 5 files.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        rlaCamera.setOnClickListener {

            // camera
            if (canAddMoreFiles()) {
//                takePhoto()
            } else {
                Toast.makeText(this, "You can upload a maximum of 5 files.", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }
        rlaDocument.setOnClickListener {
            // document PDF,Word
            if (canAddMoreFiles()) {
//                pickDocument()
            } else {
                Toast.makeText(this, "You can upload a maximum of 5 files.", Toast.LENGTH_SHORT).show()
            }
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

    private fun pickImagesFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select up to 5 images"), PICK_IMAGES_REQUEST)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImagePaths.clear()
            selectedImageFormats.clear()

            val clipData = data?.clipData
            if (clipData != null) {
                val count = minOf(clipData.itemCount, maxImages)
                for (i in 0 until count) {
                    val uri = clipData.getItemAt(i).uri
                    val path = getPathFromUri(uri)
                    val format = getFileExtension(uri)
                    selectedImagePaths.add(path)
                    selectedImageFormats.add(format)
                }

                if (clipData.itemCount > maxImages) {
                    Toast.makeText(this, "You can only select up to $maxImages images.", Toast.LENGTH_SHORT).show()
                }
            } else {
                data?.data?.let { uri ->
                    val path = getPathFromUri(uri)
                    val format = getFileExtension(uri)
                    selectedImagePaths.add(path)
                    selectedImageFormats.add(format)
                }
            }

            // Do something with selectedImagePaths and selectedImageFormats
        }
    }
    fun getPathFromUri(uri: Uri): String {
        return uri.toString() // Or use ContentResolver if actual file path is needed
    }

    fun getFileExtension(uri: Uri): String {
        val contentResolver = contentResolver
        val type = contentResolver.getType(uri)
        return type?.substringAfterLast("/") ?: "unknown"
    }







    override fun onDateSelected(date: String) {
        binding.selectdate.text = date
    }

    private fun isBackRoundChange(isClickingId: TextView) {

        if (isClickingId == binding.btnCreate) {
            binding.btnHistory.background = null
            binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
        }

        if (isClickingId == binding.btnHistory) {
            binding.btnCreate.background = null
            binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))

        }

        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.white_bg_radius)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))

    }

    private fun loadData() {
        isHomeWorkReport = listOf(
            HomeWorkReport(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isVoice", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                ""
            ),
            HomeWorkReport(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isPDF", "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                ""
            ),
            HomeWorkReport(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isImage", "",
                ""
            ),
            HomeWorkReport(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isVoice", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                ""
            ),
            HomeWorkReport(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isVideo", "https://vimeo.com/76979871", "76979871"
            ),
            HomeWorkReport(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isText", "", ""
            ),
            HomeWorkReport(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isImage", "", ""
            ),
            HomeWorkReport(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isVideo", "https://vimeo.com/76979871", "76979871"
            ),
            HomeWorkReport(
                "Annual Day celebrartions",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024",
                "isText", "", ""
            )
        )

        mAdapter = HomeWorkReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHomeWorkReport.layoutManager = LinearLayoutManager(this)
        binding.rcyHomeWorkReport.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                HomeWorkReportAdapter(isHomeWorkReport, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyHomeWorkReport.adapter = mAdapter
        }

    }

    override fun onItemTextClick(data: HomeWorkReport) {
        TODO("Not yet implemented")
    }

    override fun onItemImageClick(data: HomeWorkReport) {
        TODO("Not yet implemented")
    }

    override fun onItemPDFClick(data: HomeWorkReport) {
        val intent = Intent(this, WebView::class.java)
        intent.putExtra("isTitle", data.isTitle)
        intent.putExtra("isWebLink", data.isLink)
        startActivity(intent)
    }

    override fun onItemVoiceClick(data: HomeWorkReport) {
        TODO("Not yet implemented")
    }

    override fun onItemVideoClick(data: HomeWorkReport) {
        val intent = Intent(this, VimeoVideoPlay::class.java)
        val videoUrl = "https://vimeo.com/${data.isVideoId}"
        intent.putExtra("VIDEO_URL", videoUrl)
        intent.putExtra("VIDEO_TITLE", data.isTitle)
        startActivity(intent)
    }
}