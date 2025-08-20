package com.vs.schoolmessenger.School.LSRW

import android.content.Intent
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LSRW.Model.DashboardItem
import com.vs.schoolmessenger.School.LSRW.Model.lsrwskilldata
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LsrwSkillMainBinding

class LsrwMain : BaseActivity<LsrwSkillMainBinding>(), View.OnClickListener {

    override fun getViewBinding(): LsrwSkillMainBinding {
        return LsrwSkillMainBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var adapter: LsrwAdapter
    private lateinit var dashboardviewadapter: LsRwDashboardAdapter

    private lateinit var filterAdapter: LsrwFilterAdapter
    private var allItems: List<lsrwskilldata> = emptyList()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.newtaskbutton.setOnClickListener(this)

        binding.rcylsrwreport.layoutManager = LinearLayoutManager(this)
        adapter = LsrwAdapter(
            itemList = emptyList(),
            context = this,
            noDataImage = binding.noDataImage,
            noDataText = binding.noDataFound
        )
        binding.rcylsrwreport.adapter = adapter

        fetchLsrwSkillReportData()

        appViewModel?.islsrwskillsreport?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcylsrwreport.visibility = View.VISIBLE
                binding.noDataFound.visibility = View.GONE
                allItems = response.data
                adapter.updateList(allItems)
                setupFilters(allItems)
            } else {
                binding.rcylsrwreport.visibility = View.GONE
                binding.noDataFound.visibility = View.VISIBLE
            }
        }



//        Hardcoded Values

        val dashboardItems = listOf(
            DashboardItem(R.drawable.booksvgformatstyle, "3", "Active Tasks", "1 overdue"),
            DashboardItem(R.drawable.booksvgformatstyle, "5", "Completed", "0 overdue"),
            DashboardItem(R.drawable.booksvgformatstyle, "2", "Pending", "1 overdue")
        )

        dashboardviewadapter = LsRwDashboardAdapter(dashboardItems, this)

        binding.rcylsrwheader.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcylsrwheader.adapter = dashboardviewadapter


//        End


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.newtaskbutton -> {
                RedirectToNewTaskPage()
            }
        }
    }


    private fun RedirectToNewTaskPage() {
        val intent = Intent(this, CreateNewTask::class.java)
        startActivity(intent)
    }


    private fun fetchLsrwSkillReportData() {
        binding.rcylsrwreport.visibility = View.VISIBLE
        binding.rcylsrwreport.isNestedScrollingEnabled = false
        appViewModel?.islsrwskillsreport(isAccessToken ?: "")
    }

    private fun setupFilters(data: List<lsrwskilldata>) {
        val filters = mutableListOf("All")
        filters.addAll(data.map { it.activity_type }.distinct())

        filterAdapter = LsrwFilterAdapter(filters) { selectedFilter ->
            val filteredList = if (selectedFilter == "All") data
            else data.filter { it.activity_type == selectedFilter }
            adapter.updateList(filteredList)
        }

        binding.rcyFilter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyFilter.adapter = filterAdapter
    }
}
