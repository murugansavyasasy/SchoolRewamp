package com.vs.schoolmessenger.School.LessonPlan

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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

    private lateinit var lessonPlanData: List<LessonPlanData>
    private lateinit var adapter: LessonPlanAdapter
    private var lblFromDate: TextView? = null
    private var lblToDate: TextView? = null

    override fun getViewBinding(): LessonplanViewDetailsBinding {
        return LessonplanViewDetailsBinding.inflate(layoutInflater)
    }

    private val months = listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )
    private val statuses = listOf("Completed", "Pending", "Not Started", "In Progress")

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)

        lessonPlanData = listOf(
            LessonPlanData(
                "Atoms,Elements,States of Matter",
                "10/02/2024",
                "15/02/2024",
                "Algebra",
                "Introduction to equations",
                2
            ),
            LessonPlanData(
                "Atoms,Elements,States of Matter",
                "05/03/2024",
                "12/03/2024",
                "Physics",
                "Newton's Laws",
                3
            ),
            LessonPlanData(
                "Atoms,Elements,States of Matter",
                "20/01/2024",
                "25/01/2024",
                "Literature",
                "Poetry analysis",
                1
            )
        )

        adapter = LessonPlanAdapter(lessonPlanData, this, this, Constant.isShimmerViewDisable)
        binding.rcyLessonPlan.layoutManager = LinearLayoutManager(this)
        binding.rcyLessonPlan.adapter = adapter
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.imgBack) {
            onBackPressed()
        }
    }

    override fun onEditItem(data: LessonPlanData) {
        showEditLessonPlanDialog()
    }

    override fun onDeleteItem(data: LessonPlanData) {
        // Implement delete functionality here
    }

    private fun showEditLessonPlanDialog() {
        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.edit_lesson_plan)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.BOTTOM)
                setWindowAnimations(R.style.PopupAnimation)
            }
            show()
        }

        val lblClose = dialog.findViewById<TextView>(R.id.lblClose)
        val lblMonthChoose = dialog.findViewById<TextView>(R.id.lblMonthChoose)
        val lblStatusChoose = dialog.findViewById<TextView>(R.id.lblStatusChoose)
        lblFromDate = dialog.findViewById(R.id.lblFromData)
        lblToDate = dialog.findViewById(R.id.lblToDate)

        lblClose.setOnClickListener { dialog.dismiss() }

        lblMonthChoose.setOnClickListener {
            showDropdownMenuSort(lblMonthChoose, this, months) { lblMonthChoose.text = it }
        }

        lblStatusChoose.setOnClickListener {
            showDropdownMenuSort(lblStatusChoose, this, statuses) { lblStatusChoose.text = it }
        }

        lblFromDate?.setOnClickListener { showDatePickerDialog(this, this) }
        lblToDate?.setOnClickListener { showDatePickerDialog(this, this) }
    }

    override fun onDateSelected(date: String) {
        lblFromDate?.text = changeDateFormat(date)
        lblToDate?.text = changeDateFormat(date)
        Log.d("SelectedDate", date)
    }
}
