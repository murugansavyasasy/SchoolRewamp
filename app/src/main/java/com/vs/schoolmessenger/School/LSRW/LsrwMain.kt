package com.vs.schoolmessenger.School.LSRW

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.School.LSRW.Adapter.LsRwDashboardAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwCompletedAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwFilterAdapter
import com.vs.schoolmessenger.School.LSRW.Listener.lsrwskillreportlistener
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask
import com.vs.schoolmessenger.School.LSRW.Model.Overview
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LsrwSkillMainBinding

class LsrwMain : BaseActivity<LsrwSkillMainBinding>(), View.OnClickListener, lsrwskillreportlistener {

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

    var isLsrwId = ""
    var isLsrwPosition = 0

    private var deleteFrom: String = ""


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.text =
            getString(R.string.listening_speaking_reading_writing)
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.newtaskbutton.setOnClickListener(this)

        binding.rcylsrwreport.layoutManager = LinearLayoutManager(this)
        adapter = LsrwAdapter(
            itemList = emptyList(),
            context = this,
            this,
            noDataImage = binding.noDataImage,
            noDataText = binding.noDataFound
        )
        binding.rcylsrwreport.adapter = adapter
        binding.rcylsrwcompletedreport.layoutManager = LinearLayoutManager(this)
        completedviewadapter = LsrwCompletedAdapter(
            itemList = emptyList(),
            context = this,
            this,
            noDataImage = binding.noDataImage,
            noDataText = binding.noDataFound
        )
        binding.rcylsrwcompletedreport.adapter = completedviewadapter
        binding.rcylsrwheader.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dashboardviewadapter = LsRwDashboardAdapter(
            itemList = emptyList(),
            context = this,
            onDashboardClick = {
                val intent = Intent(this, ActiveTaskList::class.java)
                intent.putParcelableArrayListExtra(Constant.TASK_LIST, ArrayList(allTaskItems))
                startActivity(intent)
            },
            onCompletedClick = {
                val intent = Intent(this, CompletedTaskList::class.java)
                intent.putParcelableArrayListExtra(
                    Constant.COMPLETED_TASK_LIST,
                    ArrayList(allCompletedItems)
                )
                startActivity(intent)
            }
        )
        binding.rcylsrwheader.adapter = dashboardviewadapter
        fetchLsrwSkillReportData()

        appViewModel?.islsrwskillsreport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val data = response.data[0]
                allOverviewItems = data.overview
                dashboardviewadapter.updateList(allOverviewItems)
                allTaskItems = data.active
                adapter.updateList(allTaskItems)
                allCompletedItems = data.completed
                completedviewadapter.updateList(allCompletedItems)
                setupFilters(allTaskItems, allCompletedItems)
                handleVisibility(allTaskItems, allCompletedItems,"")
            } else {
                handleVisibility(emptyList(), emptyList(),response!!.message?:getString(R.string.no_data_found))
            }
        }

        appViewModel!!.isLsrwDelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@LsrwMain)
                    when (deleteFrom) {
                        "ACTIVE" -> adapter.removeItemAt(isLsrwPosition)
                        "COMPLETED" -> completedviewadapter.removeItemAt(isLsrwPosition)
                    }
                    fetchLsrwSkillReportData()
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                Constant.selectedFiles.clear()
                onBackPressed()
            }

            R.id.newtaskbutton -> RedirectToNewTaskPage()
        }
    }

    override fun onBackPressed() {
        Constant.selectedFiles.clear()
        super.onBackPressed()
    }

    private fun RedirectToNewTaskPage() {
        val intent = Intent(this, CreateNewTask::class.java)
        startActivity(intent)
    }

    private fun fetchLsrwSkillReportData() {
        Constant.showLoading(this)
        binding.rcylsrwreport.visibility = View.VISIBLE
        binding.rcylsrwreport.isNestedScrollingEnabled = false
        appViewModel?.islsrwskillsreport(isAccessToken ?: "")
    }

    private fun setupFilters(active: List<LsrwTask>, completed: List<LsrwTask>) {
        val allFilter = Constant.All_
        val pendingFilter = "Pending"
        val completedFilter = "Completed"


        val availableFilters = (active + completed)
            .map { it.activity_type }
            .distinct()


        val desiredOrder = listOf(
            allFilter,
            "Listening",
            "Speaking",
            "Reading",
            "Writing",
            pendingFilter,
            completedFilter
        )


        val rearrangedFilters = desiredOrder.filter { it in availableFilters || it in listOf(allFilter, pendingFilter, completedFilter) }

        filterAdapter = LsrwFilterAdapter(rearrangedFilters) { selectedFilter ->
            val filteredActive: List<LsrwTask>
            val filteredCompleted: List<LsrwTask>

            when (selectedFilter) {
                Constant.All_ -> {
                    filteredActive = allTaskItems
                    filteredCompleted = allCompletedItems
                }
                "Pending" -> {
                    filteredActive = allTaskItems
                    filteredCompleted = emptyList()
                }
                "Completed" -> {
                    filteredActive = emptyList()
                    filteredCompleted = allCompletedItems
                }
                else -> {
                    filteredActive = allTaskItems.filter { it.activity_type == selectedFilter }
                    filteredCompleted = allCompletedItems.filter { it.activity_type == selectedFilter }
                }
            }

            adapter.updateList(filteredActive)
            completedviewadapter.updateList(filteredCompleted)
            handleVisibility(filteredActive, filteredCompleted, getString(R.string.no_data_found))
        }

        binding.rcyFilter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyFilter.adapter = filterAdapter
    }




    private fun handleVisibility(active: List<LsrwTask>, completed: List<LsrwTask>,ErrorMsg: String) {
        val hasActive = active.isNotEmpty()
        val hasCompleted = completed.isNotEmpty()
        binding.rcylsrwreport.visibility = if (hasActive) View.VISIBLE else View.GONE
        binding.headerLabel.visibility = if (hasActive) View.VISIBLE else View.GONE
        binding.rcylsrwcompletedreport.visibility = if (hasCompleted) View.VISIBLE else View.GONE
        binding.completedLabel.visibility = if (hasCompleted) View.VISIBLE else View.GONE
        if (!hasActive && !hasCompleted) {
            binding.lytNoDataFound.visibility = View.VISIBLE
            binding.noDataFound.visibility = View.VISIBLE
            binding.noDataImage.visibility = View.VISIBLE
            binding.noDataFound.text=ErrorMsg

        } else {
            binding.lytNoDataFound.visibility = View.GONE
            binding.noDataFound.visibility = View.GONE
            binding.noDataImage.visibility = View.GONE
        }
    }

    override fun onEditAndDeleteCompleted(
        data: LsrwTask,
        anchorView: View,
        adapterPosition: Int,
        source: String
    ) {
        isLsrwId = data.id
        isLsrwPosition = adapterPosition
        deleteFrom = source
        showEditDeletePopup(data, anchorView)

    }


    fun showEditDeletePopup(data: LsrwTask, anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_delete, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        val layoutEdit = popupView.findViewById<LinearLayout>(R.id.layout_edit)
        val layoutDelete = popupView.findViewById<LinearLayout>(R.id.layout_delete)

        if(data.can_edit) {
            layoutEdit.visibility = View.GONE
        } else {
            layoutEdit.visibility = View.GONE
        }

        if(data.can_delete) {
            layoutDelete.visibility = View.VISIBLE
        } else {
            layoutDelete.visibility = View.GONE
        }

        layoutDelete.setOnClickListener {
            showSendConfirmationDialog(false)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, 0, 10)
    }



    fun showSendConfirmationDialog(isEventUpdate: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        alertMessage.text = getString(R.string.are_you_sure_want_to_delete)


        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            val jsonObject = JsonObject()
            jsonObject.addProperty(APIKeyNames.id, isLsrwId)
            appViewModel?.isLsrwDelete(isAccessToken!!, jsonObject, this)

        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }


}
