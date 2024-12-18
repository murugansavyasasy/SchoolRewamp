package com.vs.schoolmessenger.School.Assignment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.AssignmentBinding


class Assignment : BaseActivity<AssignmentBinding>(),
    View.OnClickListener, OnImageClickListener, TimeSelectedListener, OnDateSelectedListener {

    override fun getViewBinding(): AssignmentBinding {
        return AssignmentBinding.inflate(layoutInflater)
    }

    private val itemsAssignmentType = listOf(
        "Text",
        "Image",
        "PDF",
        "Video"
    )
    private val itemsCategory = listOf(
        "General",
        "Class Work",
        "Research Paper",
        "Project"
    )


    private lateinit var imageList: MutableList<ImagePickingData>
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.btnChooseRecipient.setOnClickListener(this)
        binding.rlaTypeofSending.setOnClickListener(this)
        binding.rlaAssignmentType.setOnClickListener(this)
        binding.lblDatePick.setOnClickListener(this)
        binding.lblTimePick.setOnClickListener(this)


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

        binding.edtDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the character count
                val charCount = s?.length ?: 0
                binding.lblTextCount.text = "$charCount / 100"
            }

            override fun afterTextChanged(s: Editable?) {
                // Ensure the text doesn't exceed 100 characters (optional safeguard)
                if (s?.length ?: 0 > 100) {
                    val truncatedText = s?.substring(0, 100)
                    binding.edtDescription.setText(truncatedText)
                    binding.edtDescription.setSelection(
                        truncatedText?.length ?: 0
                    ) // Keep cursor at the end
                }
            }
        })

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

            R.id.rlaAssignmentType -> {
                showDropdownMenuSort(
                    binding.lblAssignment,
                    itemsAssignmentType
                ) { selectedOption ->
                    binding.lblAssignment.text = selectedOption

                    if (binding.lblAssignment.text.toString() == "Text") {
                        binding.rlaFilePicking.visibility = View.GONE
                    } else {
                        binding.rlaFilePicking.visibility = View.VISIBLE
                    }
                }
            }

            R.id.rlaTypeofSending -> {
                showDropdownMenuSort(
                    binding.lblAssignmentSendingType,
                    itemsCategory
                ) { selectedOption ->
                    binding.lblAssignmentSendingType.text = selectedOption
                }
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

        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    override fun onTimeSelected(hour: Int, minute: Int, amPm: String) {
        binding.lblTimePick.text = String.format("%02d:%02d %s", hour, minute, amPm)
    }

    override fun onDateSelected(date: String) {
        binding.lblDatePick.text = changeDateFormat(date)
        Log.d("isSelectedDate", date)
    }
}