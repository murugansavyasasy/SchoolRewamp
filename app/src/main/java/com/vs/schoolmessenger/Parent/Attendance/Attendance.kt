package com.vs.schoolmessenger.Parent.Attendance

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.PorterDuff
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.Attendance.AttendanceReport.AttendanceReport
import com.vs.schoolmessenger.Parent.Attendance.Model.getStudentStats
import com.vs.schoolmessenger.Parent.Attendance.Model.getStudentStatsData
import com.vs.schoolmessenger.Parent.Attendance.WeekStatusModel.GetWeekStatusData
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Holidays
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequest
import com.vs.schoolmessenger.Parent.RequestLeave.NewLeaveRequest
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AttendanceBinding


class Attendance : BaseActivity<AttendanceBinding>(){

    override fun getViewBinding(): AttendanceBinding {
        return AttendanceBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStudentStatsData: getStudentStatsData? = null
    private var isChildDetails: ChildDetails? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()
        Constant

        binding.imgBack.setOnClickListener{
            onBackPressed()
        }
        binding.imgBack.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)

        isChildDetails = SharedPreference.getChildDetails(this)
        binding.lblStudentName.text = isChildDetails?.name ?: ""
        binding.lblStudentSection.text = isChildDetails?.standard_name+ " - " +isChildDetails?.section_name

        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        val dateDetails = Constant.getCurrentDateDetails()
        binding.lblDate.text = dateDetails["day"]
        binding.lblDateSuffix.text =Constant.getDaySuffix(dateDetails["day"]?.toIntOrNull() ?:1)
        binding.lblDay.text = dateDetails["weekday"]
        binding.lblMonthYear.text = dateDetails["monthYear"]
        loadStudentStats()


        appViewModel!!.isStudentStats?.observe(this) { response ->
            Constant.hideLoading(this@Attendance)
            if (response != null) {
                if (response.status) {
                    isStudentStatsData= response.data.firstOrNull()
                    isLoadStudentStats(isStudentStatsData!!)


                } else {
                    Constant.showDataValidation(
                        response.status.toString(), response.message, this
                    )
                }
            }
        }


        binding.lnrLeaveRequest.setOnClickListener{
            val myIntent = Intent(this@Attendance, NewLeaveRequest::class.java)
            this@Attendance.startActivity(myIntent)
        }

        binding.lnrAttendanceReport.setOnClickListener{
            val myIntent = Intent(this@Attendance, AttendanceReport::class.java)
            this@Attendance.startActivity(myIntent)
        }

        binding.lnrHoliday.setOnClickListener{
            val myIntent = Intent(this@Attendance, Holidays::class.java)
            this@Attendance.startActivity(myIntent)
        }

        binding.lnrLeaveHistory.setOnClickListener{
            val myIntent = Intent(this@Attendance, LeaveRequest::class.java)
            this@Attendance.startActivity(myIntent)
        }

        val weekList = listOf(
            GetWeekStatusData("M", "P"),
            GetWeekStatusData("T", "P" ),
            GetWeekStatusData("W", "A" ),
            GetWeekStatusData("T", "P" ),
            GetWeekStatusData("F", "" ),
            GetWeekStatusData("S", "" ),
        )

        binding.rcWeekStatus.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcWeekStatus.adapter = WeekStatusAdapter(weekList)



    }

    private fun isLoadStudentStats(data: getStudentStatsData) {
        binding.lblAttendancePercentage.text = data.attendance_percentage
        binding.lblLeaveTakenPercentage.text = data.absent_days.toString()
        binding.lblOngoingDaysPercentage.text = data.completed_working_days.toString()
        animateProgress(binding.attendanceProgressBar,data.attendance_percentage.toIntOrNull() ?: 0, 100)
        animateProgress(binding.leaveTakenProgressBar, data.absent_days,20)
        animateProgress(binding.ongoingDaysProgressBar, data.completed_working_days,data.total_working_days)

    }


    fun animateProgress(progressBar: ProgressBar, current: Int, max: Int, duration: Long = 1000) {
        val safeMax = if (max <= 0) 1 else max           // Avoid divide by zero
        val safeCurrent = current.coerceIn(0, safeMax)   // Clamp current within valid range

        val percentage = ((safeCurrent.toFloat() / safeMax) * 100).toInt()

        progressBar.max = 100
        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, percentage)
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    private fun loadStudentStats() {
        appViewModel!!.isStudentStats(isAccessToken!!)
    }


}

