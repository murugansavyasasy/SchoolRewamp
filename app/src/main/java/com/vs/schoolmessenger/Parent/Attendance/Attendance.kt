package com.vs.schoolmessenger.Parent.Attendance

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.PorterDuff
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
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
        binding.toolbarLayout.lblStudentName1.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name

        binding.lblHeaderTitle.setText(Constant.isSelectedMenuName)

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
                    binding.lblErrorMessage.visibility=View.GONE
                    isLoadStudentStats(isStudentStatsData!!)
                    binding.lblHeading.visibility=View.VISIBLE
                }
                else {
                    binding.lblErrorMessage.visibility=View.VISIBLE
                    binding.lblErrorMessage.text=response.message
                    binding.lblHeading.visibility=View.GONE
                    binding.rcWeekStatus.visibility = View.GONE
                }
            }
            else {
                binding.rcWeekStatus.visibility = View.GONE
                binding.lblHeading.visibility=View.GONE
                binding.lblErrorMessage.visibility=View.GONE
                Constant.showDataValidationNoDashboardRedirect(
                    getString(R.string.Oops),getString(R.string.Something_went_wrong_Please_try_again), this
                )
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

        val attendancePercentage = data.attendance_percentage.toDoubleOrNull() ?: 0.0
        val absentDays = data.absent_days
        val completedDays = data.completed_working_days
        val totalDays = if (data.total_working_days <= 0.0) 1.0 else data.total_working_days
        val ongoingPercentage = ((completedDays / totalDays) * 100).roundToInt().coerceIn(0, 100)


        // Show text values
        binding.lblAttendancePercentage.text = attendancePercentage.roundToInt().toString()
        binding.lblLeaveTakenPercentage.text = absentDays.roundToInt().toString()
        binding.lblOngoingDaysPercentage.text =ongoingPercentage.toString()


        // Animate progress bars
        animateProgress(binding.attendanceProgressBar, attendancePercentage, 100.0)
        animateProgress(binding.leaveTakenProgressBar, absentDays, totalDays)
        animateProgress(binding.ongoingDaysProgressBar, completedDays, totalDays)

        // Weekly attendance list
        val attList = data.weekly_status.att_list
        val days = listOf(
            Constant.M, Constant.allPresent, Constant.W,
            Constant.allPresent, Constant.fullDay,
            Constant.section, Constant.section
        )

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

    fun animateProgress(
        progressBar: ProgressBar,
        current: Double,
        max: Double,
        duration: Long = 1000
    ) {
        val safeMax = if (max <= 0.0) 1.0 else max
        val safeCurrent = current.coerceIn(0.0, safeMax)

        val percentage = ((safeCurrent / safeMax) * 100).toInt()

        progressBar.max = 100
        val animator = ObjectAnimator.ofInt(
            progressBar,
            Constant.progress,
            progressBar.progress,
            percentage
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}

