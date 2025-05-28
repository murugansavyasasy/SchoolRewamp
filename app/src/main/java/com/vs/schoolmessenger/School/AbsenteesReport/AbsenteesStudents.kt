package com.vs.schoolmessenger.School.AbsenteesReport

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReport
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeData
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudents.Student
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AbsenteesReportBinding
import com.vs.schoolmessenger.databinding.AbsenteesStudentlistBinding

class AbsenteesStudents : BaseActivity<AbsenteesStudentlistBinding>(),
    View.OnClickListener,AbsenteesHeaderClickListener {


    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null


    private lateinit var absenteesstudentdateadapter: AbsenteesStudentHeaderListAdapter
    private lateinit var absenteesstudentdatedetailadapter: AbsenteesStudentFooterListAdapter



    override fun getViewBinding(): AbsenteesStudentlistBinding {
        return AbsenteesStudentlistBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        binding.toolbarLayout.lblParentToolBar.text = "Absentees Report"
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name ?: ""

        isAccessToken = isStaffDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        binding.studendreport.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        fetchAbsenteeStudentData()


        appViewModel?.getabsenteesstudentbydate?.observe(this) { response ->
            Constant.hideLoading(this@AbsenteesStudents)
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI("Something went wrong. Please try again.")
                return@observe
            }
            if (response.status) {
                isLoadDailyCollectionData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }


    }


    private fun fetchAbsenteeStudentData() {
        Constant.showLoading(this@AbsenteesStudents)

        val absent_on = Constant.isAbsenteesReportDataSending?.date ?: ""
        val section_id = Constant.isAbsenteesReportDataSending?.section_wise?.firstOrNull()?.id ?: ""

        Log.d("API_CALL", "Fetching for date: $absent_on, section: $section_id")

        appViewModel?.getabsenteesstudentbydate(
            isAccessToken ?: "",
            absent_on,
            section_id,
            this
        )
    }




    private fun isLoadDailyCollectionData(data: List<Student>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI("No absentee data available")
            return
        }

        Log.d("DataLoad", "Received ${data.size} students")

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.studendreport.visibility = View.VISIBLE

        if (!::absenteesstudentdateadapter.isInitialized) {
            absenteesstudentdateadapter = AbsenteesStudentHeaderListAdapter(data, this, this, false)
            binding.studendreport.adapter = absenteesstudentdateadapter
        } else {
            absenteesstudentdateadapter.updateData(data)
        }

        absenteesstudentdateadapter.setSelectedPosition(0)
    }


    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.studendreport.visibility = View.GONE

    }



    override fun onClick(p0: View?) {

        when (p0?.id) {

        }
    }

    override fun onHeaderItemClicked(position: Int, student: Student) {
        Log.d("HeaderClick", "Clicked student at position $position: ${student.student_name}")
    }


}