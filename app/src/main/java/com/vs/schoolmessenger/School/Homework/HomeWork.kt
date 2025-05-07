package com.vs.schoolmessenger.School.Homework

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.ImagePickingData
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Communication.Adapter.VoiceHistoryAdapter
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
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
    private lateinit var isHomeWorkReport: List<HomeWorkReport>
    var isAcademicYear: List<AcademicYear>? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    var isValidAcademicYear = false
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    private var isStaffDetails: StaffDetails? = null

    //    var isSection: List<Section>? = null
    var isSection: List<Section>? = null

    var isGetStandard: List<Standard>? = null
    private lateinit var isHomeWorkReportData: List<HomeWorkReport>

    //    private var itemsSection: List<String> = emptyList()
    var mHomeWorkReportAdapter: HomeWorkReportAdapter? = null
    var isSectionId = -1


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.imgBack.setOnClickListener(this)
        binding.rlaSection.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.lblDatePick.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
        binding.AcademicYear.setOnClickListener(this)
        binding.btnChooseRecipient.setOnClickListener(this)

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        imageList = mutableListOf(
            ImagePickingData(R.drawable.add_image),
            ImagePickingData(R.drawable.student_image),
            ImagePickingData(R.drawable.circle_image),
            ImagePickingData(R.drawable.image_file),
            ImagePickingData(R.drawable.pause_icon)
        )

        binding.selectdate.text = Constant.getCurrentDate()

        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = ImagePickingAdapter(imageList, this, this)

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            Constant.hideLoading(this@HomeWork)
            response?.data?.let { academicList ->
                val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYear == reorderedList) return@observe
                isAcademicYear = reorderedList
                isValidAcademicYear = isAcademicYear?.any { it.current_academic_year == true } == true
                binding.lblAcademicYear.text = isAcademicYear!![0].year
                isAcademicYearId = isAcademicYear!![0].id
                isCurrentAcademicYear = isAcademicYear!![0].current_academic_year

                isGetStandardSection()
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            Constant.hideLoading(this@HomeWork)
            if (response != null) {
                isGetStandard = response.data
                isGetStandard?.size?.let {
                    if(it >0) {
                        isSectionId = isGetStandard!!.get(0).sections.get(0).id
                        binding.lblStandard.text = isGetStandard!!.get(0).name
                        if (isGetStandard!!.get(0).sections.size > 0) {
                            binding.lblSection.text = isGetStandard!!.get(0).sections.get(0).name
                            isSection = isGetStandard!!.get(0).sections
                        }
                    }else{
                        binding.rlaStandard.visibility = View.GONE
                        binding.rlaSection.visibility = View.GONE
                    }
                }
            }
        }

        isGetAcademicYear()




        appViewModel!!.isGetHomeWorkReport?.observe(this) { response ->
            if (response != null && response.status) {
                Log.d("statusadapter","statusadapter")
                val isHomeWorkReport = response.data
                isHomeWorkReportData = isHomeWorkReport
                loadHomeWorkReportData(isHomeWorkReportData)
            }


        }
    }




    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }


            R.id.rlaStandard -> {
                showStandardDropdown(
                    binding.rlaStandard, this, isGetStandard
                ) { selectStandard, position ->
                    binding.lblStandard.text = selectStandard.name
                    isSection = selectStandard.sections
                    Log.d(
                        "DropdownMenu",
                        "Selected Standard: Name = ${selectStandard.name}, ID = ${selectStandard.id}, Position = $position"
                    )
                    fetchHomeWorkReportData()
                }
            }

            R.id.rlaSection -> {
                isDropDownLoadDataSection(
                    binding.lblSection,
                    this,
                    isSection
                ) { selectedOption ->
                    binding.lblSection.text = selectedOption.first
                    isSectionId = selectedOption.second
                    fetchHomeWorkReportData()
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
                isGetAcademicYear()
                fetchHomeWorkReportData()
            }

            R.id.btnChooseRecipient -> {
                RedirectToSectionStudents()
            }

            R.id.AcademicYear -> {
                showAcademicDropdown(
                    binding.AcademicYear, this, isAcademicYear
                ) { selectedYear ->
                    binding.lblAcademicYear.text = selectedYear.year
                    isGetStandardSection()
                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
                    )
                    fetchHomeWorkReportData()
                }

            }

        }
    }

    private fun fetchHomeWorkReportData() {
        appViewModel?.isGetHomeWorkReport(
            isAccessToken!!,
            isSectionId!!,
            isAcademicYearId,
            binding.selectdate.text.toString(),
            this
        )
    }

    private fun loadHomeWorkReportData(isHomeWorkReportDetails: List<HomeWorkReport>) {

        binding.rcyHomeWorkReport.visibility = View.VISIBLE
        mHomeWorkReportAdapter = HomeWorkReportAdapter(isHomeWorkReportDetails, this, this, Constant.isShimmerViewShow)
        binding.rcyHomeWorkReport.layoutManager = LinearLayoutManager(this)
        binding.rcyHomeWorkReport.isNestedScrollingEnabled = false
        binding.rcyHomeWorkReport.adapter = mHomeWorkReportAdapter

    }




    private fun isGetStandardSection() {
        Constant.showLoading(this@HomeWork)
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
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

    private fun isGetAcademicYear() {
        Constant.showLoading(this@HomeWork)
        appViewModel!!.isGetAcademicYear(
            isAccessToken!!, this
        )
    }


    private fun RedirectToSectionStudents() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        if (title.isEmpty()) {
            binding.edtTitle.error = "Title is required"
            binding.edtTitle.requestFocus()
            return
        }
        if (description.isEmpty()) {
            binding.edtDescription.error = "Title is required"
            binding.edtDescription.requestFocus()
            return
        }
        val sectionDetails = SectionDetails(title, description)
        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.section_data, sectionDetails)
        startActivity(intent)
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
        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.custom_bg_blue)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))

    }



    override fun onItemTextClick(data: HomeWorkReport) {
        TODO("Not yet implemented")
    }

    override fun onItemImageClick(data: HomeWorkReport) {
        TODO("Not yet implemented")
    }

    override fun onItemPDFClick(data: HomeWorkReport) {

    }

    override fun onItemVoiceClick(data: HomeWorkReport) {
        TODO("Not yet implemented")
    }

    override fun onItemVideoClick(data: HomeWorkReport) {

    }
}