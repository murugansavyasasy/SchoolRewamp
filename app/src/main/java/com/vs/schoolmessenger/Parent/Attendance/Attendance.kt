package com.vs.schoolmessenger.Parent.Attendance

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.PorterDuff
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.Attendance.AttendanceReport.AttendanceReport
import com.vs.schoolmessenger.Parent.Attendance.Model.GetWeekStatusData
import com.vs.schoolmessenger.Parent.Attendance.Model.getStudentStatsData
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Holidays
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequest
import com.vs.schoolmessenger.Parent.RequestLeave.NewLeaveRequest
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AttendanceBinding
import kotlin.math.roundToInt


class Attendance : BaseActivity<AttendanceBinding>() {

    override fun getViewBinding(): AttendanceBinding {
        return AttendanceBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStudentStatsData: getStudentStatsData? = null
    private var isChildDetails: ChildDetails? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgInfo.setColorFilter(
            ContextCompat.getColor(this, R.color.PrimaryColor),
            PorterDuff.Mode.SRC_IN
        )

        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name


        Log.d("Menu_name",Constant.isParentMenuName)

        binding.lblHeaderTitle.setText(Constant.isParentMenuName)

        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        val dateDetails = Constant.getCurrentDateDetails()
        binding.lblDate.text = dateDetails[Constant.day]
        binding.lblDateSuffix.text = Constant.getDaySuffix(dateDetails[Constant.day]?.toIntOrNull() ?: 1)
        binding.lblDay.text = dateDetails[Constant.weekday]
        binding.lblMonthYear.text = dateDetails[Constant.monthYear]
        loadStudentStats()
        binding.imgInfo.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.imgInfo)
            popupMenu.menuInflater.inflate(R.menu.attendance_leave_status_menu, popupMenu.menu)
            forcePopupMenuIcons(popupMenu)
            popupMenu.show()
        }


        appViewModel!!.isStudentStats?.observe(this) { response ->
            Constant.hideLoading(this@Attendance)
            if (response != null) {
                if (response.status) {
                    isStudentStatsData = response.data.firstOrNull()
                    isLoadStudentStats(isStudentStatsData!!)


                } else {
                    Constant.showDataValidation(
                        response.status.toString(), response.message, this
                    )
                }
            }
        }


        binding.lnrLeaveRequest.setOnClickListener {
            val myIntent = Intent(this@Attendance, NewLeaveRequest::class.java)
            this@Attendance.startActivity(myIntent)
        }

        binding.lnrAttendanceReport.setOnClickListener {
            val myIntent = Intent(this@Attendance, AttendanceReport::class.java)
            this@Attendance.startActivity(myIntent)
        }

        binding.lnrHoliday.setOnClickListener {
            val myIntent = Intent(this@Attendance, Holidays::class.java)
            this@Attendance.startActivity(myIntent)
        }

        binding.lnrLeaveHistory.setOnClickListener {
            val myIntent = Intent(this@Attendance, LeaveRequest::class.java)
            this@Attendance.startActivity(myIntent)
        }

    }

    private fun isLoadStudentStats(data: getStudentStatsData) {
        binding.lblAttendancePercentage.text = (data.attendance_percentage.toFloatOrNull()?.roundToInt() ?: 0).toString()
        binding.lblLeaveTakenPercentage.text = data.absent_days.toString()
        binding.lblOngoingDaysPercentage.text = data.completed_working_days.toString()
        animateProgress(
            binding.attendanceProgressBar,
            data.attendance_percentage.toFloatOrNull()?.roundToInt() ?: 0,
            100
        )
        animateProgress(binding.leaveTakenProgressBar, data.absent_days, 20)
        animateProgress(
            binding.ongoingDaysProgressBar,
            data.completed_working_days,
            data.total_working_days
        )

        val attList = data.weekly_status.att_list

        val days = listOf(Constant.M, Constant.allPresent, Constant.W, Constant.allPresent, Constant.fullDay, Constant.section,Constant.section)

        if (attList.isNotEmpty()) {
            binding.rcWeekStatus.visibility = View.VISIBLE

            val weekList = days.mapIndexed { index, day ->
                GetWeekStatusData(day, attList.getOrElse(index) { "" })
            }
            binding.rcWeekStatus.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rcWeekStatus.adapter = WeekStatusAdapter(weekList)
        } else {
            binding.rcWeekStatus.visibility = View.GONE
        }
    }


//    fun animateProgress(progressBar: ProgressBar, current: Int, max: Int, duration: Long = 1000) {
//        val safeMax = if (max <= 0) 1 else max           // Avoid divide by zero
//        val safeCurrent = current.coerceIn(0, safeMax)   // Clamp current within valid range
//
//        val percentage = ((safeCurrent.toFloat() / safeMax) * 100).toInt()
//
//        progressBar.max = 100
//        val animator = ObjectAnimator.ofInt(progressBar, Constant.progress, 0, percentage)
//        animator.duration = duration
//        animator.interpolator = DecelerateInterpolator()
//        animator.start()
//    }

    fun animateProgress(
        progressBar: ProgressBar,
        current: Int,
        max: Int,
        duration: Long = 1000
    ) {
        val safeMax = if (max <= 0) 1 else max           // Avoid divide by zero
        val safeCurrent = current.coerceIn(0, safeMax)   // Clamp current within valid range

        val percentage = ((safeCurrent.toFloat() / safeMax) * 100).toInt()

        progressBar.max = 100
        val animator = ObjectAnimator.ofInt(
            progressBar,
            Constant.progress,
            progressBar.progress,   // start from current progress, not always 0
            percentage              // animate to target percentage
        )
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }




    private fun loadStudentStats() {
        appViewModel!!.isStudentStats(isAccessToken!!)
    }

    private fun forcePopupMenuIcons(menu: PopupMenu) {
        try {
            val fields = menu.javaClass.declaredFields
            for (field in fields) {
                if (field.name == Constant.mPopup) {
                    field.isAccessible = true
                    val helper = field.get(menu)
                    val classPopup = Class.forName(helper.javaClass.name)
                    val setIcons = classPopup.getMethod(Constant.setForceShowIcon, Boolean::class.java)
                    setIcons.invoke(helper, true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

