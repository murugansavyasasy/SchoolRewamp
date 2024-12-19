package com.vs.schoolmessenger.School.StudentReport

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.StudentReportBinding

class StudentReport : BaseActivity<StudentReportBinding>(), View.OnClickListener,
    StudentReportClickListener {

    private lateinit var mAdapter: StudentReportAdapter
    private lateinit var isStudentReportData: List<StudentReportData>

    val items = listOf(
        "Get all student",
        "Student name (A - Z)",
        "Student name (Z - A)",
        "Roll number (0 - 1)",
        "Roll number (1 - 0)"
    )

    val itemsStandard = listOf(
        "XII",
        "XI",
        "X",
        "IX",
        "VII"
    )
    val itemsSection = listOf(
        "A",
        "B",
        "C",
        "D",
        "E"
    )

    val handler = Handler(Looper.getMainLooper())

    override fun getViewBinding(): StudentReportBinding {
        return StudentReportBinding.inflate(layoutInflater)
    }

    private var isVisibilityStandard = true

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.rlaSort.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.dropdownTextViewStandard.setOnClickListener(this)
        binding.dropdownTextViewSection.setOnClickListener(this)


        isStudentReportData = listOf(
            StudentReportData(
                "4783567951",
                "Male",
                "Apr 1 , 2001",
                "Sathish",
                "Ganesan",
                "Abi",
                "6382677672",
                "sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",
                "Apr 1 , 2001",
                "Bharath",
                "Ganesan",
                "Abi",
                "6382677672",
                "sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",
                "Apr 1 , 2001",
                "Partha",
                "Ganesan",
                "Abi",
                "6382677672",
                "sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",
                "Apr 1 , 2001",
                "Saran",
                "Ganesan",
                "Abi",
                "6382677672",
                "sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",
                "Apr 1 , 2001",
                "Murugan",
                "Ganesan",
                "Abi",
                "6382677672",
                "sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",
                "Apr 1 , 2001",
                "Vel",
                "Ganesan",
                "Abi",
                "6382677672",
                "sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",
                "Apr 1 , 2001",
                "Vijay",
                "Ganesan",
                "Abi",
                "6382677672",
                "sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",
                "Apr 1 , 2001",
                "Ajith",
                "Ganesan",
                "Abi",
                "6382677672",
                "sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",
                "Apr 1 , 2001",
                "Surya",
                "Ganesan",
                "Abi",
                "6382677672",
                "sathishg079@gmail.com",
            )
        )

        mAdapter = StudentReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
        binding.rcyStudentReport.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                StudentReportAdapter(isStudentReportData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyStudentReport.adapter = mAdapter
        }


        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
        })
    }

    private fun filter(text: String) {

        val filteredList = if (text.isEmpty()) {
            isStudentReportData
        } else {
            isStudentReportData.filter { it.studentName.contains(text, ignoreCase = true) }
        }
        (mAdapter as StudentReportAdapter).updateData(filteredList)

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rlaSort -> {

                showDropdownMenuSort(binding.dropdownTextView, this, items) { selectedOption ->
                    binding.dropdownTextView.text = selectedOption
                }
            }

            R.id.dropdownTextViewStandard -> {

                showDropdownMenuSort(
                    binding.dropdownTextViewStandard,
                    this,
                    itemsStandard
                ) { selectedOption ->
                    binding.dropdownTextViewStandard.text = selectedOption
                    binding.lblStandard.text = selectedOption
                }
            }

            R.id.dropdownTextViewSection -> {

                showDropdownMenuSort(
                    binding.dropdownTextViewSection,
                    this,
                    itemsSection
                ) { selectedOption ->
                    binding.dropdownTextViewSection.text = selectedOption
                    binding.lblStandard.text =
                        binding.dropdownTextViewStandard.text.toString() + " - " + selectedOption
                }
            }

            R.id.rlaStandard -> {
                if (isVisibilityStandard) {
                    isVisibilityStandard = false
                    val animation = AnimationUtils.loadAnimation(
                        applicationContext,
                        R.anim.top_to_bottom_animation
                    )
                    binding.rlaStandardPicking.startAnimation(animation)
                    binding.rlaStandardPicking.visibility = View.VISIBLE

                } else {
                    isVisibilityStandard = true
                    val animation =
                        AnimationUtils.loadAnimation(applicationContext, R.anim.slide_animation)
                    binding.rlaStandardPicking.startAnimation(animation)

                    handler.postDelayed({
                        binding.rlaStandardPicking.visibility = View.GONE
                    }, 400)
                }
            }
        }
    }

    override fun onMailClick(data: StudentReportData) {
        Constant.redirectToMail(this, data.email)
    }

    override fun onPhoneClick(data: StudentReportData) {
        Constant.redirectToDialPad(this, data.mobileNumber)
    }

    override fun onMessageClick(data: StudentReportData) {
        Constant.redirectToMessage(this, data.mobileNumber)
    }
}