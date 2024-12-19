package com.vs.schoolmessenger.School.LessonPlan

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.databinding.LessonplanViewDetailsBinding

class LessonPlanViewDetails : BaseActivity<LessonplanViewDetailsBinding>(),
    View.OnClickListener, LessonPlanClickListener, OnDateSelectedListener {

    private lateinit var isLessonPlanData: List<LessonPlanData>
    private lateinit var mAdapter: LessonPlanAdapter
    private var lblFromData: TextView? = null
    private var lblToDate: TextView? = null


    override fun getViewBinding(): LessonplanViewDetailsBinding {
        return LessonplanViewDetailsBinding.inflate(layoutInflater)
    }

    private val itemsMonth = listOf(
        "January", "February", "March", "April", "May",
        "June", "July", "August", "September", "October",
        "November", "December"
    )
    private val itemsStatus = listOf(
        "Completed", "Pending", "Not Starting", "Progress"
    )


    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)



        isLessonPlanData = listOf(
            LessonPlanData(
                "Tamil",
                "12/12/2023",
                "12/12/2023",
                "Tamil",
                "Nothing",
                2
            ),
            LessonPlanData(
                "Tamil",
                "12/12/2023",
                "12/12/2023",
                "Tamil",
                "Nothing",
                3
            ),
            LessonPlanData(
                "Tamil",
                "12/12/2023",
                "12/12/2023",
                "Tamil",
                "Nothing", 1
            ),
            LessonPlanData(
                "Tamil",
                "12/12/2023",
                "12/12/2023",
                "Tamil",
                "Nothing", 2
            ),
            LessonPlanData(
                "Tamil",
                "12/12/2023",
                "12/12/2023",
                "Tamil",
                "Nothing", 1
            ),
            LessonPlanData(
                "Tamil",
                "12/12/2023",
                "12/12/2023",
                "Tamil",
                "Nothing", 3
            ),
            LessonPlanData(
                "Tamil",
                "12/12/2023",
                "12/12/2023",
                "Tamil",
                "Nothing", 2
            ),
            LessonPlanData(
                "Tamil",
                "12/12/2023",
                "12/12/2023",
                "Tamil",
                "Nothing", 1
            ),
            LessonPlanData(
                "Tamil",
                "12/12/2023",
                "12/12/2023",
                "Tamil",
                "Nothing", 3
            )
        )

        mAdapter = LessonPlanAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyLessonPlan.layoutManager = LinearLayoutManager(this)
        binding.rcyLessonPlan.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                LessonPlanAdapter(isLessonPlanData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyLessonPlan.adapter = mAdapter
        }

    }

    override fun onClick(p0: View?) {

        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    override fun onEditItem(data: LessonPlanData) {
//        editTheLessonPlan(binding.rlaHeader)
        editTheLessonPlan()
    }

    override fun onDeleteItem(data: LessonPlanData) {

    }

    private fun editTheLessonPlan() {
        // Create and configure the dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.edit_lesson_plan)

        // Show the dialog
        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ) // Size
            setGravity(Gravity.BOTTOM) // Bottom of the screen
            setWindowAnimations(R.style.PopupAnimation) // Add animation
        }

        // Set up UI elements in the dialog
        val lblClose: TextView = dialog.findViewById(R.id.lblClose)
        val lblMonthChoose: TextView = dialog.findViewById(R.id.lblMonthChoose)
        val lblStatusChoose: TextView = dialog.findViewById(R.id.lblStatusChoose)
        lblFromData = dialog.findViewById(R.id.lblFromData)
        lblToDate = dialog.findViewById(R.id.lblToDate)

        lblClose.setOnClickListener {
            dialog.dismiss()
        }

        lblMonthChoose.setOnClickListener {
            showDropdownMenuSort(
                lblMonthChoose,
                this,
                itemsMonth
            ) { selectedOption ->
                lblMonthChoose.text = selectedOption
            }
        }

        lblStatusChoose.setOnClickListener {
            showDropdownMenuSort(
                lblStatusChoose,
                this,
                itemsStatus
            ) { selectedOption ->
                lblStatusChoose.text = selectedOption
            }
        }

        lblFromData!!.setOnClickListener {
            showDatePickerDialog(this, this)
        }

        lblToDate!!.setOnClickListener {
            showDatePickerDialog(this, this)
        }
    }


    override fun onDateSelected(date: String) {
        lblFromData!!.text = changeDateFormat(date)
        lblToDate!!.text = changeDateFormat(date)
        Log.d("isSelectedDate", date)
    }
}