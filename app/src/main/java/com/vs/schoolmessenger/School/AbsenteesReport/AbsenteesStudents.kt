package com.vs.schoolmessenger.School.AbsenteesReport

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesStudentListAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesStudentListDetailAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesStudentClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesStudentDetailClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Model.Student
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AbsenteesStudentlistBinding

class AbsenteesStudents : BaseActivity<AbsenteesStudentlistBinding>(),
    View.OnClickListener, AbsenteesStudentClickListener, AbsenteesStudentDetailClickListener {

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var absenteesstudentdateadapter: AbsenteesStudentListAdapter
    private lateinit var absenteesstudentdatedetailadapter: AbsenteesStudentListDetailAdapter

    override fun getViewBinding(): AbsenteesStudentlistBinding {
        return AbsenteesStudentlistBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name ?: ""


        isAccessToken = isStaffDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        binding.studendreport.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.studentlistreport.layoutManager = LinearLayoutManager(this)

        absenteesstudentdatedetailadapter = AbsenteesStudentListDetailAdapter(
            emptyList(), this, this, true
        )
        binding.studentlistreport.adapter = absenteesstudentdatedetailadapter

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::absenteesstudentdatedetailadapter.isInitialized) {
                    absenteesstudentdatedetailadapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        fetchAbsenteeStudentData()

        appViewModel?.getabsenteesstudentbydate?.observe(this) { response ->

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


        val absent_on = Constant.isAbsenteesReportDataSending?.date ?: ""
        val section_id =
            Constant.isAbsenteesReportDataSending?.section_wise?.firstOrNull()?.section_id ?: ""

        Log.d("API_CALL", "Fetching for date: $absent_on, section: $section_id")

        appViewModel?.getabsenteesstudentbydate(
            isAccessToken ?: "", absent_on, section_id, this
        )
    }

    private fun isLoadDailyCollectionData(data: List<Student>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI("No absentee data available")
            return
        }

        val singleItemList = listOf(data.first()) // For header display

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.studendreport.visibility = View.VISIBLE
        binding.studentlistreport.visibility = View.VISIBLE

        if (!::absenteesstudentdateadapter.isInitialized) {
            absenteesstudentdateadapter =
                AbsenteesStudentListAdapter(singleItemList, this, this, false)
            binding.studendreport.adapter = absenteesstudentdateadapter
        } else {
            absenteesstudentdateadapter.updateData(singleItemList)
        }

        absenteesstudentdateadapter.setSelectedPosition(0)

        // Update footer adapter with full list
        absenteesstudentdatedetailadapter.updateData(data)
    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.studendreport.visibility = View.GONE
        binding.studentlistreport.visibility = View.GONE
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.lytList.visibility = View.VISIBLE
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = "No matching report found"
            binding.studentlistreport.visibility = View.GONE
        } else {
            binding.lytList.visibility = View.GONE
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.studentlistreport.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    override fun onHeaderItemClicked(position: Int, student: Student) {
        Log.d("HeaderClick", "Clicked student at position $position: ${student.student_name}")
    }

    override fun onFooterItemClicked(position: Int, student: Student) {
        Log.d("FooterClick", "Clicked student at position $position: ${student.student_name}")
    }

}
