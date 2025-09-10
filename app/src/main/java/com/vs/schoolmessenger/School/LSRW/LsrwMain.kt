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
import com.vs.schoolmessenger.School.LSRW.Adapter.LsRwDashboardAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwCompletedAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwFilterAdapter
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask
import com.vs.schoolmessenger.School.LSRW.Model.Overview
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
    private lateinit var completedviewadapter: LsrwCompletedAdapter
    private lateinit var filterAdapter: LsrwFilterAdapter

    private var allOverviewItems: List<Overview> = emptyList()
    private var allTaskItems: List<LsrwTask> = emptyList()
    private var allCompletedItems: List<LsrwTask> = emptyList()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.text = "Listening,Speaking,Reading,Writing"
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


        binding.rcylsrwcompletedreport.layoutManager = LinearLayoutManager(this)
        completedviewadapter = LsrwCompletedAdapter(
            itemList = emptyList(),
            context = this,
            noDataImage = binding.noDataImage,
            noDataText = binding.noDataFound
        )
        binding.rcylsrwcompletedreport.adapter = completedviewadapter


        binding.rcylsrwheader.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dashboardviewadapter = LsRwDashboardAdapter(
            itemList = emptyList(),
            context = this,
            onDashboardClick = { overviewItem ->
                val intent = Intent(this, ActiveTaskList::class.java)
                intent.putParcelableArrayListExtra("TASK_LIST", ArrayList(allTaskItems))
                startActivity(intent)
            },
            onCompletedClick = { overviewItem ->
                val intent = Intent(this, CompletedTaskList::class.java)
                intent.putParcelableArrayListExtra(
                    "COMPLETED_TASK_LIST",
                    ArrayList(allCompletedItems)
                )
                startActivity(intent)
            }
        )



        binding.rcylsrwheader.adapter = dashboardviewadapter


        fetchLsrwSkillReportData()

        appViewModel?.islsrwskillsreport?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val data = response.data[0]


                allOverviewItems = data.overview
                dashboardviewadapter.updateList(allOverviewItems)


                allTaskItems = data.active
                adapter.updateList(allTaskItems)


                allCompletedItems = data.completed
                completedviewadapter.updateList(allCompletedItems)

                setupFilters(allTaskItems, allCompletedItems)

                binding.rcylsrwreport.visibility = View.VISIBLE
                binding.rcylsrwcompletedreport.visibility = View.VISIBLE
                binding.noDataFound.visibility = View.GONE
            } else {
                binding.rcylsrwreport.visibility = View.GONE
                binding.rcylsrwcompletedreport.visibility = View.GONE
                binding.noDataFound.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.newtaskbutton -> RedirectToNewTaskPage()
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

    private fun setupFilters(active: List<LsrwTask>, completed: List<LsrwTask>) {
        val filters = mutableListOf("All")
        filters.addAll((active + completed).map { it.activity_type }.distinct())

        filterAdapter = LsrwFilterAdapter(filters) { selectedFilter ->
            if (selectedFilter == "All") {
                adapter.updateList(active)
                completedviewadapter.updateList(completed)
            } else {
                adapter.updateList(active.filter { it.activity_type == selectedFilter })
                completedviewadapter.updateList(completed.filter { it.activity_type == selectedFilter })
            }
        }

        binding.rcyFilter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyFilter.adapter = filterAdapter
    }
}

