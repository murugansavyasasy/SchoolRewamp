package com.vs.schoolmessenger.School.Assignment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.ImagePickingData
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.AssignmentBinding


class Assignment : BaseActivity<AssignmentBinding>(),
    View.OnClickListener, OnImageClickListener, TimeSelectedListener, OnDateSelectedListener {

    override fun getViewBinding(): AssignmentBinding {
        return AssignmentBinding.inflate(layoutInflater)
    }
    private val itemsCategory = listOf(
        "General",
        "Class Work",
        "Research Paper",
        "Project"
    )

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
    private var cameraPermissionDeniedCount = 0

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        internal const val CAMERA_IMAGE_REQUEST = 1004
        private const val MAX_FILES = 10
    }

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null

    private var appViewModel: App? = null
    private var isAccessToken: String? = null


    private var isStaffDetails: StaffDetails? = null
    private lateinit var imageList: MutableList<ImagePickingData>

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnChooseRecipient.setOnClickListener(this)
        binding.lblDatePick.setOnClickListener(this)
        binding.lblTimePick.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        spinnerType()
    }

    private fun spinnerType() {

        val adapter = SpinnerLoadingAdapter(this, itemsCategory)
        binding.spinnerType.adapter = adapter

        binding.spinnerType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

//                    val selectedOption = itemsCategory[position]
//                    binding.rlaFilePicking.visibility =
//                        if (selectedOption == "Text") View.GONE else View.VISIBLE
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnChooseRecipient -> {
                Log.d("isChoosingImageSize", imageList.size.toString())
            }

            R.id.lblTimePick -> {
                showTimePickerDialog(this, this)
            }

            R.id.lblDatePick -> {
                showDatePickerDialog(this, this)
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

    override fun onTimeSelected(hour: Int, minute: Int, amPm: String) {
        binding.lblTimePick.text = String.format("%02d:%02d %s", hour, minute, amPm)
    }

    override fun onDateSelected(date: String) {
        binding.lblDatePick.text = changeDateFormat(date)
        Log.d("isSelectedDate", date)
    }
}