package com.vs.schoolmessenger.Parent.Attendance

import android.content.Intent
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.Attendance.AttendanceReport.AttendanceReport
import com.vs.schoolmessenger.Parent.Attendance.WeekStatusModel.GetWeekStatusData
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequest
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AttendanceBinding


class Attendance : BaseActivity<AttendanceBinding>(){

    override fun getViewBinding(): AttendanceBinding {
        return AttendanceBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()

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

        binding.lnrAttendanceReport.setOnClickListener{
            val myIntent = Intent(this@Attendance, AttendanceReport::class.java)
            this@Attendance.startActivity(myIntent)
        }

        binding.lnrAttendanceReport.setOnClickListener{
            val myIntent = Intent(this@Attendance, AttendanceReport::class.java)
            this@Attendance.startActivity(myIntent)
        }

        binding.lnrAttendanceReport.setOnClickListener{
            val myIntent = Intent(this@Attendance, AttendanceReport::class.java)
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

}

